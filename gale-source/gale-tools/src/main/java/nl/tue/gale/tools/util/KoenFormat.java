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
 * Modified by:   $Author$
 *
 * Copyright (c) 2008-2011 Eindhoven University of Technology.
 * All Rights Reserved.
 *
 * This software is proprietary information of the Eindhoven University
 * of Technology. It may be used according to the GNU LGPL license.
 */
package nl.tue.gale.tools.util;

import nl.tue.gale.conversion.aha3.data.Action;
import nl.tue.gale.conversion.aha3.data.Assignment;
import nl.tue.gale.conversion.aha3.data.Attribute;
import nl.tue.gale.conversion.aha3.data.Case;
import nl.tue.gale.conversion.aha3.data.CaseGroup;
import nl.tue.gale.conversion.aha3.data.Concept;
import nl.tue.gale.conversion.aha3.data.StableMode;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is used to convert between the "Koen"-format of concept
 * definitions in XML.
 * 
 */
public class KoenFormat {
	public static String Message = "";

	/**
	 * This method is used to convert a concept that is in internal format to
	 * the Koen format ready to be stored in an XML file.
	 */

	public static Node getKoenXML(Concept concept, Document doc) {
		Element econcept = doc.createElement("concept");
		Element ename = doc.createElement("name");
		econcept.appendChild(ename);
		ename.appendChild(doc.createTextNode(concept.getName()));

		Element edescription = doc.createElement("description");
		econcept.appendChild(edescription);

		if (concept.getDescription() != null) {
			edescription.appendChild(doc.createTextNode(concept
					.getDescription()));
		}

		Element eresource = doc.createElement("resource");
		econcept.appendChild(eresource);

		if (concept.getResourceURL() != null) {
			eresource.appendChild(doc.createTextNode(concept.getResourceURL()
					.toString()));
		}

		if (concept.getStableMode() != StableMode.NONE) {
			Element estable = doc.createElement("stable");
			estable.appendChild(doc.createTextNode(concept.getStableMode()
					.toString()));
			econcept.appendChild(estable);
		}
		if (concept.getStableExpression() != null) {
			if (!concept.getStableExpression().equals("")) {
				Element estable_expr = doc.createElement("stable_expr");
				estable_expr.appendChild(doc.createTextNode(concept
						.getStableExpression()));
				econcept.appendChild(estable_expr);
			}
		}

		Element etype = doc.createElement("concepttype");
		econcept.appendChild(etype);
		if (concept.getType() != null) {
			etype.appendChild(doc.createTextNode(concept.getType()));
		}

		Element etitle = doc.createElement("title");
		econcept.appendChild(etitle);
		if (concept.getTitle() != null) {
			etitle.appendChild(doc.createTextNode(concept.getTitle()));
		}

		Element ehier = doc.createElement("hierarchy");
		econcept.appendChild(ehier);

		Element efirstchild = doc.createElement("firstchild");
		ehier.appendChild(efirstchild);
		if (concept.getFirstChild() != null) {
			efirstchild
					.appendChild(doc.createTextNode(concept.getFirstChild()));
		}
		Element enextsib = doc.createElement("nextsib");
		ehier.appendChild(enextsib);
		if (concept.getNextSib() != null) {
			enextsib.appendChild(doc.createTextNode(concept.getNextSib()));
		}
		Element eparent = doc.createElement("parent");
		ehier.appendChild(eparent);
		if (concept.getParent() != null) {
			eparent.appendChild(doc.createTextNode(concept.getParent()));
		}

		for (String attrname : concept.getAttributes()) {
			econcept.appendChild(createAttributeNode(
					concept.getAttribute(attrname), doc));
		}

		return econcept;
	}

