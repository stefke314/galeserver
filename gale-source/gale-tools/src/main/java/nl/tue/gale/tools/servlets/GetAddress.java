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
 * GetAddress.java
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
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.tue.gale.tools.AHAStatic;

public class GetAddress extends HttpServlet {

	private String path;

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();
		out.println("Error: this servlet does not support the GET method!");
		out.close();
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Read in the message from the servlet
		StringBuffer msgBuf = new StringBuffer();
		BufferedReader fromApplet = request.getReader();
		String line;
		while ((line = fromApplet.readLine()) != null) {
			if (msgBuf.length() > 0)
				msgBuf.append('\n');
			msgBuf.append(line);
		}
		if (msgBuf.toString().endsWith("FormEditor/")) {
			String contextpath = (String) nl.tue.gale.tools.AHAStatic.config(
					getServletContext()).Get("CONTEXTPATH");
			String xmlroot = (String) nl.tue.gale.tools.AHAStatic.config(
					getServletContext()).Get("XMLROOT");
			int index = xmlroot.lastIndexOf(contextpath) + contextpath.length();
			path = xmlroot.substring(index) + (String) AHAStatic.FORMPATH;
		} else
			path = (String) AHAStatic.AUTHORFILESPATH;
		fromApplet.close();

		response.setContentType("text/plain");

		PrintWriter toApplet = response.getWriter();
		toApplet.println(path);
		toApplet.close();

	}

}