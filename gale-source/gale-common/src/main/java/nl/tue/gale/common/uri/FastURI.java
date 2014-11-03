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
 * FastURI.java
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

import static nl.tue.gale.common.uri.CharacterMatcher.URI_AUTHORITY;
import static nl.tue.gale.common.uri.CharacterMatcher.URI_HOST;
import static nl.tue.gale.common.uri.CharacterMatcher.URI_PATH;
import static nl.tue.gale.common.uri.CharacterMatcher.URI_SCHEME;
import static nl.tue.gale.common.uri.CharacterMatcher.URI_URIC;
import static nl.tue.gale.common.uri.CharacterMatcher.URI_USERINFO;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import nl.tue.gale.common.GaleUtil;

final class FastURI implements URI {
	private final AtomicBoolean serialized;
	private final AtomicBoolean parsed;
	private transient final AtomicBoolean serializing = new AtomicBoolean(false);
	private transient final AtomicBoolean parsing = new AtomicBoolean(false);

	private String uri = null;
	private String scheme = null;
	private String userInfo = null;
	private String host = null;
	private int port = -1;
	private String authority = null;
	private String ssp = null;
	private String path = null;
	private String query = null;
	private String fragment = null;

	private FastURI(String uri) {
		this.uri = uri;
		serialized = new AtomicBoolean(true);
		parsed = new AtomicBoolean(false);
	}

	private FastURI(String scheme, String userInfo, String host, int port,
			String authority, String ssp, String path, String query,
			String fragment) {
		this.scheme = scheme;
		this.userInfo = userInfo;
		this.host = host;
		this.port = port;
		this.authority = authority;
		this.ssp = ssp;
		this.path = path;
		this.query = query;
		this.fragment = fragment;
		if (authority != null && host == null)
			parseAuthority(authority);
		serialized = new AtomicBoolean(false);
		parsed = new AtomicBoolean(true);
	}

	@Override
	public URI parseServerAuthority() throws URISyntaxException {
		return this;
	}

	@Override
	public URI normalize() {
		FastURI result = internalCopy();
		result.path = normalizePath(path);
		result.uri = null;
		result.serialized.set(false);
		return result;
	}

