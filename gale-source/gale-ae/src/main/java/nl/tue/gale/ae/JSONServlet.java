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
 * JSONServlet.java
 * Last modified: $Date$
 * In revision:   $Revision$
 * Modified by:   $Author$
 *
 * Copyright (c) 2011 Eindhoven University of Technology.
 * All Rights Reserved.
 *
 * This software is proprietary information of the Eindhoven University
 * of Technology. It may be used according to the GNU LGPL license.
 */
package nl.tue.gale.ae;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.tue.gale.common.cache.CacheSession;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.common.uri.URIs;
import nl.tue.gale.um.UMCache;
import nl.tue.gale.um.data.EntityValue;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.sdicons.json.model.JSONArray;
import com.sdicons.json.model.JSONObject;
import com.sdicons.json.model.JSONString;
import com.sdicons.json.model.JSONValue;
import com.sdicons.json.parser.JSONParser;

public class JSONServlet extends HttpServlet {
	private static final long serialVersionUID = 779878029189269484L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		ApplicationContext applicationContext = WebApplicationContextUtils
				.getRequiredWebApplicationContext(getServletContext());

		UMCache um = (UMCache) applicationContext.getBean("umCache");
		CacheSession<EntityValue> session = um.openSession();
		try {
			JSONParser parser = new JSONParser(req.getInputStream());
			JSONArray array = (JSONArray) parser.nextValue();
			for (JSONValue value : array.getValue())
				addToUM((JSONObject) value, session);
			session.commit();
		} catch (Exception e) {
			e.printStackTrace();
			error(e, resp);
		} finally {
			if (session.isOpen())
				session.rollback();
		}
		reply("succes", resp);
	}

	private void error(Exception e, HttpServletResponse resp) {
		try {
			reply(e.toString(), resp);
		} catch (Exception ie) {
			throw new IllegalStateException("unable to send reply", ie);
		}
	}

	private void reply(String s, HttpServletResponse resp) throws IOException {
		resp.setBufferSize(8192);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				resp.getOutputStream()));
		bw.write(s);
		bw.close();
	}

	private void addToUM(JSONObject object, CacheSession<EntityValue> session) {
		String user = ((JSONString) object.get("user")).getValue();
		String name = ((JSONString) object.get("name")).getValue();
		String value = ((JSONString) object.get("value")).getValue();
		URI uri = URIs.of("gale://" + user + "@gale.tue.nl/personal/");
		uri = uri.resolve(name).resolve("#value");
		session.put(uri, new EntityValue(uri, value));
		System.out.println("set um from json '" + uri + "': " + value);
	}
}
