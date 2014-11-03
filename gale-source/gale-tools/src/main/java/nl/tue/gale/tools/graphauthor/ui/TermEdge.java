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
 * TermEdge.java
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
 * This class represents an edge in the internal graph.
 */
public class TermEdge {
	public TermVertex source = null; // the source vertex
	public TermVertex target = null; // the target vertex
	public TermType type = null; // the type of this edge
	public boolean reachable = false;// can this edge be reached by a startable
										// edge

	public TermEdge(TermVertex source, TermVertex target, TermType type) {
		this.source = source;
		this.target = target;
		this.type = type;
	}

	public String toString() {
		return source + " -> (" + type + ") " + target;
	}
}