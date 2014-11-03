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
 * GaleToolsUtil.java
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
package nl.tue.gale.tools;

import java.io.File;
import java.io.StringWriter;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.BaseMarkupSerializer;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.w3c.dom.Element;

public class GaleToolsUtil {
	public static Object getBean(ServletContext sc, String name) {
		ApplicationContext applicationContext = WebApplicationContextUtils
				.getRequiredWebApplicationContext(sc);
		return applicationContext.getBean(name);
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> getConfig(ServletContext sc) {
		return (Map<String, Object>) getBean(sc, "galeConfig");
	}

	public static File getHomeDir(ServletContext sc) {
		return (File) getConfig(sc).get("homeDir");
	}

	public static BaseMarkupSerializer getSerializer() {
		return new XMLSerializer();
	}

	public static BaseMarkupSerializer getSerializer(OutputFormat format) {
		return new XMLSerializer(format);
	}

	public static BaseMarkupSerializer getSerializer(
			java.io.OutputStream output, OutputFormat format) {
		return new XMLSerializer(output, format);
	}

	public static BaseMarkupSerializer getSerializer(java.io.Writer writer,
			OutputFormat format) {
		return new XMLSerializer(writer, format);
	}

	public static String serializeElement(Element element) {
		try {
			StringWriter writer = new StringWriter();
			OutputFormat of = new OutputFormat("html", "UTF-8", true);
			of.setOmitDocumentType(true);
			of.setLineWidth(100);
			of.setNonEscapingElements(new String[] { "style" });
			BaseMarkupSerializer ser = getSerializer(writer, of);
			ser.serialize(element);
			return writer.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "Error in serializeElement: " + e.getMessage();
		}
	}

	public static DOMParser createDOMParser(boolean validation, boolean loaddtd) {
		DOMParser result = new DOMParser();
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

}
