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
 * OpenIdLoginHandler.java
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.LoginHandler;
import nl.tue.gale.ae.LoginManager;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.common.GaleUtil;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.Message;
import org.openid4java.message.MessageException;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;

public class OpenIdLoginHandler implements LoginHandler {
	private static final Logger log = Logger
			.getLogger(OpenIdLoginHandler.class);

	@Override
	public String getLoginName(Resource resource) {
		HttpServletRequest req = GaleContext.req(resource);
		if (GaleContext.resp(resource).isCommitted())
			return null;
		String openid = GaleContext.getCookie(resource, "OpenIdLogin");
		if (openid != null) {
			req.getSession().setAttribute("redirect", req.getRequestURI());
			doOpenIdLogin(resource, openid);
			GaleContext.usedResponse(resource);
		}
		return null;
	}

	@SuppressWarnings("unused")
	private static Element createOpenIdLink(String name) {
		return createOpenIdLink(name, name.toLowerCase() + ".anyopenid.com");
	}

	private static Element createOpenIdLink(String name, String uri) {
		Element result = GaleUtil.createHTMLElement("a");
		result.addAttribute("href", "#")
				.addAttribute(
						"onClick",
						"document.getElementById('openidlogo').value = '" + uri
								+ "';return false;").addText(name);
		return result;
	}

	@Override
	public void addLoginPage(String method, Element page,
			Map<String, String[]> parameters) {
		if (!LoginManager.LOGINMETHOD.equals(method))
			return;
		Element div = page.element("body").addElement("div");
		Element p = div.addElement("p");
		p.addText("Login using your ");
		p.addElement("a").addAttribute("href", "http://www.openid.net")
				.addText("OpenID");
		/*
		 * p.addElement("br"); p.add(createOpenIdLink("Facebook"));
		 * p.addText(" | "); p.add(createOpenIdLink("Twitter"));
		 * p.addText(" | "); p.add(createOpenIdLink("LinkedIn"));
		 * p.addText(" | "); p.add(createOpenIdLink("Windows Live",
		 * "live.anyopenid.com")); p.addText(" | ");
		 * p.add(createOpenIdLink("MySpace"));
		 */
		Element table = div.addElement("form")
				.addAttribute("action", "?method=openid")
				.addAttribute("method", "POST").addElement("table");
		Element tr;
		tr = table.addElement("tr");
		tr.addElement("td").addText("OpenID:");
		tr.addElement("td").addElement("input")
				.addAttribute("id", "openidlogo")
				.addAttribute("name", "openidurl").addAttribute("type", "text")
				.addAttribute("size", "30");
		tr = table.addElement("tr");
		tr.addElement("td");
		tr.addElement("td").addElement("input").addAttribute("name", "submit")
				.addAttribute("type", "submit").addAttribute("value", "Login");
	}

	@Override
	public void doLoginPage(String method, Resource resource) {
		if ("openidauth".equals(method))
			doAuthenticate(resource);
		if (!"openid".equals(method))
			return;

		String openid = GaleContext.req(resource).getParameter("openidurl");
		if (openid == null || "".equals(openid)) {
			DefaultLoginManager.error(GaleContext.resp(resource), "Unable to login",
					"No valid OpenID entered");
			return;
		}

		doOpenIdLogin(resource, openid);
	}

	public static final class MyAxMessage extends AxMessage {
		@Override
		public String getTypeUri() {
			return OpenIdLoginHandler.OPENID_NS_AX;
		}
	}

	static {
		try {
			Message.addExtensionFactory(MyAxMessage.class);
		} catch (MessageException e) {
			log.debug(e);
		}
	}

	private static final String OPENID_NS_AX = "http://openid.net/srv/ax/1.0";