	private static Node createAttributeNode(Attribute attr, Document doc) {
		Element eattribute = doc.createElement("attribute");
		eattribute.setAttribute("name", attr.getName());
		eattribute.setAttribute("type", getKoenType(attr.getType()));
		eattribute.setAttribute("isPersistent",
				new Boolean(attr.isPersistent()).toString());
		eattribute.setAttribute("isSystem",
				new Boolean(attr.isSystem()).toString());
		eattribute.setAttribute("isChangeable",
				new Boolean(!attr.isReadonly()).toString());

		Element edescription = doc.createElement("description");
		eattribute.appendChild(edescription);

		if (attr.getDescription() != null) {
			edescription.appendChild(doc.createTextNode(attr.getDescription()));
		}

		Element edefault = doc.createElement("default");
		eattribute.appendChild(edefault);

		if (attr.getDefault() != null) {
			edefault.appendChild(doc.createTextNode(attr.getDefault()));
		}

		// add stable, stable_expr and casegroup conversion here
		// added by @Bart @ 07-05-2003
		if (attr.getStableMode() != StableMode.NONE) {
			Element estable = doc.createElement("stable");
			estable.appendChild(doc.createTextNode(attr.getStableMode()
					.toString()));
			eattribute.appendChild(estable);

			if (attr.getStableMode() == StableMode.FREEZE) {
				if (attr.getStableExpression() != "") {
					Element estable_expr = doc.createElement("stable_expr");
					estable_expr.appendChild(doc.createTextNode(attr
							.getStableExpression()));
					eattribute.appendChild(estable_expr);
				}
			}
		}

		// casegroup
		if (attr.getCaseGroup() != null) {
			// casegroup
			CaseGroup cg = null;
			cg = attr.getCaseGroup();

			Element ecasegroup = doc.createElement("casegroup");
			eattribute.appendChild(ecasegroup);

			Element edefaultfragment = doc.createElement("defaultfragment");
			edefaultfragment.appendChild(doc.createTextNode(cg
					.getDefaultFragment()));
			ecasegroup.appendChild(edefaultfragment);

			// loop the casevalues
			for (Case caseValue : cg) {
				Element ecasevalue = doc.createElement("casevalue");
				ecasegroup.appendChild(ecasevalue);

				// add value
				Element evalue = doc.createElement("value");
				evalue.appendChild(doc.createTextNode(caseValue.getValue()));
				ecasevalue.appendChild(evalue);
				// add return fragment
				Element ereturnfragment = doc.createElement("returnfragment");
				ereturnfragment.appendChild(doc.createTextNode(caseValue
						.getReturnfragment()));
				ecasevalue.appendChild(ereturnfragment);
			}
		}
		// end added by @Bart
		for (Action action : attr.getActions()) {
			eattribute.appendChild(createActionNode(action, doc));
		}

		return eattribute;
	}

	private static Node createActionNode(Action action, Document doc) {
		Element egeneratelist = doc.createElement("generateListItem");
		egeneratelist.setAttribute("isPropagating",
				new Boolean(action.getTrigger()).toString());

		Element erequirement = doc.createElement("requirement");
		egeneratelist.appendChild(erequirement);
		erequirement.appendChild(doc.createTextNode(XMLUtil.S2D(action
				.getCondition())));

		int i;
		Element etrueactions = doc.createElement("trueActions");
		egeneratelist.appendChild(etrueactions);

		for (Assignment assign : action.getTrueStatements()) {
			etrueactions.appendChild(createAssignmentNode(assign, doc));
		}

		if (action.getFalseStatements().size() > 0) {
			Element efalseactions = doc.createElement("falseActions");
			egeneratelist.appendChild(efalseactions);

			for (Assignment assign : action.getFalseStatements()) {
				efalseactions.appendChild(createAssignmentNode(assign, doc));
			}
		}

		return egeneratelist;
	}

	private static Node createAssignmentNode(Assignment assign, Document doc) {
		Element eaction = doc.createElement("action");

		String var = assign.getVariable();
		String attribute = var.substring(var.lastIndexOf(".") + 1);
		String concept = (var.indexOf(".") != -1 ? var.substring(0,
				var.lastIndexOf(".")) : "");

		Element econcept = doc.createElement("conceptName");
		eaction.appendChild(econcept);

		Element eattribute = doc.createElement("attributeName");
		eaction.appendChild(eattribute);
		econcept.appendChild(doc.createTextNode(concept));
		eattribute.appendChild(doc.createTextNode(attribute));

		Element eexpr = doc.createElement("expression");
		eaction.appendChild(eexpr);
		eexpr.appendChild(doc.createTextNode(assign.getExpression()));

		return eaction;
	}

