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
 * GEBServiceImpl.java
 * Last modified: $Date$
 * In revision:   $Revision$
 * Modified by:   $Author: dsmits $
 *
 * Copyright (c) 2008-2011 Eindhoven University of Technology.
 * All Rights Reserved.
 *
 * This software is proprietary information of the Eindhoven University
 * of Technology. It may be used according to the GNU LGPL license.
 */
package nl.tue.gale.geb;

import java.util.Arrays;
import java.util.EventObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.common.uri.URIs;
import nl.tue.gale.dm.data.Concept;
import nl.tue.gale.event.AbstractEventListener;
import nl.tue.gale.event.EventHash;
import nl.tue.gale.um.data.EntityValue;

import com.google.common.collect.ImmutableList;

public class GEBServiceImpl extends AbstractEventListener {
	private static final URI galePublicNamespace = URIs
			.of("http://gale.tue.nl/predicate/");

	private String gumfUser = null;
	private String gumfToken = null;
	private boolean autoRegister = false;
	private GEBThread thread = null;
	private GEBManager gebManager = null;

	public GEBManager getGebManager() {
		return gebManager;
	}

	public void setGebManager(GEBManager gebManager) {
		this.gebManager = gebManager;
	}

	public String getGumfUser() {
		return gumfUser;
	}

	public void setGumfUser(String gumfUser) {
		this.gumfUser = gumfUser;
	}

	public String getGumfToken() {
		return gumfToken;
	}

	public void setGumfToken(String gumfToken) {
		this.gumfToken = gumfToken;
	}

	public boolean isAutoRegister() {
		return autoRegister;
	}

	public void setAutoRegister(boolean autoRegister) {
		this.autoRegister = autoRegister;
	}

	public List<String> event_ccum(List<String> params) {
		synchronized (invMap) {
			authMap.clear();
			invMap.clear();
			reverseInvMap.clear();
		}
		return ImmutableList.of("result:ok");
	}

	protected String getMethods() {
		if (!"true".equals(GaleUtil.getProperty("useGEB")))
			return "";
		return "getPublicUM;setPublicUM;updateum;ccum";
	}

	@Override
	public List<String> event(String method, List<String> params) {
		List<String> result = super.event(method, params);
		if (result != null)
			return result;
		try {
			if ("getPublicUM".equals(method))
				return event_getPublicUM(params);
			if ("setPublicUM".equals(method))
				return event_setPublicUM(params);
			if ("updateum".equals(method))
				return event_updateum(params);
			if ("ccum".equals(method))
				return event_ccum(params);
		} catch (Exception e) {
			return error(e);
		}
		throw new UnsupportedOperationException("'" + method
				+ "' method not supported");
	}

	protected void init() {
		if (thread != null)
			thread.running = false;
		thread = new GEBThread();
		thread.start();
		if (autoRegister) {
			registerGEB();
		}
	}

	public void destroy() {
		if (thread != null) {
			thread.running = false;
			thread.interrupt();
		}
	}

	public void registerGEB() {
	}

	public List<String> event_getPublicUM(List<String> params) {
		for (String param : params) {
			EventHash eh = new EventHash(param);
			if (checkEvent(eh)) {
				todoList.offer(new AskRequest(eh));
			}
		}
		return Arrays.asList(new String[] { "result:ok" });
	}

	public List<String> event_setPublicUM(List<String> params) {
		for (String param : params) {
			EventHash eh = new EventHash(param);
			checkEvent(eh);
		}
		return Arrays.asList(new String[] { "result:ok" });
	}

	private boolean checkEvent(EventHash eh) {
		if (!eh.getName().equals("publicum"))
			return false;
		URI uri = URIs.of(eh.get("uri"));
		synchronized (invMap) {
			if (reverseInvMap.containsKey(uri))
				invMap.get(reverseInvMap.get(uri)).remove(uri);
			long now = System.currentTimeMillis();
			List<URI> invList = invMap.get(now);
			if (invList == null) {
				invList = new LinkedList<URI>();
				invMap.put(now, invList);
			}
			invList.add(uri);
			reverseInvMap.put(uri, now);
			authMap.put(uri, new Properties());
			authMap.get(uri).setProperty("range",
					GaleUtil.nullToEmpty(eh.get("range")));
			if ("true".equals(eh.get("authorative"))) {
				authMap.get(uri).setProperty("publicUri", eh.get("publicUri"));
				if (eh.get("publicPredicate") != null)
					authMap.get(uri).setProperty("publicPredicate",
							eh.get("publicPredicate"));
				authMap.get(uri).setProperty("authorative", "true");
			}
		}
		return true;
	}

