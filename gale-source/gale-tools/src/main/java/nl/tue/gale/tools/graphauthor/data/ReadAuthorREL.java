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
 * ReadAuthorREL.java
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

import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * ReadAurhorREL: Reads the concept realtions into memory.
 * 
 */
public class ReadAuthorREL {
	public URL home;
	public String projectN;
	public static final int TEXT_TYPE = 3;

	public ReadAuthorREL(URL base, String fileName, String projectName) {
		projectN = projectName;

		AuthorSTATIC.relaha = new RELConceptRelations();
		AuthorSTATIC.conceptInfoList = new LinkedList();
		home = base;

		this.ReadFromFile(fileName);
		// listConceptAndAttributes();
	}

	public void ReadConceptRelation(Node n) {
		Node nod = n;
		RELConceptRelation crel = new RELConceptRelation();
		nod = nod.getFirstChild();

		while (nod != null) {
			if (nod.getNodeName().equals("source_concept_name")) {
				try {
					crel.sourceConceptName = nod.getFirstChild().getNodeValue()
							.trim();
				} catch (Exception e) {
					crel.sourceConceptName = "";
				}
			}

			if (nod.getNodeName().equals("destination_concept_name")) {
				try {
					crel.destinationConceptName = nod.getFirstChild()
							.getNodeValue().trim();
				} catch (Exception e) {
					crel.destinationConceptName = "";
				}
			}

			if (nod.getNodeName().equals("relation_type")) {
				try {
					crel.relationType = nod.getFirstChild().getNodeValue()
							.trim();
				} catch (Exception e) {
					crel.relationType = "";
				}
			}

			if (nod.getNodeName().equals("relation_label")) {
				crel.label = "";
				try {
					crel.label = nod.getFirstChild().getNodeValue().trim();
				} catch (Exception e) {
				}
			}

			nod = nod.getNextSibling();
		}

		// add the projectName in front of the conceptnames:
		crel.destinationConceptName = projectN + "."
				+ crel.destinationConceptName;
		crel.sourceConceptName = projectN + "." + crel.sourceConceptName;
		crel.printRelation();
		AuthorSTATIC.relaha.conceptRelationList.add(crel);
	}

	public void ReadConceptInformation(Node n) {
		n = n.getFirstChild();

		while (n != null) {
			if (n.getNodeName().toString().compareTo("concept_info") == 0) {
				this.ReadConceptInfo(n);
			}

			n = n.getNextSibling();
		}
	}

