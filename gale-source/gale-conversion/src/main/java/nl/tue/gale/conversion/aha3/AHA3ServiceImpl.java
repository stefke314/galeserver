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
 * AHA3ServiceImpl.java
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
package nl.tue.gale.conversion.aha3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.List;
import java.util.Map;

import nl.tue.gale.dm.FileDMServiceImpl;

import com.google.common.collect.ImmutableList;

public class AHA3ServiceImpl extends FileDMServiceImpl {
	private File galeConfig = null;

	public void setGaleConfig(Map<String, Object> galeConfig) {
		this.galeConfig = new File((File) galeConfig.get("homeDir"), "config");
	}

	@Override
	protected List<File> getFiles() {
		return ImmutableList.copyOf(galeConfig.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return (name.endsWith(".aha") || name.endsWith(".aha.xml"));
			}
		}));
	}

	@Override
	protected void doFile(File file) {
		try {
			addConcepts(AHA3Format.convertStream(new FileInputStream(file)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}