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
 * GaleContextLoader.java
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
package nl.tue.gale.ae.config;

import static nl.tue.gale.common.GaleUtil.enumIterable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.servlet.ServletContext;

import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ContextLoader;

public class GaleContextLoader extends ContextLoader {
	public GaleContextLoader() {
		super();
	}

	@Override
	protected void customizeContext(ServletContext sc,
			ConfigurableWebApplicationContext ac) {
		super.customizeContext(sc, ac);
		ArrayList<String> locations = new ArrayList<String>();
		locations.addAll(Arrays.asList(ac.getConfigLocations()));
		findLocations(sc, locations);
		ac.setConfigLocations(locations.toArray(new String[] {}));
	}

	private void findLocations(ServletContext sc, List<String> locations) {
		@SuppressWarnings("unchecked")
		Set<String> paths = (Set<String>) sc.getResourcePaths("/WEB-INF/lib/");
		for (String path : paths)
			if (path.endsWith(".jar"))
				findInJar(sc, path, locations);
	}

	private void findInJar(ServletContext sc, String path,
			List<String> locations) {
		try {
			JarFile jar = new JarFile(sc.getRealPath(path));
			for (JarEntry entry : enumIterable(jar.entries())) {
				if (entry.getName().endsWith("-galeconfig.xml"))
					locations.add("classpath:" + entry.getName());
			}
			jar.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
