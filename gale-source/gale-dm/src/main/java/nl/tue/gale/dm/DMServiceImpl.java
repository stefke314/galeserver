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
 * DMServiceImpl.java
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
package nl.tue.gale.dm;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.cache.Cache;
import nl.tue.gale.common.cache.CacheResolver;
import nl.tue.gale.common.cache.CacheSession;
import nl.tue.gale.common.cache.Caches;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.common.uri.URIs;
import nl.tue.gale.dm.data.Attribute;
import nl.tue.gale.dm.data.Concept;
import nl.tue.gale.dm.data.ConceptRelation;
import nl.tue.gale.event.AbstractEventListener;
import nl.tue.gale.event.EventHash;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.google.common.collect.ImmutableList;

public class DMServiceImpl extends AbstractEventListener {
	private UpdaterThread ut = null;
	private HibernateDaoSupport dataManager = null;
	private Cache<Concept> dm = null;

	public HibernateDaoSupport getDataManager() {
		return dataManager;
	}

	public void setDataManager(HibernateDaoSupport dataManager) {
		this.dataManager = dataManager;
	}

	protected void init() {
		if (ut != null)
			ut.running = false;
		ut = new UpdaterThread();
		ut.start();
		dm = Caches.newCache(250, new DMCacheResolver(), Concept.nullValue);
	}

	public void destroy() {
		if (ut != null) {
			ut.running = false;
			ut.interrupt();
		}
	}

