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
 * TermType.java
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
package nl.tue.gale.tools.graphauthor.ui;

import java.util.Vector;

/**
 * This represents a type of an edge. The equivelent of a concept relation type
 * in internal format. This is the place where information like the triggerlist
 * and activatorlist is stored.
 */
public class TermType {
	public String name = null;
	public TermMapping mapping = null; // the mapping of this type
	public Vector triggerlist = null;
	public boolean starter = false; // if this type can start a cycle (access
									// attribute)
	public boolean truereq = false; // if the requirement of this type is true
	public boolean sourcehalt = false;
	public boolean targethalt = false;
	public Vector haltActivators = new Vector();

	public TermType(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}
}