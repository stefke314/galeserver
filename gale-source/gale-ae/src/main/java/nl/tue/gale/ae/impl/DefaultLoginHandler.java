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
 * DefaultLoginHandler.java
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

import static nl.tue.gale.common.GaleUtil.adminpw;
import static nl.tue.gale.common.GaleUtil.digest;
import static nl.tue.gale.common.GaleUtil.getRequestURL;
import static nl.tue.gale.common.GaleUtil.newGUID;
import static nl.tue.gale.common.GaleUtil.notnull;
import static nl.tue.gale.common.GaleUtil.wrappedRequest;

import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.LoginHandler;
import nl.tue.gale.ae.LoginManager;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.common.uri.URIs;
import nl.tue.gale.um.data.UserEntity;

import org.dom4j.Element;

import com.google.common.collect.ImmutableList;

public class DefaultLoginHandler implements LoginHandler {
	@Override
	public String getLoginName(Resource resource) {
		return null;
	}

	@Override
	public void addLoginPage(String method, Element page,
			Map<String, String[]> parameters) {
		if ("register".equals(method))
			addRegisterPage(page, parameters);
		if (!LoginManager.LOGINMETHOD.equals(method))
			return;
		Element div = page.element("body").addElement("div");
		Element p = div.addElement("p");
		p.addText("Leave empty if you want to log in anonymously");
		p.addElement("br");
		p.addElement("a").addAttribute("href", "?method=register")
				.addText("Register here");
		Element table = div.addElement("form")
				.addAttribute("action", "?method=default")
				.addAttribute("method", "POST").addElement("table");
		Element tr;
		tr = table.addElement("tr");
		tr.addElement("td").addText("Username:");
		tr.addElement("td").addElement("input")
				.addAttribute("id", "focusfield")
				.addAttribute("name", "username").addAttribute("type", "text")
				.addAttribute("size", "30");
		tr = table.addElement("tr");
		tr.addElement("td").addText("Password:");
		tr.addElement("td").addElement("input")
				.addAttribute("name", "password")
				.addAttribute("type", "password").addAttribute("size", "30");
		tr = table.addElement("tr");
		tr.addElement("td");
		tr.addElement("td").addElement("input").addAttribute("name", "submit")
				.addAttribute("type", "submit").addAttribute("value", "Login");
	}

	private void addRegisterPage(Element page, Map<String, String[]> parameters) {
		String username = getParameter(parameters, "username");
		String email = getParameter(parameters, "email");
		String name = getParameter(parameters, "name");
		String error = getParameter(parameters, "error");
		if (!"".equals(error))
			page.element("body").addElement("p").addAttribute("class", "error")
					.addText(error);
		Element div = page.element("body").addElement("div");
		div.addElement("p").addText("Please fill in this form to register");
		Element table = div.addElement("form")
				.addAttribute("action", "?method=register")
				.addAttribute("method", "POST").addElement("table");
		Element tr;
		tr = table.addElement("tr");
		tr.addElement("td").addText("Username:");
		tr.addElement("td").addElement("input")
				.addAttribute("id", "focusfield")
				.addAttribute("name", "username").addAttribute("type", "text")
				.addAttribute("value", username);
		tr = table.addElement("tr");
		tr.addElement("td").addText("Password:");
		tr.addElement("td").addElement("input")
				.addAttribute("name", "password")
				.addAttribute("type", "password");
		tr = table.addElement("tr");
		tr.addElement("td").addText("Confirm password:");
		tr.addElement("td").addElement("input").addAttribute("name", "confirm")
				.addAttribute("type", "password");
		tr = table.addElement("tr");
		tr.addElement("td").addText("Full name:");
		tr.addElement("td").addElement("input").addAttribute("name", "name")
				.addAttribute("type", "text").addAttribute("value", name);
		tr = table.addElement("tr");
		tr.addElement("td").addText("E-mail address:");
		tr.addElement("td").addElement("input").addAttribute("name", "email")
				.addAttribute("type", "text").addAttribute("value", email);
		tr = table.addElement("tr");
		tr.addElement("td");
		tr.addElement("td").addElement("input").addAttribute("name", "submit")
				.addAttribute("type", "submit")
				.addAttribute("value", "Register");
	}

	private String getParameter(Map<String, String[]> parameters, String name) {
		String[] list = parameters.get(name);
		if (list != null && list.length > 0)
			return list[0];
		return "";
	}

	@Override
	public void doLoginPage(String method, Resource resource) {
		if ("register".equals(method))
			doRegisterPage(resource);
		if (!"default".equals(method))
			return;

		GaleContext context = GaleContext.of(resource);
		HttpServletRequest req = context.req();
		String username = req.getParameter("username");
		username = (username == null ? "" : username.trim());

		boolean success;
		if ("".equals(username))
			success = doAnonymousLogin(context);
		else
			success = doPasswordLogin(context);

		if (success)
			DefaultLoginManager.doRedirect(context);
	}

