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
 * ReadCRTXML.java
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
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * ReadCRTXML Read the CRTs into memory.
 * 
 */
public class ReadCRTXML {
	private CRTConceptRelationType CRT;
	public URL base;

	public ReadCRTXML(String filename, URL home) {
		AuthorSTATIC.CRTList = new LinkedList();
		base = home;
		try {
			Hashtable reqinfo = new Hashtable();
			reqinfo.put("name", "authordir");
			reqinfo.put("dir", "crt");
			Hashtable resinfo = nl.tue.gale.tools.graphauthor.ui.GraphAuthor
					.getExecRequest(reqinfo, base);
			Vector filenames = (Vector) resinfo.get("files");
			if (filenames == null)
				return;
			for (int i = 0; i < filenames.size(); i++) {
				String fn = (String) filenames.get(i);
				if (fn.endsWith(".aha")) {
					String stemp = "crt/" + fn.substring(0, fn.length() - 4);
					CRT = new CRTConceptRelationType();
					StartReadCRTXML(stemp + ".aha");
					StartReadCRTXML(stemp + ".author");
					if (CRT.properties.concept_hierarchy.booleanValue() == true)
						AuthorSTATIC.trel = CRT;
					AuthorSTATIC.CRTList.add(CRT);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void ReadName(Node n) {
		// reads the name from the dom model into the CRT structure
		Node nod;
		nod = n.getFirstChild();
		CRT.name = nod.getNodeValue().trim();
	}

	private void ReadColor(Node n) {
		Node nod;
		nod = n.getFirstChild();
		CRT.color = nod.getNodeValue().trim();
	}

	private void ReadStyle(Node n) {
		Node nod;
		nod = n.getFirstChild();
		CRT.style = nod.getNodeValue().trim();
	}

	private void ReadProperties(Node n) {
		Node nod;
		NamedNodeMap nmap;
		nod = n;

		// default values
		CRT.properties.acyclic = Boolean.FALSE;
		CRT.properties.concept_hierarchy = Boolean.FALSE;
		CRT.properties.unary = Boolean.FALSE;

		nmap = nod.getAttributes();

		if (nmap != null) {
			// read attributes
			int len = nmap.getLength();
			Attr attr;

			for (int i = 0; i < len; i++) {
				attr = (Attr) nmap.item(i);

				if (attr.getNodeName().equals("acyclic")) {
					CRT.properties.acyclic = new Boolean(attr.getNodeValue());
				}

				if (attr.getNodeName().equals("unary")) {
					CRT.properties.unary = new Boolean(attr.getNodeValue());
				}

				if (attr.getNodeName().equals("concept_hierarchy")) {
					CRT.properties.concept_hierarchy = new Boolean(
							attr.getNodeValue());
				}
			}
		}
	}

	private void ReadAction(CRTAction act, Node n) {
		Node nod = n;
		NamedNodeMap nmap;
		nmap = nod.getAttributes();
		act.combination = "NONE";

		if (nmap != null) {
			// read attributes
			int len = nmap.getLength();
			Attr attr;

			for (int i = 0; i < len; i++) {
				attr = (Attr) nmap.item(i);

				if (attr.getNodeName().equals("combination")) {
					act.combination = attr.getNodeValue().trim();
				}
			}
		}

		Node nd;

		nod = nod.getFirstChild();
		nod = nod.getNextSibling();
		nd = nod.getFirstChild();

		if (nd != null) {
			act.conceptName = nd.getNodeValue().trim();
		} else {
			act.conceptName = "";
		}

		nod = nod.getNextSibling();
		nod = nod.getNextSibling();

		nd = nod.getFirstChild();

		if (nd != null) {
			act.attributeName = nd.getNodeValue().trim();
		} else {
			act.attributeName = "";
		}

		nod = nod.getNextSibling();
		nod = nod.getNextSibling();
		nd = nod.getFirstChild();
		act.expression = nd.getNodeValue().trim();
	}

	private void ReadTrueActions(CRTGenerateListItem glitem, Node n) {
		Node nod = n.getFirstChild();
		CRTAction act;
		nod = nod.getNextSibling();

		while (nod != null) {
			if (nod.getNodeName().equals("action")) {
				act = new CRTAction();
				ReadAction(act, nod);
				glitem.trueActions.actionList.add(act);
			}

			nod = nod.getNextSibling();
		}
	}

	private void ReadFalseActions(CRTGenerateListItem glitem, Node n) {
		Node nod = n.getFirstChild();
		CRTAction act;
		nod = nod.getNextSibling();

		while (nod != null) {
			if (nod.getNodeName().equals("action")) {
				act = new CRTAction();
				ReadAction(act, nod);
				glitem.falseActions.actionList.add(act);
			}

			nod = nod.getNextSibling();
		}
	}

	private void ReadGenerateListItem(Node n) {
		CRTGenerateListItem gli = new CRTGenerateListItem();
		gli.location = "default";
		gli.propagating = Boolean.FALSE;

		Node nod = n;
		NamedNodeMap nmap = nod.getAttributes();

		if (nmap != null) {
			// read attributes
			int len = nmap.getLength();
			Attr attr;

			for (int i = 0; i < len; i++) {
				attr = (Attr) nmap.item(i);

				if (attr.getNodeName().equals("location")) {
					gli.location = attr.getNodeValue();
				}

				if (attr.getNodeName().equals("isPropagating")) {
					gli.propagating = new Boolean(attr.getNodeValue());
				}
			}

			nod = nod.getFirstChild();
			nod = nod.getNextSibling();
			gli.requirement = nod.getFirstChild().getNodeValue();

			// while van maken
			while (nod != null) {
				nod = nod.getNextSibling();
				nod = nod.getNextSibling();

				if ((nod != null) && (nod.getNodeName().equals("trueActions"))) {
					ReadTrueActions(gli, nod);
				}

				if ((nod != null) && (nod.getNodeName().equals("falseActions"))) {
					ReadFalseActions(gli, nod);
				}
			}
		}

		// add object to list
		CRT.listItem.generateListItemList.add(gli);
	}

	private void ReadSetDefault(Node n) {
		CRTSetDefault sdef = new CRTSetDefault();
		Node nod = n;
		NamedNodeMap nmap = nod.getAttributes();

		if (nmap != null) {
			// read attributes
			int len = nmap.getLength();
			Attr attr;

			for (int i = 0; i < len; i++) {
				attr = (Attr) nmap.item(i);

				if (attr.getNodeName().equals("location")) {
					sdef.location = attr.getNodeValue();
				}

				if (attr.getNodeName().equals("combination")) {
					sdef.combination = attr.getNodeValue();
				}
			}
		}

		nod = nod.getFirstChild();
		sdef.setdefault = nod.getNodeValue();
		CRT.listItem.setDefaultList.add(sdef);
	}

	private void ReadListItems(Node n) {
		Node nod = n.getFirstChild();
		nod = nod.getNextSibling();

		while (nod != null) {
			if (nod.getNodeName().equals("setdefault")) {
				ReadSetDefault(nod);
			}

			if (nod.getNodeName().equals("generateListItem")) {
				ReadGenerateListItem(nod);
			}

			nod = nod.getNextSibling();
			nod = nod.getNextSibling();
		}
	}

	private void StartReadCRTXML(String xmlfile) {
		// CRT = new CRTConceptRelationType();
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
			String path = base.getPath();
			String pathttemp = path.substring(1, path.length());
			int index = pathttemp.indexOf("/");
			index++;

			String dirname = path.substring(0, index);

			if (dirname.equals("/graphAuthor")) {
				dirname = "";
			}

			URL url = new URL("http://" + base.getHost() + ":" + base.getPort()
					+ dirname + "/authorservlets/GetFile?fileName=" + xmlfile);

			p.parse(url.toString());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		Document doc = p.getDocument();
		Node n = doc.getDocumentElement();
		n = n.getFirstChild();
		n = n.getNextSibling();

		while (n != null) {
			if (n.getNodeName().equals("name")) {
				ReadName(n);
			}

			if (n.getNodeName().equals("color")) {
				ReadColor(n);
			}

			if (n.getNodeName().equals("style")) {
				ReadStyle(n);
			}

			if (n.getNodeName().equals("properties")) {
				ReadProperties(n);
			}

			if (n.getNodeName().equals("listitems")) {
				ReadListItems(n);
			}

			n = n.getNextSibling();
		}
	}

	private void StartReadCRTXMLauthor(String xmlfile) {
		DOMParser p = new DOMParser();

		try {
			String path = base.getPath();
			String pathttemp = path.substring(1, path.length());
			int index = pathttemp.indexOf("/");
			index++;

			String dirname = path.substring(0, index);

			if (dirname.equals("/graphAuthor")) {
				dirname = "";
			}

			URL url = new URL("http://" + base.getHost() + ":" + base.getPort()
					+ dirname + "/authorservlets/GetFile?fileName=" + xmlfile
					+ ".author");

			p.parse(url.toString());
		} catch (Exception e) {
			System.out.println("Error while parsing: " + xmlfile);
		}

		Document doc = p.getDocument();
		Node n = doc.getDocumentElement();
		n = n.getFirstChild();
		n = n.getNextSibling();

		while (n != null) {
			if (n.getNodeName().equals("name")) {
				ReadName(n);
			}

			if (n.getNodeName().equals("color")) {
				ReadColor(n);
			}

			if (n.getNodeName().equals("style")) {
				ReadStyle(n);
			}

			if (n.getNodeName().equals("properties")) {
				ReadProperties(n);
			}

			if (n.getNodeName().equals("listitems")) {
				ReadListItems(n);
			}

			n = n.getNextSibling();
		}

		// AuthorSTATIC.CRTList.add(CRT);
	}
}