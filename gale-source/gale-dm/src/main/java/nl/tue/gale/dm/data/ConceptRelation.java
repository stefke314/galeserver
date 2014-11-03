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
 * ConceptRelation.java
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
package nl.tue.gale.dm.data;

import java.util.Hashtable;
import java.util.Map;

public class ConceptRelation {
	private boolean autoUpdate = true;

	public ConceptRelation() {
	}

	public ConceptRelation(boolean autoUpdate) {
		this.autoUpdate = autoUpdate;
	}

	public ConceptRelation(String name) {
		setName(name);
	}

	public ConceptRelation(boolean autoUpdate, String name) {
		this.autoUpdate = autoUpdate;
		setName(name);
	}

	public ConceptRelation(String name, Concept inconcept, Concept outconcept) {
		setName(name);
		setInConcept(inconcept);
		setOutConcept(outconcept);
		changeInConcept(inconcept);
		changeOutConcept(outconcept);
	}

	private long id = -1;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setAutoUpdate(boolean update) {
		autoUpdate = update;
	}

	private String name = null;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		updateEqualsString();
	}

	private Concept inconcept = null;

	public Concept getInConcept() {
		return inconcept;
	}

	public void setInConcept(Concept inconcept) {
		this.inconcept = inconcept;
		updateEqualsString();
	}

	public void changeInConcept(Concept nconcept) {
		if (inconcept != null)
			inconcept.getOutCR().remove(this);
		setInConcept(nconcept);
		if (inconcept != null)
			inconcept.getOutCR().add(this);
	}

	private Concept outconcept = null;

	public Concept getOutConcept() {
		return outconcept;
	}

	public void setOutConcept(Concept outconcept) {
		this.outconcept = outconcept;
		updateEqualsString();
	}

	public void changeOutConcept(Concept nconcept) {
		if (outconcept != null)
			outconcept.getInCR().remove(this);
		setOutConcept(nconcept);
		if (outconcept != null)
			outconcept.getInCR().add(this);
	}

	private Map<String, String> properties = new Hashtable<String, String>();

	public Map<String, String> getProperties() {
		return properties;
	}

	protected void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public String getProperty(String key) {
		return properties.get(key);
	}

	public void setProperty(String key, String value) {
		if (value == null)
			properties.remove(key);
		else
			properties.put(key, value);
	}

	private String equalsString = null;

	private void updateEqualsString() {
		if (!autoUpdate)
			return;
		String inUri = (inconcept != null ? inconcept.getUriString() : "[]");
		String outUri = (outconcept != null ? outconcept.getUriString() : "[]");
		equalsString = name + ";" + inUri + ";" + outUri;
	}

	public void setEqualsString(String equalsString) {
		this.equalsString = equalsString;
	}

	public boolean equals(Object other) {
		if (other == this)
			return true;
		if (!(other instanceof ConceptRelation))
			return false;
		final ConceptRelation cr = (ConceptRelation) other;
		if (equalsString == null)
			return false;
		return equalsString.equals(cr.equalsString);
	}

	public int hashCode() {
		return (int) getId();
	}

	public String toString() {
		return "ConceptRelation(" + inconcept + " -- " + name + " --> "
				+ outconcept + ")";
	}
}