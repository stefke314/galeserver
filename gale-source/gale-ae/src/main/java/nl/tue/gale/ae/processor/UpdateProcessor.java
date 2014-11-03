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
 * UpdateProcessor.java
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

import nl.tue.gale.ae.AbstractResourceProcessor;
import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.ProcessorException;
import nl.tue.gale.ae.Resource;

public class UpdateProcessor extends AbstractResourceProcessor {
	public void processResource(Resource resource) throws ProcessorException {
		if (resource.isUsed("request"))
			return;
		GaleContext gale = GaleContext.of(resource);
		if ("true".equals(resource
				.get("nl.tue.gale.ae.processor.UpdateProcessor.noUpdate")))
			return;
		if ("true".equals(gale.req().getParameter("no-update")))
			return;
		try {
			gale.em().fireEvent("access", gale.dm().get(gale.conceptUri()),
					resource);
		} catch (Exception e) {
			throw new ProcessorException("unable to update profile for '"
					+ gale.conceptUri() + "'", e);
		}
	}
}