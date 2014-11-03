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
 * IfModule.java
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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.ProcessorException;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.common.GaleUtil;

import org.dom4j.Element;
import org.dom4j.Node;

public class IfModule extends AbstractModule {
	private List<String> mimeToHandle = Arrays.asList(new String[] {
			"text/xhtml", "text/xml", "application/xml", "application/smil" });

	public List<String> getMimeToHandle() {
		return mimeToHandle;
	}

	public void setMimeToHandle(List<String> mimeToHandle) {
		this.mimeToHandle = mimeToHandle;
	}

	@SuppressWarnings("unchecked")
	public Element traverse(Element element, Resource resource)
			throws ProcessorException {
		try {
			GaleContext gale = GaleContext.of(resource);
			String expr = element.attributeValue("expr");
			Element block = null;
			List<Element> blocks = new LinkedList<Element>();
			blocks.addAll(element.elements("block"));
			if (element.element("then") != null)
				blocks.add(element.element("then"));
			if (element.element("else") != null)
				blocks.add(element.element("else"));
			if (blocks.size() == 0)
				blocks.add(element);
			if (((Boolean) gale.eval(expr)).booleanValue())
				block = (Element) blocks.get(0);
			else if (blocks.size() > 1)
				block = (Element) blocks.get(1);
			if (block == null) {
				element.detach();
				return null;
			}
			processor.traverseChildren(block, resource);
			List<Node> content = (List<Node>) element.getParent().content();
			int index = content.indexOf(element);
			for (Node node : (List<Node>) block.content()) {
				content.add(index, node);
				index++;
			}
			content.remove(element);
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return (Element) GaleUtil.replaceNode(element,
					GaleUtil.createErrorElement("[" + e.getMessage() + "]"));
		}
	}
}