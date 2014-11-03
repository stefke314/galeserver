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
 * ProcessorConfig.java
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
package nl.tue.gale.ae.config;

import java.util.LinkedList;
import java.util.List;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.ae.ResourceProcessor;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.common.uri.URIs;

public class ProcessorConfig implements ConfigResolver {
	private static final URI configIdentifier = URIs
			.of("gale://gale.tue.nl/config/processor");

	public URI getConfigIdentifier() {
		return configIdentifier;
	}

	@SuppressWarnings("unchecked")
	public Object getObject_list(Resource resource) {
		GaleContext gc = GaleContext.of(resource);
		List<ResourceProcessor> list = (List<ResourceProcessor>) gc.ac()
				.getBean("processorList");
		List<ResourceProcessor> result = new LinkedList<ResourceProcessor>();
		if (gc.concept() != null)
			for (ResourceProcessor rp : list)
				if (!"false".equals(gc.concept().getProperty(
						rp.getClass().getName())))
					result.add(rp);
		return result;
	}

	@Override
	public Object getObject(String name, Resource resource) {
		if ("list".equals(name))
			return getObject_list(resource);
		throw new UnsupportedOperationException("'" + name + "' not supported");
	}
}
