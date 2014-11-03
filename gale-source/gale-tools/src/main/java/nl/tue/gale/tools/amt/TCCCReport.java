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
 * TCCCReport.java
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

import java.util.Vector;

/**
 * Type representing the report for a CCC.
 * 
 * @author T.J. Dekker
 * @version 1.0.0
 */
public class TCCCReport implements java.io.Serializable {

	private String FAuthorname;
	private String FAppname;
	private Vector FReportList;

	/**
	 * Constructor
	 * 
	 * @param aauthorname
	 *            name of the author
	 * @param aappname
	 *            name of the application
	 */
	public TCCCReport(String aauthorname, String aappname) {
		FAuthorname = aauthorname;
		FAppname = aappname;
		FReportList = new Vector();
	}

	/**
	 * Adds part of the CCC Report to the reportlist
	 * 
	 * @param c
	 *            the part of the CCC Report
	 */
	public void add(TCCCReportPart c) {
		FReportList.add(c);
	}

	/**
	 * Retrieves the name of the author
	 * 
	 * @return name of the author (the FAuthorname field)
	 */
	public String getAuthorName() {
		return FAuthorname;
	}

	/**
	 * Retrieves the name of the application
	 * 
	 * @return name of the application (the FAppname field)
	 */
	public String getAppName() {
		return FAppname;
	}

	/**
	 * Retrieves the list of all CCCReportParts
	 * 
	 * @return The list of all CCC report parts in Vector representation. Items
	 *         of the Vector are of type TCCCReportPart.
	 */
	public Vector getRepList() {
		return FReportList;
	}

};