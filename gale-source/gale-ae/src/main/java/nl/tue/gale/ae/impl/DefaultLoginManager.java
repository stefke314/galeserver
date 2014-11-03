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
 * LoginManager.java
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

import static com.google.common.base.Preconditions.checkNotNull;
import static nl.tue.gale.common.GaleUtil.createHTMLElement;
import static nl.tue.gale.common.GaleUtil.digest;
import static nl.tue.gale.common.GaleUtil.getContextURL;
import static nl.tue.gale.common.GaleUtil.setURIPart;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.LoginHandler;
import nl.tue.gale.ae.LoginManager;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.cache.CacheSession;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.common.uri.URIs;
import nl.tue.gale.um.data.EntityValue;
import nl.tue.gale.um.data.UserEntity;

import org.dom4j.Element;

import com.google.common.collect.ImmutableList;

public class DefaultLoginManager implements LoginManager {
	private static final Element defaultLogin;
	private static final String css = ""
			+ "body {text-align:center;font-family:Tahoma,Arial,sans-serif;} "
			+ "h1 {color:#2020A0;} "
			+ "div {margin-top:4em;margin-left:auto;margin-right:auto;display:table} "
			+ "p {text-align:center} "
			+ "table {margin-left:auto;margin-right:auto} "
			+ ".error {color:#D01010} "
			+ "input#openidlogo {background: url(images/openidlogo.gif) no-repeat;background-position: 1px 1px;padding-left: 20px}";

	static {
		defaultLogin = createHTMLElement("html");
		Element head = defaultLogin.addElement("head");
		head.addElement("title").addText("GALE Login");
		head.addElement("style").addText(css);
		head.addElement("script")
				.addAttribute("language", "javascript")
				.addText(
						"function putfocus() {document.getElementById('focusfield').focus();}");
		Element body = defaultLogin.addElement("body").addAttribute("onLoad",
				"putfocus();");
		body.addElement("h1").addText("GALE Login");
	}

	private List<LoginHandler> handlerList = null;

	/* (non-Javadoc)
	 * @see nl.tue.gale.ae.impl.LoginManager#setHandlerList(java.util.List)
	 */
	@Override
	public void setHandlerList(List<LoginHandler> handlerList) {
		checkNotNull(handlerList);
		this.handlerList = ImmutableList.copyOf(handlerList);
	}

	/* (non-Javadoc)
	 * @see nl.tue.gale.ae.impl.LoginManager#getHandlerList()
	 */
	@Override
	public List<LoginHandler> getHandlerList() {
		return handlerList;
	}

	/* (non-Javadoc)
	 * @see nl.tue.gale.ae.impl.LoginManager#setCss(java.lang.String)
	 */
	@Override
	public void setCss(String css) {
		defaultLogin.element("head").element("style").setText(css);
	}

	/* (non-Javadoc)
	 * @see nl.tue.gale.ae.impl.LoginManager#getCss()
	 */
	@Override
	public String getCss() {
		return defaultLogin.element("head").elementText("style");
	}

	/* (non-Javadoc)
	 * @see nl.tue.gale.ae.impl.LoginManager#getLoginPage(java.lang.String, java.util.Map)
	 */
	@Override
	public Element getLoginPage(String method, Map<String, String[]> parameters) {
		method = (method == null ? LOGINMETHOD : method);
		Element result = defaultLogin.createCopy();
		for (LoginHandler handler : handlerList)
			handler.addLoginPage(method, result, parameters);
		return result;
	}

	/* (non-Javadoc)
	 * @see nl.tue.gale.ae.impl.LoginManager#doLoginPage(nl.tue.gale.ae.Resource)
	 */
	@Override
	public void doLoginPage(Resource resource) {
		String method = GaleContext.req(resource).getParameter("method");
		if (method == null)
			method = LOGINMETHOD;
		for (LoginHandler handler : handlerList) {
			handler.doLoginPage(method, resource);
			if (GaleContext.resp(resource).isCommitted())
				return;
		}
	}

