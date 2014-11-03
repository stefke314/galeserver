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
 * CharacterMatcher.java
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

import com.google.common.base.CharMatcher;

interface CharacterMatcher {
	public static final CharacterMatcher URI_ALPHANUM = new CharacterMatcher() {
		private CharMatcher matcher = SimpleCharMatcher.of(CharMatcher
				.inRange('0', '9').or(CharMatcher.inRange('a', 'z'))
				.or(CharMatcher.inRange('A', 'Z')));

		@Override
		public boolean matches(char ch) {
			return matcher.apply(ch);
		}
	};

	public static final CharacterMatcher URI_UNRESERVED = new CharacterMatcher() {
		private CharMatcher matcher = SimpleCharMatcher.of(CharMatcher
				.inRange('0', '9').or(CharMatcher.inRange('a', 'z'))
				.or(CharMatcher.inRange('A', 'Z'))
				.or(CharMatcher.anyOf("-_.!~*'()")));

		@Override
		public boolean matches(char ch) {
			return matcher.apply(ch);
		}
	};

	public static final CharacterMatcher URI_RESERVED = new CharacterMatcher() {
		private CharMatcher matcher = SimpleCharMatcher.of(CharMatcher
				.anyOf(";/?:@&=+$,"));

		@Override
		public boolean matches(char ch) {
			return matcher.apply(ch);
		}
	};

	public static final CharacterMatcher URI_URIC = new CharacterMatcher() {
		private CharMatcher matcher = SimpleCharMatcher.of(CharMatcher
				.inRange('0', '9').or(CharMatcher.inRange('a', 'z'))
				.or(CharMatcher.inRange('A', 'Z'))
				.or(CharMatcher.anyOf("-_.!~*'()"))
				.or(CharMatcher.anyOf(";/?:@&=+$,")));

		@Override
		public boolean matches(char ch) {
			return matcher.apply(ch);
		}
	};

	public static final CharacterMatcher URI_PATH = new CharacterMatcher() {
		private CharMatcher matcher = SimpleCharMatcher.of(CharMatcher
				.inRange('0', '9').or(CharMatcher.inRange('a', 'z'))
				.or(CharMatcher.inRange('A', 'Z'))
				.or(CharMatcher.anyOf("-_.!~*'()"))
				.or(CharMatcher.anyOf(";/:@&=+$,")));

		@Override
		public boolean matches(char ch) {
			return matcher.apply(ch);
		}
	};

	public static final CharacterMatcher URI_SCHEME = new CharacterMatcher() {
		private CharMatcher matcher = SimpleCharMatcher
				.of(CharMatcher.inRange('0', '9')
						.or(CharMatcher.inRange('a', 'z'))
						.or(CharMatcher.inRange('A', 'Z'))
						.or(CharMatcher.anyOf("+.-")));

		@Override
		public boolean matches(char ch) {
			return matcher.apply(ch);
		}
	};

	public static final CharacterMatcher URI_USERINFO = new CharacterMatcher() {
		private CharMatcher matcher = SimpleCharMatcher.of(CharMatcher
				.inRange('0', '9').or(CharMatcher.inRange('a', 'z'))
				.or(CharMatcher.inRange('A', 'Z'))
				.or(CharMatcher.anyOf("-_.!~*'()"))
				.or(CharMatcher.anyOf(";:&=+$,")));

		@Override
		public boolean matches(char ch) {
			return matcher.apply(ch);
		}
	};

	public static final CharacterMatcher URI_HOST = new CharacterMatcher() {
		private CharMatcher matcher = SimpleCharMatcher.of(CharMatcher
				.inRange('0', '9').or(CharMatcher.inRange('a', 'z'))
				.or(CharMatcher.inRange('A', 'Z')).or(CharMatcher.anyOf("-.")));

		@Override
		public boolean matches(char ch) {
			return matcher.apply(ch);
		}
	};

	public static final CharacterMatcher URI_AUTHORITY = new CharacterMatcher() {
		private CharMatcher matcher = SimpleCharMatcher.of(CharMatcher
				.inRange('0', '9').or(CharMatcher.inRange('a', 'z'))
				.or(CharMatcher.inRange('A', 'Z'))
				.or(CharMatcher.anyOf("-_.!~*'()"))
				.or(CharMatcher.anyOf(";:@&=+$,")));

		@Override
		public boolean matches(char ch) {
			return matcher.apply(ch);
		}
	};

	public boolean matches(char ch);
}
