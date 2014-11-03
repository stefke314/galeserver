/*

	This file is part of GALE (Generic Adaptation Language and Engine).

    GALE is free software: you can redistribute it and/or modify it under the 
    terms of the GNU Lesser General Public License as published by the Free 
    Software Foundation, either version 3 of the License, or (at your option) 
    any later version.

    GALE is distributed in the hope that it will be useful, but WITHOUT ANY 
    WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
    FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for 
    more details.

    You should have received a copy of the GNU Lesser General Public License
    along with GALE. If not, see <http://www.gnu.org/licenses/>.
    
 */
/**
 * UMServiceImpl.java
 * Last modified: $Date$
 * In revision:   $Revision$
 * Modified by:   $Author$
 *
 * Copyright (c) 2008-2011 Eindhoven University of Technology.
 * All Rights Reserved.
 *
 * This software is proprietary information of the Eindhoven University
 * of Technology. It may be used according to the GNU LGPL license.
 */
package nl.tue.gale.um;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.cache.Cache;
import nl.tue.gale.common.cache.CacheListener;
import nl.tue.gale.common.cache.CacheResolver;
import nl.tue.gale.common.cache.CacheSession;
import nl.tue.gale.common.cache.Caches;
import nl.tue.gale.common.code.Argument;
import nl.tue.gale.common.code.CodeResolver;
import nl.tue.gale.common.code.GELResolver;
import nl.tue.gale.common.code.JavaCodeManager;
import nl.tue.gale.common.parser.ParserException;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.common.uri.URIs;
import nl.tue.gale.dm.DMCache;
import nl.tue.gale.dm.data.Attribute;
import nl.tue.gale.dm.data.Concept;
import nl.tue.gale.event.AbstractEventListener;
import nl.tue.gale.event.EventCacheResolver;
import nl.tue.gale.event.EventHash;
import nl.tue.gale.um.data.EntityValue;
import nl.tue.gale.um.data.UserEntity;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.google.common.collect.ImmutableList;

public class UMServiceImpl extends AbstractEventListener {
	private UpdaterThread ut = null;
	private Cache<EntityValue> um = Caches.newCache(16384,
			new UMCacheResolver(), EntityValue.nullValue);
	private DMCache dm = new DMCache(new DMCacheResolver());
	private UMGraph graph = new UMGraph(dm);
	private JavaCodeManager cm = null;
	private CodeResolver cr = null;
	private HibernateDaoSupport dataManager = null;
	private int sleepTime = 30000;

	public HibernateDaoSupport getDataManager() {
		return dataManager;
	}

	public void setDataManager(HibernateDaoSupport datamanager) {
		this.dataManager = datamanager;
	}

	public JavaCodeManager getCodeManager() {
		return cm;
	}

	public void setCodeManager(JavaCodeManager cm) {
		this.cm = cm;
	}

	public CodeResolver getCodeResolver() {
		return cr;
	}

	public void setCodeResolver(CodeResolver cr) {
		this.cr = cr;
		cr.setGel(new UMGELResolver());
	}

	public void setSleepTime(int sleepTime) {
		this.sleepTime = sleepTime;
	}

	protected void init() {
		if (ut != null)
			ut.running = false;
		ut = new UpdaterThread(sleepTime);
		ut.start();
		dm.addListener(new DMCacheListener());
	}

	public void destroy() {
		if (ut != null) {
			ut.running = false;
			ut.interrupt();
		}
	}

	private class DMCacheResolver extends EventCacheResolver<Concept> {
		public DMCacheResolver() {
			super(Concept.class);
		}

		protected List<String> getEvents(URI uri) {
			try {
				return getEventBus().event(
						"getdm",
						Arrays.asList(new String[] { EventHash
								.createSingleEvent("uri", uri.toString())
								.toString() }));
			} catch (Exception e) {
				throw new IllegalArgumentException(
						"unable to retrieve objects from event bus", e);
			}
		}

		protected List<String> putEvents(List<String> events) {
			return new LinkedList<String>();
		}
	}

	private void updateGraph(Collection<Concept> concepts) {
		for (Concept concept : concepts)
			updateGraph(concept);
	}

