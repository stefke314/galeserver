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
 * TreeNodes.java
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.common.uri.URIs;
import nl.tue.gale.dm.data.Concept;
import nl.tue.gale.um.data.EntityValue;

public class TreeNodes {
	private Map<String, TreeNode> treeMap = new HashMap<String, TreeNode>();
	private GaleContext gale = null;
	private static final String TREENODES_KEY = "nl.tue.gale.ae.processor.view.TreeNodes";

	public TreeNodes(GaleContext gale) {
		this.gale = gale;
	}

	public static TreeNodes of(GaleContext gale) {
		TreeNodes result = (TreeNodes) gale.getResource().get(TREENODES_KEY);
		if (result == null) {
			result = new TreeNodes(gale);
			gale.getResource().put(TREENODES_KEY, result);
		}
		return result;
	}

	public TreeNode get(String name) {
		if (treeMap.containsKey(name))
			return treeMap.get(name);
		synchronized (treeMap) {
			try {
				Concept c = gale.dm().get(URIs.of(name));
				if (c == null) {
					treeMap.put(name, new TreeNode(name, null, null));
				} else {
					List<String> parents = getParents(c);
					List<String> children = getChildren(c);
					treeMap.put(
							name,
							new TreeNode(name,
									parents.toArray(new String[] {}), children
											.toArray(new String[] {})));
				}
			} catch (Exception e) {
				throw new IllegalArgumentException(
						"unable to build tree for concept '" + name + "': "
								+ e.getMessage(), e);
			}
		}
		return treeMap.get(name);
	}

	private List<String> getChildren(Concept concept) {
		return createSortedList(concept.getNamedInConcepts("parent"));
	}

	private List<String> getParents(Concept concept) {
		return createSortedList(concept.getNamedOutConcepts("parent"));
	}

	private List<String> createSortedList(Set<Concept> list) {
		List<String> result = new ArrayList<String>();
		List<Integer> order = new ArrayList<Integer>();
		for (Concept c : list)
			if (hierarchy(c)) {
				int i = 0;
				int j = order(c);
				while (i < order.size() && order.get(i) <= j)
					i++;
				order.add(i, j);
				result.add(i, c.getUriString());
			}
		return result;
	}

	private boolean hierarchy(Concept c) {
		EntityValue ev = gale.um().get(
				URIs.builder().uri(c.getUri()).userInfo(gale.userId())
						.fragment("hierarchy").build());
		if (ev == null)
			return true;
		return (Boolean) ev.getValue();
	}

	private int order(Concept c) {
		EntityValue ev = gale.um().get(
				URIs.builder().uri(c.getUri()).userInfo(gale.userId())
						.fragment("order").build());
		if (ev == null) {
			String result = c.getProperty("order");
			if (result != null && !"".equals(result))
				return Integer.parseInt(result);
			return 0;
		}
		return (Integer) ev.getValue();
	}
}
