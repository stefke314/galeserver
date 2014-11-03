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
 * UMCache.java
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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.tue.gale.common.cache.CacheListener;
import nl.tue.gale.common.cache.CacheResolver;
import nl.tue.gale.common.cache.Caches;
import nl.tue.gale.common.cache.ForwardingCache;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.um.data.EntityValue;

public class UMCache extends ForwardingCache<EntityValue> {
	private Map<String, Set<String>> updatedSessionMap = new HashMap<String, Set<String>>();

	public boolean pollUpdated(String userId, String sessionId) {
		synchronized (updatedSessionMap) {
			Set<String> sessionSet = updatedSessionMap.get(userId);
			if (sessionSet == null)
				return false;
			boolean result = !sessionSet.contains(sessionId);
			sessionSet.add(sessionId);
			return result;
		}
	}

	public UMCache(CacheResolver<EntityValue> resolver) {
		super(Caches.newCache(16384, resolver, EntityValue.nullValue));
		addListener(new CacheListener<EntityValue>() {
			public void objectAdded(EntityValue object) {
				if (object != null)
					updatedURI(object.getUri());
			}
		});
	}

	private void updatedURIs(Collection<URI> uris) {
		for (URI uri : uris)
			updatedURI(uri);
	}

	private void updatedURI(URI uri) {
		synchronized (updatedSessionMap) {
			updatedSessionMap.put(uri.getUserInfo(), new HashSet<String>());
		}
	}

	public void invalidate(URI uri) {
		super.invalidate(uri);
		updatedURI(uri);
	}

	public void invalidate(String userId) {
		List<URI> toInvalidate = new LinkedList<URI>();
		for (URI uri : uriSet())
			if (userId.equals(uri.getUserInfo()))
				toInvalidate.add(uri);
		invalidate(toInvalidate);
	}

	public void cacheUpdate(Map<URI, EntityValue> updateMap) {
		super.cacheUpdate(updateMap);
		updatedURIs(updateMap.keySet());
	}
}