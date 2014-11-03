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
 * ConceptStability.java
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

import javax.servlet.http.HttpSession;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.cache.CacheSession;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.dm.data.Concept;
import nl.tue.gale.um.data.EntityValue;

public class ConceptStability {
	public static String getStableData(GaleContext gale) {
		String stability = useStability(gale, gale.concept());
		if ("false".equals(stability))
			return null;
		URI stableDataURI = getStableDataURI(gale);
		if ("true".equals(stability)) {
			EntityValue ev = gale.um().get(stableDataURI);
			if (ev == null)
				return null;
			return (String) ev.getValue();
		} else if ("session".equals(stability)) {
			HttpSession session = gale.req().getSession();
			return (String) session.getAttribute(stableDataURI.toString());
		} else {
			throw new IllegalArgumentException("unknown stability value for '"
					+ stableDataURI + "': " + stability);
		}
	}

	public static void setStableData(GaleContext gale, String data) {
		if ("true".equals(gale.getResource().get("layout")))
			return;
		String stability = useStability(gale, gale.concept());
		if ("false".equals(stability))
			return;
		URI stableDataURI = getStableDataURI(gale);
		if ("true".equals(stability)) {
			EntityValue ev = new EntityValue(stableDataURI, data);
			CacheSession<EntityValue> session = gale.um().openSession();
			session.put(stableDataURI, ev);
			session.commit();
		} else if ("session".equals(stability)) {
			HttpSession session = gale.req().getSession();
			session.setAttribute(stableDataURI.toString(), data);
		} else {
			throw new IllegalArgumentException("unknown stability value for '"
					+ stableDataURI + "': " + stability);
		}
	}

	public static URI getStableDataURI(GaleContext gale) {
		URI stableDataURI = gale.conceptUri().resolve("#stableData");
		return GaleUtil.addUserInfo(stableDataURI, gale.userId());

	}

	private static String useStability(GaleContext gale, Concept concept) {
		if (concept == null)
			return "false";
		String stability = concept.getProperty("stability");
		if ("".equals(stability))
			return "false";
		if (!stability.equals("true") && !stability.equals("false")
				&& !stability.equals("session")) {
			// treat stability as expression
			try {
				stability = (String) eval(gale, stability);
			} catch (Exception e) {
				e.printStackTrace();
				return "false";
			}
		}
		return stability;
	}

	private static Object eval(GaleContext gale, String expr) {
		CacheSession<EntityValue> session = gale.openUmSession();
		return eval(gale, session, expr);
	}

	private static Object eval(GaleContext gale,
			CacheSession<EntityValue> session, String expr) {
		return gale.eval(session, expr);
	}
}
