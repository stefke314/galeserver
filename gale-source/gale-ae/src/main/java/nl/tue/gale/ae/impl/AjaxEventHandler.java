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
 * AjaxEventHandler.java
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

import java.io.IOException;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.ae.event.EventHandler;
import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.dm.data.Concept;
import nl.tue.gale.um.data.EntityValue;

public class AjaxEventHandler implements EventHandler {
	private final static String type = "ajax";

	public void fireEvent(Object source, Resource resource) {
		if (!(source instanceof Concept))
			return;
		Concept c = (Concept) source;
		GaleContext gale = GaleContext.of(resource);
		URI uri = GaleUtil.addUserInfo(c.getUri().resolve("#system.elapsed"),
				gale.userId());
		Long elapsed = (Long) resource
				.get("nl.tue.gale.ae.processor.AjaxProcessor.elapsed");
		EntityValue ev = new EntityValue(uri, elapsed);
		try {
			gale.ebc().event("async:setum", EntityValue.toEvent(ev));
		} catch (IOException e) {
			throw new IllegalArgumentException("unable to handle ajax event: "
					+ e.getMessage(), e);
		}
	}

	public String getType() {
		return type;
	}
}