	private void doAuthenticate(Resource resource) {
		GaleContext context = GaleContext.of(resource);
		HttpServletRequest req = context.req();
		HttpServletResponse resp = context.resp();

		try {
			ParameterList response = new ParameterList(req.getParameterMap());
			DiscoveryInformation discovered = (DiscoveryInformation) req
					.getSession().getAttribute("openid.discovered");
			ConsumerManager manager = (ConsumerManager) req.getSession()
					.getAttribute("openid.consumerManager");

			StringBuffer receivingURL = req.getRequestURL();
			String queryString = req.getQueryString();
			if (queryString != null && queryString.length() > 0)
				receivingURL.append("?").append(req.getQueryString());

			VerificationResult verification = manager.verify(
					receivingURL.toString(), response, discovered);

			Identifier verified = verification.getVerifiedId();
			if (verified == null)
				throw new IllegalArgumentException("unable to verify OpenID");
			AuthSuccess authSuccess = (AuthSuccess) verification
					.getAuthResponse();
			StringBuilder name = new StringBuilder((String) req.getSession()
					.getAttribute("openid.id"));
			StringBuilder email = new StringBuilder("email unknown");
			if (authSuccess.hasExtension(OPENID_NS_AX)) {
				FetchResponse fetchResp = (FetchResponse) authSuccess
						.getExtension(OPENID_NS_AX);
				fillAttribute(fetchResp, name, email);
			}
			doLoginUsingId(context, verified.getIdentifier(), name.toString(),
					email.toString());
		} catch (Exception e) {
			e.printStackTrace();
			DefaultLoginManager.error(
					resp,
					"Unable to login",
					"Error retrieving OpenID provider's response: "
							+ e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	private void fillAttribute(FetchResponse fetchResp, StringBuilder name,
			StringBuilder email) {
		String nameFirst = null;
		String nameLast = null;
		String namePerson = null;
		String email1 = null;
		String email2 = null;
		for (Map.Entry<String, List<String>> entry : (Set<Map.Entry<String, List<String>>>) fetchResp
				.getAttributes().entrySet()) {
			if (entry.getKey().endsWith("nameFirst"))
				nameFirst = (entry.getValue().size() > 0 ? entry.getValue()
						.get(0) : null);
			if (entry.getKey().endsWith("nameLast"))
				nameLast = (entry.getValue().size() > 0 ? entry.getValue().get(
						0) : null);
			if (entry.getKey().endsWith("namePerson")
					|| entry.getKey().endsWith("alias1"))
				namePerson = (entry.getValue().size() > 0 ? entry.getValue()
						.get(0) : null);
			if (entry.getKey().endsWith("email1"))
				email1 = (entry.getValue().size() > 0 ? entry.getValue().get(0)
						: null);
			if (entry.getKey().endsWith("email2"))
				email2 = (entry.getValue().size() > 0 ? entry.getValue().get(0)
						: null);
		}
		if (namePerson != null) {
			name.delete(0, name.length());
			name.append(namePerson);
		} else if (nameFirst != null && nameLast != null) {
			name.delete(0, name.length());
			name.append(nameFirst);
			name.append(" ");
			name.append(nameLast);
		}
		if (email1 != null) {
			email.delete(0, email.length());
			email.append(email1);
		} else if (email2 != null) {
			email.delete(0, email.length());
			email.append(email2);
		}
	}

	private void doLoginUsingId(GaleContext context, String identifier,
			String name, String email) {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("name", name);
		parameters.put("email", email);
		DefaultLoginManager.registerUser(identifier, "", parameters, context);
		context.addCookie("OpenIdLogin", identifier);
		DefaultLoginManager.doRedirect(context);
	}

	private void doOpenIdLogin(Resource resource, String openid) {
		GaleContext context = GaleContext.of(resource);
		HttpServletRequest req = context.req();
		HttpServletResponse resp = context.resp();

		try {
			String returnToUrl = GaleUtil.getContextURL(req)
					+ "/login?method=openidauth&http=post";
			ConsumerManager manager = new ConsumerManager();
			req.getSession().setAttribute("openid.consumerManager", manager);
			List<?> discoveries = manager.discover(openid);
			DiscoveryInformation discovered = manager.associate(discoveries);
			req.getSession().setAttribute("openid.discovered", discovered);
			req.getSession().setAttribute("openid.id", openid);
			AuthRequest authReq = manager.authenticate(discovered, returnToUrl);
			FetchRequest fetch = new FetchRequest() {
				@Override
				public String getTypeUri() {
					return OpenIdLoginHandler.OPENID_NS_AX;
				}
			};
			fetch.addAttribute("nameFirst",
					"http://axschema.org/namePerson/first", true);
			fetch.addAttribute("nameLast",
					"http://axschema.org/namePerson/last", true);
			fetch.addAttribute("namePerson", "http://axschema.org/namePerson",
					true);
			fetch.addAttribute("email1", "http://axschema.org/contact/email",
					true);
			fetch.addAttribute("email2",
					"http://schema.openid.net/contact/email", true);
			authReq.addExtension(fetch);
			resp.sendRedirect(authReq.getDestinationUrl(true));
		} catch (Exception e) {
			try {
				e.printStackTrace();
				DefaultLoginManager.error(resp, "Unable to login",
						"Error contacting OpenID provider: " + e.getMessage());
			} catch (RuntimeException ne) {
				e.printStackTrace();
				throw ne;
			}
		}
	}

	@Override
	public void logout(Resource resource) {
		GaleContext context = GaleContext.of(resource);
		if (context.getCookie("OpenIdLogin") != null)
			context.addCookie("OpenIdLogin", null, 0);
	}
}
