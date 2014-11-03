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
 * ForwardingCache.java
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
package nl.tue.gale.common.cache;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import nl.tue.gale.common.uri.URI;

public class ForwardingCache<T> implements Cache<T> {
	private final Cache<T> cache;

	public ForwardingCache(Cache<T> cache) {
		checkNotNull(cache);
		this.cache = cache;
	}

	@Override
	public void addListener(CacheListener<T> listener) {
		cache.addListener(listener);
	}

	@Override
	public void removeListener(CacheListener<T> listener) {
		cache.removeListener(listener);
	}

	@Override
	public Set<URI> uriSet() {
		return cache.uriSet();
	}

	@Override
	public boolean isInCache(URI uri) {
		return cache.isInCache(uri);
	}

	@Override
	public void invalidate() {
		cache.invalidate();
	}

	@Override
	public void invalidate(URI uri) {
		cache.invalidate(uri);
	}

	@Override
	public void invalidate(Collection<URI> uriList) {
		cache.invalidate(uriList);
	}

	@Override
	public void cacheUpdate(Map<URI, T> updateMap) {
		cache.cacheUpdate(updateMap);
	}

	@Override
	public CacheSession<T> openSession() {
		return cache.openSession();
	}

	@Override
	public CacheSession<T> openSession(CacheSession<T> parent) {
		return cache.openSession(parent);
	}

	@Override
	public T get(URI uri) {
		return cache.get(uri);
	}

	@Override
	public T getProxy(Class<T> clazz, URI uri) {
		return cache.getProxy(clazz, uri);
	}
}