	private Set<URI> getDependencies(Attribute attr) {
		Set<URI> result = new HashSet<URI>();
		if (attr == null)
			return result;
		String code = attr.getDefaultCode();
		if (code == null || "".equals(code))
			return result;

		int i = 0;
		int j = 0;
		do {
			i = code.indexOf("${", i);
			if (i >= 0) {
				j = code.indexOf("}", i);
				if (j < 0)
					throw new IllegalArgumentException("wrong default code: "
							+ code);
				Object resolved = null;
				try {
					resolved = GaleCode.resolve(attr.getConcept(), dm,
							code.substring(i + 2, j));
				} catch (ParserException e) {
					throw new IllegalArgumentException(
							"unable to resolve pseudo-code in: " + code, e);
				}
				if (resolved instanceof String)
					result.add(attr.getConcept().getUri());
				if (resolved instanceof Concept[])
					for (Concept c : (Concept[]) resolved)
						result.add(c.getUri());
				if (resolved instanceof Concept)
					result.add(((Concept) resolved).getUri());
				if (resolved instanceof Attribute)
					result.add(((Attribute) resolved).getUri());
				if (resolved instanceof Attribute[])
					for (Attribute a : (Attribute[]) resolved)
						result.add(a.getUri());
				i = i + 2;
			}
		} while (code.indexOf("${", i) >= 0);

		return result;
	}

	private void updateGraph(Concept concept) {
		synchronized (graph) {
			for (Attribute attr : concept.getVirtualAttributes()) {
				// remove old graph dependencies
				URI uri = attr.getUri();
				for (URI source : graph.getReverseSet(uri))
					graph.removeLink(source, uri);

				// add new graph dependencies
				for (URI source : getDependencies(attr))
					graph.addLink(source, uri);
			}
		}
	}

	private class DMCacheListener implements CacheListener<Concept> {
		public void objectAdded(Concept object) {
			updateGraph(object);
		}
	}

	private EntityValue getDefaultEntityValue(URI uri) {
		EntityValue result = new EntityValue();
		result.setUri(uri);
		Concept concept = dm.get(Concept.getConceptURI(uri));
		if (concept == null)
			return result;
		Attribute attr = concept.getAttribute(uri.getFragment());
		if (attr == null)
			return result;
		CacheSession<EntityValue> session = um.openSession();
		session.setBaseUri(uri);
		result.setValue(cm.evaluate(cr, attr.getDefaultCode(),
				argumentBuilder(session).build()));
		return result;
	}

	private boolean isPersistent(URI uri) {
		Concept concept = dm.get(Concept.getConceptURI(uri));
		if (concept == null)
			return true;
		Attribute attr = concept.getAttribute(uri.getFragment());
		if (attr == null)
			return true;
		return attr.isPersistent();
	}

	private class UMCacheResolver implements CacheResolver<EntityValue> {
		public Map<URI, EntityValue> get(URI uri, Cache<EntityValue> cache) {
			Map<URI, EntityValue> result = new HashMap<URI, EntityValue>();
			EntityValue ev = null;
			if (isPersistent(uri))
				ev = (EntityValue) dataManager.getHibernateTemplate().get(
						EntityValue.class, uri.toString());
			if (ev == null)
				ev = getDefaultEntityValue(uri);
			if (ev.getValue() == null
					&& "system.elapsed".equals(ev.getUri().getFragment()))
				ev.setValue(0l);
			checkPublic(uri, false);
			result.put(uri, ev);
			return result;
		}

		public Map<URI, EntityValue> put(final Map<URI, EntityValue> map,
				Cache<EntityValue> cache) {
			for (EntityValue ev : map.values()) {
				checkPublic(ev.getUri(), true);
				updatequeue.add(ev);
			}
			return new HashMap<URI, EntityValue>();
		}
	}

	private Object deProxy(Object o) {
		if (o instanceof org.hibernate.proxy.HibernateProxy)
			return ((org.hibernate.proxy.HibernateProxy) o)
					.getHibernateLazyInitializer().getImplementation();
		else if (o instanceof UserEntity) {
			final UserEntity oue = (UserEntity) o;
			UserEntity ue = new UserEntity();
			ue.setId(oue.getId());
			ue.getProperties().putAll(oue.getProperties());
			return ue;
		} else
			return o;
	}