	private void doRegisterPage(Resource resource) {
		GaleContext context = GaleContext.of(resource);
		HttpServletRequest req = context.req();

		String username = notnull(req.getParameter("username"));
		String password = notnull(req.getParameter("password"));
		String pwconfirm = notnull(req.getParameter("confirm"));
		String email = notnull(req.getParameter("email"));
		String name = notnull(req.getParameter("name"));

		// do some tests
		if (username.equals("")) {
			doRegisterError(context, "Username is required");
			return;
		}
		if (username.length() < 3) {
			doRegisterError(context, "Username too short");
			return;
		}
		if (username.startsWith("GALE_")) {
			doRegisterError(context, "Username is not available");
			return;
		}
		if (password.equals("")) {
			doRegisterError(context, "Password is required");
			return;
		}
		if (password.length() < 3) {
			doRegisterError(context, "Password too short");
			return;
		}
		if (!password.equals(pwconfirm)) {
			doRegisterError(context, "Passwords do not match");
			return;
		}
		UserEntity entity = context.uec()
				.get(UserEntity.getUriFromId(username));
		if (entity != null) {
			doRegisterError(context, "Username is not available");
			return;
		}

		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("name", name);
		parameters.put("email", email);
		@SuppressWarnings("unchecked")
		Map<String, String[]> parameterMap = (Map<String, String[]>) context
				.req().getParameterMap();
		for (String key : (Set<String>) parameterMap.keySet())
			if (!dkeys.contains(key))
				parameters.put(key, context.req().getParameter(key));
		DefaultLoginManager.registerUser(username, password, parameters, context);
		DefaultLoginManager.doRedirect(context);
	}

	private void doRegisterError(GaleContext context, String error) {
		try {
			URL url = URIs.of(
					getRequestURL(context.req()) + "&error="
							+ URLEncoder.encode(error, "UTF-8")).toURL();
			HttpServletRequest wrappedRequest = wrappedRequest(context.req(),
					url, "GET");
			context.sc().getRequestDispatcher("/login?method=register")
					.forward(wrappedRequest, context.resp());
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Unable to process registration", e);
		}
	}

	private boolean doPasswordLogin(GaleContext context) {
		HttpServletRequest req = context.req();
		HttpServletResponse resp = context.resp();
		String username = req.getParameter("username");
		String password = req.getParameter("password");

		UserEntity entity = context.uec()
				.get(UserEntity.getUriFromId(username));
		if (entity == null) {
			DefaultLoginManager
					.error(resp, "Unable to login",
							"Unable to find profile<br />Are you using the right username?");
			return false;
		}
		String pwcheck = entity.getProperty("password");
		if ((pwcheck == null) || ("".equals(pwcheck))) {
			DefaultLoginManager.error(resp, "Unable to login",
					"Invalid profile<br />Please contact the webmaster");
			return false;
		}
		if (digest(password).equals(adminpw)) {
			req.getSession().setAttribute("ahaadmin", password);
		} else {
			if (!pwcheck.equals(digest(password))) {
				DefaultLoginManager.error(resp, "Unable to login",
						"Wrong password<br />Please try again");
				return false;
			}
		}
		req.getSession().setAttribute("nl.tue.gale.userId", username);
		return true;
	}

	private static final List<String> dkeys = ImmutableList.of("username",
			"password", "method", "confirm", "redirect", "submit", "register",
			"login");

	private boolean doAnonymousLogin(GaleContext context) {
		String username = "";
		Cookie[] cookies = context.req().getCookies();
		if (cookies != null)
			for (Cookie cookie : cookies)
				if (cookie.getName().equals("GALEUser"))
					username = cookie.getValue();
		if (username.equals("")) {
			username = "GALE_" + newGUID();
			Cookie cookie = new Cookie("GALEUser", username);
			cookie.setMaxAge(63072000);
			context.resp().addCookie(cookie);
		}

		UserEntity entity = context.uec()
				.get(UserEntity.getUriFromId(username));
		if (entity == null) {
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("name", "Anonymous user");
			parameters.put("email", "email unknown");
			@SuppressWarnings("unchecked")
			Map<String, String[]> parameterMap = (Map<String, String[]>) context
					.req().getParameterMap();
			for (String key : (Set<String>) parameterMap.keySet())
				if (!dkeys.contains(key))
					parameters.put(key, context.req().getParameter(key));
			DefaultLoginManager.registerUser(username, "", parameters, context);
		} else {
			context.req().getSession()
					.setAttribute("nl.tue.gale.userId", username);
		}
		return true;
	}

	@Override
	public void logout(Resource resource) {
	}
}