	public List<String> event_updateum(List<String> params) {
		Map<URI, EntityValue> updates = EntityValue.fromEvent(params, null);
		for (EntityValue ev : updates.values())
			if (ev != null) {
				synchronized (invMap) {
					if (authMap.containsKey(ev.getUri())
							&& "true".equals(authMap.get(ev.getUri())
									.getProperty("authorative")))
						todoList.offer(new TellRequest(ev));
				}
			}
		return Arrays.asList(new String[] { "result:ok" });
	}

	private interface GUMFRequest {
	}

	private class AskRequest implements GUMFRequest {
		private URI uri = null;
		private String publicUri = null;
		private String publicPredicate = null;
		private String typeClass = null;

		public URI getUri() {
			return uri;
		}

		public String getPublicUri() {
			return publicUri;
		}

		public String getTypeClass() {
			return typeClass;
		}

		public String getPublicPredicate() {
			return publicPredicate;
		}

		@SuppressWarnings("unused")
		public void setPublicPredicate(String publicPredicate) {
			this.publicPredicate = publicPredicate;
		}

		public String getPredicateGUMF() {
			if (getPublicPredicate() != null)
				return getPublicPredicate();
			else {
				URI predicate = URIs.of(getUri().getFragment());
				if (!predicate.isAbsolute())
					predicate = galePublicNamespace.resolve(predicate);
				return predicate.toString();
			}
		}

		public String getPublicUriGUMF() {
			if (getPublicUri().equals("true"))
				return Concept.getConceptURI(getUri()).toString();
			return getPublicUri();
		}

		public AskRequest(EventHash eh) {
			uri = URIs.of(eh.get("uri"));
			publicUri = eh.get("publicUri");
			publicPredicate = eh.get("publicPredicate");
			typeClass = eh.get("type");
		}

		public String toString() {
			return "AskRequest uri=" + uri + ", publicUri="
					+ getPublicUriGUMF() + ", publicPredicate="
					+ getPredicateGUMF() + ", typeClass=" + typeClass;
		}
	}

	private class TellRequest implements GUMFRequest {
		private URI uri = null;
		private String publicUri = null;
		private String publicPredicate = null;
		private Object value = null;
		private String range = null;

		public String getRange() {
			return range;
		}

		@SuppressWarnings("unused")
		public void setRange(String range) {
			this.range = range;
		}

		public URI getUri() {
			return uri;
		}

		public String getPublicUri() {
			return publicUri;
		}

		public Object getValue() {
			return value;
		}

		public String getPublicPredicate() {
			return publicPredicate;
		}

		@SuppressWarnings("unused")
		public void setPublicPredicate(String publicPredicate) {
			this.publicPredicate = publicPredicate;
		}

		public String getPredicateGUMF() {
			if (getPublicPredicate() != null)
				return getPublicPredicate();
			else {
				URI predicate = URIs.of(getUri().getFragment());
				if (!predicate.isAbsolute())
					predicate = galePublicNamespace.resolve(predicate);
				return predicate.toString();
			}
		}

		public String getPublicUriGUMF() {
			if (getPublicUri().equals("true"))
				return Concept.getConceptURI(getUri()).toString();
			return getPublicUri();
		}

		public TellRequest(EntityValue ev) {
			uri = ev.getUri();
			value = ev.getValue();
			publicUri = authMap.get(uri).getProperty("publicUri");
			publicPredicate = authMap.get(uri).getProperty("publicPredicate");
			range = authMap.get(uri).getProperty("range");
		}

		public String toString() {
			return "TellRequest uri=" + uri + ", publicUri=" + publicUri
					+ ", value=" + value + ", range=" + range;
		}
	}

