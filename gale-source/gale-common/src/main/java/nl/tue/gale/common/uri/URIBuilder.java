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
 * URIBuilder.java
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

import static com.google.common.base.Preconditions.checkArgument;

public abstract class URIBuilder {
	protected String scheme = null;
	protected String userInfo = null;
	protected String host = null;
	protected int port = -1;
	protected String authority = null;
	protected String ssp = null;
	protected String path = null;
	protected String query = null;
	protected String fragment = null;
	protected String uri = null;

	public URIBuilder uri(URI uri) {
		this.uri = null;
		this.scheme = uri.getScheme();
		this.userInfo = null;
		this.host = null;
		this.port = -1;
		this.authority = null;
		this.ssp = null;
		if (uri.getHost() == null) {
			if (uri.getAuthority() == null) {
				this.ssp = uri.getSchemeSpecificPart();
			} else {
				this.authority = uri.getAuthority();
			}
		} else {
			this.userInfo = uri.getUserInfo();
			this.host = uri.getHost();
			this.port = uri.getPort();
		}
		this.path = uri.getPath();
		this.query = uri.getQuery();
		this.fragment = uri.getFragment();
		return this;
	}

	public URIBuilder uri(String uri) {
		this.scheme = null;
		this.authority = null;
		this.userInfo = null;
		this.host = null;
		this.port = -1;
		this.ssp = null;
		this.path = null;
		this.query = null;
		this.fragment = null;
		this.uri = uri;
		return this;
	}

	public URIBuilder scheme(String scheme) {
		if (scheme != null) {
			checkArgument(uri == null, "uri already set");
		}
		this.scheme = scheme;
		return this;
	}

	public URIBuilder userInfo(String userInfo) {
		if (userInfo != null) {
			checkArgument(authority == null, "authority already set");
			checkArgument(ssp == null, "scheme-specific part already set");
			checkArgument(uri == null, "uri already set");
		}
		this.userInfo = userInfo;
		return this;
	}

	public URIBuilder host(String host) {
		if (host != null) {
			checkArgument(authority == null, "authority already set");
			checkArgument(ssp == null, "scheme-specific part already set");
			checkArgument(uri == null, "uri already set");
		}
		this.host = host;
		return this;
	}

	public URIBuilder port(int port) {
		if (port != -1) {
			checkArgument(authority == null, "authority already set");
			checkArgument(ssp == null, "scheme-specific part already set");
			checkArgument(uri == null, "uri already set");
		}
		this.port = port;
		return this;
	}

	public URIBuilder authority(String authority) {
		if (authority != null) {
			checkArgument(ssp == null, "scheme-specific part already set");
			checkArgument(uri == null, "uri already set");
		}
		this.authority = authority;
		this.userInfo = null;
		this.host = null;
		this.port = -1;
		return this;
	}

	public URIBuilder ssp(String ssp) {
		if (ssp != null) {
			checkArgument(uri == null, "uri already set");
		}
		this.ssp = ssp;
		this.authority = null;
		this.userInfo = null;
		this.host = null;
		this.port = -1;
		return this;
	}

	public URIBuilder path(String path) {
		if (path != null) {
			checkArgument(uri == null, "uri already set");
		}
		this.path = path;
		return this;
	}

	public URIBuilder query(String query) {
		if (query != null) {
			checkArgument(uri == null, "uri already set");
		}
		this.query = query;
		return this;
	}

	public URIBuilder fragment(String fragment) {
		if (fragment != null) {
			checkArgument(uri == null, "uri already set");
		}
		this.fragment = fragment;
		return this;
	}

	public abstract URI build();
}
