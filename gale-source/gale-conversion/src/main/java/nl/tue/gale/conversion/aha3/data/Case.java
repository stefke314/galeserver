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
 * Modified by:   $Author: dsmits $
 *
 * Copyright (c) 2008-2011 Eindhoven University of Technology.
 * All Rights Reserved.
 *
 * This software is proprietary information of the Eindhoven University
 * of Technology. It may be used according to the GNU LGPL license.
 */
package nl.tue.gale.conversion.aha3.data;

/**
 * This class defines a single case statatement containing a value and a return
 * fragment.
 */
public class Case {
	private String returnfragment = null;

	public String getReturnfragment() {
		return returnfragment;
	}

	public void setReturnfragment(String returnfragment) {
		this.returnfragment = returnfragment;
	}

	private String value = null;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String toString() {
		return value + " - " + returnfragment;
	}

	public Case copy(String source, String dest) {
		Case result = new Case();
		result.setValue(value);
		result.setReturnfragment((returnfragment == null ? null
				: returnfragment.replaceAll(source, dest)));
		return result;
	}
}