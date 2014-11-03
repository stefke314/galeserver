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
 * AjaxPlugin.java
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

import java.io.PrintWriter;

import javax.servlet.http.HttpSession;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.ProcessorException;
import nl.tue.gale.ae.Resource;

public class AjaxPlugin extends AbstractPlugin {
	private static final String currentConceptId = "nl.tue.gale.ae.plugin.AjaxPlugin.currentConcept";
	private static final String currentTimeId = "nl.tue.gale.ae.plugin.AjaxPlugin.currentTime";

	public void doGet(Resource resource) throws ProcessorException {
		try {
			GaleContext gale = GaleContext.of(resource);
			if (!gale.servletAccess())
				return;
			String id = gale.req().getParameter("documentId");
			if (id == null || "".equals(id))
				id = gale.req().getSession().getId();
			boolean update = gale.um().pollUpdated(gale.userId(), id);
			if (!update) {
				gale.resp().setContentType("text/html");
				gale.resp().setBufferSize(4096);
				PrintWriter out = gale.resp().getWriter();
				out.print("no data");
				out.close();
				gale.usedResponse();
				// System.out.println("no data for: " + id);
			} else {
				resource.put("nl.tue.gale.ae.processor.AjaxProcessor.plugin",
						"true");
				// System.out.println("found data! for: " + id);
			}
			long elapsed = 0;
			HttpSession session = gale.req().getSession();
			String currentConcept = (String) session
					.getAttribute(currentConceptId);
			long currentTime = (Long) session.getAttribute(currentTimeId);
			if (!gale.concept().getUriString().equals(currentConcept)) {
				currentConcept = gale.concept().getUriString();
			} else {
				elapsed = System.currentTimeMillis() - currentTime;
			}
			currentTime = System.currentTimeMillis();
			session.setAttribute(currentConceptId, currentConcept);
			session.setAttribute(currentTimeId, currentTime);
			if (elapsed > 0) {
				resource.put("nl.tue.gale.ae.processor.AjaxProcessor.elapsed",
						elapsed);
				gale.em().fireEvent("ajax", gale.concept(), resource);
			}
		} catch (Exception e) {
			throw new ProcessorException("error in ajax plugin: "
					+ e.getMessage(), e);
		}
	}
}
