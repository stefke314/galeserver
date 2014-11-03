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
 * ParseString.java
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
package nl.tue.gale.common.parser;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents the part of the program that still needs to be parsed.
 */
public class ParseString {
	// the current part of the expression being scanned
	private StringBuilder string;

	// debug char count
	private int ccount;

	// debug line count
	private int lcount;

	private int pos;

	/**
	 * Creates a new parse string based on the specified program. The parse
	 * string specified cannot be <code>null</code>.
	 * 
	 * @param s
	 *            the program that needs to be parsed
	 */
	public ParseString(String s) {
		checkNotNull(s);
		string = new StringBuilder(s);
		ccount = 1;
		lcount = 1;
		pos = 0;
	}

	public int lineCount() {
		return lcount;
	}

	public int charCount() {
		return ccount;
	}

	public char nextChar() {
		if (!containsChars()) {
			if (ccount == -1)
				throw new IndexOutOfBoundsException(
						"error scanning: end of program");
			ccount = -1;
			return '\f';
		}
		char ch = string.charAt(pos);
		pos++;
		ccount++;
		// check new line
		if (ch == '\n') {
			lcount++;
			ccount = 1;
		}
		return ch;
	}

	public void returnChar(char ch) {
		if (pos > 0 && string.charAt(pos - 1) == ch)
			pos--;
		else
			string.insert(pos, ch);
		ccount--;
		if (ch == '\n') {
			lcount--;
			ccount = 1;
		}
	}

	public boolean containsChars() {
		return pos != string.length();
	}
}