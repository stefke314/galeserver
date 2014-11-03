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
 * LoginHandler.java
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

import java.util.Map;

import org.dom4j.Element;

public interface LoginHandler {
	/**
	 * Returns the unique identifier for the user accessing the
	 * <code>resource</code>. The identifier must be properly encoded using
	 * <code>java.net.URLEncoder</code>. If the identifier cannot be retrieved
	 * within this servlet request, this method may return <code>null</code> and
	 * modify the <code>resource</code> in such a way that allows subsequent
	 * requests to retrieve the identifier (i.e., display a login screen and
	 * gather user information).
	 * 
	 * @param resource
	 *            the currently active resource
	 * @return the unique user identifier or <code>null</code>
	 */
	public String getLoginName(Resource resource);

	public void addLoginPage(String method, Element page,
			Map<String, String[]> parameters);

	public void doLoginPage(String method, Resource resource);

	public void logout(Resource resource);
}
