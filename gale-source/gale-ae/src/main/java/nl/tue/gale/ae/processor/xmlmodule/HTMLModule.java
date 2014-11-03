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
 * HTMLModule.java
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
import java.util.List;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.ProcessorException;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.common.uri.URIs;

import org.dom4j.Element;

public class HTMLModule extends AbstractModule {
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
		Element head = element.element("head");
		if (head == null) {
			head = element.addElement("head");
			head.detach();
			element.content().add(0, head);
		}
		traverse_head(head, resource);
		return element;
	}

	@SuppressWarnings("unchecked")
	private void traverse_head(Element head, Resource resource) {
		GaleContext gale = GaleContext.of(resource);
		String css = (String) gale.cfgm().getObject(
				"gale://gale.tue.nl/config/presentation#css", resource);
		Element base = head.element("base");
		if (base == null)
			base = head.addElement("base").addAttribute("href",
					getBaseLocation(resource));
		base.detach();
		head.content().add(0, base);
		for (String cssPart : css.split(";")) {
			Element ecss = head.addElement("link")
					.addAttribute("rel", "stylesheet")
					.addAttribute("type", "text/css")
					.addAttribute("href", cssPart);
			ecss.detach();
			head.content().add((cssPart.contains("${home}") ? 0 : 1), ecss);
		}
		String titleValue = (String) gale.cfgm().getObject(
				"gale://gale.tue.nl/config/presentation#title", resource);
		Element title = head.addElement("title").addText(titleValue);
		title.detach();
		head.content().add(0, title);
	}

	private String getBaseLocation(Resource resource) {
		GaleContext gale = GaleContext.of(resource);
		String result = resource.get("original-url").toString();
		if (result.startsWith("gale:")) {
			URI uri = URIs.of(result);
			URI reqUri = URIs.of(gale.req().getRequestURL().toString());
			reqUri = GaleUtil.setURIPart(reqUri, GaleUtil.URIPart.QUERY, null);
			reqUri = GaleUtil.setURIPart(reqUri, GaleUtil.URIPart.FRAGMENT,
					null);
			result = reqUri.toString() + "/${home}" + uri.getPath();
		}
		return result;
	}
}