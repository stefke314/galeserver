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
 * XPPEntityReader.java
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
package nl.tue.gale.common;

import java.io.IOException;
import java.util.Properties;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.io.XPP3Reader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class XPPEntityReader extends XPP3Reader {
	private static final Properties properties = new Properties();
	static {
		try {
			properties.load(XPPEntityReader.class
					.getResourceAsStream("entitydef.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public XPPEntityReader() {
		super();
	}

	protected Document parseDocument() throws DocumentException, IOException,
			XmlPullParserException {
		DocumentFactory df = getDocumentFactory();
		Document document = df.createDocument();
		Element parent = null;
		XmlPullParser pp = getXPPParser();
		pp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
		pp.setFeature(XmlPullParser.FEATURE_PROCESS_DOCDECL, false);
		defineEntities(pp);

		while (true) {
			int type = pp.nextToken();

			switch (type) {
			case XmlPullParser.PROCESSING_INSTRUCTION: {
				String text = pp.getText();
				int loc = text.indexOf(" ");

				if (loc >= 0) {
					String target = text.substring(0, loc);
					String txt = text.substring(loc + 1);
					document.addProcessingInstruction(target, txt);
				} else {
					document.addProcessingInstruction(text, "");
				}

				break;
			}

			case XmlPullParser.COMMENT: {
				if (parent != null) {
					parent.addComment(pp.getText());
				} else {
					document.addComment(pp.getText());
				}

				break;
			}

			case XmlPullParser.CDSECT: {
				if (parent != null) {
					parent.addCDATA(pp.getText());
				} else {
					String msg = "Cannot have text content outside of the "
							+ "root document";
					throw new DocumentException(msg);
				}

				break;
			}

			case XmlPullParser.ENTITY_REF:
				if (parent != null) {
					if (pp.getName().equals("gt")) {
						parent.addText(">");
					} else if (pp.getName().equals("lt")) {
						parent.addText("<");
					} else if (pp.getName().equals("amp")) {
						parent.addText("&");
					} else if (pp.getName().equals("quot")) {
						parent.addText("\"");
					} else
						parent.addEntity(pp.getName(), "&" + pp.getName() + ";");
				}
				break;

			case XmlPullParser.END_DOCUMENT:
				return document;

			case XmlPullParser.START_TAG: {
				QName qname = (pp.getPrefix() == null) ? df.createQName(
						pp.getName(), pp.getNamespace()) : df.createQName(
						pp.getName(), pp.getPrefix(), pp.getNamespace());
				Element newElement = df.createElement(qname);
				int nsStart = pp.getNamespaceCount(pp.getDepth() - 1);
				int nsEnd = pp.getNamespaceCount(pp.getDepth());

				for (int i = nsStart; i < nsEnd; i++) {
					if (pp.getNamespacePrefix(i) != null) {
						newElement.addNamespace(pp.getNamespacePrefix(i),
								pp.getNamespaceUri(i));
					}
				}

				for (int i = 0; i < pp.getAttributeCount(); i++) {
					QName qa = (pp.getAttributePrefix(i) == null) ? df
							.createQName(pp.getAttributeName(i)) : df
							.createQName(pp.getAttributeName(i),
									pp.getAttributePrefix(i),
									pp.getAttributeNamespace(i));
					newElement.addAttribute(qa, pp.getAttributeValue(i));
				}

				if (parent != null) {
					parent.add(newElement);
				} else {
					document.add(newElement);
				}

				parent = newElement;

				break;
			}

			case XmlPullParser.END_TAG: {
				if (parent != null) {
					parent = parent.getParent();
				}

				break;
			}

			case XmlPullParser.TEXT: {
				String text = pp.getText();

				if (parent != null) {
					parent.addText(text);
				} else {
					String msg = "Cannot have text content outside of the "
							+ "root document";
					throw new DocumentException(msg);
				}

				break;
			}

			default:
				break;
			}
		}
	}

	private void defineEntities(XmlPullParser pp) throws XmlPullParserException {
		/*
		 * for (Map.Entry<Object, Object> entry : properties.entrySet())
		 * pp.defineEntityReplacementText(entry.getKey().toString(), entry
		 * .getValue().toString());
		 */
	}
}