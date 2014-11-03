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
 * ComputingMapCache.java
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

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.common.uri.URIs;

class ComputingMapCache<T> implements Cache<T> {
	private CacheResolver<T> cacheResolver;
	private final List<CacheListener<T>> listenerList = new LinkedList<CacheListener<T>>();
	private final Map<URI, T> cacheMap;
	private final ProxyFactory proxyFactory;
	private final T nullValue;

	public ComputingMapCache(Map<URI, T> cacheMap,
			CacheResolver<T> cacheResolver, T nullValue) {
		checkNotNull(cacheMap);
		this.cacheMap = cacheMap;
		this.cacheResolver = cacheResolver;
		this.nullValue = nullValue;
		proxyFactory = new ProxyFactory();
		proxyFactory.setFilter(new MethodFilter() {
			public boolean isHandled(Method m) {
				return !m.getName().equals("finalize");
			}
		});
	}

	@Override
	public void addListener(CacheListener<T> listener) {
		checkNotNull(listener);
		listenerList.add(listener);
	}

	@Override
	public void removeListener(CacheListener<T> listener) {
		checkNotNull(listener);
		listenerList.remove(listener);
	}

	@Override
	public Set<URI> uriSet() {
		return cacheMap.keySet();
	}

	@Override
	public boolean isInCache(URI uri) {
		return cacheMap.containsKey(uri);
	}

	@Override
	public void invalidate() {
		cacheMap.clear();
	}

	@Override
	public void invalidate(URI uri) {
		cacheMap.remove(uri);
	}

	@Override
	public void invalidate(Collection<URI> uriList) {
		for (URI uri : uriList)
			invalidate(uri);
	}

	@Override
	public void cacheUpdate(Map<URI, T> updateMap) {
		for (Map.Entry<URI, T> entry : updateMap.entrySet())
			if (entry.getValue() == null)
				cacheMap.remove(entry.getKey());
			else {
				cacheMap.put(entry.getKey(), entry.getValue());
			}
	}

	@Override
	public CacheSession<T> openSession() {
		return new ComputingMapCacheSession();
	}

	@Override
	public CacheSession<T> openSession(CacheSession<T> parent) {
		return new ComputingMapCacheSession(parent);
	}

	@Override
	public T get(URI uri) {
		boolean contains = cacheMap.containsKey(uri);
		T result = null;
		result = cacheMap.get(uri);
		if (result == null || result.equals(nullValue))
			return null;
		if (!contains)
			fireObjectAdded(result);
		return result;
	}

	@Override
	public T getProxy(final Class<T> clazz, final URI uri) {
		return AccessController.doPrivileged(new PrivilegedAction<T>() {
			@SuppressWarnings("unchecked")
			public T run() {
				try {
					proxyFactory.setSuperclass(clazz);
					return (T) proxyFactory.create(new Class[] {},
							new Object[] {}, new URIMethodHandler(uri));
				} catch (Exception e) {
					throw new IllegalArgumentException(
							"unable to create proxy for '" + uri + "'", e);
				}
			}
		});
	}

	private class URIMethodHandler implements MethodHandler {
		private URI uri = null;
		private Object object = null;

		public URIMethodHandler(URI uri) {
			this.uri = uri;
		}

		public Object invoke(Object self, Method m, Method proceed,
				Object[] args) throws Throwable {
			if (m.getName().equals("getUriString"))
				return uri.toString();
			if (m.getName().equals("getUri"))
				return uri;
			object = get(uri);
			if (object == null)
				throw new IllegalArgumentException(
						"proxy is unable to retrieve object '" + uri + "'");
			return m.invoke(object, args);
		}
	}

	private class ComputingMapCacheSession implements CacheSession<T> {
		private boolean useUserInfo = true;
		private boolean open = true;
		private URI baseUri = null;
		private Map<URI, T> localMap = new HashMap<URI, T>();
		private final CacheSession<T> parent;
		private final String guid = GaleUtil.newGUID();

		public ComputingMapCacheSession() {
			parent = null;
		}

		public ComputingMapCacheSession(CacheSession<T> parent) {
			this.parent = parent;
		}

		@Override
		public boolean getUseUserInfo() {
			return useUserInfo;
		}

		@Override
		public void setUseUserInfo(boolean useUserInfo) {
			this.useUserInfo = useUserInfo;
		}

		@Override
		public boolean isOpen() {
			return open;
		}

		@Override
		public URI getBaseUri() {
			return baseUri;
		}

		@Override
		public void setBaseUri(URI baseUri) {
			this.baseUri = baseUri;
		}

		@Override
		public T get(URI uri) {
			if (!open)
				throw new IllegalArgumentException("session is closed");

			uri = resolve(uri);
			if (localMap.containsKey(uri))
				return localMap.get(uri);
			if (parent != null)
				return parent.get(uri);
			return ComputingMapCache.this.get(uri);
		}

		@Override
		public Map<URI, T> getChangeMap() {
			return localMap;
		}

		@Override
		public String getGuid() {
			return guid;
		}

		@Override
		public void put(URI uri, T object) {
			if (!open)
				throw new IllegalArgumentException("session is closed");

			uri = resolve(uri);
			T oldObject = (parent == null ? ComputingMapCache.this.get(uri)
					: parent.get(uri));
			T curObject = get(uri);
			if (curObject == null) {
				if (object == null)
					return;
			} else if (curObject.equals(object))
				return;

			if ((oldObject == null && object == null)
					|| (oldObject != null && oldObject.equals(object))) {
				localMap.remove(uri);
				return;
			}

			localMap.put(uri, object);
		}

		@Override
		public URI resolve(String uri) {
			return resolve(URIs.of(uri));
		}

		@Override
		public URI resolve(URI uri) {
			if (baseUri != null) {
				uri = baseUri.resolve(uri);
				if (getUseUserInfo()
						&& (uri.getUserInfo() == null || "".equals(uri
								.getUserInfo()))) {
					uri = GaleUtil.addUserInfo(uri, baseUri.getUserInfo());
				}
			}
			return uri;
		}

		@Override
		public void commit() {
			if (!open)
				return;
			if (parent != null) {
				for (Map.Entry<URI, T> entry : localMap.entrySet())
					parent.put(entry.getKey(), entry.getValue());
			} else {
				commitSession(this);
			}
			open = false;
		}

		@Override
		public void rollback() {
			open = false;
		}
	}

	void fireObjectAdded(T object) {
		if (object != null)
			for (CacheListener<T> listener : listenerList)
				listener.objectAdded(object);
	}

	private void commitSession(ComputingMapCacheSession session) {
		if (cacheResolver != null) {
			Map<URI, T> additions = cacheResolver.put(session.localMap, this);
			for (Map.Entry<URI, T> entry : additions.entrySet())
				session.localMap.put(entry.getKey(), entry.getValue());
		}
		cacheUpdate(session.localMap);
		for (T object : session.localMap.values())
			fireObjectAdded(object);
	}
}
