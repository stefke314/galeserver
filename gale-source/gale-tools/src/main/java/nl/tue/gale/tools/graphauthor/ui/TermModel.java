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
 * TermModel.java
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

import java.util.Hashtable;
import java.util.Vector;

/**
 * This class represents the internal graph. It keeps track of all edges and
 * vertices. Edges should only be added or removed by the addEdge or removeEdge
 * methods, since these update information in the edges.
 */
public class TermModel {
	public Hashtable vertices = new Hashtable();
	public Vector edges = new Vector();

	public TermModel() {
	}

	public void addEdge(TermEdge edge) {
		if ((edge.source == null) || (edge.target == null)) {
			System.out.println("TermModel: edge not added: " + edge.source);
			return;
		}
		edge.source.edgesOut.add(edge);
		edge.target.edgesIn.add(edge);
		edges.add(edge);
	}

	public void removeEdge(TermEdge edge) {
		edge.source.edgesOut.remove(edge);
		edge.target.edgesIn.remove(edge);
		edges.remove(edge);
	}
}