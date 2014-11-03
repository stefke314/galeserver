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
 * GDOMServiceImpl.java
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
package nl.tue.gale.dm;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.Map;

import nl.tue.gale.common.GaleUtil;

import com.google.common.collect.ImmutableList;

public class GDOMServiceImpl extends FileDMServiceImpl {
	private File galeConfig = null;

	public void setGaleConfig(Map<String, Object> galeConfig) {
		this.galeConfig = new File((File) galeConfig.get("homeDir"), "config");
	}

	@Override
	protected List<File> getFiles() {
		return ImmutableList.copyOf(galeConfig.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return (name.endsWith(".gdom") || name.endsWith(".gdom.xml"));
			}
		}));
	}

	@Override
	protected void doFile(File file) {
		try {
			addConcepts(GDOMFormat.toGDOM(GaleUtil.parseXML(file)
					.getRootElement()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}