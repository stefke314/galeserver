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
 * PluginModule.java
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.ProcessorException;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.common.uri.URIs;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;

public class PluginModule extends AbstractModule {
	private List<String> mimeToHandle = Arrays
			.asList(new String[] { "text/xhtml" });

	public List<String> getMimeToHandle() {
		return mimeToHandle;
	}

	public void setMimeToHandle(List<String> mimeToHandle) {
		this.mimeToHandle = mimeToHandle;
	}

	@SuppressWarnings("unchecked")
	public Element traverse(Element element, Resource resource)
			throws ProcessorException {
		// first process the children (content of new anchor tag)
		processor.traverseChildren(element, resource);
		// create new anchor tag
		Element a = DocumentFactory.getInstance().createElement(
				DocumentFactory.getInstance().createQName("a", "",
						"http://www.w3.org/1999/xhtml"));
		// backward compatibility
		List<Element> dlist = element.elements("linkdescription");
		Element parent = (dlist.size() == 0 ? element : dlist.get(0));
		// add children to new anchor tag
		a.appendContent(parent);
		/*
		 * List<Node> nodes = new ArrayList<Node>(); for (Node node :
		 * (List<Node>) parent.content()) nodes.add(node); for (Node node :
		 * nodes) a.(node);
		 */
		// plugin name
		String name = element.attributeValue("name");
		if (name == null)
			name = element.elementText("name");
		// add parameters
		URI reqUri = URIs.of(GaleContext.req(resource).getRequestURL()
				.toString());
		Map<String, String> params = new HashMap<String, String>();
		params.put("plugin", name);
		for (Element child : (List<Element>) element.elements("param"))
			params.put(child.attributeValue("name"),
					child.attributeValue("value"));
		reqUri = GaleUtil.setURIPart(reqUri, GaleUtil.URIPart.QUERY,
				GaleUtil.getQueryString(params));
		// finish the new anchor tag
		a.addAttribute("href", reqUri.toString());
		a.addAttribute("class", "good");
		// replace the original tag by the new anchor tag
		GaleUtil.replaceNode(element, a);
		return null;
	}
}