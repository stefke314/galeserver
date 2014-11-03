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
 * AHA3Format.java
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

import java.io.InputStream;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.parser.ParseNode;
import nl.tue.gale.common.parser.Parser;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.common.uri.URIs;
import nl.tue.gale.conversion.parser.PCCommon;
import nl.tue.gale.conversion.parser.PCIdentifier;
import nl.tue.gale.dm.data.Attribute;
import nl.tue.gale.dm.data.Concept;
import nl.tue.gale.dm.data.ConceptRelation;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public final class AHA3Format {
	private static Parser parser = null;
	static {
		parser = new Parser();
		parser.registerParseComponent(new PCIdentifier());
		parser.registerParseComponent(new PCCommon());
	}

	public static org.apache.xerces.parsers.DOMParser createDOMParser(
			boolean validation, boolean loaddtd) {
		org.apache.xerces.parsers.DOMParser result = new org.apache.xerces.parsers.DOMParser();
		try {
			if (!validation) {
				result.setFeature("http://xml.org/sax/features/validation",
						false);
				if (!loaddtd) {
					result.setFeature(
							"http://apache.org/xml/features/nonvalidating/load-dtd-grammar",
							false);
					result.setFeature(
							"http://apache.org/xml/features/nonvalidating/load-external-dtd",
							false);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static synchronized List<Concept> convertStream(InputStream is) {
		// retrieve concept list
		Document doc = null;
		try {
			DOMParser parser = createDOMParser(false, false);
			parser.parse(new InputSource(is));
			doc = parser.getDocument();
		} catch (Exception e) {
			throw new IllegalArgumentException("unable to parse document", e);
		}
		NodeList nl = doc.getDocumentElement().getChildNodes();
		List<nl.tue.gale.conversion.aha3.data.Concept> cl = new LinkedList<nl.tue.gale.conversion.aha3.data.Concept>();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element)
				if (((Element) node).getTagName().equals("concept"))
					cl.add(KoenFormat.getKoenConcept((Element) node));
		}

		// add the new concepts
		List<Concept> result = new LinkedList<Concept>();
		for (nl.tue.gale.conversion.aha3.data.Concept c : cl)
			result.add(convertConcept(c));
		addHierarchy(cl, result);
		return result;
	}

	private static void addHierarchy(
			List<nl.tue.gale.conversion.aha3.data.Concept> cl,
			List<Concept> clist) {
		Hashtable<String, nl.tue.gale.conversion.aha3.data.Concept> concepthash = new Hashtable<String, nl.tue.gale.conversion.aha3.data.Concept>();
		for (nl.tue.gale.conversion.aha3.data.Concept c : cl)
			concepthash.put(c.getName(), c);
		Hashtable<URI, Concept> ctable = new Hashtable<URI, Concept>();
		for (Concept c : clist)
			ctable.put(c.getUri(), c);
		for (nl.tue.gale.conversion.aha3.data.Concept c : cl) {
			String parent = c.getParent();
			if ((parent == null) || ("".equals(parent)))
				continue;

			nl.tue.gale.conversion.aha3.data.Concept current = concepthash
					.get(concepthash.get(parent).getFirstChild());
			int i = 0;
			int o = 0;
			while (current != null) {
				if (current.getName().equals(c.getName()))
					o = i;
				String nextsib = current.getNextSib();
				if ((nextsib == null) || ("".equals(nextsib)))
					current = null;
				if (current != null)
					current = concepthash.get(nextsib);
				i++;
			}

			addParentRelation(convertConceptToUri(c.getName()),
					convertConceptToUri(parent), o + 1, ctable);
		}
	}

	private static void addParentRelation(URI child, URI parent, int order,
			Hashtable<URI, Concept> ctable) {
		if (GaleUtil.debug(5))
			System.out.println("adding parent [" + child + "," + order + " -> "
					+ parent + "]");
		Concept cchild = ctable.get(child);
		Concept cparent = ctable.get(parent);
		cchild.addAttribute(createAttribute("order", "java.lang.Integer", ""
				+ order, "", false));
		new ConceptRelation("parent", cchild, cparent);
		new ConceptRelation("extends", cchild, cparent);
	}

	private static Concept convertConcept(
			nl.tue.gale.conversion.aha3.data.Concept c) {
		if (GaleUtil.debug(5))
			System.out.println("adding [" + c.getName() + "]:");
		Concept result = new Concept(convertConceptToUri(c.getName()));
		result.setProperty("type", c.getType());
		result.setProperty("title", c.getTitle());
		result.setProperty("description", c.getDescription());

		nl.tue.gale.conversion.aha3.data.CaseGroup cg = (c
				.hasAttribute("showability") ? c.getAttribute("showability")
				.getCaseGroup() : null);
		String url = "\"gale:/abstract.xhtml\"";
		if (cg == null) {
			if (c.getResourceURL() != null)
				url = "\"" + c.getResourceURL() + "\"";
		} else {
			url = buildCGexpr(cg, 0);
		}
		result.addAttribute(createAttribute("resource", "java.lang.String",
				url, "", false));

		for (String aname : c.getAttributes()) {
			nl.tue.gale.conversion.aha3.data.Attribute a = c
					.getAttribute(aname);
			if (aname.equals("access")) {
				result.setEventCode(createEventCode(a.getActions(),
						c.getApplication()));
				if (GaleUtil.debug(5))
					System.out.println("- event code [" + result.getEventCode()
							+ "]:");
			} else {
				String defaultcode = convertCode(a.getDefault(),
						c.getApplication());
				String eventcode = createEventCode(a.getActions(),
						c.getApplication());
				result.addAttribute(createAttribute(aname, javaType(a),
						defaultcode, eventcode, a.isPersistent()));
			}
		}
		return result;
	}

	private static URI convertConceptToUri(String cname) {
		URI curi = null;
		try {
			curi = URIs.of("gale", "gale.tue.nl",
					"/aha3/" + cname.replaceAll("\\.", "/"), null);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"unable to create concept uri for '" + cname + "'");
		}
		return curi;
	}

	private static String javaType(nl.tue.gale.conversion.aha3.data.Attribute a) {
		if (a.getType() == nl.tue.gale.conversion.aha3.data.Attribute.Type.INTEGER)
			return "java.lang.Float";
		if (a.getType() == nl.tue.gale.conversion.aha3.data.Attribute.Type.STRING)
			return "java.lang.String";
		if (a.getType() == nl.tue.gale.conversion.aha3.data.Attribute.Type.BOOLEAN)
			return "java.lang.Boolean";
		if (a.getType() == nl.tue.gale.conversion.aha3.data.Attribute.Type.REAL)
			return "java.lang.Double";
		if (a.getType() == nl.tue.gale.conversion.aha3.data.Attribute.Type.DATE)
			return "java.util.Date";
		return "java.lang.Object";
	}

	private static String createEventCode(
			List<nl.tue.gale.conversion.aha3.data.Action> actionlist,
			String appname) {
		StringBuffer result = new StringBuffer();
		for (nl.tue.gale.conversion.aha3.data.Action action : actionlist) {
			String truecode = createAssignmentsCode(action.getTrueStatements(),
					appname);
			String falsecode = createAssignmentsCode(
					action.getFalseStatements(), appname);
			result.append("if (" + convertCode(action.getCondition(), appname)
					+ ") " + truecode);
			if (!falsecode.equals(""))
				result.append(" else " + falsecode);
		}
		return result.toString();
	}

	private static String createAssignmentsCode(
			List<nl.tue.gale.conversion.aha3.data.Assignment> assignlist,
			String appname) {
		if (assignlist.size() == 0)
			return "";
		StringBuffer sb = new StringBuffer();
		for (nl.tue.gale.conversion.aha3.data.Assignment assign : assignlist) {
			sb.append("#{"
					+ convertAttributeToUri(assign.getVariable(), appname)
					+ ", ");
			sb.append(convertCode(assign.getExpression(), appname) + "}");
			sb.append(";");
		}
		if (assignlist.size() == 1)
			return sb.toString();
		return "{" + sb.toString() + "}";
	}

	public static String convertCode(String code, String appname) {
		if (code == null)
			return null;
		try {
			if (code.startsWith("{") && code.endsWith("}"))
				return code.substring(1, code.length() - 1);
			ParseNode node = parser.parse("EXPR", code);
			return convertCode(node, appname);
		} catch (Exception e) {
			throw new IllegalArgumentException("unable to parse code: "
					+ e.getMessage(), e);
		}
	}

	public static String convertCode(ParseNode node, String appname) {
		if (node.getType().equals("id")) {
			String attrName = convertAttributeToUri(node.toString(), appname)
					.toString();
			if (attrName.equals("changed.diff"))
				return attrName;
			else
				return "${" + attrName + "}";
		} else if (node.getType().equals("const"))
			return node.toString();
		else if (node.getType().equals("expr")) {
			ParseNode second = (ParseNode) node.get("second");
			if (second == null)
				return node.get("operator")
						+ convertCode((ParseNode) node.get("first"), appname);
			return "(" + convertCode((ParseNode) node.get("first"), appname)
					+ " " + node.get("operator") + " "
					+ convertCode(second, appname) + ")";
		}
		return node.toString();
	}

	private static URI convertAttributeToUri(String aname, String appname) {
		aname = convertID(aname, appname);
		if (aname.startsWith("_"))
			return URIs.of("changed.diff");
		if (aname.indexOf(".") < 0)
			return URIs.of("#" + aname);
		String attribute = aname.substring(aname.lastIndexOf('.') + 1);
		aname = aname.substring(0, aname.lastIndexOf('.'));

		URI auri = null;
		try {
			auri = URIs.of("gale", "gale.tue.nl",
					"/aha3/" + aname.replaceAll("\\.", "/"), attribute);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"unable to create attribute uri for '" + aname + "'");
		}
		return auri;
	}

	private static String convertID(String id, String appname) {
		if (id.startsWith("_")) {
			if (id.startsWith("_" + appname))
				return id;
			return "_" + appname + id.substring(1);
		}
		if (id.startsWith(appname) || id.indexOf(".") < 0)
			return id;
		return appname + "." + id;
	}

	private static String buildCGexpr(
			nl.tue.gale.conversion.aha3.data.CaseGroup cg, int i) {
		if (i < cg.size()) {
			nl.tue.gale.conversion.aha3.data.Case c = cg.get(i);
			return "(${#showability} == " + c.getValue() + "?\""
					+ c.getReturnfragment() + "\":" + buildCGexpr(cg, i + 1)
					+ ")";
		} else {
			return "\"" + cg.getDefaultFragment() + "\"";
		}
	}

	private static Attribute createAttribute(String name, String type,
			String defaultcode, String eventcode, boolean persistent) {
		Attribute result = new Attribute(name);
		result.setType(type);
		result.setDefaultCode(defaultcode);
		result.setEventCode(eventcode);
		result.setProperty("persistent", persistent + "");
		if (GaleUtil.debug(5))
			System.out.println("--- adding [" + name + "]: " + type + ", "
					+ defaultcode + ", " + eventcode);
		return result;
	}
}