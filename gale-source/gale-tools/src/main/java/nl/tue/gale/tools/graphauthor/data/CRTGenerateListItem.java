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
 * CRTGenerateListItem.java
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
 * CRTGenerateListItem data class to store a GenerateListItem.
 * 
 */
public class CRTGenerateListItem {
	public String location;
	public Boolean propagating;
	public String requirement;
	public CRTTrueActions trueActions;
	public CRTFalseActions falseActions;

	/**
	 * Default constructor.
	 */
	public CRTGenerateListItem() {
		propagating = Boolean.TRUE;
		trueActions = new CRTTrueActions();
		falseActions = new CRTFalseActions();
		location = "";
		requirement = "";
	}

	/**
	 * Extract the attribute out of the location variable.
	 * 
	 * @return The attribute
	 */
	public String getAttribute() {
		String returnS = "";

		if (location.equals("")) {
			return returnS;
		}

		int firstDotPlace = location.indexOf(".");
		int lastDotPlace = location.lastIndexOf(".");

		try {
			if (firstDotPlace == lastDotPlace) {
				firstDotPlace++;
				returnS = location.substring(firstDotPlace);
			} else {
				firstDotPlace++;

				// lastDotPlace--;
				returnS = location.substring(firstDotPlace, lastDotPlace);
			}
		} catch (Exception e) {
		}

		return returnS;
	}
}