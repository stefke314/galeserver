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
 * Assignment.java
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

/**
 * This class defines a single assignment from one expression to a variable.
 */
public class Assignment {
	/**
	 * Creates a new statement with the specified expression that should be
	 * assigned to the specified variable.
	 */
	public Assignment(String variable, String expr) {
		setVariable(variable);
		setExpression(expr);
	}

	private String variable = null;

	/**
	 * Returns the variable that should be assigned by this assignment.
	 */
	public String getVariable() {
		return variable;
	}

	/**
	 * Sets the variable that should be assigned by this assignment.
	 */
	public void setVariable(String variable) {
		this.variable = variable;
	}

	private String expr = null;

	/**
	 * Returns the expression that should be assigned to a variable by this
	 * assigment.
	 */
	public String getExpression() {
		return expr;
	}

	/**
	 * Sets the expression that should be assigned to a variable by this
	 * assignment.
	 */
	public void setExpression(String expr) {
		this.expr = expr;
	}

	/**
	 * Returns a string representation of this assignment.
	 */
	public String toString() {
		return variable + "=" + expr;
	}

	/**
	 * Returns a copy of this Assignment.
	 */
	public Assignment copy(String source, String dest) {
		return new Assignment((variable == null ? null : variable.replaceAll(
				source, dest)), (expr == null ? null : expr.replaceAll(source,
				dest)));
	}
}