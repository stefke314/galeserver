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
 * Attribute.java
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
 * This class defines a single attribute, including the type and the actions
 * that will be executed when this attribute is changed.
 */
public class Attribute implements Serializable {
	private static final long serialVersionUID = -6397884614857106522L;

	public static enum Type {
		INTEGER, STRING, BOOLEAN, REAL, DATE
	};

	public static Object defaultValue(Type type) {
		if (type == Type.INTEGER)
			return new Integer(0);
		if (type == Type.STRING)
			return "";
		if (type == Type.BOOLEAN)
			return new Boolean(false);
		if (type == Type.REAL)
			return new Float(0);
		if (type == Type.DATE)
			try {
				return java.text.DateFormat.getDateInstance().parse("1/1/1980");
			} catch (Exception e) {
				return null;
			}
		return null;
	}

	/**
	 * Creates a new attribute with the specified name and type.
	 */
	public Attribute(String name) {
		setName(name);
	}

	private String name = null;

	/**
	 * Returns the name of this attribute
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of this attribute
	 */
	public void setName(String name) {
		if (name == null)
			throw new NullPointerException();
		this.name = name;
	}

	private String description = null;

	/**
	 * Returns the description of this attribute
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description of this attribute
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	private String def = null;

	/**
	 * Returns the default value of this attribute
	 */
	public String getDefault() {
		return def;
	}

	/**
	 * Sets the default value of this attribute
	 */
	public void setDefault(String def) {
		this.def = def;
	}

	private Type type = Type.INTEGER;

	/**
	 * Sets the type of this attribute.
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * Returns the type of this attribute.
	 */
	public Type getType() {
		return type;
	}

	private List<Action> actions = new LinkedList<Action>();

	/**
	 * Returns a list (possibly empty) of actions that are to be executed if
	 * this attribute is changed.
	 */
	public List<Action> getActions() {
		return actions;
	}

	private CaseGroup casegroup = null;

	/**
	 * Returns a list (possibly empty) of cases
	 */
	public CaseGroup getCaseGroup() {
		return casegroup;
	}

	/**
	 * Returns a list (possibly empty) of cases
	 */
	public void setCaseGroup(CaseGroup casegroup) {
		this.casegroup = casegroup;
	}

	private boolean readonly = false;

	/**
	 * Returns whether this is a readonly attribute.
	 */
	public boolean isReadonly() {
		return readonly;
	}

	/**
	 * Sets whether this is a readonly attribute.
	 */
	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}

	private boolean system = false;

	/**
	 * Returns whether this is a system attribute.
	 */
	public boolean isSystem() {
		return system;
	}

	/**
	 * Sets whether this is a system attribute.
	 */
	public void setSystem(boolean system) {
		this.system = system;
	}

	private boolean persistent = true;

	/**
	 * Returns whether this is a persistent attribute.
	 */
	public boolean isPersistent() {
		return persistent;
	}

	/**
	 * Sets whether this is a persistent attribute.
	 */
	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}

	private StableMode stablemode = StableMode.NONE;

	/**
	 * Sets what the stable property of this attribute is. Is optional so it can
	 * be null
	 */
	public void setStableMode(StableMode stablemode) {
		this.stablemode = stablemode;
	}

	/**
	 * Returns what the stable property of this attribute is. Is optional so it
	 * can be null
	 */
	public StableMode getStableMode() {
		return stablemode;
	}

	private String stableexpression = null;

	/**
	 * Sets what the stable expression property of this attribute is. Is
	 * optional so it can be null
	 */
	public void setStableExpression(String stableexpression) {
		this.stableexpression = stableexpression;
	}

	/**
	 * Returns what the stable expression property of this attribute is. Is
	 * optional so it can be null
	 */
	public String getStableExpression() {
		return this.stableexpression;
	}

	/**
	 * Returns a string representation of this attribute.
	 */
	public String toString() {
		return name;
	}

	/**
	 * Returns a copy of this Attribute.
	 */
	public Attribute copy(String source, String dest) {
		Attribute result = new Attribute((name == null ? null
				: name.replaceAll(source, dest)));
		result.setType(type);
		result.setDescription(description);
		result.setDefault((def == null ? null : def.replaceAll(source, dest)));
		result.setReadonly(readonly);
		result.setSystem(system);
		result.setPersistent(persistent);
		result.setStableMode(stablemode);
		result.setStableExpression((stableexpression == null ? null
				: stableexpression.replaceAll(source, dest)));
		if (casegroup != null)
			result.setCaseGroup(casegroup.copy(source, dest));
		for (Action action : actions)
			result.getActions().add(action.copy(source, dest));
		return result;
	}
}