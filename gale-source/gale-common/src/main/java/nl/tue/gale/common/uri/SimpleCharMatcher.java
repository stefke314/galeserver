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
 * SimpleCharMatcher.java
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

class SimpleCharMatcher extends CharMatcher {
	private int[] table = new int[8];

	private SimpleCharMatcher(CharMatcher matcher) {
		for (int i = 0; i < 256; i++)
			if (matcher.matches((char) i))
				table[i >> 5] |= (1 << i);
	}

	@Override
	public boolean matches(char c) {
		if (c > '~')
			return false;
		return (table[c >> 5] & (1 << c)) != 0;
	}

	public static CharMatcher of(CharMatcher matcher) {
		return new SimpleCharMatcher(matcher);
	}
}
