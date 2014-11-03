package nl.tue.gale.um;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.cache.Cache;
import nl.tue.gale.common.cache.CacheSession;
import nl.tue.gale.common.code.Argument;
import nl.tue.gale.common.code.CodeManager;
import nl.tue.gale.common.code.CodeResolver;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.common.uri.URIs;
import nl.tue.gale.dm.DMCache;
import nl.tue.gale.dm.data.Attribute;
import nl.tue.gale.dm.data.Concept;
import nl.tue.gale.um.data.EntityValue;

public final class ActionQueue {
	private final DMCache dm;
	private final Cache<EntityValue> um;
	private final CodeManager cm;
	private final CodeResolver cr;
	private final UMGraph graph;
	private final CacheSession<EntityValue> session;
	private final Queue<Action> queue;
	private boolean done = false;
	private int queued = 0;

	private static final int MAX_QUEUE = 10000;

	private ActionQueue(DMCache dm, Cache<EntityValue> um, CodeManager cm,
			CodeResolver cr, UMGraph graph) {
		this.dm = dm;
		this.um = um;
		this.cm = cm;
		this.cr = cr;
		this.graph = graph;
		session = um.openSession();
		queue = new LinkedList<Action>();
	}

	public static ActionQueue create(DMCache dm, Cache<EntityValue> um,
			CodeManager cm, CodeResolver cr, UMGraph graph) {
		return new ActionQueue(dm, um, cm, cr, graph);
	}

	public void queue(EntityValue ev) {
		if (done)
			throw new IllegalStateException("this action queue is finished");
		queue.offer(new ChangeAction(ev));
		queued++;
	}

	public void run() {
		if (done)
			throw new IllegalStateException("this action queue is finished");
		done = true;
		while (!queue.isEmpty() && queued < MAX_QUEUE)
			queue.poll().run();
		if (queued == MAX_QUEUE) {
			session.rollback();
			throw new IllegalStateException(
					"maximum queue count reached (possible cycle)");
		}
	}

	public CacheSession<EntityValue> session() {
		return session;
	}

	private abstract class Action {
		public abstract void run();
	}

	class ChangeAction extends Action {
		private final EntityValue change;

		public ChangeAction(EntityValue change) {
			this.change = change;
		}

		@Override
		public void run() {
			EntityValue current = session.get(change.getUri());
			if (GaleUtil.safeEquals(current, change))
				return;
			session.put(change.getUri(), change);
			createExecAction(current, change);
			queue.offer(new ExpandAction(current.getUri()));
			queued++;
		}

		public String toString() {
			return change.toString();
		}
	}

	class ExpandAction extends Action {
		private final URI uri;

		public ExpandAction(URI uri) {
			this.uri = uri;
		}

		@Override
		public void run() {
			String user = uri.getUserInfo();
			for (URI linked : graph.getLinkSet(Attribute.getAttributeURI(uri))) {
				queue.offer(new CheckAction(URIs.builder().uri(linked)
						.userInfo(user).build()));
				queued++;
			}
		}

		public String toString() {
			return "(Expanding: " + uri + ")";
		}
	}

	class CheckAction extends Action {
		private final URI uri;

		public CheckAction(URI uri) {
			this.uri = uri;
		}

		@Override
		public void run() {
			EntityValue change = new EntityValue();
			change.setUri(uri);
			Concept concept = dm.get(Concept.getConceptURI(uri));
			if (concept == null)
				return;
			Attribute attr = concept.getAttribute(uri.getFragment());
			if (attr == null)
				return;
			if (attr.isPersistent())
				return;
			CacheSession<EntityValue> internalSession = um.openSession(session);
			internalSession.setBaseUri(uri);
			change.setValue(cm.evaluate(cr, attr.getDefaultCode(),
					argumentBuilder(internalSession).build()));
			internalSession.rollback();
			queue.offer(new ChangeAction(change));
			queued++;
		}

		public String toString() {
			return "(Checking: " + uri + ")";
		}
	}

	class ExecAction extends Action {
		private final Changed changed;
		private final String code;
		private final URI baseUri;

		public ExecAction(Changed changed, String code, URI baseUri) {
			this.changed = changed;
			this.code = code;
			this.baseUri = baseUri;
		}

		@Override
		public void run() {
			CacheSession<EntityValue> internalSession = um.openSession(session);
			internalSession.setBaseUri(baseUri);
			List<Argument> arguments = argumentBuilder(internalSession).arg(
					"changed", changed.getClass().getName(), changed).build();
			cm.execute(cr, code, arguments);
			for (EntityValue ev : internalSession.getChangeMap().values()) {
				createExecAction(session.get(ev.getUri()), ev);
				queue.offer(new ExpandAction(ev.getUri()));
				queued++;
			}
			internalSession.commit();
		}

