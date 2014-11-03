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
 * UpdateContentPlugin.java
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
package nl.tue.gale.ae.processor.plugin;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.ProcessorException;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.ae.grapple.UpdateContentManager;
import nl.tue.gale.common.GaleUtil;

import org.dom4j.Element;

public class UpdateContentPlugin extends AbstractPlugin {
	public void doGet(Resource resource) throws ProcessorException {
		GaleContext gale = GaleContext.of(resource);
		try {
			String camFileName = gale.req().getParameter("cam");
			Element cam = GaleUtil.parseXML(
					GaleUtil.generateURL(camFileName, gale.gc().getHomeDir()))
					.getRootElement();
			((UpdateContentManager) gale.ac().getBean("updateContentManager"))
					.updateCAMModel(cam);
			Element result = GaleUtil.createHTMLElement("p");
			result.addText("Succes!");
			gale.usedStream();
			resource.put("xml", result);
			resource.put("mime", "text/xhtml");
		} catch (Exception e) {
			throw new ProcessorException("unable to run debug plugin: "
					+ e.getMessage(), e);
		}
	}
}