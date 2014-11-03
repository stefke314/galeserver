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
 * ResourceProcessor.java
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

/**
 * The main interface to extending Gale. A resource processor is called by
 * GaleServlet to process a particular resource. A resource can be at a
 * particular level of processing. The current state is tracked by indicating
 * whether the various variables in the resource are used or not.
 */
public interface ResourceProcessor {
	/**
	 * Processes a resource. The implementation will most likely perform some
	 * changes to the resource and set the 'used' state on the appropriate
	 * variables.
	 * 
	 * @param resource
	 *            the {@link Resource} to process
	 * @throws ProcessorException
	 *             if some error occurs during processing
	 */
	public void processResource(Resource resource) throws ProcessorException;
}
