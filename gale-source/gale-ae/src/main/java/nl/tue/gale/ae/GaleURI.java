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
 * GaleURI.java
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
package nl.tue.gale.ae;

import nl.tue.gale.common.uri.URI;
import nl.tue.gale.dm.data.Attribute;
import nl.tue.gale.dm.data.Concept;
import nl.tue.gale.um.data.EntityValue;
import nl.tue.gale.um.data.UserEntity;

public class GaleURI {
	public static Class<?> getType(URI uri) {
		String typeName = getTypeName(uri);
		if ("concept".equals(typeName))
			return Concept.class;
		if ("attribute".equals(typeName))
			return Attribute.class;
		if ("userentity".equals(typeName))
			return UserEntity.class;
		if ("entityvalue".equals(typeName))
			return EntityValue.class;
		return null;
	}

	public static String getTypeName(URI uri) {
		if (uri == null)
			return "null";
		if (!uri.getScheme().equals("gale"))
			return "unknown";
		if (uri.getSchemeSpecificPart().startsWith("entity:"))
			return "userentity";
		if (uri.getUserInfo() != null)
			return "entityvalue";
		if (uri.getFragment() != null)
			return "attribute";
		if (uri.getPath() != null)
			return "concept";
		return "unknown";
	}
}
