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
 * StableMode.java
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
 * The stability mode to use. Stability at the concept level keeps objects that
 * are included in resources accessed by this concept stable.
 */
public enum StableMode {
	/**
	 * Don't use stability.
	 */
	NONE {
		public String toString() {
			return "";
		}
	},
	/**
	 * Stability mode where included objects are kept stable for a single
	 * session.
	 */
	SESSION {
		public String toString() {
			return "session";
		}
	},
	/**
	 * Stability mode where included objects are always kept stable.
	 */
	ALWAYS {
		public String toString() {
			return "always";
		}
	},
	/**
	 * Stability mode where included objects are kept stable as long as the
	 * stable expression holds.
	 */
	FREEZE {
		public String toString() {
			return "freeze";
		}
	};

	public static StableMode fromString(String mode) {
		if ("".equals(mode))
			return NONE;
		if ("session".equals(mode))
			return SESSION;
		if ("always".equals(mode))
			return ALWAYS;
		if ("freeze".equals(mode))
			return FREEZE;
		return NONE;
	}
}
