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
 * KoenFormat.java
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
package nl.tue.gale.conversion.aha3;

import java.util.LinkedList;

import nl.tue.gale.conversion.aha3.data.Action;
import nl.tue.gale.conversion.aha3.data.Assignment;
import nl.tue.gale.conversion.aha3.data.Attribute;
import nl.tue.gale.conversion.aha3.data.Case;
import nl.tue.gale.conversion.aha3.data.CaseGroup;
import nl.tue.gale.conversion.aha3.data.Concept;
import nl.tue.gale.conversion.aha3.data.StableMode;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is used to convert between the "Koen"-format of concept
 * definitions in XML.
 * 
 */
public final class KoenFormat {
	private static String tostring(Node n) {
		return (n == null ? null : n.getNodeValue());
	}

	private static String convertF2A(String url) {
		return (url.startsWith("file:") ? "gale:" + url.substring(5) : url);
	}

	/**
	 * This method is used to create the internal format of a concept from the
	 * Koen format in an XML file.
	 */
	public static Concept getKoenConcept(Element node) {
		Concept concept = new Concept(getTextChild(node, "name"));

		NodeList nl = node.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			if (!(nl.item(i) instanceof Element))
				continue;
			Element e = (Element) nl.item(i);
			String s = tostring(e.getFirstChild());
			if (s != null) {
				if (e.getTagName().equals("description"))
					concept.setDescription(s);
				if (e.getTagName().equals("resource"))
					concept.setResourceURL(convertF2A(s));
				if (e.getTagName().equals("stable"))
					concept.setStableMode(StableMode.fromString(s));
				if (e.getTagName().equals("stable_expr"))
					concept.setStableExpression(s);
				if (e.getTagName().equals("concepttype"))
					concept.setType(s);
				if (e.getTagName().equals("title"))
					concept.setTitle(s);
			}
			if (e.getTagName().equals("hierarchy")) {
				concept.setFirstChild(getTextChild(e, "firstchild"));
				concept.setNextSib(getTextChild(e, "nextsib"));
				concept.setParent(getTextChild(e, "parent"));
			}
			if (e.getTagName().equals("attribute"))
				concept.setAttribute(createAttributeObject(e));
		}