	private Queue<GUMFRequest> todoList = new ConcurrentLinkedQueue<GUMFRequest>();
	private Map<URI, Properties> authMap = new HashMap<URI, Properties>();
	private SortedMap<Long, List<URI>> invMap = new TreeMap<Long, List<URI>>();
	private Map<URI, Long> reverseInvMap = new HashMap<URI, Long>();

	private class GEBThread extends Thread {
		private static final long invTime = 15000;

		public boolean running = true;

		public void run() {
			do {
				boolean loop;
				try {
					do {
						GUMFRequest request = null;
						request = todoList.poll();
						loop = request != null;
						if (loop)
							doRequest(request);
					} while (loop);
					do {
						synchronized (invMap) {
							loop = (!invMap.isEmpty())
									&& ((System.currentTimeMillis() - invMap
											.firstKey()) > invTime);
							if (loop) {
								EventHash eh = new EventHash("invalidate");
								List<URI> invList = invMap.get(invMap
										.firstKey());
								for (URI uri : invList) {
									Properties p = authMap.remove(uri);
									boolean auth = false;
									if (p != null
											&& "true"
													.equals(p
															.getProperty("authorative")))
										auth = true;
									if (!auth)
										eh.addItem(uri.toString());
									reverseInvMap.remove(uri);
								}
								invMap.remove(invMap.firstKey());
								if (eh.getItems().size() > 0)
									getEventBus().event(
											"invalidateum",
											Arrays.asList(new String[] { eh
													.toString() }));
							}
						}
					} while (loop);
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					sleep(500);
				} catch (InterruptedException e) {
				}
			} while (running);
		}
	}

	private void doRequest(final TellRequest tellRequest) {
		// DISABLED: requires dependancy update //
		/*
		GumfGebClientExtended client = ClientFactory.getGumfGebClientExtended();
		System.out.println("set gumf from um '" + tellRequest.getUri() + "': "
				+ tellRequest.getValue());
		// System.out.println("SENDING GEB TELLREQUEST:\n" + tellRequest);
		String eventId = client.tellToDefaultDataspace(tellRequest.getUri()
				.getUserInfo(), tellRequest.getPredicateGUMF(), tellRequest
				.getPublicUriGUMF(), tellRequest.value.toString(), null,
				"GALE", null, tellRequest.getRange());
		gebManager.registerCallback(eventId, new CallbackListener() {
			public void callback(EventObject source) {
				String body = source.getSource().toString();
				System.out.println("TELLREQUEST RESPONSE:\n" + body);
			}
		});
		*/
	}

	private void doRequest(final AskRequest askRequest) {
		// DISABLED: requires dependancy update //
		/*
		GumfGebClientExtended client = ClientFactory.getGumfGebClientExtended();
		System.out.println("SENDING GEB ASKREQUEST:\n" + askRequest);
		String eventId = client.askDefaultDataspace(askRequest.getUri()
				.getUserInfo(), askRequest.getPredicateGUMF(), askRequest
				.getPublicUriGUMF(), null, null, "GALE");
		System.out.println("eventId: " + eventId);
		gebManager.registerCallback(eventId, new CallbackListener() {
			public void callback(EventObject source) {
				String body = source.getSource().toString();
				@SuppressWarnings("rawtypes")
				List<GrappleStatement> statements = ReadWriteUtility
						.deserializeFromRDF_XML(body);
				@SuppressWarnings("rawtypes")
				GrappleStatement latest = ClientUtility
						.selectLatestGrappleStatement(statements);
				try {
					String value = latest.getLevel().toString();
					Object ovalue = GaleUtil.typedObject(
							askRequest.getTypeClass(), value);
					EntityValue ev = new EntityValue(askRequest.getUri(),
							ovalue);
					getEventBus().event("setum", EntityValue.toEvent(ev));
					System.out.println("set um from gumf '" + ev.getUriString()
							+ "': " + ovalue);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		*/
	}

	private void doRequest(GUMFRequest request) {
		if (request instanceof TellRequest)
			doRequest((TellRequest) request);
		else if (request instanceof AskRequest)
			doRequest((AskRequest) request);
	}
}
