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
 * PluginProcessor.java
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

import java.util.Map;

import nl.tue.gale.ae.AbstractResourceProcessor;
import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.ProcessorException;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.ae.processor.plugin.Plugin;

import com.google.common.collect.ImmutableMap;

public class PluginProcessor extends AbstractResourceProcessor {
	private Map<String, Plugin> pluginTable = ImmutableMap.of();

	public void setPluginTable(Map<String, Plugin> pluginTable) {
		this.pluginTable = ImmutableMap.copyOf(pluginTable);
	}

	public Map<String, Plugin> getPluginTable() {
		return pluginTable;
	}

	public void processResource(Resource resource) throws ProcessorException {
		if (resource.isUsed("request"))
			return;
		GaleContext gale = GaleContext.of(resource);
		String plugin = gale.req().getParameter("plugin");
		if (plugin == null)
			return;
		Plugin p = pluginTable.get(plugin);
		if (p == null)
			throw new ProcessorException("plugin not found: '" + plugin + "'");
		if (gale.req().getMethod().equals("GET"))
			p.doGet(resource);
		if (gale.req().getMethod().equals("POST"))
			p.doPost(resource);
	}
}