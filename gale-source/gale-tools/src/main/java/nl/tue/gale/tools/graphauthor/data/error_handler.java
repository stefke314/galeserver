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
 * error_handler.java
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

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * Error_handler: default xml error handler.
 * 
 */
public class error_handler implements ErrorHandler {
	private boolean errors_occurred;

	public error_handler() {
		errors_occurred = false;
	}

	public boolean getErrorsOccurred() {
		return errors_occurred;
	}

	public void error(SAXParseException e) {
		show_message("error", e);
		errors_occurred = true;
	}

	public void fatalError(SAXParseException e) {
		show_message("fatalError", e);
		errors_occurred = true;
	}

	public void warning(SAXParseException e) {
		show_message("warning", e);
	}

	private void show_message(String type, SAXParseException e) {
		e.printStackTrace();
		System.err.println("[" + type + "]: " + e.toString() + " Line: "
				+ e.getLineNumber() + " Column: " + e.getColumnNumber());
	}
}