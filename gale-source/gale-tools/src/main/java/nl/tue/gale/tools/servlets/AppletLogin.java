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
 * AppletLogin.java
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
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.tue.gale.tools.config.AhaAuthor;
import nl.tue.gale.tools.config.AuthorsConfig;

public class AppletLogin extends HttpServlet {

	AuthorsConfig aconf;
	AhaAuthor ahaAuthor = null;

	public AppletLogin() {
	}

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
		aconf = new AuthorsConfig(getServletContext());
		Boolean exists = Boolean.FALSE;
		StringBuffer msgBuf = new StringBuffer();
		BufferedReader fromApplet = request.getReader();
		String line;
		while ((line = fromApplet.readLine()) != null) {
			if (msgBuf.length() > 0)
				msgBuf.append('\n');
			msgBuf.append(line);
		}
		String temp = msgBuf.toString();
		fromApplet.close();
		StringTokenizer tokenizer = new StringTokenizer(temp, "\n");
		String login = temp.substring(0, temp.indexOf("\n"));
		String passwd = temp.substring(temp.indexOf("\n") + 1);
		if ((login != null) && (passwd != null)) {
			ahaAuthor = aconf.GetAuthor(login);
			exists = new Boolean((ahaAuthor != null)
					&& ahaAuthor.checkPasswd(passwd));
		}
		response.setContentType("text/plain");
		PrintWriter toApplet = response.getWriter();
		if (exists.booleanValue()) {
			toApplet.println("true");
		} else {
			toApplet.println("false");
		}
		toApplet.close();
	}

}