	/**
	 * Reads the concept_info node from the gaf file and creates an concept
	 * 
	 * @param n
	 *            ConceptInfo node Changed by @Bart @ 03-04-2002
	 */
	public void ReadConceptInfo(Node n) {
		AHAOutAttribute attr = null;
		AHAOutConcept ain = new AHAOutConcept();
		Vector cv = new Vector();
		Case caseValue = null;
		n = n.getFirstChild();

		String conceptName = "";
		String conceptDescription = "";
		String conceptResource = "";
		String conceptTemplate = "";
		// added by @David @21-05-2004
		String conceptType = "";
		String conceptTitle = "";
		// end added by @David @21-05-2004
		String defaultFragment = "";
		// added by @Bart @ 29-04-2003
		boolean nocommit = false;
		// end added by @Bart @ 29-04-2003
		// added by @Bart @ 10-06-2003
		String conceptStable = "";
		String conceptStable_expr = "";
		// end added by @Bart @ 10-06-2003

		try {
			while (n != null) {
				if (n.getNodeName().equals("concept_name")) {
					conceptName = n.getFirstChild().getNodeValue().trim();
				} else if (n.getNodeName().equals("concept_description")) {
					try {
						conceptDescription = n.getFirstChild().getNodeValue()
								.trim();
					} catch (Exception e) {
						conceptDescription = "";
					}
				} else if (n.getNodeName().equals("concept_resource")) {
					try {
						conceptResource = n.getFirstChild().getNodeValue();
					} catch (Exception e) {
						conceptResource = "";
					}
				} else if (n.getNodeName().equals("concept_template")) {
					try {
						conceptTemplate = n.getFirstChild().getNodeValue()
								.trim();
					} catch (Exception e) {
						conceptTemplate = "";
					}
				}
				// added by @David @21-05-2004
				else if (n.getNodeName().equals("concept_type")) {
					try {
						conceptType = n.getFirstChild().getNodeValue().trim();
					} catch (Exception e) {
						conceptType = "";
					}
				} else if (n.getNodeName().equals("concept_title")) {
					try {
						conceptTitle = n.getFirstChild().getNodeValue().trim();
					} catch (Exception e) {
						conceptTitle = "";
					}
				}
				// end added by @David @21-05-2004
				// added by @Bart @ 29-04-2003
				else if (n.getNodeName().equals("concept_nocommit")) {
					try {
						String tempNoCommit = n.getFirstChild().getNodeValue()
								.trim();
						nocommit = Boolean.getBoolean(tempNoCommit);
					} catch (Exception e) {
						nocommit = false;
					}
				}
				// end added by @Bart @ 29-04-2003
				// added by @Bart @ 10-06-2003
				else if (n.getNodeName().equals("concept_stable")) {
					try {
						conceptStable = n.getFirstChild().getNodeValue().trim();
					} catch (Exception e) {
						conceptStable = "";
					}
				} else if (n.getNodeName().equals("concept_stable_expr")) {
					try {
						conceptStable_expr = n.getFirstChild().getNodeValue()
								.trim();
					} catch (Exception e) {
						conceptStable_expr = "";
					}
				}
				// end added by @Bart @ 10-06-2003

				else if (n.getNodeName().equals("returnfragment_information")) {
					NodeList nodeList = n.getChildNodes();
					String s = new String();

					for (int i = 0; i < nodeList.getLength(); i++) {
						Node childNode = nodeList.item(i);
						if (childNode.getNodeName().equals("defaultfragment")) {
							try {
								defaultFragment = childNode.getFirstChild()
										.getNodeValue().trim();
							} catch (Exception e) {
								defaultFragment = "";
							}
						}
						if (childNode.getNodeName().equals("case_info")) {
							caseValue = ReadCaseInfo(childNode);
							cv.add(caseValue);
						}
					}
				} else if (n.getNodeName().equals("attribute_information")) {
					NodeList nodeList = n.getChildNodes();
					String s = new String();

					for (int i = 0; i < nodeList.getLength(); i++) {
						Node childNode = nodeList.item(i);
						if (childNode.getNodeName().equals("attribute_info")) {
							attr = ReadAttributeInfo(childNode);
							ain.attributeList.add(attr);
						}
					}
				}
				n = n.getNextSibling();
			}
		} catch (Exception e) {
			System.out
					.println("ReadAuthorREL: ReadConceptInfo: error reading extra information");
			System.out.println("Exception is: " + e.getMessage());
		}

		// find showability attribute
		for (Iterator i = ain.attributeList.iterator(); i.hasNext();) {
			AHAOutAttribute tempAttr = (AHAOutAttribute) i.next();
			if (tempAttr.name.equals("showability")) {
				// add the casegroup
				Vector tempCV = null;
				Case tempCase = null;
				if (tempAttr.casegroup == null) {
					tempAttr.casegroup = new CaseGroup();
				}
				tempAttr.casegroup.setDefaultFragment(defaultFragment);
				tempCV = tempAttr.casegroup.getCaseValues();
				// loop the stored casevalues and add these to this vector
				for (int j = 0; j < cv.size(); j++) {
					tempCase = (Case) cv.get(j);
					tempCV.add(tempCase);
				}
			}
		}

		ain.name = this.projectN + "." + conceptName.trim();
		ain.description = conceptDescription.trim();
		ain.resource = conceptResource.trim();
		ain.template = conceptTemplate.trim();
		// added by @David @18-05-2004
		ain.concepttype = conceptType.trim();
		ain.title = conceptTitle.trim();
		// end added by @David @18-05-2004
		// added by @Bart @ 29-04-2003
		ain.nocommit = nocommit;
		// end added by @Bart @ 29-04-2003
		// added by @Bart @ 10-06-2003
		ain.stable = conceptStable;
		ain.stable_expr = conceptStable_expr;
		// end added by @Bart @ 10-06-2003

		AuthorSTATIC.conceptInfoList.add(ain);
	}

	/**
	 * Reads the attribute information of a concept from the gaf file and
	 * returns an attribute object with the information.
	 * 
	 * @param n
	 * @return attr Added by @Bart @ 03-04-2003
	 */
	public AHAOutAttribute ReadAttributeInfo(Node n) {
		AHAOutAttribute attr = new AHAOutAttribute();
		Node tempNode = null;
		NodeList childNodes = null;
		childNodes = n.getChildNodes();

		try {
			for (int i = 0; i < childNodes.getLength(); i++) {
				tempNode = childNodes.item(i);

				if (tempNode.getNodeName().equals("attribute_name")) {
					attr.name = tempNode.getFirstChild().getNodeValue().trim();
				} else if (tempNode.getNodeName().equals(
						"attribute_description")) {
					try {
						attr.description = tempNode.getFirstChild()
								.getNodeValue().trim();
					} catch (Exception e) {
						attr.description = "";
					}
				} else if (tempNode.getNodeName().equals("attribute_type")) {
					try {
						attr.type = tempNode.getFirstChild().getNodeValue()
								.trim();
					} catch (Exception e) {
						attr.type = "";
					}
				} else if (tempNode.getNodeName().equals("attribute_default")) {
					attr.setDefaultList = new CRTSetDefault();
					try {
						attr.setDefaultList.setdefault = tempNode
								.getFirstChild().getNodeValue();
					} catch (Exception e) {
						attr.setDefaultList.setdefault = "";
					}
				} else if (tempNode.getNodeName().equals("attribute_isSystem")) {
					try {
						attr.isSystem = Boolean.valueOf(tempNode
								.getFirstChild().getNodeValue());
					} catch (Exception e) {
						attr.isSystem = Boolean.FALSE;
					}
				} else if (tempNode.getNodeName().equals(
						"attribute_isPersistent")) {
					try {
						attr.isPersistent = Boolean.valueOf(tempNode
								.getFirstChild().getNodeValue());
					} catch (Exception e) {
						attr.isPersistent = Boolean.FALSE;
					}
				} else if (tempNode.getNodeName().equals(
						"attribute_isChangeable")) {
					try {
						attr.isChangeable = Boolean.valueOf(tempNode
								.getFirstChild().getNodeValue());
					} catch (Exception e) {
						attr.isChangeable = Boolean.FALSE;
					}
				} else if (tempNode.getNodeName().equals("attribute_Stable")) {
					try {
						attr.stable = tempNode.getFirstChild().getNodeValue()
								.trim();
					} catch (Exception e) {
						attr.stable = "";
					}
				} else if (tempNode.getNodeName().equals(
						"attribute_Stable_Expr")) {
					try {
						attr.stable_expr = tempNode.getFirstChild()
								.getNodeValue().trim();
					} catch (Exception e) {
						attr.stable_expr = "";
					}
				}

			}
		} catch (Exception e) {
			System.out
					.println("ReadAuthorREL: ReadAttributeInfo: error reading extra information");
		}

		// return the created attribute
		return attr;
	}

