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
 * CLConvert.java
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
package nl.tue.gale.conversion.aha3;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import nl.tue.gale.common.GaleUtil;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;

public class CLConvert {
	public CLConvert() {
		File root = new File(
				"D:/opt/gale/gale-source/gale/src/main/webapp/2ID65/test");
		for (File f : root.listFiles())
			if (f.toString().endsWith(".xhtml"))
				convertFile(f);
	}

	private void convertFile(File f) {
		System.out.println("converting: " + f.getName());
		Element root = GaleUtil.parseXML(f).getRootElement();
		convertElement(root);
		try {
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(
					f)));
			pw.print(root.asXML());
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void convertElement(Element element) {
		if (element.getName().equals("object"))
			convertObject(element);
		if (element.getName().equals("if"))
			convertIf(element);
		if (element.getName().equals("a"))
			convertA(element);
		if (element.getName().equals("variable"))
			convertVariable(element);
		if (element.getName().equals("test")
				|| element.getName().equals("question")
				|| element.getName().equals("answer")
				|| element.getName().equals("explain")
				|| element.getName().equals("result"))
			convertTest(element);
		convertChildren(element);

	}

	private void convertTest(Element element) {
		if ("gale".equals(element.getQName().getNamespacePrefix()))
			return;
		element.setQName(DocumentFactory.getInstance().createQName(
				element.getName(), "gale", "http://gale.tue.nl/adaptation"));
		if (element.getName().equals("test"))
			element.addAttribute("expr",
					convertExpr(element.attributeValue("expr")));
	}

	private void convertA(Element element) {
		if ("conditional".equals(element.attributeValue("class"))) {
			element.addAttribute("class", null);
			element.setQName(DocumentFactory.getInstance().createQName("a",
					"gale", "http://gale.tue.nl/adaptation"));
		}
	}

	private void convertVariable(Element element) {
		if ("gale".equals(element.getQName().getNamespacePrefix()))
			return;
		System.out.println("-- variable found: " + element.asXML());
	}

	@SuppressWarnings("unchecked")
	private void convertIf(Element element) {
		if ("gale".equals(element.getQName().getNamespacePrefix()))
			return;
		element.setQName(DocumentFactory.getInstance().createQName("if",
				"gale", "http://gale.tue.nl/adaptation"));
		String expr = convertExpr(element.attributeValue("expr"));
		element.addAttribute("expr", expr);
		List<Element> blocks = element.elements("block");
		if (blocks.size() > 0) {
			blocks.get(0).setQName(
					DocumentFactory.getInstance().createQName("then", "gale",
							"http://gale.tue.nl/adaptation"));
		}
		if (blocks.size() > 1) {
			blocks.get(1).setQName(
					DocumentFactory.getInstance().createQName("else", "gale",
							"http://gale.tue.nl/adaptation"));
		}
	}

	private String convertExpr(String expr) {
		int i = 0;
		int j = 0;
		do {
			j = expr.indexOf("${", i);
			if (j >= 0) {
				int k = expr.indexOf("}", j);
				String rstr = expr.substring(j + 2, k);
				if (rstr.indexOf(".") >= 0) {
					String[] parts = rstr.split("\\.");
					rstr = parts[parts.length - 2] + "#"
							+ parts[parts.length - 1];
				} else
					rstr = "#" + rstr;
				expr = expr.substring(0, j + 2) + rstr + expr.substring(k);
				i = j + 2;
			}
		} while (j >= 0);
		return expr;
	}

	private void convertObject(Element element) {
		if ("text/aha".equals(element.attributeValue("type"))) {
			if (element.attributeValue("name") == null) {
				element.addAttribute("type", null);
				element.setQName(DocumentFactory.getInstance().createQName(
						"object", "gale", "http://gale.tue.nl/adaptation"));
			} else {
				System.out.println("-- found name object: "
						+ element.attributeValue("name"));
				element.addAttribute("type", null);
				element.setQName(DocumentFactory.getInstance().createQName(
						"object", "gale", "http://gale.tue.nl/adaptation"));
				String name = element.attributeValue("name");
				if (name.indexOf(".") >= 0)
					name = name.substring(name.lastIndexOf(".") + 1);
				element.addAttribute("name", name);
			}
		}
	}

	private void convertChildren(Element element) {
		if (element.elements().size() == 0)
			return;
		Element current = (Element) element.elements().get(0);
		while (current != null) {
			Element nextCurrent = null;
			int nextIndex = current.getParent().elements().indexOf(current) + 1;
			if (nextIndex < current.getParent().elements().size())
				nextCurrent = (Element) current.getParent().elements()
						.get(nextIndex);
			convertElement(current);
			current = nextCurrent;
		}
	}

	public static void main(String[] args) {
		new CLConvert();
	}
}
