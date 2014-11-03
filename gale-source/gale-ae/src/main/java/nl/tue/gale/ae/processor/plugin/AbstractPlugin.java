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
 * AbstractPlugin.java
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
package nl.tue.gale.ae.processor.plugin;

import nl.tue.gale.ae.ProcessorException;
import nl.tue.gale.ae.Resource;

public abstract class AbstractPlugin implements Plugin {
	public void doPost(Resource resource) throws ProcessorException {
		throw new ProcessorException("HTTP POST not implemented by '"
				+ this.getClass().getName() + "'");
	}

	public void doGet(Resource resource) throws ProcessorException {
		throw new ProcessorException("HTTP GET not implemented by '"
				+ this.getClass().getName() + "'");
	}

	/*
	 * protected Element createRowElement(Concept c, int width, boolean bold,
	 * Document doc) { Element tr = doc.createElement("tr"); Element td =
	 * doc.createElement("td"); tr.appendChild(td); td.setAttribute("style",
	 * "padding-left: " + width + "px;"); if (bold) { Element b =
	 * doc.createElement("b"); td.appendChild(b); td = b; } Element a =
	 * doc.createElement("a"); td.appendChild(a); a.setAttribute("href",
	 * c.getUri().toString()); a.setAttribute("class", "conditional");
	 * a.appendChild(doc.createTextNode(c.getTitle())); return tr; }
	 */
}