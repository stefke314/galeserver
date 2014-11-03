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
 * GaleServlet.java
 * Last modified: $Date$
 * In revision:   $Revision$
 * Modified by:   $Author: dsmits $
 *
 * Copyright (c) 2008-2011 Eindhoven University of Technology.
 * All Rights Reserved.
 *
 * This software is proprietary information of the Eindhoven University
 * of Technology. It may be used according to the GNU LGPL license.
 */
package nl.tue.gale.ae;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * The main class of the AHA engine system. All URLs that should be handled by
 * AHA! should be directed to this servlet. This can be done by setting the
 * appropriate url-pattern in web.xml.
 * <p>
 * AHA! relies on a file called ahaconfig.xml located in the WEB-INF directory
 * of the web package containing AHA!. This file's root element should be named
 * <code>ahaconfig</code>. It should contain at least two sub-elements called
 * <code>LoginManager</code> and <code>ConceptManager</code>. These sub-elements
 * should contain a text node specifying the java class that implements
 * <code>LoginManager</code> and <code>ConceptManager</code>.
 * <p>
 * AHA! will create a new <code>Resource</code> object for the specified URL.
 * The <code>HttpServletRequest</code>, <code>HttpServletResponse</code> and
 * <code>ServletContext</code> are added to the resource as <code>request
 * </code>, <code>response</code> and <code>context</code> respectively.
 * <p>
 * If there is no profile in the session (to be specific, if the attribute named
 * <code>profile</code> does not exist in the session), then the <code>
 * LoginManager</code> found in ahaconfig.xml is called to provide a profile. If
 * a profile is returned this is stored in the session and AHA! continues. If no
 * profile is returned AHA! will assume that the LoginManager has written to the
 * <code>HttpServletResponse</code> and return.
 * <p>
 * The <code>Profile</code> object found in the session is added to the resource
 * using the name <code>profile</code>.
 * <p>
 * The <code>ConceptManager</code> is called to add conceptual information to
 * the resource and provide a list of 'ConceptConfig.xml' files to be loaded and
 * merged sequentially. The resulting configuration <code>Element</code> is
 * added to the resource using the name <code>config</code>.
 * <p>
 * The configuration element is used to determine the <code>ResourceProcessor
 * </code> objects to call. See <code>ConfigManager</code> for details on this
 * process.
 * <p>
 * After the appropriate <code>ResourceProcessor</code> objects have been
 * called, the level of the resource (to be specific, <code>Resource.getLevel
 * </code>) is used to determine the next action. If the level is 0, AHA! tries
 * to load the URL directly and will store the <code>InputStream</code> in the
 * resource using the name <code>stream</code>. If the level is less than 100
 * AHA! will assume that the <code>HttpServletResponse</code> has not yet been
 * written to. The contents of the resource variable named <code>stream</code>
 * of type <code>InputStream</code> is send to the client.
 * 
 * @author David Smits
 * @see ConceptManager
 * @see DefaultConfigManager
 * @see LoginHandler
 * @see Resource
 * @since 3.5
 * @version 4.0
 */
public final class GaleServlet extends HttpServlet {
	private static final long serialVersionUID = 3067487096267490893L;

	protected final void service(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		ApplicationContext applicationContext = WebApplicationContextUtils
				.getRequiredWebApplicationContext(getServletContext());

		GaleServletBean gsb = (GaleServletBean) applicationContext
				.getBean("galeServletBean");
		gsb.service(req, resp);
	}
}