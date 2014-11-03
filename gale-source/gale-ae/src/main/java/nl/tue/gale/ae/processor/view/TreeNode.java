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
 * TreeNode.java
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
package nl.tue.gale.ae.processor.view;

import nl.tue.gale.ae.GaleContext;

public class TreeNode {
	private String concept = null;
	private String[] parents = null;
	private String[] children = null;

	public TreeNode(String concept, String[] parents, String[] children) {
		this.concept = concept;
		if (parents == null)
			this.parents = new String[] {};
		else
			this.parents = parents;
		if (children == null)
			this.children = new String[] {};
		else
			this.children = children;
	}

	public static TreeNode of(GaleContext gale) {
		return TreeNodes.of(gale).get(gale.conceptUri().toString());
	}

	public String getConcept() {
		return concept;
	}

	public String[] getParents() {
		return parents;
	}

	public String[] getChildren() {
		return children;
	}

	public boolean equals(Object object) {
		if (!(object instanceof TreeNode))
			return false;
		TreeNode treeNode = (TreeNode) object;
		if (concept == null)
			return treeNode.concept == null;
		return concept.equals(treeNode.concept);
	}

}
