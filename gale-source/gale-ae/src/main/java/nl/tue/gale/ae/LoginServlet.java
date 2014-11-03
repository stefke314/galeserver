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
 * LoginServlet.java
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

import static nl.tue.gale.common.GaleUtil.sendToClient;
import static nl.tue.gale.common.GaleUtil.serializeXML;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = -7908411588825917063L;

	private volatile ApplicationContext context = null;

	private ApplicationContext getApplicationContext() {
		if (context == null) {
			context = WebApplicationContextUtils
					.getRequiredWebApplicationContext(getServletContext());
		}
		return context;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		if ("post".equals(req.getParameter("http"))) {
			doPost(req, resp);
			return;
		}
		LoginManager login = (LoginManager) getApplicationContext().getBean(
				"loginManager");
		@SuppressWarnings("unchecked")
		Map<String, String[]> parameterMap = (Map<String, String[]>) req
				.getParameterMap();
		String loginPage = serializeXML(login.getLoginPage(
				req.getParameter("method"), parameterMap));
		sendToClient(resp,
				new ByteArrayInputStream(loginPage.getBytes("UTF-8")),
				"text/html", "UTF-8");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		LoginManager login = (LoginManager) getApplicationContext().getBean(
				"loginManager");
		ProcessorManager pm = (ProcessorManager) getApplicationContext()
				.getBean("processorManager");
		login.doLoginPage(pm.createResource(req, resp));
		if (!resp.isCommitted())
			resp.sendError(501);
	}
}