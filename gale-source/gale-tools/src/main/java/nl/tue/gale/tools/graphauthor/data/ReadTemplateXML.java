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
 * ReadTemplateXML.java
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
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * ReadAttributeXML: Reads the attributeXML into memory.
 * 
 */
public class ReadTemplateXML {
	public URL home;

	public ReadTemplateXML(String filename, URL base) {
		String attString;
		AuthorSTATIC.templateList = new LinkedList();
		home = base;
		try {
			Hashtable reqinfo = new Hashtable();
			reqinfo.put("name", "authordir");
			reqinfo.put("dir", "templates");
			Hashtable resinfo = nl.tue.gale.tools.graphauthor.ui.GraphAuthor
					.getExecRequest(reqinfo, base);
			Vector filenames = (Vector) resinfo.get("files");
			if (filenames == null)
				return;
			for (int i = 0; i < filenames.size(); i++) {
				String fn = (String) filenames.get(i);
				if (fn.endsWith(".ct"))
					ReadFromFile("templates/" + fn);
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void ReadFromFile(String xmlfile) {
		DOMParser p = new DOMParser();

		try {
			p.setFeature("http://xml.org/sax/features/validation", true);
		} catch (SAXException e) {
			System.out.println("error in setting up parser feature");
		}

		// sets error handling
		error_handler eh = new error_handler();
		p.setErrorHandler(eh);

		try {
			String path = home.getPath();
			String pathttemp = path.substring(1, path.length());
			int index = pathttemp.indexOf("/");
			index++;

			String dirname = path.substring(0, index);

			if (dirname.equals("/graphAuthor")) {
				dirname = "";
			}

			URL url = new URL("http://" + home.getHost() + ":" + home.getPort()
					+ dirname + "/authorservlets/GetFile?fileName=" + xmlfile);

			p.parse(url.toString());
		} catch (Exception e) {
			System.out.println("url error");
		}

		ConceptTemplate cTemplate = new ConceptTemplate();

		Document doc = p.getDocument();
		Node node = doc.getDocumentElement();

		org.w3c.dom.Node tNode = node.getFirstChild();

		while (tNode != null) {
			if (tNode.getNodeName().equals("name")) {
				cTemplate.name = tNode.getFirstChild().getNodeValue();
			}

			if (tNode.getNodeName().equals("attributes")) {
				Node cNode = tNode.cloneNode(true);
				cNode = cNode.getFirstChild();

				while (cNode != null) {
					if (cNode.getNodeName().equals("attribute")) {
						AHAOutAttribute attribute = this.ReadAttribute(cNode);
						cTemplate.attributes.add(attribute);
					}

					cNode = cNode.getNextSibling();
				}
			}

			if (tNode.getNodeName().equals("hasresource")) {
				cTemplate.hasresource = tNode.getFirstChild().getNodeValue();
			}

			if (tNode.getNodeName().equals("concepttype")) {
				cTemplate.concepttype = tNode.getFirstChild().getNodeValue();
			}

			if (tNode.getNodeName().equals("conceptrelations")) {
				Node cNode = tNode.cloneNode(true);
				cNode = cNode.getFirstChild();

				while (cNode != null) {
					if (cNode.getNodeName().equals("conceptrelation")) {
						templateConceptRelation tcr = this
								.ReadConceptRelations(cNode);
						cTemplate.conceptRelations.put(tcr.name, tcr.label);
					}

					cNode = cNode.getNextSibling();
				}
			}

			tNode = tNode.getNextSibling();
		}

		AuthorSTATIC.templateList.add(cTemplate);
	}

	public templateConceptRelation ReadConceptRelations(Node n) {
		templateConceptRelation tcr = new templateConceptRelation();
		n = n.getFirstChild();

		while (n != null) {
			if (n.getNodeName().equals("name")) {
				tcr.name = n.getFirstChild().getNodeValue();
			}

			if (n.getNodeName().equals("label")) {
				try {
					tcr.label = n.getFirstChild().getNodeValue();
				} catch (Exception e) {
					tcr.label = "";
				}
			}

			n = n.getNextSibling();
		}

		return tcr;
	}

	public AHAOutAttribute ReadAttribute(Node n) {
		AHAOutAttribute att = new AHAOutAttribute();

		n = n.getFirstChild();

		while (n != null) {
			if (n.getNodeName().equals("name")) {
				att.name = n.getFirstChild().getNodeValue();
			}

			if (n.getNodeName().equals("type")) {
				att.type = n.getFirstChild().getNodeValue();
			}

			if (n.getNodeName().equals("description")) {
				try {
					att.description = n.getFirstChild().getNodeValue();
				} catch (Exception e) {
					att.description = "";
				}
			}

			// PDB: do not ignore default
			if (n.getNodeName().equals("default")) {
				att.setDefaultList = new CRTSetDefault();
				try {
					att.setDefaultList.setdefault = n.getFirstChild()
							.getNodeValue();
				} catch (Exception e) {
					att.setDefaultList.setdefault = "";
				}
			}

			if (n.getNodeName().equals("isPersistent")) {
				att.isPersistent = new Boolean(n.getFirstChild().getNodeValue());
			}

			if (n.getNodeName().equals("isSystem")) {
				att.isSystem = new Boolean(n.getFirstChild().getNodeValue());
			}

			if (n.getNodeName().equals("isChangeable")) {
				att.isChangeable = new Boolean(n.getFirstChild().getNodeValue());
			}

			n = n.getNextSibling();
		}

		return att;

		// AuthorSTATIC.attributeList.add(att);
	}

	public static void main(String[] args) {
	}

	public class templateConceptRelation {
		public String name;
		public String label;
		public String hasresource;
		public String concepttype;

		public templateConceptRelation() {
			name = "";
			label = "";
			hasresource = "";
			concepttype = "";
		}
	}
}