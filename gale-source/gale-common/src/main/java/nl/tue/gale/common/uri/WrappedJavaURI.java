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
 * WrappedJavaURI.java
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

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

class WrappedJavaURI implements URI {
	protected final java.net.URI javaURI;

	protected WrappedJavaURI(java.net.URI javaURI) {
		checkNotNull(javaURI);
		this.javaURI = javaURI;
	}

	@Override
	public URI parseServerAuthority() throws URISyntaxException {
		return new WrappedJavaURI(javaURI.parseServerAuthority());
	}

	@Override
	public URI normalize() {
		return new WrappedJavaURI(javaURI.normalize());
	}

	@Override
	public URI resolve(URI uri) {
		return new WrappedJavaURI(javaURI.resolve(java.net.URI.create(uri
				.toString())));
	}

	@Override
	public URI resolve(String str) {
		return new WrappedJavaURI(javaURI.resolve(str));
	}

	@Override
	public URI relativize(URI uri) {
		return new WrappedJavaURI(javaURI.relativize(java.net.URI.create(uri
				.toString())));
	}

	@Override
	public URL toURL() throws MalformedURLException {
		return javaURI.toURL();
	}

	@Override
	public String getScheme() {
		return javaURI.getScheme();
	}

	@Override
	public boolean isAbsolute() {
		return javaURI.isAbsolute();
	}

	@Override
	public boolean isOpaque() {
		return javaURI.isOpaque();
	}

	@Override
	public String getRawSchemeSpecificPart() {
		return javaURI.getRawSchemeSpecificPart();
	}

	@Override
	public String getSchemeSpecificPart() {
		return javaURI.getSchemeSpecificPart();
	}

	@Override
	public String getRawAuthority() {
		return javaURI.getRawAuthority();
	}

	@Override
	public String getAuthority() {
		return javaURI.getAuthority();
	}

	@Override
	public String getRawUserInfo() {
		return javaURI.getRawUserInfo();
	}

	@Override
	public String getUserInfo() {
		return javaURI.getUserInfo();
	}

	@Override
	public String getHost() {
		return javaURI.getHost();
	}

	@Override
	public int getPort() {
		return javaURI.getPort();
	}

	@Override
	public String getRawPath() {
		return javaURI.getRawPath();
	}

	@Override
	public String getPath() {
		return javaURI.getPath();
	}

	@Override
	public String getRawQuery() {
		return javaURI.getRawQuery();
	}

	@Override
	public String getQuery() {
		return javaURI.getQuery();
	}

	@Override
	public String getRawFragment() {
		return javaURI.getRawFragment();
	}

	@Override
	public String getFragment() {
		return javaURI.getFragment();
	}

	@Override
	public int compareTo(URI that) {
		return javaURI.compareTo(java.net.URI.create(that.toString()));
	}

	@Override
	public String toASCIIString() {
		return javaURI.toASCIIString();
	}

	@Override
	public int hashCode() {
		return javaURI.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof URI))
			return false;
		return javaURI.equals(java.net.URI.create(obj.toString()));
	}

	@Override
	public String toString() {
		return javaURI.toString();
	}

	static class Builder extends URIBuilder {
		protected java.net.URI buildJavaURI() {
			try {
				if (host != null) {
					if (userInfo == null && port == -1 && query == null)
						return new java.net.URI(scheme, host, path, fragment);
					return new java.net.URI(scheme, userInfo, host, port, path,
							query, fragment);
				}
				if (authority != null)
					return new java.net.URI(scheme, authority, path, query,
							fragment);
				if (ssp != null)
					return new java.net.URI(scheme, ssp, fragment);
				return new java.net.URI(uri);
			} catch (URISyntaxException e) {
				throw new IllegalArgumentException(e.getMessage(), e);
			}
		}

		@Override
		public URI build() {
			return new WrappedJavaURI(buildJavaURI());
		}
	}
}
