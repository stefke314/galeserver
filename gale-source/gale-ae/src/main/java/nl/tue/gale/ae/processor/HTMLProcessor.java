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
 * HTMLProcessor.java
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

import java.io.InputStreamReader;

import nl.tue.gale.ae.AbstractResourceProcessor;
import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.ProcessorException;
import nl.tue.gale.ae.Resource;

import org.dom4j.io.SAXReader;

public class HTMLProcessor extends AbstractResourceProcessor {
	public void processResource(Resource resource) throws ProcessorException {
		if (resource.isUsed("stream"))
			return;
		try {
			GaleContext gale = GaleContext.of(resource);
			if (!("text/html".equals(gale.mime())))
				return;
			SAXReader reader = new SAXReader(
					new org.ccil.cowan.tagsoup.Parser());
			resource.put(
					"xml",
					reader.read(
							new InputStreamReader(gale.stream(), gale
									.encoding())).getRootElement());
			resource.put("mime", "text/xhtml");
			gale.usedStream();
		} catch (Exception e) {
			throw new ProcessorException("unable to convert HTML to XHTML", e);
		}
	}
}