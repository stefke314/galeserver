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
 * GDOMFormat.java
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
package nl.tue.gale.dm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.common.uri.URIs;
import nl.tue.gale.dm.data.Attribute;
import nl.tue.gale.dm.data.Concept;
import nl.tue.gale.dm.data.ConceptRelation;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;

public class GDOMFormat {
	public static Element toXML(List<Concept> concepts) {
		DocumentFactory df = DocumentFactory.getInstance();
		Element gdom = df.createElement("gdom", GaleUtil.gdomns);
		Set<URI> uriSet = new HashSet<URI>();
		Set<ConceptRelation> relationSet = new HashSet<ConceptRelation>();
		for (Concept concept : concepts) {
			gdom.add(toXML(concept, df));
			uriSet.add(concept.getUri());
			for (ConceptRelation cr : concept.getInCR())
				relationSet.add(cr);
			for (ConceptRelation cr : concept.getOutCR())
				relationSet.add(cr);
		}
		for (ConceptRelation cr : relationSet) {
			if (uriSet.contains(cr.getInConcept().getUri())
					&& uriSet.contains(cr.getOutConcept().getUri()))
				gdom.add(toXML(cr, df));
		}
		return gdom;
	}

	private static Element toXML(ConceptRelation cr, DocumentFactory df) {
		Element result = df.createElement("relation", GaleUtil.gdomns)
				.addAttribute("name", cr.getName());
		result.addElement("inconcept", GaleUtil.gdomns).addText(
				cr.getInConcept().getUriString());
		result.addElement("outconcept", GaleUtil.gdomns).addText(
				cr.getOutConcept().getUriString());
		toXMLProperties(result, cr.getProperties(), df);
		return result;
	}

	private static Element toXML(Concept concept, DocumentFactory df) {
		Element result = df.createElement("concept", GaleUtil.gdomns)
				.addAttribute("name", concept.getUriString());
		for (Attribute attr : concept.getAttributes())
			result.add(toXML(attr, df));
		result.addElement("event", GaleUtil.gdomns).addText(
				concept.getEventCode());
		toXMLProperties(result, concept.getProperties(), df);
		return result;
	}

	private static Element toXML(Attribute attr, DocumentFactory df) {
		Element result = df.createElement("attribute", GaleUtil.gdomns)
				.addAttribute("name", attr.getName())
				.addAttribute("type", attr.getType());
		result.addElement("default", GaleUtil.gdomns).addText(
				attr.getDefaultCode());
		result.addElement("event", GaleUtil.gdomns)
				.addText(attr.getEventCode());
		toXMLProperties(result, attr.getProperties(), df);
		return result;
	}

	private static void toXMLProperties(Element result,
			Map<String, String> properties, DocumentFactory df) {
		for (Map.Entry<String, String> entry : properties.entrySet()) {
			Element property = df.createElement("property", GaleUtil.gdomns)
					.addAttribute("name", entry.getKey())
					.addAttribute("value", entry.getValue());
			result.add(property);
		}
	}

	@SuppressWarnings("unchecked")
	public static List<Concept> toGDOM(Element element) {
		Map<URI, Concept> table = new HashMap<URI, Concept>();
		for (Element econcept : (List<Element>) element.elements("concept")) {
			Concept c = toGDOMConcept(econcept);
			table.put(c.getUri(), c);
		}
		for (Element erelation : (List<Element>) element.elements("relation")) {
			new ConceptRelation(
					erelation.attributeValue("name"),
					table.get(URIs.of(erelation.element("inconcept").getText())),
					table.get(URIs
							.of(erelation.element("outconcept").getText())));
		}
		List<Concept> result = new LinkedList<Concept>();
		result.addAll(table.values());
		return result;
	}

	@SuppressWarnings("unchecked")
	private static Concept toGDOMConcept(Element econcept) {
		Concept result = new Concept(URIs.of(econcept.attributeValue("name")));
		for (Element eattribute : (List<Element>) econcept
				.elements("attribute"))
			result.addAttribute(toGDOMAttribute(eattribute));
		readProps(econcept, result.getProperties());
		if (econcept.element("event") != null)
			result.setEventCode(econcept.element("event").getText());
		return result;
	}

	private static Attribute toGDOMAttribute(Element eattribute) {
		Attribute result = new Attribute(eattribute.attributeValue("name"));
		if (eattribute.element("default") != null)
			result.setDefaultCode(eattribute.element("default").getText());
		if (eattribute.element("event") != null)
			result.setEventCode(eattribute.element("event").getText());
		if (eattribute.attributeValue("type") != null)
			result.setType(eattribute.attributeValue("type"));
		readProps(eattribute, result.getProperties());
		return result;
	}

	@SuppressWarnings("unchecked")
	private static void readProps(Element element, Map<String, String> props) {
		for (Element eprop : (List<Element>) element.elements("property"))
			props.put(eprop.attributeValue("name"),
					eprop.attributeValue("value"));
	}
}