	static String normalizePath(String path) {
		if (path == null)
			return null;
		if ("".equals(path))
			return path;
		long dots = 0;
		long dd = 0;
		String[] segments = path.split("/");
		if (segments[0].equals(""))
			dots = 1;
		for (int i = 0; i < segments.length; i++)
			if (".".equals(segments[i]))
				dots |= (1l << i);
			else if ("..".equals(segments[i]))
				dd |= (1l << i);
		long remove = 0;
		for (int i = 0; i < segments.length; i++)
			if ((dd & (1 << i)) != 0) {
				int j = i - 1;
				int k = 1 << j;
				while ((j >= 0)
						&& (((dots & k) != 0) || ((remove & k) != 0) || ((dd & k) != 0))) {
					j--;
					k = k >> 1;
				}
				if (j >= 0)
					remove |= k | 1 << i;
			}
		long complete = dots | remove;
		StringBuilder sb = new StringBuilder();
		if (path.startsWith("/"))
			sb.append("/");
		for (int i = 0; i < segments.length; i++)
			if (((1 << i) & complete) == 0) {
				if ((sb.length() == 0) && (segments[i].indexOf(':') >= 0))
					sb.append("./");
				sb.append(segments[i]);
				sb.append('/');
			}
		if ((sb.length() > 0) && !path.endsWith("/"))
			sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	private FastURI internalCopy() {
		if (!serialized.get())
			serialize();
		if (!parsed.get())
			parse();
		FastURI copy = new FastURI(uri);
		copy.scheme = scheme;
		copy.userInfo = userInfo;
		copy.host = host;
		copy.port = port;
		copy.authority = authority;
		copy.ssp = ssp;
		copy.path = path;
		copy.query = query;
		copy.fragment = fragment;
		copy.parsed.set(true);
		return copy;
	}

	@Override
	public URI resolve(URI uri) {
		if (uri.isAbsolute() || isOpaque())
			return uri;
		FastURI result = internalCopy();
		result.path = resolvePath(path, uri.getPath());
		result.query = uri.getQuery();
		result.fragment = uri.getFragment();
		result.serialized.set(false);
		return result;
	}

	static String resolvePath(String base, String relativePath) {
		if (relativePath == null)
			return base;
		if (relativePath.startsWith("/"))
			return relativePath;
		int i = base.lastIndexOf('/') + 1;
		if (i < 0)
			i = 0;
		StringBuilder sb = new StringBuilder();
		sb.append(base.substring(0, i));
		sb.append(relativePath);
		return normalizePath(sb.toString());
	}

	@Override
	public URI resolve(String str) {
		return resolve(new FastURI(str));
	}

	@Override
	public URI relativize(URI uri) {
		if (!parsed.get())
			parse();
		if (isOpaque() || uri.isOpaque())
			return uri;
		if (!GaleUtil.safeEquals(scheme, uri.getScheme()))
			return uri;
		if (!GaleUtil.safeEquals(getAuthority(), uri.getAuthority()))
			return uri;
		FastURI result = internalCopy();
		result.path = relativizePath(path, uri.getPath());
		return result;
	}

	private static String relativizePath(String base, String relativePath) {
		// TODO: fill this
		return relativePath;
	}

	@Override
	public URL toURL() throws MalformedURLException {
		if (!serialized.get())
			serialize();
		return new URL(uri);
	}

	@Override
	public String getScheme() {
		if (!parsed.get())
			parse();
		return scheme;
	}

	@Override
	public boolean isAbsolute() {
		if (!parsed.get())
			parse();
		return (scheme != null);
	}

	@Override
	public boolean isOpaque() {
		if (!parsed.get())
			parse();
		return ((scheme != null) && ((ssp != null && !ssp.startsWith("/")) || (path != null && !path
				.startsWith("/"))));
	}

	@Override
	public String getRawSchemeSpecificPart() {
		if (!parsed.get())
			parse();
		if (ssp == null)
			return null;
		return encode(ssp, URI_URIC);
	}

	@Override
	public String getSchemeSpecificPart() {
		parse();
		return ssp;
	}

	@Override
	public String getRawAuthority() {
		if (!parsed.get())
			parse();
		if (getAuthority() == null)
			return null;
		return encode(getAuthority(), URI_AUTHORITY);
	}

	@Override
	public String getAuthority() {
		if (!parsed.get())
			parse();
		if (authority == null && host != null) {
			StringBuilder sb = new StringBuilder();
			if (userInfo != null) {
				encodeAppend(sb, userInfo, URI_USERINFO);
				sb.append('@');
			}
			encodeAppend(sb, host, URI_HOST);
			if (port != -1) {
				sb.append(':');
				sb.append(port);
			}
			return sb.toString();
		}
		return authority;
	}

	@Override
	public String getRawUserInfo() {
		if (!parsed.get())
			parse();
		if (userInfo == null)
			return null;
		return encode(userInfo, URI_USERINFO);
	}

	@Override
	public String getUserInfo() {
		parse();
		return userInfo;
	}

	@Override
	public String getHost() {
		if (!parsed.get())
			parse();
		return host;
	}

	@Override
	public int getPort() {
		if (!parsed.get())
			parse();
		return port;
	}

	@Override
	public String getRawPath() {
		if (!parsed.get())
			parse();
		if (path == null)
			return null;
		return encode(path, URI_PATH);
	}

	@Override
	public String getPath() {
		if (!parsed.get())
			parse();
		return path;
	}

	@Override
	public String getRawQuery() {
		if (!parsed.get())
			parse();
		if (query == null)
			return null;
		return encode(query, URI_URIC);
	}

	@Override
	public String getQuery() {
		if (!parsed.get())
			parse();
		return query;
	}

	@Override
	public String getRawFragment() {
		if (!parsed.get())
			parse();
		if (fragment == null)
			return null;
		return encode(fragment, URI_URIC);
	}

	@Override
	public String getFragment() {
		if (!parsed.get())
			parse();
		return fragment;
	}

	@Override
	public int compareTo(URI that) {
		if (!serialized.get())
			serialize();
		return uri.compareTo(that.toString());
	}

	@Override
	public String toASCIIString() {
		if (!serialized.get())
			serialize();
		return uri;
	}

	@Override
	public int hashCode() {
		if (!serialized.get())
			serialize();
		return uri.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof URI))
			return false;
		if (!serialized.get())
			serialize();
		return uri.equals(((URI) obj).toString());
	}

	@Override
	public String toString() {
		if (!serialized.get())
			serialize();
		return uri;
	}

	private Lock lock = new ReentrantLock();

	private void parse() {
		if (parsed.get())
			return;
		if (parsing.getAndSet(true)) {
			// somebody started parsing, so wait for them
			while (!parsed.get()) {
				lock.lock();
				lock.unlock();
			}
		} else {
			// nobody else is parsing
			lock.lock();
			doParse();
			parsed.set(true);
			parsing.set(false);
			lock.unlock();
		}
	}

	private void serialize() {
		if (serialized.get())
			return;
		if (serializing.getAndSet(true)) {
			// somebody started serializing, so wait for them
			while (!serialized.get()) {
				lock.lock();
				lock.unlock();
			}
		} else {
			// nobody else is serializing
			lock.lock();
			doSerialize();
			serialized.set(true);
			serializing.set(false);
			lock.unlock();
		}
	}

	private void parseAuthority(String authority) {
		int i = authority.indexOf('@');
		if (i >= 0) {
			userInfo = decode(authority.substring(0, i));
			authority = authority.substring(i + 1);
		}
		i = authority.indexOf(':');
		if (i >= 0) {
			host = decode(authority.substring(0, i));
			port = Integer.parseInt(authority.substring(i + 1));
		} else {
			host = decode(authority);
		}
	}

	private void doParse() {
		if (uri == null)
			return;
		int i = 0;
		while (i < uri.length() && URI_SCHEME.matches(uri.charAt(i)))
			i++;
		if (i < uri.length() && uri.charAt(i) == ':') {
			// got scheme
			scheme = uri.substring(0, i);
			i++;
		} else {
			i = 0;
		}
		String ssp = uri.substring(i);
		if (ssp.startsWith("//")) {
			// got authority
			i = ssp.indexOf('/', 2);
			if (i < 0)
				i = ssp.indexOf('?');
			if (i < 0)
				i = ssp.indexOf('#');
			if (i < 0)
				i = ssp.length();
			String authority = ssp.substring(2, i);
			ssp = ssp.substring(i);
			parseAuthority(authority);
		} else if (!ssp.startsWith("/") && scheme != null) {
			// opaque
			i = ssp.indexOf('#');
			if (i < 0) {
				this.ssp = decode(ssp);
			} else {
				this.ssp = decode(ssp.substring(0, i));
				this.fragment = decode(ssp.substring(i + 1));
			}
			return;
		}
		// got path
		i = ssp.indexOf('?');
		if (i < 0)
			i = ssp.indexOf('#');
		if (i < 0)
			i = ssp.length();
		String path = ssp.substring(0, i);
		if ("".equals(path))
			path = null;
		else
			this.path = decode(path);
		ssp = ssp.substring(i);
		if (ssp.startsWith("?")) {
			// got query
			i = ssp.indexOf('#');
			if (i < 0)
				i = ssp.length();
			String query = ssp.substring(1, i);
			ssp = ssp.substring(i);
			this.query = decode(query);
		}
		if (ssp.startsWith("#")) {
			// got fragment
			this.fragment = decode(ssp.substring(1));
		}
	}

	private void doSerialize() {
		StringBuilder sb = new StringBuilder();
		if (scheme != null) {
			sb.append(scheme);
			sb.append(':');
		}
		if (ssp != null) {
			if (!"".equals(ssp)) {
				if (ssp.charAt(0) == '/') {
					sb.append("%2F");
					int cur = sb.length();
					encodeAppend(sb, ssp, URI_URIC);
					sb.delete(cur, cur + 1);
				} else {
					encodeAppend(sb, ssp, URI_URIC);
				}
			}
		} else {
			if (authority != null) {
				sb.append("//");
				encodeAppend(sb, authority, URI_AUTHORITY);
			} else {
				if (host != null) {
					sb.append("//");
					if (userInfo != null) {
						encodeAppend(sb, userInfo, URI_USERINFO);
						sb.append('@');
					}
					encodeAppend(sb, host, URI_HOST);
					if (port != -1) {
						sb.append(':');
						sb.append(port);
					}
				}
			}
			if (path != null)
				encodeAppend(sb, path, URI_PATH);
			if (query != null) {
				sb.append('?');
				encodeAppend(sb, query, URI_URIC);
			}
		}
		if (fragment != null) {
			sb.append('#');
			sb.append(fragment);
		}
		uri = sb.toString();
	}

	private void decodeAppend(StringBuilder sb, String encoded) {
		if (encoded.indexOf('%') < 0) {
			sb.append(encoded);
			return;
		}
		int i = 0;
		char ch;
		while (i < encoded.length()) {
			ch = encoded.charAt(i);
			if (ch == '%') {
				sb.append((char) Integer.parseInt(
						encoded.substring(i + 1, i + 3), 16));
				i += 2;
			} else
				sb.append(ch);
			i++;
		}
	}

	private void encodeAppend(StringBuilder sb, String decoded,
			CharacterMatcher allowed) {
		char ch;
		for (int i = 0; i < decoded.length(); i++) {
			ch = decoded.charAt(i);
			if (allowed.matches(ch))
				sb.append(ch);
			else {
				sb.append('%');
				sb.append(hexChar(ch >> 4 & 15));
				sb.append(hexChar(ch & 15));
			}
		}
	}

	private String decode(String encoded) {
		if (encoded.indexOf('%') < 0)
			return encoded;
		StringBuilder result = new StringBuilder(encoded.length());
		decodeAppend(result, encoded);
		return result.toString();
	}

	private String encode(String decoded, CharacterMatcher allowed) {
		StringBuilder result = new StringBuilder(decoded.length() + 12);
		encodeAppend(result, decoded, allowed);
		return result.toString();
	}

	private char hexChar(int i) {
		return (i < 10 ? (char) (i + 48) : (char) (i + 55));
	}

	static class Builder extends URIBuilder {
		@Override
		public URI build() {
			if (uri == null)
				return new FastURI(scheme, userInfo, host, port, authority,
						ssp, path, query, fragment);
			return new FastURI(uri);
		}
	}
}
