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
 * GetFile.java
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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.tue.gale.tools.AHAStatic;

public class GetFile extends HttpServlet {

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {

		String aharoot = (String) AHAStatic.config(getServletContext()).Get(
				"AHAROOT");
		String graphauthorpath = (String) AHAStatic.GRAPHAUTHORPATH;
		String graphauthorfiles = (String) AHAStatic.AUTHORFILESPATH;
		String contextPath = (String) AHAStatic.config(getServletContext())
				.Get("CONTEXTPATH");
		String graphauthorwebpath = contextPath + graphauthorfiles;

		res.setContentType("text/plain");
		PrintWriter out = res.getWriter();
		String filename = req.getParameter("fileName");
		String username = req.getParameter("userName");
		String outstring = null;
		String filen = "";
		if (username != null) {
			// substring(1) added by Natalia Stash, 18-07-2003
			// filen = aharoot + graphauthorfiles + username + "\\" + filename;
			filen = aharoot + graphauthorfiles.substring(1) + username + "/"
					+ filename;
		} else {
			// filen = aharoot + graphauthorfiles + filename;
			filen = aharoot + graphauthorfiles.substring(1) + filename;
		}
		String dirname = graphauthorwebpath;

		try {
			FileReader fr = new FileReader(filen);
			BufferedReader in = new BufferedReader(fr);
			// the path to the dtd should be in ahaconfig!
			while ((outstring = in.readLine()) != null) {
				outstring = outstring.replaceFirst(
						"concept_relation_type.dtd",
						"http://" + req.getServerName() + ":"
								+ req.getServerPort() + dirname
								+ "/dtd/concept_relation_type.dtd");
				outstring = outstring.replaceFirst(
						"aha_relation_type.dtd",
						"http://" + req.getServerName() + ":"
								+ req.getServerPort() + dirname
								+ "/crt/aha_relation_type.dtd");
				outstring = outstring.replaceFirst(
						"author_relation_type.dtd",
						"http://" + req.getServerName() + ":"
								+ req.getServerPort() + dirname
								+ "/crt/author_relation_type.dtd");
				outstring = outstring.replaceFirst("attribute.dtd", "http://"
						+ req.getServerName() + ":" + req.getServerPort()
						+ dirname + "/dtd/attribute.dtd");
				outstring = outstring.replaceFirst("template.dtd", "http://"
						+ req.getServerName() + ":" + req.getServerPort()
						+ dirname + "/templates/template.dtd");
				outstring = outstring.replaceFirst(
						"generatelist4.dtd",
						"http://" + req.getServerName() + ":"
								+ req.getServerPort() + dirname
								+ "/dtd/generatelist4.dtd");
				out.println(outstring);
			}

			in.close();
		} catch (IOException e) {
			System.out
					.println("File not found! The getfile servlet couldn`t find: "
							+ filen);
		}

	}

}