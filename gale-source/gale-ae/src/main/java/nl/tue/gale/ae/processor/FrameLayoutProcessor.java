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
 * FrameLayoutProcessor.java
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
package nl.tue.gale.ae.processor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import nl.tue.gale.ae.AbstractResourceProcessor;
import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.ProcessorException;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.UrlEncodedQueryString;
import nl.tue.gale.common.uri.URIs;

import org.dom4j.Attribute;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

public class FrameLayoutProcessor extends AbstractResourceProcessor {
	public void processResource(Resource resource) throws ProcessorException {
		if (resource.isUsed("request"))
			return;

		GaleContext gale = GaleContext.of(resource);

		// skip this processor for objects
		if (gale.isObject())
			return;

		// check if this is a call for a frame or the actual content
		if ("true".equals(gale.req().getParameter("frame"))) {
			Attribute target = GaleUtil.createHTMLElement("a")
					.addAttribute("target", "_parent").attribute("target");
			resource.put(
					"nl.tue.gale.ae.processor.xmlmodule.AdaptLinkModule.content",
					target);
			resource.put("nl.tue.gale.ae.processor.UpdateProcessor.noUpdate",
					"true");
			return;
		}

		// this is a call for the frame
		Element layoutConfig = (Element) gale.cfgm().getObject(
				"gale://gale.tue.nl/config/presentation#layout", resource);
		if (layoutConfig == null)
			return;
		Element html = GaleUtil.createHTMLElement("html");
		Element body = html.addElement("body").addAttribute("style",
				"margin:0px;padding:0px;");
		body.add(layoutConfig);
		layoutConfig = processLayoutConfig(layoutConfig);
		String url = GaleUtil.getRequestURL(gale.req());
		UrlEncodedQueryString qs = UrlEncodedQueryString.parse(URIs.of(url));
		qs.append("frame", "true");
		qs.remove("framewait");
		Element iframe = GaleUtil.createHTMLElement("iframe").addAttribute(
				"src", qs.apply(URIs.of(url)).toString());
		iframe.addAttribute("width", "100%");
		iframe.addAttribute("height", "100%");
		iframe.addAttribute("frameborder", "0");
		Element content = GaleUtil.findElement(layoutConfig,
				"gale-layoutprocessor-placeholder");
		GaleUtil.replaceNode(content, iframe);
		try {
			resource.put("url", new URL(url));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		resource.put("original-url", url);
		resource.put("mime", "text/xhtml");
		resource.put("xml", html);
		resource.put("layout", "true");
		try {
			if (!"true".equals(gale.req().getParameter("no-update")))
				gale.em().fireEvent("access", gale.dm().get(gale.conceptUri()),
						resource);
		} catch (Exception e) {
			throw new ProcessorException("unable to update profile for '"
					+ gale.conceptUri() + "'", e);
		}
		gale.usedRequest();
	}

	@SuppressWarnings("unchecked")
	static Element processLayoutConfig(Element layoutConfig) {
		if (layoutConfig.getName().equals("view")) {
			layoutConfig.setQName(DocumentFactory.getInstance().createQName(
					"view", "", "http://gale.tue.nl/adaptation"));
		} else if (layoutConfig.getName().equals("struct")) {
			Element table = GaleUtil.createHTMLElement("table")
					.addAttribute("cellspacing", "0")
					.addAttribute("cellpadding", "3")
					.addAttribute("width", "100%")
					.addAttribute("height", "100%").addAttribute("border", "0");
			boolean rows = layoutConfig.attributeValue("rows") != null;
			String attrname = (rows ? "height" : "width");
			String[] sizes = (rows ? layoutConfig.attributeValue("rows").split(
					";") : layoutConfig.attributeValue("cols").split(";"));
			Element tr = null;
			if (!rows) {
				tr = GaleUtil.createHTMLElement("tr");
				table.add(tr);
			}
			int i = 0;
			List<Element> elist = new LinkedList<Element>();
			elist.addAll(layoutConfig.elements());
			for (Element child : elist) {
				if (rows) {
					tr = GaleUtil.createHTMLElement("tr");
					table.add(tr);
				}
				Element td = GaleUtil.createHTMLElement("td")
						.addAttribute("valign", "top")
						.addAttribute(attrname, sizes[i])
						.addAttribute("style", "border-style:none");
				tr.add(td);
				child = processLayoutConfig(child);
				child.detach();
				td.add(child);
				i++;
			}
			return (Element) GaleUtil.replaceNode(layoutConfig, table);
		} else if (layoutConfig.getName().equals("content")) {
			layoutConfig.setQName(DocumentFactory.getInstance().createQName(
					"gale-layoutprocessor-placeholder"));
		} else {
			for (Element child : (List<Element>) layoutConfig.elements())
				processLayoutConfig(child);
		}
		return layoutConfig;
	}
}
