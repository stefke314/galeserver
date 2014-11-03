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
 * PasswordPlugin.java
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.ProcessorException;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.cache.CacheSession;
import nl.tue.gale.um.data.UserEntity;

public class PasswordPlugin extends AbstractPlugin {
	public void doPost(Resource resource) throws ProcessorException {
		doGet(resource);
	}

	public void doGet(Resource resource) throws ProcessorException {
		GaleContext gale = GaleContext.of(resource);
		String oldpw = gale.req().getParameter("oldpw");
		String newpw = gale.req().getParameter("newpw");
		String newpw2 = gale.req().getParameter("newpw2");
		String redirect = gale.req().getParameter("redirect");
		if (redirect == null)
			redirect = "";
		String error = null;

		// test provided passwords
		String storedpw = gale.uec().get(gale.userUri())
				.getProperty("password");
		if (newpw.trim().equals(""))
			error = "new password missing";
		else if (!newpw.equals(newpw2))
			error = "passwords do not match";
		else if (newpw.length() < 5)
			error = "password too small";
		else if (!GaleUtil.digest(oldpw).equals(storedpw))
			error = "old password is invalid";

		if (error == null) {
			// update password
			CacheSession<UserEntity> session = gale.uec().openSession();
			UserEntity ue = session.get(gale.userUri());
			ue.setProperty("password", GaleUtil.digest(newpw));
			session.put(gale.userUri(), ue);
			session.commit();
		}

		if (error != null) {
			// return error
			try {
				gale.resp()
						.sendRedirect(
								gale.req().getRequestURL() + "?redirect="
										+ URLEncoder.encode(redirect, "UTF-8")
										+ "&error="
										+ URLEncoder.encode(error, "UTF-8"));
			} catch (Exception e) {
				throw new IllegalArgumentException(error + " (and "
						+ e.getMessage() + ")", e);
			}
			gale.usedResponse();
		} else if (redirect == null || "".equals(redirect)) {
			// display success message
			String text = "<span>Password updated succesfully!</span>";
			try {
				resource.put("stream",
						new ByteArrayInputStream(text.getBytes("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				throw new IllegalArgumentException(
						"unable to service password update request", e);
			}
			resource.put("mime", "text/html");
			gale.usedRequest();
		} else {
			// redirect to page
			try {
				gale.resp().sendRedirect(redirect);
			} catch (IOException e) {
				throw new IllegalArgumentException(
						"unable to service password update request", e);
			}
			gale.usedResponse();
		}
	}
}
