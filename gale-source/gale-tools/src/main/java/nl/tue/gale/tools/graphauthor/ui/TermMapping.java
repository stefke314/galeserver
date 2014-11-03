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
 * TermMapping.java
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

/**
 * This class maps the concept relation types used in the graph author to the
 * types used in the internal graph by the termination algorithm.
 */
public class TermMapping {
	public int source; // 0 is source, 1 is destination
	public int target; // 0 is source, 1 is destination
	public String name;
	public String req; // the requirement of this mapping
	public String actionattr; // the concept+attribute of the action of this
								// mapping
	public String actionexpr; // the expression of the action of this mapping

	public TermMapping(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}
}