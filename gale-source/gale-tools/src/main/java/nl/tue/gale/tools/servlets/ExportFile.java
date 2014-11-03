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
 * ExportFile.java
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.tue.gale.common.cache.CacheSession;
import nl.tue.gale.conversion.aha3.AHA3Format;
import nl.tue.gale.dm.DMCache;
import nl.tue.gale.dm.data.Concept;
import nl.tue.gale.tools.AHAStatic;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class ExportFile extends HttpServlet {
	private static final long serialVersionUID = -1688180204692184231L;

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		String filename = request.getParameter("fileName");
		String author = request.getParameter("author");
		boolean created = false;
		try {
			String graphauthorfiles = (String) AHAStatic.AUTHORFILESPATH;
			String aharoot = (String) AHAStatic.config(getServletContext())
					.Get("AHAROOT");
			String fileUrl = aharoot + graphauthorfiles.substring(1) + author
					+ "/" + filename + ".aha";
			created = copyToConfig(fileUrl, filename);
		} catch (Exception e) {
			e.printStackTrace();
		}
		out.println(created);
	}

	private boolean copyToConfig(String file, String app) {
		try {
			ApplicationContext applicationContext = WebApplicationContextUtils
					.getRequiredWebApplicationContext(getServletContext());
			DMCache cache = (DMCache) applicationContext.getBean("dmCache");
			CacheSession<Concept> session = cache.openSession();
			for (Concept c : AHA3Format.convertStream(new FileInputStream(
					new File(file))))
				session.put(c.getUri(), c);
			session.commit();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}