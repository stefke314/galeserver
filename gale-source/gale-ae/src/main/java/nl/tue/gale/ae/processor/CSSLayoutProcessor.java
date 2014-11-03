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
 * CSSLayoutProcessor.java
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

import java.io.StringReader;
import java.util.List;

import nl.tue.gale.ae.AbstractResourceProcessor;
import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.ProcessorException;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.common.GaleUtil;

import org.dom4j.Element;
import org.dom4j.Node;

import com.google.common.collect.ImmutableList;

public class CSSLayoutProcessor extends AbstractResourceProcessor {
	public void processResource(Resource resource) throws ProcessorException {
		if (resource.isUsed("xml") || resource.isUsed("response"))
			return;

		// skip this processor for objects
		GaleContext gale = GaleContext.of(resource);
		if (gale.isObject())
			return;

		String cssLayout = (String) gale.cfgm().getObject(
				"gale://gale.tue.nl/config/presentation#cssLayout", resource);
		if (cssLayout == null)
			return;

		Element xml = gale.xml();
		Element body = xml.element("body");
		if (body == null)
			body = xml;

		Element div = GaleUtil.createHTMLElement("div").addAttribute("id",
				"gale-content");
		@SuppressWarnings("unchecked")
		List<Node> content = (List<Node>) body.content();
		for (Node n : ImmutableList.copyOf(content)) {
			content.remove(n);
			div.add(n);
		}
		content.add(div);

		Element cssElement = GaleUtil.parseXML(new StringReader(cssLayout))
				.getRootElement();
		content.add(0, cssElement);
		resource.put("serialize-xhtml-strict", "true");
	}
}
