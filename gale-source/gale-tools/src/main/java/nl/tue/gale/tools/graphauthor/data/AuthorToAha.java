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
 * AuthorToAha.java
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
package nl.tue.gale.tools.graphauthor.data;

import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import nl.tue.gale.tools.graphauthor.ui.GraphAuthor;

/**
 * AuthorToAHA converts the relations to AHA! code.
 * 
 */
public class AuthorToAha {
	public CRTConceptRelationType creltype;

	/**
	 * Default constructor.
	 */
	public AuthorToAha() {

		AuthorSTATIC.ahaOut = new AHAOutConceptList();
		this.AddConceptsToAHAOut();

		AuthorSTATIC.relaha.AddDivideInformation();

		// group all relation
		LinkedList groupedRelations = AuthorSTATIC.relaha
				.SplitConceptRelations();

		// check if empty
		if (groupedRelations.isEmpty()) {
			String error = "Relation list is empty, there is nothing to save!";
			javax.swing.JOptionPane.showMessageDialog(null, error, "Error",
					javax.swing.JOptionPane.ERROR_MESSAGE);

			return;
		}

		for (Iterator i = groupedRelations.iterator(); i.hasNext();) {
			this.ProcessConceptRel((LinkedList) i.next());
		}
	}

	private void listConceptInfoAndAttributes() {
		for (Iterator i = AuthorSTATIC.conceptInfoList.iterator(); i.hasNext();) {
			AHAOutConcept tempConcept = (AHAOutConcept) i.next();
			for (Iterator j = tempConcept.attributeList.iterator(); j.hasNext();) {
				AHAOutAttribute tempAttr = (AHAOutAttribute) j.next();
			}
		}
	}

	private void listConceptAndAttributes() {
		for (Iterator i = AuthorSTATIC.conceptList.iterator(); i.hasNext();) {
			AHAOutConcept tempConcept = (AHAOutConcept) i.next();
			for (Iterator j = tempConcept.attributeList.iterator(); j.hasNext();) {
				AHAOutAttribute tempAttr = (AHAOutAttribute) j.next();
			}
		}
	}

	private void listAHAOut() {
		for (Iterator i = AuthorSTATIC.ahaOut.conceptList.iterator(); i
				.hasNext();) {
			AHAOutConcept tempConcept = (AHAOutConcept) i.next();
			for (Iterator j = tempConcept.attributeList.iterator(); j.hasNext();) {
				AHAOutAttribute tempAttr = (AHAOutAttribute) j.next();
			}
		}
	}

	/**
	 * debug function
	 */
	public void printAHAOut() {
		for (Iterator j = AuthorSTATIC.ahaOut.conceptList.iterator(); j
				.hasNext();) {
			AHAOutConcept outtest = (AHAOutConcept) j.next();
			for (Iterator k = outtest.attributeList.iterator(); k.hasNext();) {
				AHAOutAttribute atttest = (AHAOutAttribute) k.next();
			}
		}
	}

	// added by @David @18-05-2004
	private DefaultMutableTreeNode findRecNode(DefaultMutableTreeNode node,
			String name) {
		if ((AuthorSTATIC.projectName + "." + node.toString()).trim().equals(
				name))
			return node;
		else {
			for (Enumeration i = node.children(); i.hasMoreElements();) {
				DefaultMutableTreeNode next = (DefaultMutableTreeNode) i
						.nextElement();
				DefaultMutableTreeNode result = findRecNode(next, name);
				if (result != null)
					return result;
			}
		}
		return null;
	}