	private void checkPublic(URI uri, boolean tell) {
		Concept concept = dm.get(Concept.getConceptURI(uri));
		if (concept == null)
			return;
		Attribute attr = concept.getAttribute(uri.getFragment());
		if (attr == null)
			return;
		if (attr.getProperty("public") == null
				|| "".equals(attr.getProperty("public")))
			return;
		boolean authorative = "true".equals(attr.getProperty("authorative"));
		EventHash eh = new EventHash("publicum");
		eh.put("uri", uri.toString());
		eh.put("authorative", (new Boolean(authorative)).toString());
		eh.put("publicUri", attr.getProperty("public"));
		if (attr.getProperty("publicPredicate") != null)
			eh.put("publicPredicate", attr.getProperty("publicPredicate"));
		eh.put("type", attr.getType());
		String range = attr.getProperty("gumf.range");
		if (range != null)
			eh.put("range", range);
		if (!authorative && !tell)
			getEventBus().event("getPublicUM",
					Arrays.asList(new String[] { eh.toString() }));
		else if (authorative && tell)
			getEventBus().event("setPublicUM",
					Arrays.asList(new String[] { eh.toString() }));
	}

	@SuppressWarnings("unchecked")
	private List<String> event_queryum(List<String> params) {
		try {
			EventHash eh = new EventHash(params.get(0));
			if (!eh.getName().equals("query"))
				throw new IllegalArgumentException(
						"first argument is not a query");
			final String query = eh.getItems().get(0);
			ut.flush();
			return (List<String>) dataManager.getHibernateTemplate().execute(
					new HibernateCallback() {
						public Object doInHibernate(Session session)
								throws HibernateException, SQLException {
							List<Object> rlist = new LinkedList<Object>();
							List<Object> qlist = (List<Object>) session
									.createQuery(query).list();
							for (Object o : qlist)
								rlist.add(deProxy(o));
							try {
								return Arrays.asList(new String[] {
										"result:ok",
										GaleUtil.gson().toJson(rlist) });
							} catch (Exception e) {
								return error(e);
							}
						}
					});
		} catch (Exception e) {
			return error(e);
		}
	}

	private List<String> event_invalidateum(List<String> params) {
		try {
			Set<URI> toRemove = new HashSet<URI>();
			for (String param : params) {
				EventHash eh = new EventHash(param);
				if (!eh.getName().equals("invalidate"))
					continue;
				Set<URI> noUserInfo = new HashSet<URI>();
				for (String uriString : eh.getItems()) {
					URI uri = URIs.of(uriString);
					if (uri.getUserInfo() == null)
						noUserInfo.add(uri);
					else
						toRemove.add(uri);
				}

				if (noUserInfo.size() > 0) {
					Set<URI> removeSet = new HashSet<URI>();
					for (URI uri : um.uriSet())
						if (noUserInfo.contains(removeUserInfo(uri)))
							removeSet.add(uri);
					toRemove.addAll(removeSet);
				}
			}
			Set<URI> toRemoveExpand = new HashSet<URI>();
			for (URI uri : toRemove) {
				Set<URI> tempSet = new HashSet<URI>();
				tempSet.add(uri);
				String userInfo = removeUserInfo(tempSet);
				synchronized (graph) {
					graph.expandSet(tempSet);
				}
				addUserInfo(tempSet, userInfo);
				toRemoveExpand.addAll(tempSet);
			}
			Map<URI, EntityValue> updateMap = new HashMap<URI, EntityValue>();
			for (URI uri : toRemoveExpand)
				updateMap.put(uri, null);
			um.cacheUpdate(updateMap);
			List<String> rparams = new LinkedList<String>();

			// System.out.println("\ntesting invalidateum:\n");
			// for (String param : params)
			// System.out.println(param + ", ");
			// System.out.println("\n->\n" + toRemoveExpand);

			for (URI uri : toRemoveExpand)
				rparams.addAll(EntityValue.toEvent(new EntityValue(uri, null)));
			getEventBus().event("updateum", rparams);
			return Arrays.asList(new String[] { "result:ok" });
		} catch (Exception e) {
			return error(e);
		}
	}

