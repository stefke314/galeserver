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
 * ExecPlugin.java
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
package nl.tue.gale.ae.processor.plugin;

import java.util.Map;

import javax.servlet.RequestDispatcher;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.ProcessorException;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.common.cache.CacheSession;
import nl.tue.gale.um.data.EntityValue;

public class ExecPlugin extends AbstractPlugin {
	@SuppressWarnings("unchecked")
	public void doPost(Resource resource) throws ProcessorException {
		GaleContext gale = GaleContext.of(resource);
		try {
			String guid = gale.req().getParameter("guid");
			Map<String, String[]> lru = (Map<String, String[]>) gale.req()
					.getSession().getAttribute("ExecPlugin:map");
			if (lru == null)
				return;

			String[] entry = lru.remove(guid);
			if (entry == null)
				return;
			CacheSession<EntityValue> session = gale.openUmSession();
			session.setBaseUri(session.resolve(entry[0]));
			gale.exec(session, entry[1]);
			session.commit();
		} catch (Exception e) {
			try {
				RequestDispatcher rd = gale.sc().getRequestDispatcher(
						"/ErrorServlet");
				gale.req()
						.getSession()
						.setAttribute(
								"exception",
								new ProcessorException(
										"unable to process update rules: "
												+ e.getMessage(), e));
				rd.forward(gale.req(), gale.resp());
				gale.usedResponse();
			} catch (Exception ee) {
				throw new ProcessorException(
						"unexpected error while trying to display the errorpage",
						ee);
			}
		}
	}

	public void doGet(Resource resource) throws ProcessorException {
		doPost(resource);
	}
}