	/* (non-Javadoc)
	 * @see nl.tue.gale.ae.impl.LoginManager#getLoginName(nl.tue.gale.ae.Resource)
	 */
	@Override
	public String getLoginName(Resource resource) {
		HttpServletRequest req = (HttpServletRequest) resource.get("request");
		String result = (String) req.getSession().getAttribute(
				"nl.tue.gale.userId");
		if (result != null)
			return result;
		for (LoginHandler manager : handlerList) {
			if ((result = manager.getLoginName(resource)) != null) {
				req.getSession().setAttribute("nl.tue.gale.userId", result);
				return result;
			}
		}
		HttpServletResponse resp = (HttpServletResponse) resource
				.get("response");
		if (result == null && !resp.isCommitted()) {
			req.getSession().setAttribute("redirect", req.getRequestURI());
			try {
				resp.sendRedirect(getContextURL(req) + "/login");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see nl.tue.gale.ae.impl.LoginManager#logout(nl.tue.gale.ae.Resource)
	 */
	@Override
	public void logout(Resource resource) {
		GaleContext.req(resource).getSession().invalidate();
		for (LoginHandler handler : handlerList)
			handler.logout(resource);
	}

	public static void error(HttpServletResponse resp, String title,
			String error) {
		try {
			resp.setContentType("text/html");
			resp.setBufferSize(4096);
			PrintWriter out = resp.getWriter();
			out.println("<html><head><title>GALE Error: " + title
					+ "</title></head>");
			out.println("    <body style=\"font-family:Tahoma,Arial,sans-serif\">");
			out.println("        <h1 style=\"text-align:center;color:#D02020\">"
					+ title + "</h1>");
			out.println("        <p style=\"margin-bottom:100;text-align:center;font-size:16\">"
					+ error + "</p>");
			out.println("    </body>");
			out.println("</html>");
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException("unable to display error message: "
					+ title + ", " + error, e);
		}
	}

	public static void registerUser(String username, String password,
			Map<String, String> parameters, GaleContext context) {
		CacheSession<UserEntity> entitySession = context.uec().openSession();
		CacheSession<EntityValue> umSession = context.um().openSession();
		boolean commit = false;
		try {
			UserEntity entity = entitySession.get(UserEntity
					.getUriFromId(username));
			if (entity == null) {
				try {
					entity = new UserEntity(username);
				} catch (Exception e) {
					e.printStackTrace();
					DefaultLoginManager.error(context.resp(), "Unable to login",
							"Unable to create profile: " + e.getMessage()
									+ "<br />Please contact the webmaster");
					return;
				}
			}
			entity.setProperty("password", digest(password));
			entitySession.put(entity.getUri(), entity);
			URI uri = URIs.of("gale://gale.tue.nl/personal");
			uri = setURIPart(uri, GaleUtil.URIPart.USERINFO, username);
			for (Map.Entry<String, String> entry : parameters.entrySet()) {
				umSession.put(uri.resolve("#" + entry.getKey()), EntityValue
						.create(uri.resolve("#" + entry.getKey()),
								entry.getValue()));
			}
			context.req().getSession()
					.setAttribute("nl.tue.gale.userId", username);
			commit = true;
			return;
		} catch (Exception e) {
			e.printStackTrace();
			DefaultLoginManager.error(context.resp(), "Unable to login",
					"An unexpected error occured: " + e.getMessage());
			return;
		} finally {
			if (commit) {
				umSession.commit();
				entitySession.commit();
			} else {
				umSession.rollback();
				entitySession.rollback();
			}
		}
	}

	public static void doRedirect(GaleContext context) {
		try {
			String redirect = (String) context.req().getSession()
					.getAttribute("redirect");
			if (redirect != null) {
				context.resp().sendRedirect(redirect);
			} else {
				context.resp().setContentType("text/html");
				context.resp().setBufferSize(4096);
				PrintWriter out = context.resp().getWriter();
				out.println("<html><head><title>Gale Login</title></head>");
				out.println("    <body style=\"font-family:Tahoma,Arial,sans-serif\">");
				out.println("        <h1 style=\"text-align:center;color:#2020A0\">Gale Login</h1>");
				out.println("        <p style=\"margin-bottom:100;text-align:center;font-size:16\">You are succesfully logged in</p>");
				out.println("    </body>");
				out.println("</html>");
				out.close();
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to redirect", e);
		}
	}
}
