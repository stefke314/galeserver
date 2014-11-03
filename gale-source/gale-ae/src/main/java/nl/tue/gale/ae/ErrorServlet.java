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
 * ErrorServlet.java
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
package nl.tue.gale.ae;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ErrorServlet extends HttpServlet {
	private static final long serialVersionUID = -543275881066165469L;

	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			Throwable cause = (Throwable) req
					.getAttribute("javax.servlet.error.exception");
			if (cause == null)
				cause = (Throwable) req.getSession().getAttribute("exception");
			if (cause == null)
				cause = new Exception("unknown exception");
			System.err
					.println("------- nl.tue.gale.ae.ErrorServlet report --------");
			cause.printStackTrace();
			System.err
					.println("---- end of nl.tue.gale.ae.ErrorServlet report ----");
			resp.setContentType("text/html");
			resp.setBufferSize(4096);
			PrintWriter out = resp.getWriter();
			out.println("<html><head><title>Gale Error</title></head>");
			out.println("    <body style=\"font-family:Tahoma,Arial\">");
			out.println("        <h1 style=\"text-align:center;color:#D02020\">Gale Error</h1>");
			out.println("        <p style=\"margin-bottom:2em;text-align:center\">"
					+ cause.getMessage() + "</p>");
			out.println("        <div style=\"overflow:auto;position:fixed;bottom:1em;top:8em;padding:1em;left:1em;right:1em;border:1px solid #A0A0A0\">");
			while (cause != null) {
				out.println("            <pre style=\"font-size:0.9em\">"
						+ cause.getClass().getName() + ": "
						+ cause.getMessage() + "</pre>");
				cause = cause.getCause();
			}
			out.println("        </div>");
			out.println("        <p></p>");
			out.println("        <p></p>");
			out.println("        <p></p>");
			out.println("        <p></p>");
			out.println("        <p></p>");
			out.println("        <p></p>");
			out.println("        <p></p>");
			out.println("        <p></p>");
			out.println("        <p></p>");
			out.println("        <p></p>");
			out.println("        <p></p>");
			out.println("    </body>");
			out.println("</html>");
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}