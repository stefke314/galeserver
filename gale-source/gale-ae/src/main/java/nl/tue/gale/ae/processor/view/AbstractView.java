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
 * AbstractView.java
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
package nl.tue.gale.ae.processor.view;

import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.dm.data.Concept;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;

public abstract class AbstractView implements LayoutView {
	protected Element createRowElement(Concept c, int width, boolean bold) {
		DocumentFactory df = DocumentFactory.getInstance();

		Element tr = GaleUtil.createHTMLElement("tr");
		Element td = GaleUtil.createHTMLElement("td").addAttribute("style",
				"padding-left: " + width + "px;");
		tr.add(td);
		if (bold) {
			Element b = GaleUtil.createHTMLElement("b");
			td.add(b);
			td = b;
		}
		Element a = df
				.createElement(
						df.createQName("a", "", "http://gale.tue.nl/adaptation"))
				.addAttribute("href", c.getUriString()).addText(c.getTitle());
		td.add(a);
		return tr;
	}
}