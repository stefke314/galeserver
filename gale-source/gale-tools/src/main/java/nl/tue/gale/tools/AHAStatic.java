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
 * AHAStatic.java
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
package nl.tue.gale.tools;

import javax.servlet.ServletContext;

import nl.tue.gale.tools.config.AhaConfig;

public class AHAStatic {
	public static final String AUTHORFILESPATH = "/author/authorfiles/";
	public static final String FORMEDITORPATH = "/author/FormEditor/";
	public static final String FORMPATH = "/form/";
	public static final String GENLISTEDITORPATH = "/author/GenerateList_Editor/";
	public static final String GRAPHAUTHORPATH = "/author/GraphAuthor/";
	public static final String TESTEDITORPATH = "/author/TestEditor/";
	public static final String AMTPATH = "/author/AMt/";

	private static AhaConfig cfg = null;

	public static AhaConfig config(ServletContext sc) {
		try {
			if (cfg == null)
				cfg = new AhaConfig(sc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cfg;
	}
}