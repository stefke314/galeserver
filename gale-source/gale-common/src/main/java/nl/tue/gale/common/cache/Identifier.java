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
 * Identifier.java
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
package nl.tue.gale.common.cache;

import static com.google.common.base.Preconditions.checkArgument;

final class Identifier implements Comparable<Identifier> {
	private final String string;

	private Identifier(String string) {
		this.string = string;
	}

	public static Identifier of(String string) {
		checkArgument(string != null && !string.isEmpty(),
				"'%s' can not be used as identifier", string);
		return new Identifier(string);
	}

	@Override
	public int hashCode() {
		return string.hashCode();
	}

	@Override
	public int compareTo(Identifier other) {
		if (equals(other))
			return 0;
		return string.compareTo(other.string);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Identifier))
			return false;
		Identifier other = (Identifier) obj;
		if (string.length() != other.string.length())
			return false;
		if (hashCode() != other.hashCode())
			return false;
		return string.equals(other.string);
	}

	@Override
	public String toString() {
		return string;
	}
}
