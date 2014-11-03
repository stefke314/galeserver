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
 * ConfigManager.java
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
package nl.tue.gale.ae.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.tue.gale.ae.Resource;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.common.uri.URIs;

public class ConfigManager {
	private Map<URI, ConfigResolver> resolveMap = new HashMap<URI, ConfigResolver>();

	public void setResolveList(List<ConfigResolver> resolveList) {
		resolveMap.clear();
		for (ConfigResolver cr : resolveList)
			resolveMap.put(cr.getConfigIdentifier(), cr);
	}

	public Object getObject(URI configIdentifier, String name, Resource resource) {
		try {
			return resolveMap.get(configIdentifier).getObject(name, resource);
		} catch (Exception e) {
			throw new IllegalArgumentException("no configuration found for '"
					+ configIdentifier + "#" + name + "'", e);
		}
	}

	public Object getObject(URI attribute, Resource resource) {
		URI configIdentifier;
		try {
			configIdentifier = URIs.of(attribute.getScheme(),
					attribute.getAuthority(), attribute.getPath(),
					attribute.getQuery(), null);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"unable to parse attribute URI: '" + attribute + "'", e);
		}
		return getObject(configIdentifier, attribute.getFragment(), resource);
	}

	public Object getObject(String attribute, Resource resource) {
		return getObject(URIs.of(attribute), resource);
	}
}
