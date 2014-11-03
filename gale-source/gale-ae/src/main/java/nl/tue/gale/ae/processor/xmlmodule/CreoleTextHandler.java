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
 * CreoleTextHandler.java
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
package nl.tue.gale.ae.processor.xmlmodule;

import java.util.LinkedList;
import java.util.List;

import nl.tue.gale.common.GaleUtil;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;

import com.google.common.collect.ImmutableList;

public class CreoleTextHandler extends AbstractTextHandler {
	public CreoleTextHandler() {
		setType("creole");
	}

	@Override
	public void handleTextElement(Element textElement) {
		traverse(textElement);
	}

	@SuppressWarnings("unchecked")
	private void traverse(Element textElement) {
		boolean flat = ("true".equals(textElement.attributeValue("flat")));
		List<Element> elements = ImmutableList
				.copyOf((List<Element>) textElement.elements());
		for (Element element : elements) {
			textElement.content().add(
					textElement.content().indexOf(element),
					DocumentFactory.getInstance().createText(
							"(% " + GaleUtil.serializeXML(element) + " %)"));
			textElement.remove(element);
		}
		textElement.normalize();
		List<Node> content = ImmutableList.copyOf((List<Node>) textElement
				.content());
		for (Node node : content)
			if (node.getNodeType() == Node.TEXT_NODE) {
				textElement.content().addAll(
						textElement.content().indexOf(node),
						parse(node.getText(), flat));
				textElement.content().remove(node);
			}
	}

	@SuppressWarnings("unchecked")
	private List<Node> parse(String text, boolean flat) {
		Element result = CreoleParser.instance().parse(text);
		List<Node> content;
		if (flat) {
			content = new LinkedList<Node>();
			for (Element element : (List<Element>) result.elements())
				content.addAll(element.content());
			content = ImmutableList.copyOf(content);
		} else
			content = (List<Node>) ImmutableList.copyOf(result.content());
		for (Node node : content)
			node.detach();
		return content;
	}
}
