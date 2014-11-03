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
 * EventCacheResolver.java
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
package nl.tue.gale.event;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import nl.tue.gale.common.cache.Cache;
import nl.tue.gale.common.cache.CacheResolver;
import nl.tue.gale.common.uri.URI;

public abstract class EventCacheResolver<T> implements CacheResolver<T> {
	protected abstract List<String> getEvents(URI uri);

	protected abstract List<String> putEvents(List<String> events);

	private Class<T> clazz = null;

	public EventCacheResolver(Class<T> clazz) {
		this.clazz = clazz;
	}

	public Map<URI, T> get(URI uri, Cache<T> cache) {
		Map<URI, T> result = fromEvent(getEvents(uri), cache);
		return result;
	}

	public Map<URI, T> put(Map<URI, T> map, Cache<T> cache) {
		List<String> events = new LinkedList<String>();
		for (T object : map.values())
			events.addAll(toEvent(object));
		if (events.size() > 0)
			return fromEvent(putEvents(events), cache);
		else
			return new HashMap<URI, T>();
	}

	@SuppressWarnings("unchecked")
	public List<String> toEvent(T object) {
		try {
			return (List<String>) object.getClass()
					.getMethod("toEvent", object.getClass())
					.invoke(null, object);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"unable to create events from object: " + e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	public Map<URI, T> fromEvent(List<String> events, Cache<T> cache) {
		try {
			return (Map<URI, T>) clazz.getMethod("fromEvent", List.class,
					Cache.class).invoke(null, events, cache);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"unable to retrieve objects from events: " + e.getMessage(),
					e);
		}
	}
}
