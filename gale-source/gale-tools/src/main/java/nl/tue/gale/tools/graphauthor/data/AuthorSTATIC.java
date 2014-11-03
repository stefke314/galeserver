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
 * AuthorSTATIC.java
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
package nl.tue.gale.tools.graphauthor.data;

import java.util.LinkedList;

/**
 * AuthorSTATIC stores the complete datamodel.
 * 
 */
public class AuthorSTATIC {
	public static LinkedList CRTList;
	public static CRTConceptRelationType trel;
	public static RELConceptRelations relaha;
	public static AHAOutConceptList ahaOut;
	// public static LinkedList attributeList;
	public static LinkedList conceptInfoList;
	public static LinkedList templateList;
	public static LinkedList conceptList;
	public static String projectName = "unnamed";
	public static String authorName = "";

	/**
	 * Default constructor.
	 */
	public AuthorSTATIC() {
		CRTList = new LinkedList();
		trel = new CRTConceptRelationType();
		relaha = new RELConceptRelations();
		ahaOut = new AHAOutConceptList();
		// attributeList = new LinkedList();
		conceptInfoList = new LinkedList();
		templateList = new LinkedList();
		conceptList = new LinkedList();
	}
}