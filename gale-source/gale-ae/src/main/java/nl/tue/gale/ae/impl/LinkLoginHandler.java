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
 * LinkLoginHandler.java
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
import java.util.Map;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.LoginHandler;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.cache.CacheSession;
import nl.tue.gale.um.data.UserEntity;

import org.dom4j.Element;

public class LinkLoginHandler implements LoginHandler {
	private String psk = null;

	public String getPsk() {
		return psk;
	}

	public void setPsk(String psk) {
		this.psk = psk;
	}

	public String getLoginName(Resource resource) {
		GaleContext context = GaleContext.of(resource);

		// test the validity of the request
		String username = null;
		if (psk.equals(context.req().getParameter("psk")))
			username = context.req().getParameter("user");
		if (username == null)
			return null;

		// try to get or create a profile
		UserEntity entity = context.uec()
				.get(UserEntity.getUriFromId(username));
		if (entity == null) {
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("name", username);
			parameters.put("email", "email unknown");
			DefaultLoginManager.registerUser(username, GaleUtil.digest(psk),
					parameters, context);
			entity = context.uec().get(UserEntity.getUriFromId(username));
		}
		if (!"true".equals(entity.getProperty("linked"))) {
			CacheSession<UserEntity> entitySession = context.uec()
					.openSession();
			entity.setProperty("linked", "true");
			entitySession.put(UserEntity.getUriFromId(username), entity);
			entitySession.commit();
		}
		return username;
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
