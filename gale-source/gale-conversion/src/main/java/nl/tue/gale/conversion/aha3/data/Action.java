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
 * Action.java
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
package nl.tue.gale.conversion.aha3.data;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * This class defines a single action, containing a condition, a set of
 * statements if the condition is true and a set of statements if it is false.
 */
public class Action implements Serializable {
	private static final long serialVersionUID = -8703153766617952675L;
	private String expr = "true";

	/**
	 * Returns the condition part of this action.
	 */
	public String getCondition() {
		return expr;
	}

	/**
	 * Sets the condition part of this action.
	 */
	public void setCondition(String expr) {
		if (expr == null)
			throw new NullPointerException();
		this.expr = expr;
	}

	private boolean trigger = true;

	/**
	 * Returns the trigger flag.
	 */
	public boolean getTrigger() {
		return trigger;
	}

	/**
	 * Sets the trigger flag.
	 */
	public void setTrigger(boolean trigger) {
		this.trigger = trigger;
	}

	private List<Assignment> stattrue = new LinkedList<Assignment>();

	/**
	 * Returns a list of actions that should be executed if the condition is
	 * true.
	 */
	public List<Assignment> getTrueStatements() {
		return stattrue;
	}

	private List<Assignment> statfalse = new LinkedList<Assignment>();

	/**
	 * Returns a list (possibly empty) of actions that should be executed of the
	 * condition is false.
	 */
	public List<Assignment> getFalseStatements() {
		return statfalse;
	}

	/**
	 * Returns a string representation of this Action.
	 */
	public String toString() {
		return expr;
	}

	/**
	 * Returns a copy of this Action.
	 */
	public Action copy(String source, String dest) {
		Action result = new Action();
		result.setCondition((expr == null ? null : expr
				.replaceAll(source, dest)));
		for (Assignment assign : stattrue)
			result.getTrueStatements().add(assign.copy(source, dest));
		for (Assignment assign : statfalse)
			result.getFalseStatements().add(assign.copy(source, dest));
		result.setTrigger(trigger);
		return result;
	}
}