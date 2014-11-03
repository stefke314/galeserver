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
 * ExportPlugin.java
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
package nl.tue.gale.ae.processor.plugin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Set;
import java.util.TreeSet;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.ProcessorException;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.ae.processor.xmlmodule.CountModule;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.common.uri.URIs;
import nl.tue.gale.dm.GDOMFormat;
import nl.tue.gale.dm.data.Concept;

import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.google.common.collect.ImmutableList;

public class ExportPlugin extends AbstractPlugin {
	public void doGet(Resource resource) throws ProcessorException {
		GaleContext gale = GaleContext.of(resource);
		try {
			String root = gale.req().getParameter("root");
			if (root == null || "".equals(root))
				throw new IllegalArgumentException(
						"no 'root' specified as parameter");
			URI[] conceptList = CountModule.getUriCache(URIs.of(root), gale);
			Set<Concept> concepts = new TreeSet<Concept>(
					new Concept.comparator());
			for (URI concept : conceptList) {
				Concept c = gale.dm().get(concept);
				concepts.add(c);
				concepts.addAll(c.getNamedOutConcepts("extends"));
			}
			Element result = GDOMFormat.toXML(ImmutableList.copyOf(concepts));

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			XMLWriter writer = new XMLWriter(out, new OutputFormat("  ", true));
			writer.write(result);
			resource.put("stream", new ByteArrayInputStream(out.toByteArray()));
			resource.put("mime", "application/gdom");
			gale.usedStream();
		} catch (Exception e) {
			throw new ProcessorException(
					"unable to export domain model to .gdom file: "
							+ e.getMessage(), e);
		}
	}
}