	private List<String> event_updatedm(List<String> params) {
		try {
			CacheSession<Concept> session = dm.openSession();
			Map<URI, Concept> map = Concept.fromEvent(params, dm);
			Set<URI> updateSet = new HashSet<URI>();
			for (Map.Entry<URI, Concept> entry : map.entrySet()) {
				session.put(entry.getKey(), entry.getValue());
				for (Attribute attr : entry.getValue().getAttributes())
					updateSet.add(attr.getUri());
			}
			ut.flush();
			session.commit();
			for (URI uri : map.keySet())
				dm.get(uri).refresh();
			updateGraph(map.values());
			synchronized (graph) {
				graph.expandSet(updateSet);
			}
			EventHash eh = new EventHash("invalidate");
			for (URI uri : updateSet)
				eh.addItem(uri.toString());
			getEventBus().event("invalidateum",
					Arrays.asList(new String[] { eh.toString() }));
		} catch (Exception e) {
			return error(e);
		}
		return Arrays.asList(new String[] { "result:ok" });
	}

	private String removeUserInfo(Collection<URI> uriList) {
		String result = null;
		List<URI> resultList = new LinkedList<URI>();
		for (URI uri : uriList) {
			if (uri.getUserInfo() != null)
				result = uri.getUserInfo();
			resultList.add(removeUserInfo(uri));
		}
		uriList.clear();
		uriList.addAll(resultList);
		return result;
	}

	private URI removeUserInfo(URI uri) {
		return Attribute.getAttributeURI(uri);
	}

	private void addUserInfo(Collection<URI> uriList, String userInfo) {
		List<URI> resultList = new LinkedList<URI>();
		for (URI uri : uriList)
			resultList.add(addUserInfo(uri, userInfo));
		uriList.clear();
		uriList.addAll(resultList);
	}

