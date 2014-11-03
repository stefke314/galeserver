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
 * GAMServiceImpl.java
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

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.tue.gale.common.cache.Cache;
import nl.tue.gale.common.cache.CacheSession;
import nl.tue.gale.common.cache.Caches;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.common.uri.URIs;
import nl.tue.gale.dm.data.Concept;
import nl.tue.gale.event.AbstractEventListener;
import nl.tue.gale.event.EventHash;

import org.apache.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class GAMServiceImpl extends AbstractEventListener implements
		UpdateListener {
	private static final Logger log = Logger.getLogger(GAMServiceImpl.class);

	private DefinitionLocator locator = DefinitionLocator.instance();
	private Cache<Concept> cache = Caches.newCache(1000);
	private Set<String> safeDomains = ImmutableSet.of();

	@SuppressWarnings("unchecked")
	public void setGaleConfig(Map<String, Object> galeConfig) {
		safeDomains = (Set<String>) galeConfig.get("safeDomains");
	}

	private List<String> event_getdm(List<String> params) {
		EventHash eh = new EventHash(params.get(0));
		if (!eh.getName().equals("uri"))
			throw new IllegalArgumentException("first argument is no uri: '"
					+ params.get(0) + "'");
		String conceptUriString = eh.getItems().get(0);
		Concept concept = cache.get(URIs.of(conceptUriString));
		if (concept == null) {
			URI conceptUri = URIs.of(conceptUriString);
			if (!"http".equals(conceptUri.getScheme()) && !"https".equals(conceptUri.getScheme())) {
			} else if (!safeDomains.contains(conceptUri.getHost().toLowerCase()
					.trim()))
				System.err.println("the domain '" + conceptUri.getHost()
						+ "' is not in the safe domains list");
			else {
				URL definitionURL = locator.getDefinitionURL(conceptUri);
				if (definitionURL != null) {
					Set<Concept> loaded = locator.loadDefinition(definitionURL);
					List<String> loadedEvents = new LinkedList<String>();
					for (Concept c : loaded)
						loadedEvents.addAll(Concept.toEvent(c));
					loaded.clear();
					loaded.addAll(Concept.fromEvent(loadedEvents, cache)
							.values());
					CacheSession<Concept> session = cache.openSession();
					for (Concept c : loaded) {
						session.put(c.getUri(), c);
						if (c.getUriString().equals(conceptUriString))
							concept = c;
					}
					session.commit();
				}
			}
		}
		List<String> result = new LinkedList<String>();
		result.add("result:ok");
		if (concept != null)
			result.addAll(Concept.toEvent(concept));
		return result;
	}

	private List<String> event_ccdm(List<String> params) {
		cache.invalidate();
		locator.invalidate();
		return ImmutableList.of("result:ok");
	}

	@Override
	protected String getMethods() {
		return "getdm;ccdm";
	}

	@Override
	protected void init() {
		locator.setUpdateListener(this);
		locator.setCache(cache);
	}

	public void destroy() {
		locator.destroy();
	}

	@Override
	public void update(List<Concept> concepts) {
		log.debug("concepts have changed: " + concepts);
		List<String> events = new LinkedList<String>();
		CacheSession<Concept> session = cache.openSession();
		for (Concept concept : concepts) {
			session.put(concept.getUri(), concept);
			events.addAll(Concept.toEvent(concept));
		}
		session.commit();
		try {
			getEventBus().event("updatedm", events);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<String> event(String method, List<String> params) {
		List<String> result = super.event(method, params);
		if (result != null)
			return result;
		try {
			if ("getdm".equals(method))
				return event_getdm(params);
			if ("ccdm".equals(method))
				return event_ccdm(params);
		} catch (Exception e) {
			return error(e);
		}
		throw new UnsupportedOperationException("'" + method
				+ "' method not supported");
	}
}