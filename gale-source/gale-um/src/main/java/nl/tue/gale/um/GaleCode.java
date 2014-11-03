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
 * PCPseudoCode.java
 * Last modified: $Date$
 * In revision:   $Revision$
 * Modified by:   $Author: dsmits $
 *
 * Copyright (c) 2008-2011 Eindhoven University of Technology.
 * All Rights Reserved.
 *
 * This software is proprietary information of the Eindhoven University
 * of Technology. It may be used according to the GNU LGPL license.
 */
package nl.tue.gale.um;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashSet;
import java.util.Set;

import nl.tue.gale.common.cache.Cache;
import nl.tue.gale.common.parser.ParserException;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.common.uri.URIs;
import nl.tue.gale.dm.data.Attribute;
import nl.tue.gale.dm.data.Concept;

public final class GaleCode {
	private static Attribute proxyAttribute(final URI uri) {
		return new Attribute() {
			public URI getUri() {
				return uri;
			}
		};
	}

	public static Object resolve(Concept start, Cache<Concept> cache,
			String expr) throws ParserException {
		Code code = new Code(expr);
		Concept current = start;
		if (".".equals(code.peekToken()))
			return start;
		while (code.peekToken() != null) {
			if (code.isRel()) {
				String token = code.nextToken();
				int nameEnd = token.indexOf(')');
				String name = token.substring(token.indexOf('(') + 1, nameEnd);
				Set<Concept> set;
				if (current == null)
					set = new HashSet<Concept>();
				else
					set = (token.charAt(0) == '<' ? current
							.getNamedInConcepts(name) : current
							.getNamedOutConcepts(name));
				if (token.charAt(0) == '=' || token.charAt(1) == '=') {
					if (code.isRel())
						throw new IllegalArgumentException(
								"=> or <= should be followed by attribute");
					token = code.nextToken();
					Attribute[] result = new Attribute[set.size()];
					int index = 0;
					URI attr = URIs.of(token);
					for (Concept c : set) {
						result[index] = proxyAttribute(c.getUri().resolve(attr));
						index++;
					}
					return result;
				}
				int index = 0;
				if (nameEnd + 1 < token.length())
					index = Integer.parseInt(token.substring(nameEnd + 2,
							token.indexOf(']', nameEnd)));
				else if (code.peekToken() == null)
					return set.toArray(new Concept[] {});
				Concept result = null;
				for (Concept c : set) {
					result = c;
					index--;
					if (index < 0)
						break;
				}
				current = result;
			} else {
				String token = code.nextToken();
				URI uri;
				if (current == null) {
					uri = URIs.of(token);
				} else {
					if (token.startsWith("?"))
						uri = URIs.of(current.getUri().toString() + token);
					else
						uri = current.getUri().resolve(token);
				}
				if (uri.getFragment() != null || uri.getQuery() != null) {
					Concept newConcept = cache.get(Concept.getConceptURI(uri));
					if (uri.getFragment() != null) {
						if (newConcept == null)
							return proxyAttribute(uri);
						Attribute attr = newConcept.getAttribute(uri
								.getFragment());
						if (attr == null)
							return proxyAttribute(uri);
						if (uri.getQuery() != null)
							return attr.getProperty(uri.getQuery());
						return attr;
					}
					return newConcept.getProperty(uri.getQuery());
				} else {
					current = cache.get(uri);
				}
			}
		}
		return current;
	}

	private static final class Code {
		private final String code;
		private int pos = 0;
		private String token = null;

		public Code(String code) {
			checkNotNull(code);
			this.code = code;
		}

		public String nextToken() {
			String result;
			if (token != null) {
				result = token;
				token = null;
			} else {
				peekToken();
				result = token;
				token = null;
			}
			pos += (result != null ? result.length() : 0);
			return result;
		}

		public String peekToken() {
			if (pos >= code.length())
				return null;
			String result;
			if (rel())
				result = code.substring(pos, relEnd());
			else
				result = code.substring(pos, nameEnd());
			token = result;
			return result;
		}

		public boolean isRel() {
			return rel();
		}

		private int nameEnd() {
			assert !rel() && pos < code.length();
			int oldPos = pos;
			while (current() != '\f' && !rel())
				pos++;
			int result = pos;
			pos = oldPos;
			return result;
		}

		private int relEnd() {
			assert rel() && pos < code.length() - 4;
			int oldPos = pos;
			pos += 2;
			if (current() != '(')
				throw new IllegalArgumentException("'(' expected");
			pos++;
			while (current() != ')' && current() != '\f')
				pos++;
			if (current() == '\f')
				throw new IllegalArgumentException("')' expected");
			pos++;
			if (current() == '[') {
				pos++;
				while (current() != ']' && current() != '\f')
					pos++;
				if (current() == '\f')
					throw new IllegalArgumentException("']' expected");
				pos++;
			}
			int result = pos;
			pos = oldPos;
			return result;
		}

		private boolean rel() {
			return ((current() == '-' || current() == '=') && peek() == '>')
					|| (current() == '<' && (peek() == '-' || peek() == '='));
		}

		private char current() {
			if (pos < code.length())
				return code.charAt(pos);
			return '\f';
		}

		private char peek() {
			if (pos < code.length() - 1)
				return code.charAt(pos + 1);
			return '\f';
		}
	}
}