	@SuppressWarnings("unchecked")
	private <T> Collection<T> deProxyCollection(Collection<T> c, Class<?> clazz) {
		Collection<T> result;
		try {
			result = (Collection<T>) clazz.newInstance();
			for (T item : c)
				result.add((T) deProxy(item));
			return result;
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"unable to instantiate collection: " + e.getMessage(), e);
		}
	}

	private Object deProxy(Object o) {
		if (o instanceof org.hibernate.proxy.HibernateProxy)
			return ((org.hibernate.proxy.HibernateProxy) o)
					.getHibernateLazyInitializer().getImplementation();
		else
			return o;
	}

	@SuppressWarnings("unchecked")
	public List<String> event_querydm(List<String> params) {
		try {
			EventHash eh = new EventHash(params.get(0));
			if (!eh.getName().equals("query"))
				throw new IllegalArgumentException(
						"first argument is not a query");
			String query = eh.getItems().get(0);
			List<Object> rlist = new LinkedList<Object>();
			List<Object> qlist = (List<Object>) dataManager
					.getHibernateTemplate().find(query);
			for (Object o : qlist)
				rlist.add(deProxy(o));
			return Arrays.asList(new String[] { "result:ok",
					GaleUtil.gson().toJson(rlist) });
		} catch (Exception e) {
			return error(e);
		}
	}

	public List<String> event_getdm(List<String> params) {
		try {
			EventHash eh = new EventHash(params.get(0));
			if (!eh.getName().equals("uri"))
				throw new IllegalArgumentException("first argument is no uri");
			URI uri = URIs.of(eh.getItems().get(0));
			Concept c = dm.get(uri);
			List<String> result = new LinkedList<String>();
			result.add("result:ok");
			result.addAll(Concept.toEvent(c));
			return result;
		} catch (Exception e) {
			return error(e);
		}
	}

	public List<String> event_setdm(List<String> params) {
		synchronized (updatequeue) {
			updatequeue.addAll(params);
		}
		return Arrays.asList(new String[] { "result:ok" });
	}

	private class DMCacheResolver implements CacheResolver<Concept> {
		public Map<URI, Concept> get(final URI uri, Cache<Concept> cache) {
			final List<String> incr = new LinkedList<String>();
			final List<String> outcr = new LinkedList<String>();
			final Map<URI, Concept> result = new HashMap<URI, Concept>();
			Concept c = (Concept) dataManager.getHibernateTemplate().execute(
					new HibernateCallback() {
						public Object doInHibernate(Session session)
								throws HibernateException, SQLException {
							Concept concept = (Concept) session.get(
									Concept.class, uri.toString());
							if (concept == null)
								return null;
							for (ConceptRelation cr : concept.getInCR())
								incr.add(cr.getName() + ";"
										+ cr.getInConcept().getUriString());
							for (ConceptRelation cr : concept.getOutCR())
								outcr.add(cr.getName() + ";"
										+ cr.getOutConcept().getUriString());
							return concept;
						}
					});
			if (c == null)
				return result;
			c = (Concept) deProxy(c);
			c.setAttributes((Set<Attribute>) deProxyCollection(
					c.getAttributes(), HashSet.class));
			c.setInCR(convertProxySet(true, incr, c, cache));
			c.setOutCR(convertProxySet(false, outcr, c, cache));
			result.put(uri, c);
			return result;
		}

		private Set<ConceptRelation> convertProxySet(boolean incr,
				List<String> list, Concept c, Cache<Concept> cache) {
			Set<ConceptRelation> result = new HashSet<ConceptRelation>();
			for (String entry : list) {
				String key = entry.substring(0, entry.indexOf(";"));
				String value = entry.substring(key.length() + 1);
				ConceptRelation cr = new ConceptRelation(false, key);
				if (incr) {
					cr.setEqualsString(key + ";" + value + ";" + c.getUri());
					cr.setOutConcept(c);
					cr.setInConcept(cache.getProxy(Concept.class,
							URIs.of(value)));
				} else {
					cr.setEqualsString(key + ";" + c.getUri() + ";" + value);
					cr.setInConcept(c);
					cr.setOutConcept(cache.getProxy(Concept.class,
							URIs.of(value)));
				}
				result.add(cr);
			}
			return result;
		}

		private void modifyCR(Session session, ConceptRelation cr) {
			cr.setInConcept((Concept) session.get(Concept.class, cr
					.getInConcept().getUriString()));
			cr.setOutConcept((Concept) session.get(Concept.class, cr
					.getOutConcept().getUriString()));
		}

		public Map<URI, Concept> put(final Map<URI, Concept> map,
				Cache<Concept> cache) {
			dataManager.getHibernateTemplate().execute(new HibernateCallback() {
				@SuppressWarnings("unchecked")
				public Object doInHibernate(Session session)
						throws HibernateException, SQLException {
					for (Concept concept : map.values()) {
						try {
							List<ConceptRelation> oldcr = (List<ConceptRelation>) session
									.createQuery(
											"select cr from ConceptRelation cr where cr.inConcept.uriString = :uri or cr.outConcept.uriString = :uri")
									.setString("uri", concept.getUriString())
									.list();
							for (ConceptRelation cr : oldcr)
								session.delete(cr);
							Object old = session.get(Concept.class,
									concept.getUriString());
							if (old != null)
								session.delete(old);
							session.persist(concept);
						} catch (Exception e) {
							throw new IllegalArgumentException(
									"unable to store concept in database '"
											+ concept + "': " + e.getMessage(),
									e);
						}
					}
					for (Concept concept : map.values()) {
						try {
							for (ConceptRelation cr : concept.getInCR()) {
								modifyCR(session, cr);
								session.saveOrUpdate(cr);
							}
							for (ConceptRelation cr : concept.getOutCR()) {
								modifyCR(session, cr);
								session.saveOrUpdate(cr);
							}
						} catch (Exception e) {
							throw new IllegalArgumentException(
									"unable to store conceptrelation in database '"
											+ concept + "': " + e.getMessage(),
									e);
						}
					}
					return null;
				}
			});
			return new HashMap<URI, Concept>();
		}
	}

	private Queue<String> updatequeue = new ConcurrentLinkedQueue<String>();

	private class UpdaterThread extends Thread {
		public boolean running = true;

		public void run() {
			do {
				if (updatequeue.size() > 0) {
					List<String> sendEvent;
					sendEvent = new LinkedList<String>();
					sendEvent.addAll(updatequeue);
					updatequeue.clear();

					CacheSession<Concept> session = dm.openSession();
					try {
						Map<URI, Concept> putMap = Concept.fromEvent(sendEvent,
								dm);
						for (Concept c : putMap.values())
							session.put(c.getUri(), c);
						session.commit();
						getEventBus().event("updatedm", sendEvent);
					} catch (Exception e) {
						session.rollback();
						e.printStackTrace();
					}
				}

				try {
					sleep(1000);
				} catch (InterruptedException e) {
				}
			} while (running);
		}
	}

	public List<String> event_ccdm(List<String> params) {
		dm.invalidate();
		return ImmutableList.of("result:ok");
	}

	protected String getMethods() {
		return "getdm;setdm;querydm;ccdm";
	}

	@Override
	public List<String> event(String method, List<String> params) {
		List<String> result = super.event(method, params);
		if (result != null)
			return result;
		try {
			if ("getdm".equals(method))
				return event_getdm(params);
			if ("setdm".equals(method))
				return event_setdm(params);
			if ("querydm".equals(method))
				return event_querydm(params);
			if ("ccdm".equals(method))
				return event_ccdm(params);
		} catch (Exception e) {
			return error(e);
		}

		throw new UnsupportedOperationException("'" + method
				+ "' method not supported");
	}
}
