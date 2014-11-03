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
 * ParseList.java
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

import java.util.List;

/**
 * ParseList maintains a list of tokens and a current position in this list.
 * ParseList is used by the various parse components to obtain the current
 * token.
 */
public class ParseList {
	private List<Token> tokens = null;
	private int current = 0;

	public ParseList(List<Token> tokens) {
		this.tokens = tokens;
	}

	public Token current() throws ParserException {
		if (!valid())
			throw new ParserException("unexpected end of program");
		return tokens.get(current);
	}

	public int currentindex() {
		return current;
	}

	public void moveNext() {
		current++;
	}

	public void movePrevious() {
		if (current > 0)
			current--;
	}

	public boolean valid() {
		return ((current >= 0) && (current < tokens.size()));
	}

	public String toString() {
		return tokens.toString();
	}
}