		public String toString() {
			return "(Executing '" + baseUri + "': " + code + " -> " + changed
					+ ")";
		}
	}

	private void createExecAction(EntityValue current, EntityValue change) {
		Concept c = dm.get(Concept.getConceptURI(current.getUri()));
		if (c == null)
			return;
		Attribute a = c.getAttribute(current.getUri().getFragment());
		if (a == null)
			return;
		String code = a.getProperty("event");
		if (code != null && !"".equals(code)) {
			queue.offer(new ExecAction(createChanged(current.getValue(),
					change.getValue()), code, change.getUri()));
			queued++;
		}
	}

	private Argument.ListBuilder argumentBuilder(
			CacheSession<EntityValue> session) {
		return Argument
				.listBuilder()
				.arg("dm", "nl.tue.gale.dm.DMCache", dm)
				.arg("session", "nl.tue.gale.common.cache.CacheSession",
						session);
	}

	private static final List<String> order = Arrays.asList(new String[] {
			"java.lang.Byte", "java.lang.Short", "java.lang.Integer",
			"java.lang.Long", "java.lang.Float", "java.lang.Double" });

	public static Changed createChanged(Object oldValue, Object newValue) {
		int a = order.indexOf(oldValue.getClass().getName());
		int b = order.indexOf(newValue.getClass().getName());
		if (a == -1 || b == -1)
			return new Changed(oldValue, newValue);
		int c = (a > b ? a : b);
		switch (c) {
		case 0:
			return new ChangedByte(oldValue, newValue);
		case 1:
			return new ChangedShort(oldValue, newValue);
		case 2:
			return new ChangedInteger(oldValue, newValue);
		case 3:
			return new ChangedLong(oldValue, newValue);
		case 4:
			return new ChangedFloat(oldValue, newValue);
		case 5:
			return new ChangedDouble(oldValue, newValue);
		}
		return new Changed(oldValue, newValue);
	}

	public static class Changed {
		public Object oldValue = null;
		public Object newValue = null;

		public Changed(Object oldValue, Object newValue) {
			this.oldValue = oldValue;
			this.newValue = newValue;
		}
	}

	public static class ChangedByte extends Changed {
		public byte diff = 0;
		public byte oldNumber = 0;
		public byte newNumber = 0;

		public ChangedByte(Object oldValue, Object newValue) {
			super(oldValue, newValue);
			oldNumber = ((Number) oldValue).byteValue();
			newNumber = ((Number) newValue).byteValue();
			diff = (byte) (((Number) newValue).byteValue() - ((Number) oldValue)
					.byteValue());
		}
	}

	public static class ChangedShort extends Changed {
		public short diff = 0;
		public short oldNumber = 0;
		public short newNumber = 0;

		public ChangedShort(Object oldValue, Object newValue) {
			super(oldValue, newValue);
			oldNumber = ((Number) oldValue).shortValue();
			newNumber = ((Number) newValue).shortValue();
			diff = (short) (((Number) newValue).shortValue() - ((Number) oldValue)
					.shortValue());
		}
	}

	public static class ChangedInteger extends Changed {
		public int diff = 0;
		public int oldNumber = 0;
		public int newNumber = 0;

		public ChangedInteger(Object oldValue, Object newValue) {
			super(oldValue, newValue);
			oldNumber = ((Number) oldValue).intValue();
			newNumber = ((Number) newValue).intValue();
			diff = (int) (((Number) newValue).intValue() - ((Number) oldValue)
					.intValue());
		}
	}

	public static class ChangedLong extends Changed {
		public long diff = 0;
		public long oldNumber = 0;
		public long newNumber = 0;

		public ChangedLong(Object oldValue, Object newValue) {
			super(oldValue, newValue);
			oldNumber = ((Number) oldValue).longValue();
			newNumber = ((Number) newValue).longValue();
			diff = (long) (((Number) newValue).longValue() - ((Number) oldValue)
					.longValue());
		}
	}

	public static class ChangedFloat extends Changed {
		public float diff = 0;
		public float oldNumber = 0;
		public float newNumber = 0;

		public ChangedFloat(Object oldValue, Object newValue) {
			super(oldValue, newValue);
			oldNumber = ((Number) oldValue).floatValue();
			newNumber = ((Number) newValue).floatValue();
			diff = (float) (((Number) newValue).floatValue() - ((Number) oldValue)
					.floatValue());
		}
	}

	public static class ChangedDouble extends Changed {
		public double diff = 0;
		public double oldNumber = 0;
		public double newNumber = 0;

		public ChangedDouble(Object oldValue, Object newValue) {
			super(oldValue, newValue);
			oldNumber = ((Number) oldValue).doubleValue();
			newNumber = ((Number) newValue).doubleValue();
			diff = (double) (((Number) newValue).doubleValue() - ((Number) oldValue)
					.doubleValue());
		}
	}
}
