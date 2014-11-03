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
 * LogoutPlugin.java
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
package nl.tue.gale.ae.processor.plugin;

import java.io.PrintWriter;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.LoginManager;
import nl.tue.gale.ae.ProcessorException;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.common.GaleUtil;

public class LogoutPlugin extends AbstractPlugin {
	public void doGet(Resource resource) throws ProcessorException {
		GaleContext gale = GaleContext.of(resource);
		((LoginManager) gale.ac().getBean("loginManager")).logout(resource);
		try {
			gale.resp().setContentType("text/html");
			gale.resp().setBufferSize(4096);
			PrintWriter out = gale.resp().getWriter();
			out.println("<html><head><title>Gale Logout</title></head>");
			out.println("    <body style=\"font-family:Tahoma,Arial\">");
			out.println("        <h1 style=\"text-align:center;color:#2020A0\">Gale Logout</h1>");
			out.println("        <p style=\"margin-bottom:100;text-align:center;font-size:16\">You are succesfully logged out<br /><a target=\"_top\" href=\""
					+ GaleUtil.getRequestURL(gale.req())
					+ "\">Login here</a></p>");
			out.println("    </body>");
			out.println("</html>");
			out.close();
			gale.usedResponse();
		} catch (Exception e) {
			throw new ProcessorException("unable to create response", e);
		}
	}
}