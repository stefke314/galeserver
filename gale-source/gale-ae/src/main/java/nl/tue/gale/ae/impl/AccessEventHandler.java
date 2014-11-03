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
 * AccessEventHandler.java
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

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.ae.event.EventHandler;
import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.cache.CacheSession;
import nl.tue.gale.common.code.CodeManager;
import nl.tue.gale.dm.data.Concept;
import nl.tue.gale.um.UMCache;
import nl.tue.gale.um.data.EntityValue;

public class AccessEventHandler implements EventHandler {
	private final static String type = "access";
	private UMCache umCache = null;
	private CodeManager codeManager = null;

	public UMCache getUmCache() {
		return umCache;
	}

	public void setUmCache(UMCache umCache) {
		this.umCache = umCache;
	}

	public CodeManager getCodeManager() {
		return codeManager;
	}

	public void setCodeManager(CodeManager codeManager) {
		this.codeManager = codeManager;
	}

	public String getType() {
		return type;
	}

	public void fireEvent(Object source, Resource resource) {
		if (!(source instanceof Concept))
			return;
		Concept c = (Concept) source;
		String eventCode = c.getProperty("event");
		if ((eventCode == null) || ("".equals(eventCode)))
			return;
		GaleContext gale = GaleContext.of(resource);
		CacheSession<EntityValue> session = umCache.openSession();
		session.setBaseUri(GaleUtil.addUserInfo(c.getUri(),
				GaleContext.userId(resource)));
		gale.exec(session, eventCode);
		session.commit();
	}
}