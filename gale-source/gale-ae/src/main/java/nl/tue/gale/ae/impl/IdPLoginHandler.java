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
 * IdPLoginHandler.java
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
package nl.tue.gale.ae.impl;

import java.io.PrintWriter;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.iharder.Base64;
import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.LoginHandler;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.common.GaleUtil;

import org.dom4j.Element;

public class IdPLoginHandler implements LoginHandler {
	private URL idpLocation = null;
	private String spIssuer = null;

	public String getSpIssuer() {
		return spIssuer;
	}

	public void setSpIssuer(String spIssuer) {
		this.spIssuer = spIssuer;
	}

	public URL getIdpLocation() {
		return idpLocation;
	}

	public void setIdpLocation(URL idpLocation) {
		this.idpLocation = idpLocation;
	}

	private String utcTime() {
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		c.setTime(new Date());
		String result = addZero(c.get(Calendar.YEAR), 4) + "-"
				+ addZero(c.get(Calendar.MONTH) + 1, 2) + "-"
				+ addZero(c.get(Calendar.DAY_OF_MONTH), 2) + "T"
				+ addZero(c.get(Calendar.HOUR_OF_DAY), 2) + ":"
				+ addZero(c.get(Calendar.MINUTE), 2) + "Z";
		return result;
	}

	private String addZero(int i, int num) {
		String result = "" + i;
		while (result.length() < num)
			result = "0" + result;
		return result;
	}

	public String getLoginName(Resource resource) {
		HttpServletResponse resp = GaleContext.resp(resource);
		HttpServletRequest req = GaleContext.req(resource);
		String userid = (String) req.getSession().getAttribute(
				"nl.tue.gale.userId");
		if (userid != null)
			return userid;

		String now = utcTime();
		String samlRequest = "<samlp:AuthnRequest"
				+ "    xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\""
				+ "    xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\""
				+ "    ID=\"" + GaleUtil.newGUID() + "\""
				+ "    Version=\"2.0\"" + "    IssueInstant=\"" + now + "\""
				+ "    AssertionConsumerServiceIndex=\"0\""
				+ "    AttributeConsumingServiceIndex=\"0\">"
				+ "  <saml:Issuer>" + spIssuer + "</saml:Issuer>"
				+ "</samlp:AuthnRequest>";

		try {
			resp.setContentType("text/html");
			resp.setBufferSize(4096);
			PrintWriter out = resp.getWriter();
			out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
			out.println("    <body onload=\"document.forms[0].submit()\">");
			out.println("        <noscript><p>");
			out.println("        	 <strong>Note:</strong> Since your browser does not support JavaScript,");
			out.println("        	 you must press the Continue button twice to proceed.");
			out.println("        	 <em>This is the first time.</em>");
			out.println("        </p></noscript>");
			out.println("        <form method=\"POST\" action=\"" + idpLocation
					+ "\">");
			out.println("        	 <input type=\"hidden\" name=\"SAMLRequest\" value=\""
					+ Base64.encodeBytes(samlRequest.getBytes("UTF-8"))
					+ "\"/>");
			out.println("        	 <input type=\"hidden\" name=\"RelayState\" value=\""
					+ req.getRequestURL().toString() + "\"/>");
			out.println("            <input type=\"submit\" value=\"Continue\"/>");
			out.println("        </form>");
			out.println("    </body>");
			out.println("</html>");
			out.close();
		} catch (Exception e) {
			throw new IllegalArgumentException("unable to login: "
					+ e.getMessage(), e);
		}

		return null;
	}

	@Override
	public void addLoginPage(String method, Element page,
			Map<String, String[]> parameters) {
	}

	@Override
	public void doLoginPage(String method, Resource resource) {
	}

	@Override
	public void logout(Resource resource) {
	}
}
