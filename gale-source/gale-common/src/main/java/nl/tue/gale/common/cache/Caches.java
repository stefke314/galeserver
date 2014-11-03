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
 * Caches.java
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

import java.util.concurrent.TimeUnit;

import nl.tue.gale.common.uri.URI;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

public final class Caches {
	private Caches() {
		throw new AssertionError();
	}

	public static <T> Cache<T> newFixedCache(int maxSize) {
		Cache<T> result = new ComputingMapCache<T>(new MapMaker().maximumSize(
				maxSize).<URI, T> makeMap(), null, null);
		return result;
	}

	public static <T> Cache<T> newCache(int maxSize) {
		Cache<T> result = new ComputingMapCache<T>(new MapMaker()
				.maximumSize(maxSize).softValues().<URI, T> makeMap(), null,
				null);
		return result;
	}

	private static abstract class DelayedCacheFunction<T> implements
			Function<URI, T> {
		protected ComputingMapCache<T> cache = null;
	}

	public static <T> Cache<T> newCache(int maxSize,
			final CacheResolver<T> resolver, final T nullValue) {
		if (resolver == null)
			return newCache(maxSize);
		final DelayedCacheFunction<T> function = new DelayedCacheFunction<T>() {
			@Override
			public T apply(URI uri) {
				T result = resolver.get(uri, cache).get(uri);
				if (result == null)
					return nullValue;
				return result;
			}
		};
		ComputingMapCache<T> result = new ComputingMapCache<T>(new MapMaker()
				.maximumSize(maxSize).softValues()
				.<URI, T> makeComputingMap(function), resolver, nullValue);
		function.cache = result;
		return result;
	}

	public static <T> Cache<T> newVolatileCache(int maxSize, long duration,
			TimeUnit unit) {
		Cache<T> result = new ComputingMapCache<T>(new MapMaker()
				.maximumSize(maxSize).softValues()
				.expireAfterWrite(duration, unit).<URI, T> makeMap(), null,
				null);
		return result;
	}

	public static <T> Cache<T> newVolatileCache(int maxSize,
			final CacheResolver<T> resolver, final T nullValue, long duration,
			TimeUnit unit) {
		if (resolver == null)
			return newVolatileCache(maxSize, duration, unit);
		final DelayedCacheFunction<T> function = new DelayedCacheFunction<T>() {
			@Override
			public T apply(URI uri) {
				T result = resolver.get(uri, cache).get(uri);
				if (result == null)
					return nullValue;
				return result;
			}
		};
		ComputingMapCache<T> result = new ComputingMapCache<T>(new MapMaker()
				.maximumSize(maxSize).softValues()
				.expireAfterWrite(duration, unit)
				.<URI, T> makeComputingMap(function), resolver, nullValue);
		function.cache = result;
		return result;
	}
}
