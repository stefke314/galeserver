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
 * URIs.java
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
package nl.tue.gale.common.uri;

import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

public final class URIs {
	private URIs() {
		throw new AssertionError();
	}

	private static final Map<String, URI> cache = new MapMaker().maximumSize(
			250).makeComputingMap(new Function<String, URI>() {
		@Override
		public URI apply(String input) {
			return builder().uri(input).build();
		}
	});

	public static URI of(String uri) {
		if (uri.length() < 20)
			return cache.get(uri);
		return builder().uri(uri).build();
	}

	public static URI of(String scheme, String ssp, String fragment) {
		return builder().scheme(scheme).ssp(ssp).fragment(fragment).build();
	}

	public static URI of(String scheme, String userInfo, String host, int port,
			String path, String query, String fragment) {
		return builder().scheme(scheme).userInfo(userInfo).host(host)
				.port(port).path(path).query(query).fragment(fragment).build();
	}

	public static URI of(String scheme, String host, String path,
			String fragment) {
		return builder().scheme(scheme).host(host).path(path)
				.fragment(fragment).build();
	}

	public static URI of(String scheme, String authority, String path,
			String query, String fragment) {
		return builder().scheme(scheme).authority(authority).path(path)
				.query(query).fragment(fragment).build();
	}

	public static URIBuilder builder() {
		return new FastURI.Builder();
	}
}
