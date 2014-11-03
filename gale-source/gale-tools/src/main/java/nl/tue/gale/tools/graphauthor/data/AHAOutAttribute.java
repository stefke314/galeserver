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
 * AHAOutAttribute.java
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

import java.util.LinkedList;
import java.util.Vector;

/**
 * AHAOutAttribute stores the attribute values.
 * 
 */
public class AHAOutAttribute {
	public String description;
	public LinkedList generateListItemList;
	public CRTSetDefault setDefaultList;
	public String type;
	public Boolean isChangeable;
	public Boolean isPersistent;
	public Boolean isSystem;
	public String name;
	public String stable;
	public String stable_expr;
	public CaseGroup casegroup = null;

	/**
	 * Default constructor.
	 */
	public AHAOutAttribute() {
		isChangeable = Boolean.FALSE;
		isPersistent = Boolean.FALSE;
		isSystem = Boolean.FALSE;
		generateListItemList = new LinkedList();
		setDefaultList = new CRTSetDefault();
		description = "";
		type = "bool"; // default value
		name = "";
		stable = "";
		stable_expr = "";
	}

	/**
	 * Checks if there is a casegroup object, if so return true else false.
	 * 
	 * @return <Code>true</Code> if and only if there is a casegroup for this
	 *         attribute.
	 */
	public boolean hasCaseGroup() {
		if (this.casegroup == null) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * copies all the values of the original attribute to the clone attribute so
	 * that they contain the same information.
	 * 
	 * @return the cloned attribute Added by @Bart @ 25-03-2003
	 */
	public AHAOutAttribute cloneAttribute() {
		AHAOutAttribute outAttribute = new AHAOutAttribute();
		Case originalCase = null;
		Case cloneCase = null;
		Vector originalCaseVector = null;
		Vector cloneCaseVector = null;

		outAttribute.description = this.description;
		outAttribute.isChangeable = this.isChangeable;
		outAttribute.isPersistent = this.isPersistent;
		outAttribute.isSystem = this.isSystem;
		outAttribute.name = this.name;
		outAttribute.type = this.type;
		outAttribute.stable = this.stable;
		outAttribute.stable_expr = this.stable_expr;
		outAttribute.setDefaultList.setdefault = this.setDefaultList.setdefault;
		if (this.hasCaseGroup()) {
			if (!outAttribute.hasCaseGroup()) {
				outAttribute.casegroup = new CaseGroup();
			}
			outAttribute.casegroup.setDefaultFragment(this.casegroup
					.getDefaultFragment());
			// copy the casevalues
			originalCaseVector = this.casegroup.getCaseValues();
			cloneCaseVector = outAttribute.casegroup.getCaseValues();
			for (int i = 0; i < originalCaseVector.size(); i++) {
				originalCase = (Case) originalCaseVector.get(i);
				cloneCase = new Case();
				cloneCase.setReturnfragment(originalCase.getReturnfragment());
				cloneCase.setValue(originalCase.getValue());
				cloneCaseVector.add(cloneCase);
			}
		}
		return outAttribute;
	}
}