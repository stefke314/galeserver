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
 * AEServiceImpl.java
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

import java.util.Arrays;
import java.util.List;

import nl.tue.gale.dm.DMCache;
import nl.tue.gale.dm.data.Concept;
import nl.tue.gale.event.AbstractEventListener;
import nl.tue.gale.um.UMCache;
import nl.tue.gale.um.data.EntityValue;

import com.google.common.collect.ImmutableList;

public class AEServiceImpl extends AbstractEventListener {
	private UMCache um = null;
	private DMCache dm = null;

	public UMCache getUm() {
		return um;
	}

	public void setUm(UMCache um) {
		this.um = um;
	}

	public DMCache getDm() {
		return dm;
	}

	public void setDm(DMCache dm) {
		this.dm = dm;
	}

	private List<String> event_updateum(List<String> params) {
		um.cacheUpdate(EntityValue.fromEvent(params, um));
		return Arrays.asList(new String[] { "result:ok" });
	}

	private List<String> event_updatedm(List<String> params) {
		dm.cacheUpdate(Concept.fromEvent(params, dm));
		return Arrays.asList(new String[] { "result:ok" });
	}

	private List<String> event_ccae(List<String> params) {
		dm.invalidate();
		um.invalidate();
		return ImmutableList.of("result:ok");
	}

	protected void init() {
	}

	protected String getMethods() {
		return "updateum;updatedm;ccae";
	}

	@Override
	public List<String> event(String method, List<String> params) {
		List<String> result = super.event(method, params);
		if (result != null)
			return result;
		try {
			if ("updateum".equals(method))
				return event_updateum(params);
			if ("updatedm".equals(method))
				return event_updatedm(params);
			if ("ccae".equals(method))
				return event_ccae(params);
		} catch (Exception e) {
			return error(e);
		}
		throw new UnsupportedOperationException("'" + method
				+ "' method not supported");
	}
}
