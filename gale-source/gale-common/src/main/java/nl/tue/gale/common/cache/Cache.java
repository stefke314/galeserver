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
 * Cache.java
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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import nl.tue.gale.common.uri.URI;

public interface Cache<T> {
	public void addListener(CacheListener<T> listener);

	public void removeListener(CacheListener<T> listener);

	public Set<URI> uriSet();

	public boolean isInCache(URI uri);

	public void invalidate();

	public void invalidate(URI uri);

	public void invalidate(Collection<URI> uriList);

	public void cacheUpdate(Map<URI, T> updateMap);

	public CacheSession<T> openSession();

	public CacheSession<T> openSession(CacheSession<T> parent);

	public T get(URI uri);

	public T getProxy(Class<T> clazz, URI uri);
}