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
 * FormPlugin.java
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
import java.net.URLEncoder;

import javax.servlet.http.HttpServletResponse;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.ProcessorException;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.common.GaleUtil;

public class FormPlugin extends AbstractPlugin {
	public void doPost(Resource resource) throws ProcessorException {
		GaleContext gale = GaleContext.of(resource);
		String code = gale.req().getParameter("exec");
		if (code == null || "".equals(code)) {
			redirect(resource);
			return;
		}
		try {
			gale.exec(code);
		} catch (Exception e) {
			e.printStackTrace();
			String redirect = gale.req().getRequestURL().toString();
			try {
				redirect += "?error="
						+ URLEncoder.encode(e.getMessage(), "UTF-8");
			} catch (Exception eInner) {
			}
			try {
				gale.resp().sendRedirect(redirect);
				gale.usedResponse();
				return;
			} catch (Exception eInner) {
				throw new IllegalArgumentException("unable to redirect", e);
			}
		}
		redirect(resource);
		gale.usedResponse();
	}

	private void redirect(Resource resource) {
		String redirect = GaleContext.req(resource).getParameter("redirect");
		HttpServletResponse resp = GaleContext.resp(resource);
		try {
			if (redirect == null || "".equals(redirect)) {
				resp.setContentType("text/html");
				resp.setBufferSize(4096);
				PrintWriter out = resp.getWriter();
				out.println("<html><head><title>GALE</title></head>");
				out.println("    <body style=\"font-family:Tahoma,Arial\">");
				out.println("        <h1 style=\"text-align:center;color:#20A020\">Form Processed</h1>");
				out.println("    </body>");
				out.println("</html>");
				out.close();
				GaleContext.usedResponse(resource);
			} else {
				String context = GaleUtil.getContextURL(
						GaleContext.req(resource)).toString();
				resp.sendRedirect(context + "/concept/" + redirect);
				GaleContext.usedResponse(resource);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("unable to redirect", e);
		}
	}
}
