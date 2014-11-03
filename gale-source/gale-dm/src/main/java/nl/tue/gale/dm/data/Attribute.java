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
package nl.tue.gale.dm.data;

import java.util.HashMap;
import java.util.Map;

import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.common.uri.URIs;

public class Attribute implements Cloneable {
	public Attribute() {
	}

	public Attribute(String name) {
		setName(name);
	}

	public Object clone() {
		try {
			Attribute result = (Attribute) super.clone();
			result.setProperties(new HashMap<String, String>());
			result.getProperties().putAll(properties);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private long id = -1;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	private String name = null;

	public String getName() {
		return name;
	}

	private transient volatile URI uri = null;

	public URI getUri() {
		if (uri == null)
			uri = concept.getUri().resolve("#" + getName());
		return uri;
	}

	public void setUri(URI uri) {
		setName(uri.getFragment());
	}

	public void setName(String name) {
		this.name = name;
		uri = null;
	}

	private Concept concept = null;

	public Concept getConcept() {
		return concept;
	}

	public void setConcept(Concept concept) {
		this.concept = concept;
		uri = null;
	}

	private String type = "java.lang.Object";

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	private Map<String, String> properties = new HashMap<String, String>();

	public Map<String, String> getProperties() {
		return properties;
	}

	protected void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public String getProperty(String key) {
		if ("event".equals(key) && !properties.containsKey("event"))
			return getEventCode();
		return properties.get(key);
	}

	public void setProperty(String key, String value) {
		if (value == null)
			properties.remove(key);
		else
			properties.put(key, value);
	}

	private String defaultcode = null;

	public String getDefaultCode() {
		return defaultcode;
	}

	public void setDefaultCode(String defaultcode) {
		this.defaultcode = defaultcode;
	}

	private String eventcode = null;

	public String getEventCode() {
		return eventcode;
	}

	public void setEventCode(String eventcode) {
		this.eventcode = eventcode;
	}

	public boolean isPersistent() {
		return (!"false".equals(getProperty("persistent")));
	}

	public boolean equals(Object other) {
		if (other == this)
			return true;
		if (!(other instanceof Attribute))
			return false;
		final Attribute attribute = (Attribute) other;
		if (!GaleUtil.safeEquals(getUri(), attribute.getUri()))
			return false;
		if (!GaleUtil.safeEquals(getType(), attribute.getType()))
			return false;
		if (!GaleUtil.safeEquals(getEventCode(), attribute.getEventCode()))
			return false;
		if (!GaleUtil.safeEquals(getDefaultCode(), attribute.getDefaultCode()))
			return false;
		if (!GaleUtil.safeEquals(getProperties(), attribute.getProperties()))
			return false;
		return true;
	}

	public int hashCode() {
		try {
			return (int) getUri().hashCode();
		} catch (Exception e) {
			return (int) id;
		}
	}

	public String toString() {
		return "Attribute(" + getUri() + ")";
	}

	public static URI getAttributeURI(URI uri) {
		try {
			return URIs.of(uri.getScheme(), null, uri.getHost(), uri.getPort(),
					uri.getPath(), uri.getQuery(), uri.getFragment());
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"unable to create attribute URI", e);
		}
	}

	private transient Map<String, Object> transientData = new HashMap<String, Object>();

	public void addTransientData(String name, Object data) {
		transientData.put(name, data);
	}

	public Object getTransientData(String name) {
		return transientData.get(name);
	}
}