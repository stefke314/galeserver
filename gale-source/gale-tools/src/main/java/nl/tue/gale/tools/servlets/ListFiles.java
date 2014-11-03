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
 * ListFiles.java
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
package nl.tue.gale.tools.servlets;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.tue.gale.tools.AHAStatic;
import nl.tue.gale.tools.config.AhaAuthor;
import nl.tue.gale.tools.config.AuthorsConfig;

public class ListFiles extends HttpServlet {

	public Vector courseList = new Vector();

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {

		String aharoot = (String) nl.tue.gale.tools.AHAStatic.config(
				getServletContext()).Get("AHAROOT");
		String graphauthorfiles = (String) AHAStatic.AUTHORFILESPATH;

		res.setContentType("text/plain");
		PrintWriter out = res.getWriter();
		// extention
		final String extention = req.getParameter("extention").trim();
		final String userName = req.getParameter("userName").trim();

		String outstring = null;
		String dirname = "";

		if (userName != null) {
			// substring(1) added by Natlia Stash, 18-07-2003
			// dirname = aharoot + graphauthorfiles + "\\" + userName;
			dirname = aharoot + graphauthorfiles.substring(1) + userName;
		} else {
			// dirname = aharoot + graphauthorfiles;
			dirname = aharoot + graphauthorfiles.substring(1);
		}

		this.checkAuthor(userName);

		try {

			File file_obj = new File(dirname);

			FileFilter extfilter = new FileFilter() {
				public boolean accept(File f) {
					// filter the file extention
					String fileName = f.getName().toLowerCase();
					return fileName.endsWith(extention);
				}
			};

			File[] filenames = file_obj.listFiles(extfilter);

			for (int i = 0; i < filenames.length;) {
				// System.out.println("== file: " + filenames[i]);
				Filename fname = new Filename(filenames[i].toString(),
						File.separatorChar, '.');
				String tempFile = fname.filename().replaceAll(extention, "");

				if (courseList.contains(tempFile)) {
					out.println(fname.filename() + "." + fname.extension());

				}

				i++;
			}

		}

		catch (Exception e) {
			System.out.println("Path not found! : " + dirname + " " + e);
		}
	}

	public void checkAuthor(String authorname) {

		AuthorsConfig aconfig = new AuthorsConfig(getServletContext());
		AhaAuthor author = aconfig.GetAuthor(authorname);

		for (Enumeration i = author.getCourseList().elements(); i
				.hasMoreElements();) {
			String course = (String) i.nextElement();
			courseList.add(course);
		}

	}

	public class Filename {
		private String fullPath;
		private char pathSeparator, extensionSeparator;

		public Filename(String str, char sep, char ext) {
			fullPath = str;
			pathSeparator = sep;
			extensionSeparator = ext;
		}

		public String extension() {
			int dot = fullPath.lastIndexOf(extensionSeparator);
			return fullPath.substring(dot + 1);
		}

		public String filename() {
			int dot = fullPath.lastIndexOf(extensionSeparator);
			int sep = fullPath.lastIndexOf(pathSeparator);
			return fullPath.substring(sep + 1, dot);
		}

		public String path() {
			int sep = fullPath.lastIndexOf(pathSeparator);
			return fullPath.substring(0, sep);
		}
	}

}