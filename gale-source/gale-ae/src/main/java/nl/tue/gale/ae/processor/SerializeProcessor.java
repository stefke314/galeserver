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
 * SerializeProcessor.java
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

import java.io.ByteArrayInputStream;

import nl.tue.gale.ae.AbstractResourceProcessor;
import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.ProcessorException;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.ae.impl.ConceptStability;
import nl.tue.gale.common.GaleUtil;

public class SerializeProcessor extends AbstractResourceProcessor {
	public void processResource(Resource resource) throws ProcessorException {
		if (resource.isUsed("response"))
			return;
		try {
			GaleContext gale = GaleContext.of(resource);
			String mime = gale.mime();
			if (!("text/xhtml".equals(mime) || "text/xml".equals(mime)
					|| "application/xml".equals(mime) || "application/smil"
						.equals(mime)))
				return;
			if (gale.xml() == null)
				return;

			String resultData = serializedString(gale);
			ConceptStability.setStableData(gale, resultData);
			resource.putUsed("stream",
					new ByteArrayInputStream(resultData.getBytes("UTF-8")));
			resource.put("mime", "text/html");
		} catch (Exception e) {
			throw new ProcessorException("unable to serialize document", e);
		}
	}

	private String serializedString(GaleContext gale) throws Exception {
		StringBuffer sb = new StringBuffer();
		if ("true".equals(gale.getResource().get("serialize-xhtml-strict"))
				|| "true".equals(gale.concept().getProperty("strict")))
			sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
		sb.append(GaleUtil.serializeXML(gale.xml()));
		return sb.toString();
	}
}