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
 * ConfigResolver.java
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

import nl.tue.gale.ae.Resource;
import nl.tue.gale.common.uri.URI;

public interface ConfigResolver {
	/**
	 * Retrieves the named configuration object from the <code>resource</code>.
	 * 
	 * @param name
	 *            the name of the object to retrieve
	 * @param resource
	 *            the <code>resource</code> from which to retrieve the
	 *            configuration
	 * @return
	 */
	public Object getObject(String name, Resource resource);

	/**
	 * Returns the <code>URI</code> identifying this <code>ConfigResolver</code>
	 * within the {@link ConfigManager}.
	 * 
	 * @return the <code>URI</code> identifying this <code>ConfigResolver</code>
	 */
	public URI getConfigIdentifier();
}
