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
 * AjaxProcessor.java
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

import nl.tue.gale.ae.AbstractResourceProcessor;
import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.ProcessorException;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.common.GaleUtil;

import org.dom4j.Element;

public class AjaxProcessor extends AbstractResourceProcessor {
	private String scriptLocation = null;

	public String getScriptLocation() {
		return scriptLocation;
	}

	public void setScriptLocation(String scriptLocation) {
		this.scriptLocation = scriptLocation;
	}

	public void processResource(Resource resource) throws ProcessorException {
		try {
			GaleContext gale = GaleContext.of(resource);
			String mime = gale.mime();
			if (!("text/xhtml".equals(mime) || "text/xml".equals(mime)
					|| "application/xml".equals(mime) || "application/smil"
						.equals(mime)))
				return;
			if (gale.xml() == null)
				return;
			boolean plugin = "true".equals(resource
					.get("nl.tue.gale.ae.processor.AjaxProcessor.plugin"));
			Element xml = gale.xml();
			if (!plugin && xml.element("head") != null
					&& xml.element("body") != null) {
				try {
					String requestURL = gale.req().getRequestURL().toString();
					String qstr = gale.req().getQueryString();
					if (qstr == null || "".equals(qstr))
						qstr = "?";
					else
						qstr = "?" + qstr + "&";
					String guid = GaleUtil.newGUID();
					qstr = qstr + "plugin=ajax&documentId=" + guid;
					gale.um().pollUpdated(gale.userId(), guid);
					requestURL = requestURL + qstr;
					Element meta = GaleUtil.createHTMLElement("meta")
							.addAttribute("name", "requestURL")
							.addAttribute("content", requestURL)
							.addAttribute("id", "requestURL");
					xml.element("head").add(meta);
					xml.element("body").addAttribute("id", "ajaxUpdate");
					xml.element("body").addAttribute("onload", "startTimer()");
					Element script = GaleUtil.createHTMLElement("script")
							.addAttribute("type", "text/javascript")
							.addAttribute("src", scriptLocation).addText(" ");
					xml.element("head").add(script);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (plugin) {
				resource.put("xml", xml.element("body"));
			}
			gale.usedStream();
		} catch (Exception e) {
			throw new ProcessorException("unable to add ajax elements", e);
		}
	}
}