	// returns the node of the concept with specified name.
	private DefaultMutableTreeNode findNode(String name) {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) GraphAuthor.sharedConceptTree
				.getModel().getRoot();
		return findRecNode(root, name);
	}

	/**
	 * Adds the hierarchy information to the AHAOutConcept.
	 */
	private void addTreeInformation(AHAOutConcept outc) {
		DefaultMutableTreeNode node = findNode(outc.name.trim());
		if (node != null) {
			outc.parent = "";
			if (node.getParent() != null)
				outc.parent = AuthorSTATIC.projectName + "."
						+ node.getParent().toString().trim();
			outc.firstchild = "";
			if (node.getChildCount() != 0)
				outc.firstchild = AuthorSTATIC.projectName + "."
						+ node.getFirstChild().toString().trim();
			outc.nextsib = "";
			if (node.getNextSibling() != null)
				outc.nextsib = AuthorSTATIC.projectName + "."
						+ node.getNextSibling().toString().trim();
		}
	}

	// end added by @David @18-05-2004

	/**
	 * AddConceptsToAHAOut adds all the concepts with attributes to the ahaout
	 * concept list.
	 */
	public void AddConceptsToAHAOut() {
		// all concepts
		for (Iterator i = AuthorSTATIC.conceptInfoList.iterator(); i.hasNext();) {
			AHAOutConcept inC = (AHAOutConcept) i.next();

			String aConceptName = (AuthorSTATIC.projectName + "." + inC.name)
					.trim();
			AHAOutConcept outc = new AHAOutConcept();
			outc.name = aConceptName;
			outc = inC;

			// added by @David @18-05-2004
			addTreeInformation(outc);
			// end added by @David @18-05-2004

			AuthorSTATIC.ahaOut.conceptList.add(outc);
		}
	}

	/**
	 * AddAttributesToAHAOut adds the attributes defined in attlist to the
	 * output concepts.
	 */
	public void AddAttributesToAHAOut() {

		// all concepts
		for (Iterator i = AuthorSTATIC.conceptList.iterator(); i.hasNext();) {
			AHAOutConcept inC = (AHAOutConcept) i.next();

			String aConceptName = (AuthorSTATIC.projectName + "." + inC.name)
					.trim();
			AHAOutConcept outc = new AHAOutConcept();
			outc.name = aConceptName;
			// find the template for a concept
			for (Iterator k = AuthorSTATIC.templateList.iterator(); k.hasNext();) {
				ConceptTemplate cTemp = (ConceptTemplate) k.next();
				if (inC.template.equals(cTemp.name)) {

					// process all attributes form the template
					for (Iterator l = cTemp.attributes.iterator(); l.hasNext();) {
						AHAOutAttribute tempAtt = (AHAOutAttribute) l.next();

						// new is needed to reserve new memory
						AHAOutAttribute cloneAtt = new AHAOutAttribute();
						cloneAtt.description = tempAtt.description;
						cloneAtt.setDefaultList = new CRTSetDefault();
						cloneAtt.setDefaultList.setdefault = tempAtt.setDefaultList.setdefault;
						cloneAtt.isChangeable = tempAtt.isChangeable;
						cloneAtt.isPersistent = tempAtt.isPersistent;
						cloneAtt.isSystem = tempAtt.isSystem;
						cloneAtt.name = tempAtt.name;
						cloneAtt.type = tempAtt.type;

						outc.attributeList.add(cloneAtt);
					}
				}
			}

			for (Iterator iIterator = AuthorSTATIC.conceptInfoList.iterator(); iIterator
					.hasNext();) {
				AHAOutConcept info = (AHAOutConcept) iIterator.next();

				if (info.name.equals(outc.name.trim())) {
					outc.description = info.description;
					outc.resource = info.resource;
				}
			}
			AuthorSTATIC.ahaOut.conceptList.add(outc);
		}
	}

	/**
	 * CheckAttributeName returns true if attributename exists in conceptname.
	 */
	public boolean CheckAttributeName(String conceptname, String attributename) {
		boolean result;
		result = false;

		for (Iterator i = AuthorSTATIC.ahaOut.conceptList.iterator(); i
				.hasNext();) {
			AHAOutConcept acon = (AHAOutConcept) i.next();

			if (acon.name.equals(conceptname)) {
				for (Iterator j = acon.attributeList.iterator(); j.hasNext();) {
					AHAOutAttribute attr = (AHAOutAttribute) j.next();

					if (attr.name.equals(attributename)) {
						result = true;
					}
				}
			}
		}

		return result;
	}

	/**
	 * AddAttributeToConceptAttribute, add to attribute expression to the output
	 * concept
	 */
	public void AddAttributeToConceptAttribute(String conceptName,
			String attributeName, String sValue) {
		String sNewValue = new String(sValue);
		sValue = " && " + sNewValue;

		for (Iterator i = AuthorSTATIC.ahaOut.conceptList.iterator(); i
				.hasNext();) {
			AHAOutConcept acon = (AHAOutConcept) i.next();

			if (acon.name.equals(conceptName)) {
				// found conceptName
				for (Iterator j = acon.attributeList.iterator(); j.hasNext();) {
					AHAOutAttribute outat = (AHAOutAttribute) j.next();

					if (outat.name.equals(attributeName)) {
						// found attributename
						if (outat.setDefaultList.setdefault.trim().equals("")) {
							outat.setDefaultList.setdefault = sNewValue;
						} else {
							outat.setDefaultList.setdefault = outat.setDefaultList.setdefault
									+ sValue;
						}
					}
				}
			}
		}
	}

	/**
	 * AddAttibuteToConcept adds a new attribute to a concept.
	 * 
	 * This is function isn`t used anymore, all attributes are set through
	 * attlist.xml
	 */
	public void AddAttributeToConcept(String conceptname, AHAOutAttribute ain) {
		for (Iterator i = AuthorSTATIC.ahaOut.conceptList.iterator(); i
				.hasNext();) {
			AHAOutConcept acon = (AHAOutConcept) i.next();

			if (acon.name.equals(conceptname)) {
				acon.attributeList.add(ain);
			}
		}
	}

	/**
	 * CheckConceptNameExist returns true if conceptname is already in the
	 * output conceptlist.
	 */
	public boolean CheckConceptNameExist(String conceptname) {
		boolean result;
		result = false;

		for (Iterator i = AuthorSTATIC.ahaOut.conceptList.iterator(); i
				.hasNext();) {
			AHAOutConcept acon = (AHAOutConcept) i.next();

			if (acon.name.equals(conceptname)) {
				result = true;
			}
		}

		return result;
	}

	/**
	 * ReplaceVar replaces the keyword "var:" with the label.
	 */
	public String ReplaceVar(String sin, RELConceptRelation rin) {
		String sout;
		sout = sin;

		if (rin.label.equals("")) {
			sout = sout.replaceAll("var:", "");

			return sout;
		}

		int n = sout.indexOf("var:");
		int last = n;
		int first = n;
		char ch;
		ch = '1';

		while (n != -1) {
			last = n;
			first = last;
			ch = '1';

			while ((last < sout.length()) && (ch != ' ')) {
				ch = sout.charAt(last);
				last = last + 1;
			}

			if (last < sout.length()) {
				last = last - 1;
			}

			String subs = sout.substring(first, last);
			int m = rin.label.indexOf("%");
			String sProcent;

			if (m != -1) {
				sProcent = rin.label.replaceFirst("%", "");

				int intProcent = Integer.parseInt(sProcent);
				double doubleProcent = ((double) intProcent / 100);
				DecimalFormat df = new DecimalFormat();
				df.setMaximumFractionDigits(2);

				String test = df.format(doubleProcent);
				sout = sout.replaceFirst(subs, test);
				n = sout.indexOf("var:");
			} else {
				sout = sout.replaceFirst(subs, rin.label);
				n = sout.indexOf("var:");
			}
		}

		return sout;
	}

	/**
	 * ReplaceKeyword change source,destination,parent, child with the correct
	 * concept.
	 */
	public String ReplaceKeyword(String sin, RELConceptRelation rin) {
		String sout;
		sout = sin;
		sout = sout.replaceAll("___destination", rin.destinationConceptName);
		sout = sout.replaceAll("___source", rin.sourceConceptName);
		sout = sout.replaceAll("___parent", rin.destinationConceptName);
		sout = sout.replaceAll("___child", rin.sourceConceptName);

		return sout;
	}

	/**
	 * checkgeneratelistitem rerturn true if the requirements allready exsists.
	 */
	public boolean CheckGenerateListItem(String conceptName,
			String attributeName, String requirement) {
		boolean result = false;

		for (Iterator i = AuthorSTATIC.ahaOut.conceptList.iterator(); i
				.hasNext();) {
			AHAOutConcept acon = (AHAOutConcept) i.next();

			if (acon.name.equals(conceptName)) {
				for (Iterator j = acon.attributeList.iterator(); j.hasNext();) {
					AHAOutAttribute attr = (AHAOutAttribute) j.next();

					if (attr.name.equals(attributeName)) {
						for (Iterator k = attr.generateListItemList.iterator(); k
								.hasNext();) {
							CRTGenerateListItem glItem = (CRTGenerateListItem) k
									.next();

							if (glItem.requirement.equals(requirement)) {
								result = true;
							}
						}
					}
				}
			}
		}

		return result;
	}

	public void AddGenereateListItemToAttributeConcept(String conceptName,
			String attributeName, CRTGenerateListItem glout) {
		for (Iterator i = AuthorSTATIC.ahaOut.conceptList.iterator(); i
				.hasNext();) {
			AHAOutConcept acon = (AHAOutConcept) i.next();

			if (acon.name.equals(conceptName)) {
				// found conceptName
				for (Iterator j = acon.attributeList.iterator(); j.hasNext();) {
					AHAOutAttribute outat = (AHAOutAttribute) j.next();

					if (outat.name.equals(attributeName)) {
						outat.generateListItemList.add(glout);
					}
				}
			}
		}
	}

	/**
	 * AddActionToAttributeConcept adds an action to the output concept.
	 */
	public void AddActionToAttributeConcept(String conceptName,
			String attributeName, String actionPlace, CRTAction outAction) {
		for (Iterator i = AuthorSTATIC.ahaOut.conceptList.iterator(); i
				.hasNext();) {
			AHAOutConcept acon = (AHAOutConcept) i.next();

			if (acon.name.equals(conceptName)) {
				// found conceptName
				for (Iterator j = acon.attributeList.iterator(); j.hasNext();) {
					AHAOutAttribute outat = (AHAOutAttribute) j.next();

					if (outat.name.equals(attributeName)) {
						for (Iterator k = outat.generateListItemList.iterator(); k
								.hasNext();) {
							CRTGenerateListItem gList = (CRTGenerateListItem) k
									.next();

							if (actionPlace.equals("trueActions")) {
								gList.trueActions.actionList.add(outAction);
							} else {
								gList.falseActions.actionList.add(outAction);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * ProcessHashtableGL reads the concepts out of the hashtable.
	 */
	public void ProcessHashtableGL(Hashtable hashIn,
			CRTGenerateListItem genlistIn, String attributeName,
			String actionPlace, CRTAction origAction) {
		for (Iterator i = hashIn.entrySet().iterator(); i.hasNext();) {
			Map.Entry m = (Map.Entry) i.next();
			String sValue = (String) m.getValue();
			String sKey = "";
			CRTAction outAction = new CRTAction();
			CRTGenerateListItem glout = new CRTGenerateListItem();

			if (sValue.equals("") == false) {
				sValue = sValue + ")";
				sKey = (String) m.getKey();

				if (this.CheckConceptNameExist(sKey) == false) {
					// concept does not exists
					AHAOutAttribute atout = new AHAOutAttribute();
					atout.name = attributeName; // oppassen met suitability
					glout.location = genlistIn.location;
					glout.propagating = genlistIn.propagating;
					glout.requirement = genlistIn.requirement;
					outAction.attributeName = origAction.attributeName;
					outAction.conceptName = origAction.conceptName;
					outAction.expression = sValue;

					if (actionPlace.equals("trueActions")) {
						glout.trueActions.actionList.add(outAction);
					} else {
						glout.falseActions.actionList.add(outAction);
					}

					atout.generateListItemList.add(glout);

					AHAOutConcept outc = new AHAOutConcept();
					outc.name = sKey;
					outc.attributeList.add(atout);
					AuthorSTATIC.ahaOut.conceptList.add(outc);
				} else {
					// concepts does exists
					if (this.CheckAttributeName(sKey, attributeName) == false) {
						// new attribute
						AHAOutAttribute atout = new AHAOutAttribute();
						atout.name = attributeName;
						glout.location = genlistIn.location;
						glout.propagating = genlistIn.propagating;
						glout.requirement = genlistIn.requirement;
						outAction.attributeName = origAction.attributeName;
						outAction.conceptName = origAction.conceptName;
						outAction.expression = sValue;

						if (actionPlace.equals("trueActions")) {
							glout.trueActions.actionList.add(outAction);
						} else {
							glout.falseActions.actionList.add(outAction);
						}

						atout.generateListItemList.add(glout);
						this.AddAttributeToConcept(sKey, atout);
					} else {
						// attribute exists
						if (this.CheckGenerateListItem(sKey, attributeName,
								genlistIn.requirement) == false) {
							glout.location = genlistIn.location;
							glout.propagating = genlistIn.propagating;
							glout.requirement = genlistIn.requirement;
							outAction.attributeName = origAction.attributeName;
							outAction.conceptName = origAction.conceptName;
							outAction.expression = sValue;

							if (actionPlace.equals("trueActions")) {
								glout.trueActions.actionList.add(outAction);
							} else {
								glout.falseActions.actionList.add(outAction);
							}

							this.AddGenereateListItemToAttributeConcept(sKey,
									attributeName, glout);
						} else {
							outAction.attributeName = origAction.attributeName;
							outAction.conceptName = origAction.conceptName;
							outAction.expression = sValue;
							this.AddActionToAttributeConcept(sKey,
									attributeName, actionPlace, outAction);
						}
					}
				}
			}
		}
	}

	/**
	 * ProceessActioniNone reads the action, change all keywords and give them
	 * to the output functions.
	 */
	public void ProcessActionNone(LinkedList crelGroupIn, CRTAction actionIn,
			String location, CRTGenerateListItem genIn, String actionPlace) {
		for (Iterator i = crelGroupIn.iterator(); i.hasNext();) {
			RELConceptRelation crel = (RELConceptRelation) i.next();

			CRTGenerateListItem glout = new CRTGenerateListItem();
			AHAOutAttribute atout = new AHAOutAttribute();
			CRTAction outAction = new CRTAction();
			String sKey = this.ReplaceKeyword(location, crel);

			int n = sKey.lastIndexOf(".");
			String glConceptName = sKey.substring(0, n);
			String glAttributeName = sKey.substring(n + 1);

			if (this.CheckConceptNameExist(glConceptName) == false) {
				// concept does not exists
				atout.name = glAttributeName;
				glout.location = genIn.location;
				glout.propagating = genIn.propagating;

				glout.requirement = this
						.ReplaceKeyword(genIn.requirement, crel);
				glout.requirement = this.ReplaceVar(glout.requirement, crel);
				outAction.attributeName = this.ReplaceKeyword(
						actionIn.attributeName, crel);
				outAction.conceptName = this.ReplaceKeyword(
						actionIn.conceptName, crel);

				outAction.expression = this.ReplaceVar(
						this.ReplaceKeyword(actionIn.expression, crel), crel);

				if (actionPlace.equals("trueActions")) {
					glout.trueActions.actionList.add(outAction);
				} else {
					glout.falseActions.actionList.add(outAction);
				}

				atout.generateListItemList.add(glout);

				AHAOutConcept outc = new AHAOutConcept();
				outc.name = glConceptName;
				outc.attributeList.add(atout);
				AuthorSTATIC.ahaOut.conceptList.add(outc);
			} else {
				if (this.CheckAttributeName(glConceptName, glAttributeName) == false) {
					// new attribute
					atout = new AHAOutAttribute();
					atout.name = glAttributeName;
					glout.location = genIn.location;
					glout.propagating = genIn.propagating;

					glout.requirement = this.ReplaceKeyword(genIn.requirement,
							crel);
					glout.requirement = this
							.ReplaceVar(glout.requirement, crel);
					outAction.attributeName = this.ReplaceKeyword(
							actionIn.attributeName, crel);
					outAction.conceptName = this.ReplaceKeyword(
							actionIn.conceptName, crel);
					outAction.expression = this.ReplaceVar(
							this.ReplaceKeyword(actionIn.expression, crel),
							crel);

					if (actionPlace.equals("trueActions")) {
						glout.trueActions.actionList.add(outAction);
					} else {
						glout.falseActions.actionList.add(outAction);
					}

					atout.generateListItemList.add(glout);
					this.AddAttributeToConcept(glConceptName, atout);
				} else {
					// attribute exists
					if (this.CheckGenerateListItem(glConceptName,
							glAttributeName, genIn.requirement) == false) {
						glout.location = genIn.location;
						glout.propagating = genIn.propagating;

						// glout.requirement = genIn.requirement;
						glout.requirement = this.ReplaceKeyword(
								genIn.requirement, crel);
						glout.requirement = this.ReplaceVar(glout.requirement,
								crel);
						outAction.attributeName = this.ReplaceKeyword(
								actionIn.attributeName, crel);
						outAction.conceptName = this.ReplaceKeyword(
								actionIn.conceptName, crel);
						outAction.expression = this.ReplaceVar(
								this.ReplaceKeyword(actionIn.expression, crel),
								crel);

						if (actionPlace.equals("trueActions")) {
							glout.trueActions.actionList.add(outAction);
						} else {
							glout.falseActions.actionList.add(outAction);
						}

						this.AddGenereateListItemToAttributeConcept(
								glConceptName, glAttributeName, glout);
					} else {
						outAction.attributeName = this.ReplaceKeyword(
								actionIn.attributeName, crel);
						outAction.conceptName = this.ReplaceKeyword(
								actionIn.conceptName, crel);
						outAction.expression = this.ReplaceVar(
								this.ReplaceKeyword(actionIn.expression, crel),
								crel);
						this.AddActionToAttributeConcept(glConceptName,
								glAttributeName, actionPlace, outAction);
					}
				}
			}
		}
	}

	/**
	 * ProcessAction replaces the keywords.
	 */
	public void ProcessAction(LinkedList crelGroupIn, CRTAction actionIn,
			String location, CRTGenerateListItem genIn, String actionPlace) {
		Hashtable hashCT = new Hashtable();
		FillHashtable(hashCT, crelGroupIn);

		RELConceptRelation drel;
		String actionExp = "";

		int n = location.lastIndexOf(".");
		String glConceptName = location.substring(0, n);
		String glAttributeName = location.substring(n + 1);
		String combination = actionIn.combination;

		for (Iterator i = crelGroupIn.iterator(); i.hasNext();) {
			drel = (RELConceptRelation) i.next();

			String hashValue = "";
			actionExp = this.ReplaceVar(
					ReplaceKeyword(actionIn.expression, drel), drel);

			if (glConceptName.equals("___destination")) {
				hashValue = (String) hashCT.get(drel.destinationConceptName);
			} else {
				hashValue = (String) hashCT.get(drel.sourceConceptName);
			}

			if (hashValue.equals("")) {
				// first value
				hashValue = "((" + actionExp + ")";
			} else {
				// combination
				if (combination.equals("AND")) {
					hashValue = hashValue + "&& (" + actionExp + ")";
				}

				if (combination.equals("OR")) {
					hashValue = hashValue + "|| (" + actionExp + ")";
				}
			}

			if (glConceptName.equals("___destination")) {
				hashCT.remove(drel.destinationConceptName);
				hashCT.put(drel.destinationConceptName, hashValue);
			}

			if (glConceptName.equals("___source")) {
				hashCT.remove(drel.sourceConceptName);
				hashCT.put(drel.sourceConceptName, hashValue);
			}
		}

		this.ProcessHashtableGL(hashCT, genIn, glAttributeName, actionPlace,
				actionIn);
	}

	/**
	 * FindDivideS retrieves the divide information.
	 */
	public int FindDivideS(LinkedList crelgroup, String crt, String conceptName) {
		for (Iterator k = crelgroup.iterator(); k.hasNext();) {
			RELConceptRelation relConcept = (RELConceptRelation) k.next();

			if (relConcept.sourceConceptName.equals(conceptName)) {
				LinkedList llInfo = (LinkedList) AuthorSTATIC.relaha.divideInformation
						.get(relConcept.destinationConceptName);

				for (Iterator i = llInfo.iterator(); i.hasNext();) {
					ConceptDivideInfo dInfo = (ConceptDivideInfo) i.next();

					if (crt.equals(dInfo.crtName)) {
						return dInfo.destination;
					} else if (crt.equals("tree")
							&& dInfo.crtName.equals("tree")) {
						return dInfo.destination;
					}
				}
			}
		}

		return 0;
	}

	/**
	 * ReplaceDiv_S replaces the keyword into the correct number.
	 */
	public void ReplaceDIV_S(LinkedList crelGroupIn, String location,
			String actionPlace) {
		for (Iterator i = crelGroupIn.iterator(); i.hasNext();) {
			RELConceptRelation crel = (RELConceptRelation) i.next();
			double value = 0;
			int number = -1;

			for (Iterator j = AuthorSTATIC.ahaOut.conceptList.iterator(); j
					.hasNext();) {
				AHAOutConcept outConcept = (AHAOutConcept) j.next();

				for (Iterator k = outConcept.attributeList.iterator(); k
						.hasNext();) {
					number = this.FindDivideS(crelGroupIn, crel.relationType,
							outConcept.name);

					AHAOutAttribute outAtt = (AHAOutAttribute) k.next();

					for (Iterator l = outAtt.generateListItemList.iterator(); l
							.hasNext();) {
						CRTGenerateListItem glOut = (CRTGenerateListItem) l
								.next();

						if (number != 0) {
							value = 1 / (double) number;
						}

						for (Iterator m = glOut.trueActions.actionList
								.iterator(); m.hasNext();) {
							CRTAction aOut = (CRTAction) m.next();

							aOut.expression = aOut.expression.replaceAll(
									"DIVIDE", Double.toString(value));
						} // close for m
					} // close for l

					for (Iterator l = outAtt.generateListItemList.iterator(); l
							.hasNext();) {
						CRTGenerateListItem glOut = (CRTGenerateListItem) l
								.next();

						if (number != 0) {
							value = 1 / number;
						}

						for (Iterator m = glOut.trueActions.actionList
								.iterator(); m.hasNext();) {
							CRTAction aOut = (CRTAction) m.next();
							aOut.expression = aOut.expression.replaceAll(
									"DIVIDE", Double.toString(value));
						} // close for m
					} // close for l
				}
			} // close for k
		}
	}

	/**
	 * ReplaceDiv_D not used anymore.
	 */
	public void ReplaceDIV_D(LinkedList crelGroupIn, String location,
			String actionPlace) {

		for (Iterator i = crelGroupIn.iterator(); i.hasNext();) {
			RELConceptRelation crel = (RELConceptRelation) i.next();
			String sKey = this.ReplaceKeyword(location, crel);
			int n = sKey.lastIndexOf(".");
			String glConceptName = sKey.substring(0, n);
			String glAttributeName = sKey.substring(n + 1);
			double value = 0;

			for (Iterator j = AuthorSTATIC.ahaOut.conceptList.iterator(); j
					.hasNext();) {
				AHAOutConcept outConcept = (AHAOutConcept) j.next();

				if (outConcept.name.equals(glConceptName)) {
					for (Iterator k = outConcept.attributeList.iterator(); k
							.hasNext();) {
						AHAOutAttribute outAtt = (AHAOutAttribute) k.next();

						if (outAtt.name.equals(glAttributeName)) {
							for (Iterator l = outAtt.generateListItemList
									.iterator(); l.hasNext();) {
								CRTGenerateListItem glOut = (CRTGenerateListItem) l
										.next();
								int number = glOut.trueActions.actionList
										.size();

								if (number != 0) {
									value = 1 / (double) number;
								}

								for (Iterator m = glOut.trueActions.actionList
										.iterator(); m.hasNext();) {
									CRTAction aOut = (CRTAction) m.next();
									aOut.expression = aOut.expression
											.replaceAll("DIVIDE",
													Double.toString(value));
								} // close for m
							} // close for l

							for (Iterator l = outAtt.generateListItemList
									.iterator(); l.hasNext();) {
								CRTGenerateListItem glOut = (CRTGenerateListItem) l
										.next();
								int number = glOut.falseActions.actionList
										.size();

								if (number != 0) {
									value = 1 / number;
								}

								for (Iterator m = glOut.trueActions.actionList
										.iterator(); m.hasNext();) {
									CRTAction aOut = (CRTAction) m.next();
									aOut.expression = aOut.expression
											.replaceAll("DIVIDE",
													Double.toString(value));
								} // close for m
							} // close for l
						} // close if
					} // close for k
				}
			}
		}
	}

	/**
	 * ProcessGenerateListItem addes the generatelist item from the crt to the
	 * output concepts.
	 */
	public void ProcessGenerateListItem(LinkedList crelGroupIn,
			CRTGenerateListItem glistIn) {
		RELConceptRelation drel = (RELConceptRelation) crelGroupIn.getFirst();

		String location = glistIn.location;
		CRTGenerateListItem outGenList = new CRTGenerateListItem();

		for (Iterator i = glistIn.trueActions.actionList.iterator(); i
				.hasNext();) {
			CRTAction actionIn = (CRTAction) i.next();
			String actionPlace = "trueActions";

			if (actionIn.combination.equals("NONE")) {
				this.ProcessActionNone(crelGroupIn, actionIn, location,
						glistIn, actionPlace);
			} else if (actionIn.combination.equals("DIV_D")) {
				this.ProcessActionNone(crelGroupIn, actionIn, location,
						glistIn, actionPlace);
				this.ReplaceDIV_D(crelGroupIn, location, actionPlace);
			} else if (actionIn.combination.equals("DIV_S")) {
				this.ProcessActionNone(crelGroupIn, actionIn, location,
						glistIn, actionPlace);

				this.ReplaceDIV_S(crelGroupIn, location, actionPlace);
			} else {
				this.ProcessAction(crelGroupIn, actionIn, location, glistIn,
						actionPlace);
			}
		}

		for (Iterator i = outGenList.falseActions.actionList.iterator(); i
				.hasNext();) {
			CRTAction actionIn = (CRTAction) i.next();
			String actionPlace = "falseActions";

			if (actionIn.combination.equals("NONE")) {
				this.ProcessActionNone(crelGroupIn, actionIn, location,
						glistIn, actionPlace);
			} else {
				this.ProcessAction(crelGroupIn, actionIn, location, glistIn,
						actionPlace);
			}
		}
	}

	/**
	 * FillHashtable: Fills the hashtable with all the concepts used in
	 * crelGroupIn.
	 */
	public void FillHashtable(Hashtable hashIn, LinkedList crelGroupIn) {
		// fills the hashIn table with all the concepts used in crelGroupIn
		RELConceptRelation drel;
		String hashKey;

		for (Iterator i = crelGroupIn.iterator(); i.hasNext();) {
			drel = (RELConceptRelation) i.next();
			hashKey = drel.destinationConceptName;

			if (hashIn.containsKey(hashKey) == false) {
				hashIn.put(hashKey, "");
			}

			hashKey = drel.sourceConceptName;

			if (hashIn.containsKey(hashKey) == false) {
				hashIn.put(hashKey, "");
			}
		}
	}

	/**
	 * ProcessHashtable all concepts in hashtable
	 */
	public void ProcessHashtable(Hashtable hashIn, String attributeName) {
		for (Iterator i = hashIn.entrySet().iterator(); i.hasNext();) {
			Map.Entry m = (Map.Entry) i.next();
			String sValue = (String) m.getValue();
			String sKey = "";
			CRTSetDefault outDefault = new CRTSetDefault();

			if (sValue.equals("") == false) {
				sValue = sValue + ")";
				sKey = (String) m.getKey();

				if (this.CheckConceptNameExist(sKey) == false) {
					// concept does not exists
					AHAOutAttribute atout = new AHAOutAttribute();
					atout.name = attributeName; // oppassen met suitability
					outDefault.setdefault = sValue;
					atout.setDefaultList = outDefault;

					AHAOutConcept outc = new AHAOutConcept();
					outc.name = sKey;
					outc.attributeList.add(atout);
					AuthorSTATIC.ahaOut.conceptList.add(outc);
				} else {
					// concepts does exists
					if (this.CheckAttributeName(sKey, attributeName) == false) {
						// new attribute
						AHAOutAttribute atout = new AHAOutAttribute();
						atout.name = attributeName;
						outDefault.setdefault = sValue;
						atout.setDefaultList = outDefault;
						this.AddAttributeToConcept(sKey, atout);
					} else {
						// attribute exists
						this.AddAttributeToConceptAttribute(sKey,
								attributeName, sValue);
					}
				}
			}
		}
	}

	/**
	 * ProcessSetDefault start function to set the default values.
	 */
	public void ProcessSetDefault(LinkedList crelGroupIn,
			CRTSetDefault setDefault) {
		Hashtable hashCT = new Hashtable();
		FillHashtable(hashCT, crelGroupIn);

		RELConceptRelation drel = (RELConceptRelation) crelGroupIn.getFirst();

		// set attributeName and conceptName
		String location = setDefault.location;
		int n = location.lastIndexOf(".");
		String defConceptName = location.substring(0, n);

		String defAttributeName = location.substring(n + 1);

		String combination = setDefault.combination;
		CRTSetDefault outDefault = new CRTSetDefault();
		String tdefault;

		for (Iterator i = crelGroupIn.iterator(); i.hasNext();) {
			drel = (RELConceptRelation) i.next();

			String hashValue = "";
			tdefault = this.ReplaceVar(
					ReplaceKeyword(setDefault.setdefault, drel), drel);

			if (defConceptName.equals("___destination")) {
				hashValue = ((String) hashCT.get(drel.destinationConceptName))
						.trim();
			}

			if (defConceptName.equals("___parent")) {
				hashValue = ((String) hashCT.get(drel.destinationConceptName))
						.trim();
			}

			if (defConceptName.equals("___source")) {
				hashValue = ((String) hashCT.get(drel.sourceConceptName))
						.trim();
			}

			if (defConceptName.equals("___child")) {
				hashValue = ((String) hashCT.get(drel.sourceConceptName))
						.trim();
			}

			// combination check
			if (hashValue.equals("")) {
				// first value
				hashValue = "((" + tdefault + ")";
			} else {
				// combination
				if (combination.equals("AND")) {
					hashValue = hashValue + "&& (" + tdefault + ")";
				}

				if (combination.equals("OR")) {
					hashValue = hashValue + "|| (" + tdefault + ")";
				}
			}

			if (defConceptName.equals("___destination")) {
				hashCT.remove(drel.destinationConceptName);
				hashCT.put(drel.destinationConceptName, hashValue);
			}

			if (defConceptName.equals("___parent")) {
				hashCT.remove(drel.destinationConceptName);
				hashCT.put(drel.destinationConceptName, hashValue);
			}

			if (defConceptName.equals("___source")) {
				hashCT.remove(drel.sourceConceptName);
				hashCT.put(drel.sourceConceptName, hashValue);
			}

			if (defConceptName.equals("___child")) {
				hashCT.remove(drel.sourceConceptName);
				hashCT.put(drel.sourceConceptName, hashValue);
			}
		}

		ProcessHashtable(hashCT, defAttributeName);
	}

	/**
	 * processConceptRel main function to process conceptrels of a certain type.
	 */
	public void ProcessConceptRel(LinkedList crelGroupIn) {
		creltype = null;

		String conceptname = "";
		String attributename = "";
		CRTConceptRelationType dummie = null;
		RELConceptRelation cdummie = null;

		// creltype to the correct CRT
		for (Iterator i = AuthorSTATIC.CRTList.iterator(); i.hasNext();) {
			dummie = (CRTConceptRelationType) i.next();
			cdummie = (RELConceptRelation) crelGroupIn.getFirst();

			if (dummie.name.equals(cdummie.relationType)) {
				creltype = dummie;
			}
		}

		if (cdummie.relationType.trim().equals("unary")) {
			creltype = dummie;
		}

		dummie = AuthorSTATIC.trel;
		cdummie = (RELConceptRelation) crelGroupIn.getFirst();

		if (cdummie.relationType.trim().equals("tree")) {
			creltype = dummie;
		}

		// get generatelistitem from crt
		if (creltype == null) {
			String error = "Error: The concept relation "
					+ cdummie.relationType + " does not exist!";
			JOptionPane.showConfirmDialog(null, error, "error",
					JOptionPane.ERROR_MESSAGE);
		}

		// process the setDefaults
		for (Iterator i = creltype.listItem.setDefaultList.iterator(); i
				.hasNext();) {
			this.ProcessSetDefault(crelGroupIn, (CRTSetDefault) i.next());
		}

		for (Iterator i = creltype.listItem.generateListItemList.iterator(); i
				.hasNext();) {
			// processGenerateListItem
			this.ProcessGenerateListItem(crelGroupIn,
					(CRTGenerateListItem) i.next());
		}
	}

	public void AddDivideInfo() {
	}

	public static void main(String[] args) {
		AuthorToAha authorToAha1 = new AuthorToAha();
	}
}