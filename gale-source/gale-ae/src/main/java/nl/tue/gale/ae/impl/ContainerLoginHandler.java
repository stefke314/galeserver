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
 * ContainerLoginHandler.java
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

import java.security.Principal;
import java.util.Map;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.LoginHandler;
import nl.tue.gale.ae.Resource;

import org.dom4j.Element;

public class ContainerLoginHandler implements LoginHandler {
	@Override
	public String getLoginName(Resource resource) {
		@SuppressWarnings("unused")
		Principal principal = GaleContext.req(resource).getUserPrincipal();
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