		return concept;
	}

	private static Attribute createAttributeObject(Element node) {
		Attribute attr = new Attribute(node.getAttribute("name"));

		attr.setType(setKoenType(node.getAttribute("type")));
		attr.setReadonly(!(new Boolean(node.getAttribute("isChangeable")))
				.booleanValue());
		attr.setSystem((new Boolean(node.getAttribute("isSystem")))
				.booleanValue());
		attr.setPersistent((new Boolean(node.getAttribute("isPersistent")))
				.booleanValue());

		NodeList nl = node.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			if (!(nl.item(i) instanceof Element))
				continue;
			Element e = (Element) nl.item(i);
			String s = tostring(e.getFirstChild());
			if (s != null) {
				if (e.getTagName().equals("description"))
					attr.setDescription(s);
				if (e.getTagName().equals("default"))
					attr.setDefault(s);
				if (e.getTagName().equals("stable"))
					attr.setStableMode(StableMode.fromString(s));
				if (e.getTagName().equals("stable_expr"))
					attr.setStableExpression(s);
			}
			if (e.getTagName().equals("casegroup")) {
				CaseGroup cg = new CaseGroup();
				attr.setCaseGroup(cg);
				cg.setDefaultFragment(convertF2A(getTextChild(e,
						"defaultfragment")));
				NodeList nlcg = getChildElementsByTagName(e, "casevalue");
				for (int j = 0; j < nlcg.getLength(); j++)
					cg.add(createCaseObject((Element) nlcg.item(j)));
			}
			if (e.getTagName().equals("generateListItem"))
				attr.getActions().add(createActionObject(e));
		}

		return attr;
	}

	private static Case createCaseObject(Element node) {
		Case casevalue = new Case();
		casevalue.setValue(getTextChild(node, "value"));
		casevalue.setReturnfragment(convertF2A(getTextChild(node,
				"returnfragment")));
		return casevalue;
	}

	private static Action createActionObject(Element node) {
		Action action = new Action();
		action.setTrigger((new Boolean(node.getAttribute("isPropagating")))
				.booleanValue());
		action.setCondition(getTextChild(node, "requirement"));

		Element ta = getFirstElementByName(((Element) node), "trueActions");
		Element fa = getFirstElementByName(((Element) node), "falseActions");

		if (ta != null) {
			NodeList nl = ta.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				if (!(nl.item(i) instanceof Element))
					continue;
				Element e = (Element) nl.item(i);
				action.getTrueStatements().add(createAssignmentObject(e));
			}
		}

		if (fa != null) {
			NodeList nl = fa.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				if (!(nl.item(i) instanceof Element))
					continue;
				Element e = (Element) nl.item(i);
				action.getFalseStatements().add(createAssignmentObject(e));
			}
		}

		return action;
	}

	private static Assignment createAssignmentObject(Element node) {
		String cname = getTextChild(node, "conceptName");
		String aname = getTextChild(node, "attributeName");
		String expr = getTextChild(node, "expression");
		return new Assignment(cname + "." + aname, expr);
	}

	private static Attribute.Type setKoenType(String type) {
		if ("int".equals(type))
			return Attribute.Type.INTEGER;
		if ("string".equals(type))
			return Attribute.Type.STRING;
		if ("bool".equals(type))
			return Attribute.Type.BOOLEAN;
		return Attribute.Type.STRING;
	}

	private static class ListNodeList implements org.w3c.dom.NodeList {
		private LinkedList<? extends org.w3c.dom.Node> v;

		public ListNodeList(LinkedList<? extends org.w3c.dom.Node> v) {
			this.v = v;
		}

		public int getLength() {
			return v.size();
		}

		public org.w3c.dom.Node item(int index) {
			return v.get(index);
		}
	}

	/**
	 * Returns a <code>org.w3c.dom.NodeList</code> of all child elements with
	 * the specified tagname.
	 * 
	 * @param element
	 *            the <code>org.w3c.dom.Element</code> whose child elements
	 *            should be selected
	 * @param name
	 *            the name of the the child elements
	 * @return a <code>org.w3c.dom.NodeList</code> containing the selected child
	 *         elements
	 */
	public static org.w3c.dom.NodeList getChildElementsByTagName(
			org.w3c.dom.Element element, String name) {
		org.w3c.dom.NodeList children = element.getChildNodes();
		LinkedList<org.w3c.dom.Element> v = new LinkedList<org.w3c.dom.Element>();
		for (int i = 0; i < children.getLength(); i++) {
			org.w3c.dom.Node n = children.item(i);
			if (n instanceof org.w3c.dom.Element)
				if (name.equals(getLocalName((org.w3c.dom.Element) n)))
					v.add((org.w3c.dom.Element) n);
		}
		return new ListNodeList(v);
	}

	/**
	 * Returns the value of the text node of an element with the specified name.
	 * The first step is to find the first child element with the specified
	 * name. If no such element is found this method returns the empty string.
	 * Assume this found element has a text node child and return its value.
	 * Otherwise return the empty string.
	 * 
	 * @param element
	 *            the <code>org.w3c.dom.Element</code> that should be searched
	 * @param name
	 *            the name of the element to search for
	 * @return the value of the text node of the found element or the empty
	 *         string if this value is not found
	 */
	public static String getTextChild(org.w3c.dom.Element element, String name) {
		org.w3c.dom.Element child = getFirstElementByName(element, name);
		if (child == null)
			return "";
		org.w3c.dom.Node node = child.getFirstChild();
		if (node == null)
			return "";
		return node.getNodeValue();
	}

	/**
	 * Returns the first child element whose tag name matches the name
	 * specified. Returns <code>null</code> if no such element was found.
	 * 
	 * @param current
	 *            the <code>org.w3c.dom.Element</code> whose children have to be
	 *            searched
	 * @param name
	 *            the tagname to be searched for
	 * @return the first child element with the specified name
	 */
	public static org.w3c.dom.Element getFirstElementByName(
			org.w3c.dom.Element current, String name) {
		org.w3c.dom.NodeList nl = current.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			org.w3c.dom.Node n = nl.item(i);
			if (n instanceof org.w3c.dom.Element)
				if (name.equals(getLocalName((org.w3c.dom.Element) n)))
					return (org.w3c.dom.Element) n;
		}
		return null;
	}

	public static String getLocalName(org.w3c.dom.Element element) {
		String result = element.getLocalName();
		if (result == null)
			result = element.getTagName();
		return result;
	}
}
