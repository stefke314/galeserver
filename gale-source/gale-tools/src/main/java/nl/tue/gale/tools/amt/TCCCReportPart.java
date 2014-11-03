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
 * TCCCReportPart.java
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
package nl.tue.gale.tools.amt;

/**
 * Type representing the report of an individual concept-resource link for a
 * CCC.
 * 
 * @author T.J. Dekker
 * @version 1.0.0
 */
public class TCCCReportPart implements java.io.Serializable {

	private String FConceptName;
	private String FResourceName;
	private boolean FPassed;

	/**
	 * Default Constructor
	 * 
	 * @param aconceptname
	 *            the name of the concept
	 * @param aresourcename
	 *            the name of the resource
	 * @param haspassed
	 *            <Code>true</Code> if and only if the resource exists
	 */
	public TCCCReportPart(String aconceptname, String aresourcename,
			boolean haspassed) {
		FConceptName = aconceptname;
		FResourceName = aresourcename;
		FPassed = haspassed;
	}

	/**
	 * Retrieves the name of the concept
	 * 
	 * @return name of the concept (the FConceptName field)
	 */
	public String getConceptName() {
		return FConceptName;
	}

	/**
	 * Retrieves the name of the resource
	 * 
	 * @return Name of the resource (the FResourceName field)
	 */
	public String getResourceName() {
		return FResourceName;
	}

	/**
	 * Retrieves the status for this concept-resource link
	 * 
	 * @return <Code>true</Code> if and only if the resource exists for this
	 *         concept.
	 */
	public boolean isPassed() {
		return FPassed;
	}
};