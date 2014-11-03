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
 * AHAOutConceptList.java
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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * AHAOutConcept stores the output concept
 * 
 */
public class AHAOutConceptList {
	public String name;
	public LinkedList conceptList;

	/**
	 * Default constructor
	 */
	public AHAOutConceptList() {
		conceptList = new LinkedList();
		name = "";
	}

	/**
	 * Returns a Hashtable with the concept names as keys
	 */
	public Hashtable GetConceptNames() {
		Hashtable conceptNames = new Hashtable();

		for (Iterator i = conceptList.iterator(); i.hasNext();) {
			AHAOutConcept aout = (AHAOutConcept) i.next();
			conceptNames.put(aout.name.trim(), null);
		}

		return conceptNames;
	}
}