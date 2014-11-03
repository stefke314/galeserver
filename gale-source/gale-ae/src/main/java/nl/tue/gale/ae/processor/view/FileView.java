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
 * FileView.java
 * Last modified: $Date$
 * In revision:   $Revision$
 * Modified by:   $Author: dsmits $
 *
 * Copyright (c) 2008-2011 Eindhoven University of Technology.
 * All Rights Reserved.
 *
 * This software is proprietary information of the Eindhoven University
 * of Technology. It may be used according to the GNU LGPL license.
 */
package nl.tue.gale.ae.processor.view;

import java.io.StringReader;
import java.net.URL;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.common.GaleUtil;

import org.dom4j.Element;

public class FileView extends AbstractView {
	public Element getXml(Resource resource, Object... params) {
		GaleContext gale = GaleContext.of(resource);
		String expr = GaleUtil.getParam(params, "expr");
		String file = (expr == null ? GaleUtil.getParam(params, "file") : gale
				.eval(expr).toString());
		if (file != null) {
			URL url = GaleUtil.generateURL(file, gale.gc().getHomeDir());
			return GaleUtil.parseXML(url).getRootElement();
		}
		String content = GaleUtil.getParam(params, "content");
		if (content == null)
			return GaleUtil.createErrorElement("[no content for FileView]");
		content = gale.eval(content).toString();
		if (!content.trim().startsWith("<"))
			content = "<span xmlns=\"http://www.w3.org/1999/xhtml\">" + content
					+ "</span>";
		return GaleUtil.parseXML(new StringReader(content)).getRootElement();
	}
}