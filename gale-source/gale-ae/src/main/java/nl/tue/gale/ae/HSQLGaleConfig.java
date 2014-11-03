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
 * HSQLGaleConfig.java
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

import java.io.File;

public class HSQLGaleConfig extends GaleConfig {
	private static final long serialVersionUID = -7535702106819625712L;
	private org.hsqldb.Server server = null;

	@Override
	protected void initConfig() {
		super.initConfig();

		try {
			String dir = System.getenv("userprofile");
			if ((dir == null) || ("".equals(dir)))
				dir = System.getenv("HOME");
			if (dir == null || dir.isEmpty())
				dir = "/root";
			File dfile = new File(dir);
			dfile = new File(dfile, "grapple");
			if (!dfile.exists())
				dfile.mkdir();
			server = new org.hsqldb.Server();
			server.setDatabaseName(0, "galedb");
			server.setDatabasePath(0,
					"file:" + dfile.toString().replace("\\", "/")
							+ "/galedb;sql.enforce_strict_size=true");
			server.setLogWriter(null);
			server.setErrWriter(null);
			server.start();
		} catch (Exception e) {
			System.out.println("***** unable to start database *****");
			e.printStackTrace();
		}
	}

	@Override
	public void destroy() {
		server.stop();
	}
}