	/**
	 * Reads the case information of a return fragment of an attribute from the
	 * gaf file and returns an case object with the information.
	 * 
	 * @param n
	 *            case_info Node
	 * @return caseValue Added by @Bart @ 04-04-2003
	 */
	public Case ReadCaseInfo(Node n) {
		Case caseValue = new Case();
		Node tempNode = null;
		NodeList childNodes = null;
		try {
			childNodes = n.getChildNodes();
		} catch (Exception e) {
			System.out.println("ReadAuthorREL: ReadCaseInfo: exception: "
					+ e.toString());
		}

		try {
			for (int i = 0; i < childNodes.getLength(); i++) {
				tempNode = childNodes.item(i);
				if (tempNode.getNodeName().equals("case_expression")) {
					caseValue.setValue(tempNode.getFirstChild().getNodeValue()
							.trim());
				} else if (tempNode.getNodeName().equals("case_fragment")) {
					caseValue.setReturnfragment(tempNode.getFirstChild()
							.getNodeValue().trim());
				}

			}

		} catch (Exception e) {
			System.out
					.println("ReadAuthorREL: ReadCaseInfo: error reading extra information");
		}

		// return the created attribute
		return caseValue;
	}

	public void ReadConceptRelations(Node n) {
		Node nod = n;
		nod = nod.getFirstChild();

		while (nod != null) {
			if (nod.getNodeName().equals("concept_relation")) {
				this.ReadConceptRelation(nod);
			}

			nod = nod.getNextSibling();
		}
	}

	public void ReadViews(Node n) {
		// tree view implemented in graph author
	}

	public void ReadFromFile(String fileName) {
		String crtString;
		DOMParser p = new DOMParser();

		try {
			String path = home.getPath();
			String pathttemp = path.substring(1, path.length());
			int index = pathttemp.indexOf("/");
			index++;

			String dirname = path.substring(0, index);

			if (dirname.equals("/GraphAuthor")) {
				dirname = "";
			}

			URL url = new URL("http://" + home.getHost() + ":" + home.getPort()
					+ dirname + "/authorservlets/GetFile?fileName=" + fileName
					+ "&userName=" + AuthorSTATIC.authorName);
			p.parse(url.toString());
		} catch (Exception e) {
			System.out
					.println("ReadAuthorRel: ReadFromFile: Parse error! file: "
							+ fileName);
			e.printStackTrace();
		}

		Document doc = p.getDocument();
		Node n = doc.getDocumentElement();
		n = n.getFirstChild();
		while (n != null) {
			if (n.getNodeName().equals("concept_relations")) {
				this.ReadConceptRelations(n);
			}

			if (n.getNodeName().equals("concept_information")) {
				this.ReadConceptInformation(n);
			}

			n = n.getNextSibling();
		}
		// this.ReadViews(n); //no implementation yet
	}

	public static void printNode(Node node) {
		Node tempNode = null;
		String inline = "";

		tempNode = node;

		if (tempNode != null) {
			printRecNode(tempNode, inline);
		} else {
		}

	}

	public static void printRecNode(Node node, String inline) {
		Node tempNode = null;
		tempNode = node;
		NodeList list = null;

		if (tempNode != null) {
			if (tempNode.getNodeType() == TEXT_TYPE) {
			} else {
			}
			list = tempNode.getChildNodes();
			inline = inline + "    ";
			for (int i = 0; i < list.getLength(); i++) {
				if (list.item(i) != null) {
					printRecNode(list.item(i), inline);
				}
			}
		}
	}

	private void listConceptAndAttributes() {
		for (Iterator i = AuthorSTATIC.conceptInfoList.iterator(); i.hasNext();) {
			AHAOutConcept tempConcept = (AHAOutConcept) i.next();
			for (Iterator j = tempConcept.attributeList.iterator(); j.hasNext();) {
				AHAOutAttribute tempAttr = (AHAOutAttribute) j.next();
			}
		}
	}

}