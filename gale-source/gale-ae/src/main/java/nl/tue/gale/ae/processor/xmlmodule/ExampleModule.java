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
 * ExampleModule.java
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

import static nl.tue.gale.common.GaleUtil.createHTMLElement;
import static nl.tue.gale.common.GaleUtil.replaceNode;

import java.util.List;

import nl.tue.gale.ae.ProcessorException;
import nl.tue.gale.ae.Resource;

import org.dom4j.Element;
import org.dom4j.Node;

import com.google.common.collect.ImmutableList;

public class ExampleModule extends AbstractModule {
	private ImmutableList<String> mimeToHandle = ImmutableList.of("text/xhtml");

	@Override
	public List<String> getMimeToHandle() {
		return mimeToHandle;
	}

	public void setMimeToHandle(List<String> mimeToHandle) {
		this.mimeToHandle = ImmutableList.copyOf(mimeToHandle);
	}

	@Override
	public Element traverse(Element element, Resource resource)
			throws ProcessorException {
		Element pre = createHTMLElement("pre");
		@SuppressWarnings("unchecked")
		List<Node> content = (List<Node>) element.content();
		content = ImmutableList.copyOf(content);
		for (Node node : content) {
			node.detach();
			pre.add(node);
		}
		replaceNode(element, pre);
		return pre;
	}
}