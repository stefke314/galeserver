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
 * XMLProcessor.java
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import nl.tue.gale.ae.AbstractResourceProcessor;
import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.ProcessorException;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.ae.processor.xmlmodule.Module;
import nl.tue.gale.common.GaleUtil;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Namespace;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class XMLProcessor extends AbstractResourceProcessor {
	private Map<String, Module> moduleTable = new HashMap<String, Module>();

	public void setModuleTable(Map<List<String>, Module> moduleTable) {
		this.moduleTable.clear();
		for (Map.Entry<List<String>, Module> entry : moduleTable.entrySet()) {
			entry.getValue().init(this);
			for (String key : entry.getKey())
				this.moduleTable.put(key, entry.getValue());
		}
	}

	public Map<List<String>, Module> getModuleTable() {
		Map<List<String>, Module> result = new HashMap<List<String>, Module>();
		Multimap<Module, String> buildMap = HashMultimap.create();
		for (Map.Entry<String, Module> entry : moduleTable.entrySet())
			buildMap.put(entry.getValue(), entry.getKey());
		for (Map.Entry<Module, Collection<String>> entry : buildMap.asMap()
				.entrySet())
			result.put(new ArrayList<String>(entry.getValue()), entry.getKey());
		return result;
	}

	public void traverse(Element element, Resource resource)
			throws ProcessorException {
		if (element == null)
			return;
		Namespace ns = element.getNamespace();
		String tag = null;
		if (ns != Namespace.NO_NAMESPACE) {
			tag = "{" + ns.getURI() + "}" + element.getName();
			if (!moduleTable.containsKey(tag))
				tag = null;
		}
		if (tag == null)
			tag = element.getName();
		if (moduleTable.containsKey(tag)) {
			Module mod = moduleTable.get(tag);
			if (mod.getMimeToHandle().contains(GaleContext.mime(resource))) {
				try {
					element = moduleTable.get(tag).traverse(element, resource);
				} catch (Exception e) {
					e.printStackTrace();
					element = (Element) GaleUtil.replaceNode(
							element,
							GaleUtil.createErrorElement("[" + e.getMessage()
									+ "]"));
				}
			}
		}
		traverseChildren(element, resource);
		if ("text/xhtml".equals(GaleContext.mime(resource)))
			if (element != null)
				if ("http://www.w3.org/1999/xhtml".equals(element
						.getNamespaceURI()))
					element.setQName(DocumentFactory.getInstance().createQName(
							element.getName(), "",
							"http://www.w3.org/1999/xhtml"));
	}

	@SuppressWarnings("unchecked")
	public void traverseChildren(Element element, Resource resource)
			throws ProcessorException {
		if (element == null)
			return;
		List<Element> children = new LinkedList<Element>();
		children.addAll(element.elements());
		for (Element child : children)
			traverse(child, resource);
	}

	public void processResource(Resource resource) throws ProcessorException {
		if (resource.isUsed("xml"))
			return;
		try {
			GaleContext gale = GaleContext.of(resource);
			String mime = gale.mime();
			if (!("text/xhtml".equals(mime) || "text/xml".equals(mime)
					|| "application/xml".equals(mime) || "application/smil"
						.equals(mime)))
				return;
			Element xml = GaleUtil.createNSElement("root", "");
			xml.add(gale.xml());
			traverse(xml, resource);
			gale.getResource().put("xml", xml.elements().get(0));
		} catch (Exception e) {
			throw new ProcessorException("unable to process xml document", e);
		}
	}
}
