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
 * LogManager.java
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

public class LogManager {
	private static File logDir = null;

	public void setLogDir(org.springframework.core.io.Resource logDirP) {
		try {
			logDir = logDirP.getFile();
			if (!logDir.exists())
				logDir.mkdir();
		} catch (Exception e) {
			throw new IllegalArgumentException("unable to open log directory: "
					+ e.getMessage());
		}
	}

	private static final Map<String, PrintWriter> cache = new MapMaker()
			.makeComputingMap(new Function<String, PrintWriter>() {
				@Override
				public PrintWriter apply(String input) {
					try {
						return new PrintWriter(
								new BufferedWriter(new FileWriter(new File(
										logDir, input + ".log"), true)));
					} catch (IOException e) {
						throw new IllegalArgumentException(e.getMessage(), e);
					}
				}
			});

	public void log(String file, String log) {
		cache.get(file).println(log);
	}

	public void destroy() {
		for (Map.Entry<String, PrintWriter> entry : cache.entrySet())
			entry.getValue().close();
	}
}