	/**
	 * This method is used to create the internal format of a concept from the
	 * Koen format in an XML file.
	 */
	public static Concept getKoenConcept(Node node) {
		Concept concept = null;
		NodeList nodes = node.getChildNodes();
		int i = 0;
		concept = new Concept(XMLUtil.nodeValue(nodes.item(i)));
		i++;

		if (i < nodes.getLength()) {
			if (nodes.item(i).getNodeName().equals("description")) {
				concept.setDescription(XMLUtil.nodeValue(nodes.item(i)));
				i++;
			}
		}

		if (i < nodes.getLength()) {
			if (nodes.item(i).getNodeName().equals("resource")) {
				String resourcestr = XMLUtil.nodeValue(nodes.item(i));

				if (resourcestr != null) {
					try {
						concept.setResourceURL(resourcestr);
					} catch (Exception e) {
						System.out.println(e);
						setMessage("<h2>Warning!</h2>\nThe resource attribute for concept \""
								+ concept.getName()
								+ "\" is not defined correctly.<br>Check if it starts with \"http:\", \"ftp:\" or \"file:\".");
					}
				}

				i++;
			}
		}

		if (i < nodes.getLength()) {
			if (nodes.item(i).getNodeName().equals("stable")) {
				concept.setStableMode(StableMode.fromString(XMLUtil
						.nodeValue(nodes.item(i))));
				i++;
			}
		}

		if (i < nodes.getLength()) {
			if (nodes.item(i).getNodeName().equals("stable_expr")) {
				concept.setStableExpression(XMLUtil.nodeValue(nodes.item(i)));
				i++;
			}
		}

		if (i < nodes.getLength()) {
			if (nodes.item(i).getNodeName().equals("concepttype")) {
				concept.setType(XMLUtil.nodeValue(nodes.item(i)));
				i++;
			}
		}

		if (i < nodes.getLength()) {
			if (nodes.item(i).getNodeName().equals("title")) {
				concept.setTitle(XMLUtil.nodeValue(nodes.item(i)));
				i++;
			}
		}

		if (i < nodes.getLength()) {
			if (nodes.item(i).getNodeName().equals("hierarchy")) {
				NodeList hier = nodes.item(i).getChildNodes();
				concept.setFirstChild(XMLUtil.nodeValue(hier.item(0)));
				concept.setNextSib(XMLUtil.nodeValue(hier.item(1)));
				concept.setParent(XMLUtil.nodeValue(hier.item(2)));
				i++;
			}
		}

		for (; i < nodes.getLength(); i++) {
			concept.setAttribute(createAttributeObject(nodes.item(i)));
		}

		return concept;
	}

