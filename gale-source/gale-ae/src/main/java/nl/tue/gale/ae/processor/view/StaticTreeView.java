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
 * StaticTreeView.java
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

import java.util.LinkedList;
import java.util.List;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.uri.URIs;
import nl.tue.gale.dm.data.Concept;

import org.dom4j.Element;

public class StaticTreeView extends AbstractView {
	private void menuList(TreeNode parent, List<TreeNode> pathlist,
			int listindex, Element table, TreeNode rootnode, GaleContext gale,
			TreeNodes nodes) {
		if (parent.getChildren().length == 0)
			return;
		for (String node : parent.getChildren()) {
			table.add(createRowElement(gale.dm().get(URIs.of(node)),
					listindex * 15, node.equals(rootnode.getConcept())));
			if (listindex < pathlist.size())
				if (node.equals((pathlist.get(listindex)).getConcept())
						|| node.equals(rootnode.getConcept()))
					menuList(nodes.get(node), pathlist, listindex + 1, table,
							rootnode, gale, nodes);
		}
	}

	public Element getXml(Resource resource, Object... params) {
		if ("true".equals(GaleUtil.getParam(params, "css")))
			return getCssXml(resource, params);
		String smaxDepth = GaleUtil.getParam(params, "maxdepth");
		int maxDepth = 20;
		if (smaxDepth != null)
			maxDepth = Integer.parseInt(smaxDepth);

		GaleContext gale = GaleContext.of(resource);

		Element table = GaleUtil.createHTMLElement("table");
		// table.addAttribute("style",
		// "font-family:Tahoma,Arial;font-size:medium");

		TreeNodes nodes = new TreeNodes(gale);
		TreeNode rootnode = nodes.get(gale.conceptUri().toString());

		TreeNode curnode = rootnode;
		List<TreeNode> path = new LinkedList<TreeNode>();
		while (curnode != null && maxDepth > 0) {
			path.add(0, curnode);
			curnode = (curnode.getParents().length > 0 ? nodes.get(curnode
					.getParents()[0]) : null);
			maxDepth--;
		}

		boolean showRoot = (curnode != null);
		while (curnode != null && curnode.getParents().length > 0)
			curnode = nodes.get(curnode.getParents()[0]);
		if (showRoot) {
			table.add(createRowElement(
					gale.dm().get(URIs.of(curnode.getConcept())), 0, curnode
							.getConcept().equals(rootnode.getConcept())));
		}
		curnode = path.get(0);
		table.add(createRowElement(
				gale.dm().get(URIs.of(curnode.getConcept())), 0, curnode
						.getConcept().equals(rootnode.getConcept())));
		menuList(curnode, path, 1, table, rootnode, gale, nodes);

		return table;
	}

	private Element getCssXml(Resource resource, Object[] params) {
		GaleContext gale = GaleContext.of(resource);
		TreeNodes nodes = new TreeNodes(gale);
		TreeNode rootnode = nodes.get(gale.conceptUri().toString());
		TreeNode curnode = rootnode;
		List<TreeNode> path = new LinkedList<TreeNode>();
		while (curnode != null) {
			path.add(0, curnode);
			curnode = (curnode.getParents().length > 0 ? nodes.get(curnode
					.getParents()[0]) : null);
		}

		Element result = GaleUtil.createHTMLElement("div");
		result.addAttribute("id", "static-tree-view");
		curnode = path.get(0);
		Element ul = createMenu(gale, nodes, curnode, rootnode, path);
		ul.addAttribute("id", "nav");
		result.add(ul);
		return result;
	}

	private Element createMenu(GaleContext gale, TreeNodes nodes,
			TreeNode curNode, TreeNode rootNode, List<TreeNode> path) {
		Element result = GaleUtil.createHTMLElement("ul");
		for (String child : curNode.getChildren()) {
			Element li = createMenuItem(gale, child);
			if (rootNode.getConcept().equals(child))
				li.addAttribute("class", "current");
			if (path.contains(new TreeNode(child, null, null))
					&& nodes.get(child).getChildren().length > 0) {
				li.add(createMenu(gale, nodes, nodes.get(child), rootNode, path));
			}
			result.add(li);
		}
		return result;
	}

	private Element createMenuItem(GaleContext gale, String conceptName) {
		Element result = GaleUtil.createHTMLElement("li");
		Element a = GaleUtil.createNSElement("a", GaleUtil.adaptns);
		result.add(a);
		Concept c = gale.dm().get(URIs.of(conceptName));
		a.addText(c.getTitle());
		a.addAttribute("href", c.getUriString());
		return result;
	}
}
