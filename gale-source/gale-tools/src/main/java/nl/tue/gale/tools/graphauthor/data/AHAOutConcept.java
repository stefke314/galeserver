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
 * AHAOutConcept.java
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

import java.util.Iterator;
import java.util.LinkedList;

/**
 * AHAOutConcept stores the output concept
 * 
 */
public class AHAOutConcept {
	public String name;
	public String description;
	public String resource;
	public String template;
	public boolean nocommit;
	public LinkedList attributeList;
	public String stable;
	public String stable_expr;
	// added by @David @18-05-2004
	public String concepttype;
	public String title;
	public String firstchild;
	public String nextsib;
	public String parent;

	// end added by @David @18-05-2004

	/**
	 * Default constructor.
	 */
	public AHAOutConcept() {
		attributeList = new LinkedList();
		name = "";
		description = "";
		resource = "";
		template = "";
		nocommit = false;
		concepttype = "";
		title = "";
		firstchild = null;
		nextsib = null;
		parent = null;
	}

	/**
	 * 
	 * @param Name
	 * @return The requested attribute for this concept or <Code>null</Code> if
	 *         the concept does not have the requested attribute.
	 */
	public AHAOutAttribute getAttribute(String Name) {
		if (this.attributeList.isEmpty()) {
			return null;
		} else {
			// loop list
			for (Iterator i = this.attributeList.iterator(); i.hasNext();) {
				AHAOutAttribute attribute = (AHAOutAttribute) i.next();
				if (attribute.name.equals(Name)) {
					return attribute;
				}
			}
			// nothing found
			return null;
		}
	}

	/**
	 * Adds the attributes of the specified template to the concept. Adds the
	 * attributes of the specified template to the concept. Added by @Bart
	 */
	public void AddTemplateAttributes() {

		// find the template for a concept
		String templateName = this.template;
		for (Iterator k = AuthorSTATIC.templateList.iterator(); k.hasNext();) {
			ConceptTemplate cTemp = (ConceptTemplate) k.next();
			if (templateName.equals(cTemp.name)) {

				// process all attributes form the template
				for (Iterator l = cTemp.attributes.iterator(); l.hasNext();) {
					AHAOutAttribute tempAtt = (AHAOutAttribute) l.next();

					// new is needed to reserve new memory
					AHAOutAttribute cloneAtt = null;
					cloneAtt = tempAtt.cloneAttribute();

					// add attribute to this concept
					this.attributeList.add(cloneAtt);
				}
			}
		}
	}
}