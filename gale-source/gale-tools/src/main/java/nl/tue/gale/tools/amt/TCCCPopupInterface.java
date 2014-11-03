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
 * TCCCPopupInterface.java
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
 * Interface for the CCCPopup
 * 
 * @author T.J. Dekker
 * @version 1.0.0
 */
public interface TCCCPopupInterface {

	/**
	 * Retrieves a CCC report for an application
	 * 
	 * @param aauthorname
	 *            name of an author
	 * @param aappname
	 *            name of an application
	 * @return a CCC report for the requested application
	 */
	public TCCCReport getReport(String aauthorname, String aappname);

	/**
	 * Opens the <Code>.gaf</Code> file for an application.
	 * 
	 * @param aauthorname
	 *            name of an author
	 * @param aappname
	 *            name of an application
	 */
	public void openGafFile(String aauthorname, String aappname);

	/**
	 * Opens the <Code>.aha</Code> file for an application.
	 * 
	 * @param aauthorname
	 *            name of an author
	 * @param aappname
	 *            name of an application
	 */
	public void openAhaFile(String aauthorname, String aappname);

}