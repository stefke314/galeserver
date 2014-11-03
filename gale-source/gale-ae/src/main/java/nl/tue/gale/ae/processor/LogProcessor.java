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
 * LogProcessor.java
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
package nl.tue.gale.ae.processor;

import java.util.Date;

import nl.tue.gale.ae.AbstractResourceProcessor;
import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.ProcessorException;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.common.cache.CacheSession;
import nl.tue.gale.um.data.EntityValue;

public class LogProcessor extends AbstractResourceProcessor {
	public void processResource(Resource resource) throws ProcessorException {
		GaleContext gale = GaleContext.of(resource);
		if (gale.isObject()) return;
		StringBuffer sb = new StringBuffer();
		sb.append("\"");
		sb.append(gale.userId());
		sb.append("\";\"");
		sb.append((new Date()).toString());
		sb.append("\";");
		if (gale.servletAccess()) {
			sb.append("\"");
			sb.append(gale.req().getRequestURL());
			sb.append("\";");
			String referer = gale.req().getHeader("Referer");
			if (referer != null) {
				sb.append("\"");
				sb.append(referer);
				sb.append("\";");
			} else {
				sb.append(";");
			}
		} else {
			sb.append(";;");
		}
		sb.append("\"");
		sb.append(gale.conceptUri());
		sb.append("\";\"");
		sb.append(gale.url());
		sb.append("\";\"");
		//Save the link class of the followed link in the referrer page
		sb.append(gale.req().getAttribute("linkClass"));
		sb.append("\";\"");
		//Save from which view the followed link was clicked in the referrer page
		sb.append(gale.req().getParameter("view"));
		sb.append("\"");
		
		gale.log().log("access", sb.toString());
	}
}
