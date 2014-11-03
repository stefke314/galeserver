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
 * Case.java
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

/**
 * This class defines a single case statatement containing a value and a return
 * fragment.
 */

public class Case {
	// the actual condition of this case
	private String value = null;
	// the returnfragment indicated which fragment is connected to the value of
	// "value"
	private String returnfragment = null;

	/**
	 * Creates a new case.
	 */
	public Case() {
		value = null;
		returnfragment = null;
	}

	/**
	 * returns the returnfragment as a string
	 * 
	 */
	public String getReturnfragment() {
		if (this.returnfragment != null) {
			return this.returnfragment;
		} else {
			return "";
		}

	}

	/**
	 * returns the returnfragment as a string
	 * 
	 */

	public String getValue() {
		if (this.value != null) {
			return this.value;
		} else {
			return "";
		}
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setReturnfragment(String value) {
		this.returnfragment = value;
	}

}