	private URI addUserInfo(URI uri, String userInfo) {
		try {
			return URIs.of(uri.getScheme(), userInfo, uri.getHost(),
					uri.getPort(), uri.getPath(), uri.getQuery(),
					uri.getFragment());
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"unable to add user info to uri", e);
		}
	}

	private List<String> event_setum(List<String> params) {
		try {
			ActionQueue queue = ActionQueue.create(dm, um, cm, cr, graph);
			for (EntityValue ev : EntityValue.fromEvent(params, um).values())
				if (ev.getUri().getFragment().equals("system.elapsed"))
					queue.queue(calcElapsed(ev));
				else
					queue.queue(ev);
			queue.run();
			List<String> result = new LinkedList<String>();
			for (EntityValue ev : queue.session().getChangeMap().values())
				result.addAll(EntityValue.toEvent(ev));
			queue.session().commit();
			return result;
		} catch (Exception e) {
			return error(e);
		}
	}

	private EntityValue calcElapsed(EntityValue ev) {
		EntityValue result = new EntityValue();
		result.setUri(ev.getUri());
		Long current = (Long) um.get(ev.getUri()).getValue();
		result.setValue(current + (Long) ev.getValue());
		ev.setValue(result.getValue());
		return result;
	}

	private Argument.ListBuilder argumentBuilder(
			CacheSession<EntityValue> session) {
		return Argument
				.listBuilder()
				.arg("dm", "nl.tue.gale.dm.DMCache", dm)
				.arg("session", "nl.tue.gale.common.cache.CacheSession",
						session);
	}

	private List<String> event_getum(List<String> params) {
		// TODO: handle multiple uris
		try {
			EventHash eh = new EventHash(params.get(0));
			if (!eh.getName().equals("uri"))
				throw new IllegalArgumentException("first argument is no uri");
			URI uri = URIs.of(eh.getItems().get(0));
			EntityValue ev = um.get(uri);
			List<String> result = new LinkedList<String>();
			result.add("result:ok");
			result.addAll(EntityValue.toEvent(ev));
			return result;
		} catch (Exception e) {
			return error(e);
		}
	}

	private List<String> event_getentity(List<String> params) {
		try {
			EventHash eh = new EventHash(params.get(0));
			if (!eh.getName().equals("uri"))
				throw new IllegalArgumentException("first argument is no uri");
			URI uri = URIs.of(eh.getItems().get(0));
			UserEntity ue = (UserEntity) dataManager.getHibernateTemplate()
					.get(UserEntity.class, UserEntity.getIdFromUri(uri));
			List<String> result = new LinkedList<String>();
			result.add("result:ok");
			result.addAll(UserEntity.toEvent(ue));
			return result;
		} catch (Exception e) {
			return error(e);
		}
	}

	private List<String> event_setentity(final List<String> params) {
		try {
			dataManager.getHibernateTemplate().execute(new HibernateCallback() {
				public Object doInHibernate(Session session)
						throws HibernateException, SQLException {
					Map<URI, UserEntity> map = UserEntity.fromEvent(params,
							null);
					for (UserEntity ue : map.values()) {
						UserEntity old = (UserEntity) session.get(
								UserEntity.class, ue.getId());
						if (old != null)
							session.delete(old);
						session.save(ue);
					}
					return null;
				}
			});
		} catch (Exception e) {
			return error(e);
		}
		return Arrays.asList(new String[] { "result:ok" });
	}

	private Queue<EntityValue> updatequeue = new ConcurrentLinkedQueue<EntityValue>();

	private class UpdaterThread extends Thread {
		private final int sleepTime;

		public UpdaterThread(int sleepTime) {
			this.sleepTime = sleepTime;
		}

		private final List<String> updateEvents = new LinkedList<String>();

		public boolean running = true;
		private AtomicBoolean lock = new AtomicBoolean(false);

		public void run() {
			do {
				updateEvents.clear();
				if (!updatequeue.isEmpty())
					try {
						dataManager.getHibernateTemplate().execute(
								new HibernateCallback() {
									public Object doInHibernate(Session session)
											throws HibernateException,
											SQLException {
										storeValue(session, updateEvents);
										return null;
									}
								});
					} catch (Exception e) {
						e.printStackTrace();
					}
				if (!updateEvents.isEmpty())
					getEventBus().event("updateum", updateEvents);
				while (!lock.compareAndSet(false, true)) {
				}
				try {
					sleep(sleepTime);
				} catch (InterruptedException e) {
				}
				lock.set(false);
			} while (running);
			if (!updatequeue.isEmpty())
				try {
					dataManager.getHibernateTemplate().execute(
							new HibernateCallback() {
								public Object doInHibernate(Session session)
										throws HibernateException, SQLException {
									storeValue(session, updateEvents);
									return null;
								}
							});
				} catch (Exception e) {
					e.printStackTrace();
				}
		}

		public void flush() {
			if (lock.compareAndSet(true, false)) {
				interrupt();
				while (!lock.get()) {
				}
			}
		}
	}

	private void storeValue(Session session, List<String> updateEvents) {
		EntityValue ev;
		Map<URI, EntityValue> todo = new LinkedHashMap<URI, EntityValue>();
		while ((ev = updatequeue.poll()) != null) {
			if (isPersistent(ev.getUri())) {
				EntityValue old = (EntityValue) session.get(EntityValue.class,
						ev.getUri().toString());
				if (old != null)
					session.delete(old);
				todo.put(ev.getUri(), ev);
			}
			if (!ev.getUri().getFragment().startsWith("system."))
				updateEvents.addAll(EntityValue.toEvent(ev));
		}
		for (EntityValue value : todo.values())
			session.save(value);
	}

	private class UMGELResolver implements GELResolver {
		@Override
		public void resolveStateChange(List<Argument> oldValues,
				List<Argument> newValues) {
			@SuppressWarnings("unchecked")
			CacheSession<EntityValue> session = (CacheSession<EntityValue>) getArgument(
					oldValues, "nl.tue.gale.common.cache.CacheSession")
					.getValue();
			try {
				for (int i = 0; i < oldValues.size(); i++) {
					if (oldValues.get(i).getName().startsWith("_v_")
							&& !GaleUtil.safeEquals(
									oldValues.get(i).getValue(),
									newValues.get(i).getValue()))
						storeStateChange(session, newValues.get(i));
				}
			} catch (Exception e) {
				throw new IllegalArgumentException(
						"unable to reconcile state change", e);
			}
		}

		private void storeStateChange(CacheSession<EntityValue> session,
				Argument argument) {
			if (!(argument.getUserData() instanceof Attribute))
				throw new IllegalStateException(
						"the value of a non user-model variable changed");
			Attribute attr = (Attribute) argument.getUserData();
			if (!isPersistent(attr.getUri()))
				throw new IllegalArgumentException(
						"can only set persistent user model variables: '"
								+ attr.getUri() + "'");
			URI evURI = session.resolve(attr.getUri());
			session.put(evURI, new EntityValue(evURI, argument.getValue()));
		}

		@Override
		public Argument resolveGaleVariable(String name, List<Argument> params) {
			DMCache dm = (DMCache) getArgument(params, "nl.tue.gale.dm.DMCache")
					.getValue();
			@SuppressWarnings("unchecked")
			CacheSession<EntityValue> session = (CacheSession<EntityValue>) getArgument(
					params, "nl.tue.gale.common.cache.CacheSession").getValue();
			try {
				Object o = GaleCode.resolve(
						dm.get(Concept.getConceptURI(session.getBaseUri())),
						dm, name);
				if (o == null)
					throw new IllegalArgumentException("variable not found: '"
							+ name + "'");
				int max = -1;
				for (Argument arg : params) {
					if (arg.getName().startsWith("_v_"))
						max = Math.max(max,
								Integer.parseInt(arg.getName().substring(3)));
					if (o.equals(arg.getUserData()))
						return arg;
				}
				max++;
				Object userData = o;
				if (o instanceof Attribute) {
					o = getAttributeValue(session, (Attribute) o);
				} else if (o instanceof Attribute[]) {
					Attribute[] attrArray = (Attribute[]) o;
					attrArray = Arrays.copyOf(attrArray, attrArray.length);
					o = new Object[attrArray.length];
					for (int i = 0; i < attrArray.length; i++)
						((Object[]) o)[i] = getAttributeValue(session,
								attrArray[i]);
				}
				Argument result = Argument.of("_v_" + max, o.getClass()
						.getSimpleName(), o);
				result.setUserData(userData);
				return result;
			} catch (Exception e) {
				throw new IllegalArgumentException(
						"unable to resolve variable '" + name + "'", e);
			}
		}

		private Object getAttributeValue(CacheSession<EntityValue> session,
				Attribute attr) {
			return session.get(attr.getUri()).getValue();
		}

		private Argument getArgument(List<Argument> arguments, String className) {
			for (Argument arg : arguments)
				if (arg.getType().equals(className))
					return arg;
			return null;
		}
	}

	private List<String> event_resetum(List<String> params) {
		try {
			EventHash eh = new EventHash(params.get(0));
			if (!eh.getName().equals("user"))
				throw new IllegalArgumentException(
						"first argument is no userId");
			final String userId = eh.getItems().get(0);
			ut.flush();
			boolean all = (params.size() > 1 && (new EventHash(params.get(1))
					.getName().equals("all")));
			final String qString = "from EntityValue where userId=:userId"
					+ (all ? "" : " and not (attributeUri like '%#comments')");

			// remove from database
			dataManager.getHibernateTemplate().execute(new HibernateCallback() {
				@Override
				public Object doInHibernate(Session session)
						throws HibernateException, SQLException {
					@SuppressWarnings("unchecked")
					List<EntityValue> evs = (List<EntityValue>) session
							.createQuery(qString).setString("userId", userId)
							.list();
					for (EntityValue ev : evs)
						if (!Concept.getConceptURI(ev.getUri()).toString()
								.startsWith("gale://gale.tue.nl/personal"))
							session.delete(ev);
					return null;
				}
			});

			// invalidate cache
			List<URI> toInvalidate = new LinkedList<URI>();
			for (URI uri : um.uriSet())
				if (userId.equals(uri.getUserInfo()))
					toInvalidate.add(uri);
			um.invalidate(toInvalidate);

			return ImmutableList.of("result:ok");
		} catch (Exception e) {
			return error(e);
		}
	}

	private List<String> event_ccum(List<String> params) {
		ut.flush();
		synchronized (graph) {
			dm.invalidate();
			um.invalidate();
			graph.invalidate();
		}
		return ImmutableList.of("result:ok");
	}

	protected String getMethods() {
		return "getum;setum;queryum;getentity;setentity;updatedm;invalidateum;resetum;ccum";
	}

	@Override
	public List<String> event(String method, List<String> params) {
		List<String> result = super.event(method, params);
		if (result != null)
			return result;
		try {
			if ("getum".equals(method))
				return event_getum(params);
			if ("setum".equals(method))
				return event_setum(params);
			if ("queryum".equals(method))
				return event_queryum(params);
			if ("getentity".equals(method))
				return event_getentity(params);
			if ("setentity".equals(method))
				return event_setentity(params);
			if ("updatedm".equals(method))
				return event_updatedm(params);
			if ("invalidateum".equals(method))
				return event_invalidateum(params);
			if ("resetum".equals(method))
				return event_resetum(params);
			if ("ccum".equals(method))
				return event_ccum(params);
		} catch (Exception e) {
			return error(e);
		}
		throw new UnsupportedOperationException("'" + method
				+ "' method not supported");
	}
}