	private static Attribute createAttributeObject(Node node) {
		Attribute attr = null;
		NodeList nodes = node.getChildNodes();
		int i = 0;
		attr = new Attribute(((Element) node).getAttribute("name"));
		attr.setType(setKoenType(((Element) node).getAttribute("type")));
		attr.setReadonly(!(new Boolean(((Element) node)
				.getAttribute("isChangeable"))).booleanValue());
		attr.setSystem((new Boolean(((Element) node).getAttribute("isSystem")))
				.booleanValue());
		attr.setPersistent((new Boolean(((Element) node)
				.getAttribute("isPersistent"))).booleanValue());

		if (i < nodes.getLength()) {
			if (nodes.item(i).getNodeName().equals("description")) {
				attr.setDescription(XMLUtil.nodeValue(nodes.item(i)));
				i++;
			}
		}

		if (i < nodes.getLength()) {
			if (nodes.item(i).getNodeName().equals("default")) {
				attr.setDefault(XMLUtil.nodeValue(nodes.item(i)));
				i++;
			}
		}

		if (i < nodes.getLength()) {
			if (nodes.item(i).getNodeName().equals("stable")) {
				attr.setStableMode(StableMode.fromString(XMLUtil
						.nodeValue(nodes.item(i))));
				i++;
			}
		}

		if (i < nodes.getLength()) {
			if (nodes.item(i).getNodeName().equals("stable_expr")) {
				attr.setStableExpression(XMLUtil.nodeValue(nodes.item(i)));
				i++;
			}
		}

		if (i < nodes.getLength()) {

			if (nodes.item(i).getNodeName().equals("casegroup")) {
				CaseGroup cg = new CaseGroup();

				String defaultfragment;
				NodeList nodelist = nodes.item(i).getChildNodes();

				for (int ii = 0; ii < nodelist.getLength(); ii++) {
					if (nodelist.item(ii).getNodeName()
							.equals("defaultfragment")) {
						cg.setDefaultFragment(nodelist.item(ii).getFirstChild()
								.getNodeValue());
					}

					if (nodelist.item(ii).getNodeName().equals("casevalue")) {
						cg.add(createCaseObject(nodelist.item(ii)));
					}
				}

				attr.setCaseGroup(cg);
			}

			// end added by Barend at 8-4-2003
		}

		if (i < nodes.getLength()) {
			if (nodes.item(i).getNodeName().equals("generateListItem")) {
				for (; i < nodes.getLength(); i++) {
					attr.getActions().add(createActionObject(nodes.item(i)));
				}
			}
		}

		/*
		 * while (nodes.item(i).getNodeName().equals("actions")) {
		 * attr.getActions().add(createActionObject(nodes.item(i))); i++; }
		 */
		/*
		 * for (; i < nodes.getLength(); i++) {
		 * attr.getActions().add(createActionObject(nodes.item(i))); }
		 */

		return attr;
	}

	// added by Barend on 9-4-2003
	private static Case createCaseObject(Node node) {
		Case casevalue = new Case();

		// get the value value of case statement
		casevalue.setValue(node.getFirstChild().getFirstChild().getNodeValue());

		// get the returnfragment of this case statement
		casevalue.setReturnfragment(node.getChildNodes().item(1)
				.getFirstChild().getNodeValue());
		return casevalue;
	}

	// end added by Barend on 9-4-2003
	private static Action createActionObject(Node node) {
		Action action = new Action();
		NodeList nodes = node.getChildNodes();
		int i = 0;
		action.setTrigger((new Boolean(((Element) node)
				.getAttribute("isPropagating"))).booleanValue());
		action.setCondition(XMLUtil.nodeValue(nodes.item(i)));
		i++;

		int j;

		for (j = 0; j < nodes.item(i).getChildNodes().getLength(); j++) {
			action.getTrueStatements().add(
					createAssignmentObject(nodes.item(i).getChildNodes()
							.item(j)));
		}

		i++;

		if (i < nodes.getLength()) {
			for (j = 0; j < nodes.item(i).getChildNodes().getLength(); j++) {
				action.getFalseStatements().add(
						createAssignmentObject(nodes.item(i).getChildNodes()
								.item(j)));
			}
		}

		return action;
	}

	private static Assignment createAssignmentObject(Node node) {
		Assignment assign = null;
		NodeList nodes = node.getChildNodes();
		int i = 0;
		String name = XMLUtil.nodeValue(nodes.item(i)) + ".";
		i++;
		name = name + XMLUtil.nodeValue(nodes.item(i));
		i++;
		assign = new Assignment(name, XMLUtil.nodeValue(nodes.item(i)));

		return assign;
	}

	private static String getKoenType(Attribute.Type type) {
		if (type == Attribute.Type.INTEGER)
			return "int";
		if (type == Attribute.Type.STRING)
			return "string";
		if (type == Attribute.Type.BOOLEAN)
			return "bool";
		return "";
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

	public static String getMessage() {
		return Message;
	}

	public static void setMessage(String msg) {
		Message = msg;
	}
}
