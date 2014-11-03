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
 * SAMLServlet.java
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.iharder.Base64;
import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.ProcessorManager;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.cache.CacheSession;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.common.uri.URIs;
import nl.tue.gale.um.UMCache;
import nl.tue.gale.um.data.EntityValue;
import nl.tue.gale.um.data.UserEntity;

import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class SAMLServlet extends HttpServlet {
	private static final long serialVersionUID = -5470263051514880768L;

	private GaleContext getGaleContext(HttpServletRequest req,
			HttpServletResponse resp) {
		ApplicationContext applicationContext = WebApplicationContextUtils
				.getRequiredWebApplicationContext(getServletContext());
		ProcessorManager pm = (ProcessorManager) applicationContext
				.getBean("processorManager");
		Resource resource = pm.createResource(req, resp);
		return GaleContext.of(resource);
	}

	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String sxml = new String(
				Base64.decode(req.getParameter("SAMLResponse")), "UTF-8");
		BufferedReader br = new BufferedReader(new StringReader(sxml));
		Document xml = GaleUtil.parseXML(br);
		String userid = null;
		String email = "unknown email";
		String name = "unknown user";
		String firstname = null;
		String lastname = null;
		for (Element assertion : (List<Element>) xml.getRootElement().elements(
				"Assertion")) {
			for (Element attributeStatement : (List<Element>) assertion
					.elements("AttributeStatement")) {
				for (Element attribute : (List<Element>) attributeStatement
						.elements("Attribute")) {
					String attrname = attribute.attributeValue("Name");
					String attrvalue = attribute.elementText("AttributeValue");
					if ("urn:oid:2.5.4.4".equals(attrname))
						lastname = attrvalue;
					if ("urn:oid:2.5.4.3".equals(attrname))
						userid = attrvalue;
					if ("urn:oid:0.9.2342.19200300.100.1.3".equals(attrname))
						email = attrvalue;
					if ("urn:oid:2.5.4.42".equals(attrname))
						firstname = attrvalue;
				}
			}
		}
		if (firstname == null && lastname == null) {
			if (userid != null)
				name = userid;
		} else if (firstname == null)
			name = lastname;
		else if (lastname == null)
			name = firstname;
		else
			name = firstname + " " + lastname;

		GaleContext gc = getGaleContext(req, resp);
		UserEntityCache userEntityCache = gc.uec();
		UMCache umCache = gc.um();
		CacheSession<UserEntity> entitySession = userEntityCache.openSession();
		UserEntity entity = entitySession.get(UserEntity.getUriFromId(userid));
		if (entity == null) {
			CacheSession<EntityValue> umSession = umCache.openSession();
			entity = new UserEntity(userid);
			entity.setProperty("password", GaleUtil.adminpw);
			entitySession.put(entity.getUri(), entity);
			URI uri = URIs.of("gale://" + entity.getId()
					+ "@gale.tue.nl/personal");
			umSession.put(uri.resolve("#name"),
					EntityValue.create(uri.resolve("#name"), name));
			umSession.put(uri.resolve("#email"),
					EntityValue.create(uri.resolve("#email"), email));
			umSession.commit();
		}
		entitySession.commit();

		if (userid != null) {
			req.getSession().setAttribute("nl.tue.gale.userId", userid);
			resp.sendRedirect(req.getParameter("RelayState"));
		} else {
			throw new IllegalArgumentException("unable to login: " + sxml);
		}
	}
}
