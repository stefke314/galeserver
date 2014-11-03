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
 * ParseComponent.java
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

/**
 * Implemented by classes that add functionality to the general parser.
 */
public interface ParseComponent {
	/**
	 * A list of recursive decent parsing methods contained in this parse
	 * component.
	 */
	public String[] getParseMethods();

	/**
	 * Uses the specified method to parse the next tokens in the list.
	 */
	public ParseNode parse(String method, ParseList pl, ParseInfo info)
			throws ParserException;

	/**
	 * Scans the string to see if the first token in the string is handled by
	 * this component.
	 */
	public Token scan(ParseString ps, Token token) throws ParserException;

	/**
	 * Evaluates the specified ParseNode handled by this component.
	 */
	public Object evaluate(ParseNode node, ParseInfo info, VariableLocator vl)
			throws ParserException;
}