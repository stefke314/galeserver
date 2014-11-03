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
 * Modified by:   $Author: dsmits $
 *
 * Copyright (c) 2008-2011 Eindhoven University of Technology.
 * All Rights Reserved.
 *
 * This software is proprietary information of the Eindhoven University
 * of Technology. It may be used according to the GNU LGPL license.
 */
package nl.tue.gale.conversion.aha3.data;

import java.util.LinkedList;

/**
 * This class defines a single case statatement containing a value and a return
 * fragment.
 * 
 */
public class CaseGroup extends LinkedList<Case> {
	private static final long serialVersionUID = -3171653638601250045L;

	/**
	 * Creates a new case.
	 */
	public CaseGroup() {
		super();
	}

	private String defaultFragment = null;

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
	 * Returns a copy of this CaseGroup.
	 */
	public CaseGroup copy(String source, String dest) {
		CaseGroup result = new CaseGroup();
		result.setDefaultFragment((defaultFragment == null ? null
				: defaultFragment.replaceAll(source, dest)));
		Case casevalue;
		for (int i = 0; i < size(); i++) {
			casevalue = get(i);
			result.add(casevalue.copy(source, dest));
		}
		return result;
	}
}