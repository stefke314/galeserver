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
 * CaseGroup.java
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
package nl.tue.gale.tools.graphauthor.data;

import java.util.Vector;

/**
 * This class defines a single case statatement containing a value and a return
 * fragment.
 */
public class CaseGroup {

	// the actual condition of this case
	private String defaultFragment = null;
	// the returnfragment indicated which fragment is connected to the value of
	// "value"
	private Vector caseValues = null;

	/**
	 * Creates a new case.
	 */
	public CaseGroup() {
		caseValues = new Vector();
		defaultFragment = null;
	}

	/**
	 * Returns the default fragment of this case as a string.
	 * 
	 * @return string default fragment
	 */
	public String getDefaultFragment() {
		return this.defaultFragment;
	}

	/**
	 * Sets the default fragment as a string.
	 * 
	 * @param def
	 *            default fragment
	 */
	public void setDefaultFragment(String def) {
		this.defaultFragment = def;
	}

	/**
	 * Returns the vector with casevalues.
	 * 
	 * @return caseValues caseValues
	 */
	public Vector getCaseValues() {
		return caseValues;
	}

	// Added by @Bart @ 13-05-2003
	// it's a debug function to see the casegroup
	public void printCaseGroup() {
		for (int i = 0; i < this.caseValues.size(); i++) {
			Case tempCase = (Case) caseValues.get(i);
		}
	}
	// End Added by @Bart @ 13-05-2003
};
