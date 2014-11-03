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
 * NextView.java
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

import java.util.Arrays;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.cache.CacheSession;
import nl.tue.gale.common.uri.URIs;
import nl.tue.gale.um.data.EntityValue;

import org.dom4j.Element;

public class NextView extends AbstractView {
	private static final int loopMax = 200;

	private String defaultExpr = "${#suitability}";

	public void setDefaultExpr(String defaultExpr) {
		this.defaultExpr = defaultExpr;
	}

	public Element getXml(Resource resource, Object... params) {
		GaleContext gale = GaleContext.of(resource);
		String expr = GaleUtil.getParam(params, "expr");
		if (expr == null)
			expr = defaultExpr;
		String label = GaleUtil.getParam(params, "label");
		String done = GaleUtil.getParam(params, "done");
		if (done == null)
			done = "end of course";

		TreeNodes nodes = new TreeNodes(gale);
		TreeNode startNode = nodes.get(gale.concept().getUriString());
		TreeNode currentNode = nextNode(startNode, nodes);
		int loop = 0;
		while (currentNode != null && !startNode.equals(currentNode)
				&& loop < loopMax) {
			CacheSession<EntityValue> session = gale.openUmSession();
			session.setBaseUri(URIs.of(currentNode.getConcept()));
			Boolean exprResult = (Boolean) gale.eval(session, expr);
			if (exprResult)
				break;
			currentNode = nextNode(currentNode, nodes);
			loop++;
		}
		if (loop == loopMax)
			return GaleUtil.createHTMLElement("span").addText(done + " (loop)");
		Element span = GaleUtil.createHTMLElement("span");
		if (currentNode == null || startNode.equals(currentNode)) {
			span.addText(done);
			return span;
		}
		if (label != null)
			span.addText(label);
		Element a = GaleUtil.createNSElement("a", GaleUtil.adaptns);
		span.add(a);
		a.addAttribute("href", currentNode.getConcept());
		a.addText(gale.dm().get(URIs.of(currentNode.getConcept())).getTitle());
		return span;
	}

	// depth first algorithm
	private TreeNode nextNode(TreeNode currentNode, TreeNodes nodes) {
		// try first child
		if (currentNode.getChildren().length > 0)
			return nodes.get(currentNode.getChildren()[0]);
		// try next sibling, then parent's next sibling, etc...
		int loop = 0;
		while (currentNode != null && loop < loopMax) {
			String parent = (currentNode.getParents().length == 0 ? null
					: currentNode.getParents()[0]);
			if (parent == null)
				return currentNode;
			TreeNode parentNode = nodes.get(parent);
			int index = Arrays.asList(parentNode.getChildren()).indexOf(
					currentNode.getConcept());
			if ((index + 1) < parentNode.getChildren().length)
				return nodes.get(parentNode.getChildren()[index + 1]);
			currentNode = parentNode;
			loop++;
		}
		if (loop == loopMax)
			throw new IllegalArgumentException("parent relations form a loop");
		return null;
	}
}
