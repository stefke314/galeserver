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
 * CheckCourse.java
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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.tue.gale.tools.config.AhaAuthor;
import nl.tue.gale.tools.config.AuthorsConfig;

public class CheckCourse extends HttpServlet {

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {

		res.setContentType("text/plain");
		PrintWriter out = res.getWriter();
		String username = req.getParameter("userName");
		String coursename = req.getParameter("courseName");
		if (coursename.endsWith(".gaf"))
			coursename = coursename.substring(0, coursename.indexOf(".gaf"));

		String outstring = null;
		AuthorsConfig aconfig = new AuthorsConfig(getServletContext());
		boolean courseExists = aconfig.containsCourse(coursename)
				&& (!(aconfig.GetAuthor(username).getCourseList()
						.contains(coursename)));

		// added by Natalia Stash, 15-07-2003
		// when an author creates a new course it is added to authorlistfile.xml

		if (!courseExists) {
			AhaAuthor ahaAuthor = aconfig.GetAuthor(username);
			Vector v = ahaAuthor.getCourseList();
			if (v.indexOf(coursename) == -1) {
				v.addElement(coursename);
				ahaAuthor.setCourseList(v);
				aconfig.PutAuthor(ahaAuthor);
				aconfig.StoreConfig();
			}
		}
		out.println(courseExists);

	}
}
