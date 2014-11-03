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
 * XMLUtil.java
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;

import nl.tue.gale.tools.GaleToolsUtil;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * This class provides some general functions used in processing XML-documents.
 * //@see UserModelXMLDB //@see ConceptXMLDB
 */
public class XMLUtil {
	/**
	 * Loads an index (Hashtable) from a file.
	 * 
	 * @param root_
	 *            The file where the index is stored in.
	 * @return The indextable.
	 * @exception IOException
	 *                If an internal error prevents the file from being read.
	 */
	public static Hashtable loadIndex(File root_, String name)
			throws IOException {
		File indexfile = new File(root_, name);
		Hashtable index = new Hashtable();
		if (indexfile.exists()) {
			LineNumberReader in = new LineNumberReader(
					new FileReader(indexfile));
			while (in.ready())
				index.put(in.readLine(), new Long(in.readLine()));
			in.close();
		}
		return index;
	}

	/**
	 * Saves an index (Hashtable) to a file.
	 * 
	 * @param root_
	 *            The file where the index is stored in.
	 * @param index
	 *            The indextable.
	 * @exception IOException
	 *                If an internal error prevents the file from being written.
	 */
	public static void saveIndex(File root_, String name, Hashtable index)
			throws IOException {
		File indexfile = new File(root_, name);
		PrintWriter out = new PrintWriter(new FileWriter(indexfile));
		Enumeration keys = index.keys();
		String key = null;
		while (keys.hasMoreElements()) {
			key = (String) keys.nextElement();
			out.println(key);
			out.println((Long) index.get(key));
		}
		out.close();
	}

	/**
	 * Loads an index (Hashtable) from a file.
	 * 
	 * @param root_
	 *            The file where the index is stored in.
	 * @return The indextable.
	 * @exception IOException
	 *                If an internal error prevents the file from being read.
	 */
	public static Hashtable loadIndex(File root_) throws IOException {
		return loadIndex(root_, "index");
	}

	/**
	 * Saves an index (Hashtable) to a file.
	 * 
	 * @param root_
	 *            The file where the index is stored in.
	 * @param index
	 *            The indextable.
	 * @exception IOException
	 *                If an internal error prevents the file from being written.
	 */
	public static void saveIndex(File root_, Hashtable index)
			throws IOException {
		saveIndex(root_, "index", index);
	}

	/**
	 * Tries to parse a XML document.
	 * 
	 * @param xmlfile
	 *            The file that has to be parsed.
	 * @return The parsed document.
	 * @exception IOException
	 *                If the document cannot be parsed.
	 */
	public static Document getXML(File xmlfile) throws IOException {
		return getXML(xmlfile.toURI().toURL());
	}

	/**
	 * Tries to parse a XML document.
	 * 
	 * @param xmlfile
	 *            The uri that has to be parsed.
	 * @return The parsed document.
	 * @exception IOException
	 *                If the document cannot be parsed.
	 */
	public static Document getXML(URL xmlfile) throws IOException {
		try {
			DOMParser dp = GaleToolsUtil.createDOMParser(false, false);
			InputSource is = new InputSource(xmlfile.openConnection()
					.getInputStream());
			dp.parse(is);
			return dp.getDocument();
		} catch (Exception e) {
			throw new IllegalArgumentException("unable to parse document: "
					+ e.toString(), e);
		}
	}

	/**
	 * Tries to write a document to a outputstream.
	 */
	public static void writeXML(OutputStream os, Document document)
			throws IOException {
		PrintWriter pw = new PrintWriter(new BufferedWriter(
				new OutputStreamWriter(os)));
		pw.write(GaleToolsUtil.serializeElement(document.getDocumentElement()));
	}

	/**
	 * Returns the value of <code>Node</code>. This is done by assuming that
	 * this node has one child that is a textnode. The value of this textnode is
	 * returned.
	 * 
	 * @param node
	 *            The node that has to be examined.
	 * @return The value of the node.
	 */
	public static String nodeValue(Node node) {
		if (node == null)
			return null;
		Node child = node.getFirstChild();
		if (child == null)
			return null;
		return (child.toString().equals("") ? null : child.toString());
	}

	/**
	 * Conversion from null values to empty values.
	 * 
	 * @param s
	 *            A string possibly null.
	 * @return A string possibly empty.
	 */
	public static String S2D(String s) {
		if (s == null)
			return "";
		else
			return s;
	}

	/**
	 * Conversion from empty values to null values.
	 * 
	 * @param s
	 *            A string possibly empty.
	 * @return A string possibly null.
	 */
	public static String D2S(String s) {
		if (s.equals(""))
			return null;
		else
			return s;
	}
}