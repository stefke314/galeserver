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
 * Concept.java
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

import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.tue.gale.common.cache.Cache;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.common.uri.URIs;
import nl.tue.gale.event.EventHash;

import com.google.common.collect.MapMaker;

public class Concept {
	public static final Concept nullValue = new Concept(
			URIs.of("null://gale.tue.nl/null"));

	public Concept() {
	}

	public Concept(URI uri) {
		setUri(uri);
	}

	private URI uri = null;

	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		checkUri(uri);
		this.uri = uri;
	}

	private void checkUri(URI uri) {
		String error = null;
		if (!uri.getPath().equals(uri.getRawPath()))
			error = "Concept URI path component contains illegal characters";
		if (error != null)
			throw new IllegalArgumentException(error + " (for " + uri + ")");
	}

	public void setUriString(String uri) {
		try {
			setUri(URIs.of(uri));
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"unable to create URI for String '" + uri + "'", e);
		}
	}

	public String getUriString() {
		return uri.toString();
	}

	private Set<Attribute> attributes = new HashSet<Attribute>();

	public Set<Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(Set<Attribute> attributes) {
		this.attributes = attributes;
	}

	public void addAttribute(Attribute a) {
		a.setConcept(this);
		attributes.add(a);
	}

	public void removeAttribute(Attribute a) {
		attributes.remove(a);
		a.setConcept(null);
	}

	private Map<String, Attribute> localAttrCache = new MapMaker().softValues()
			.makeMap();

	public Attribute getAttribute(String name) {
		Attribute result = localAttrCache.get(name);
		if (result != null)
			return result;
		for (Attribute a : attributes)
			if (a.getName().equals(name))
				result = a;
		if (result != null) {
			String eprop = result.getProperty("~extends");
			if (eprop == null
					|| (eprop.equals("=") && result.getDefaultCode() != null && !""
							.equals(result.getDefaultCode()))) {
				localAttrCache.put(name, result);
				return result;
			}
		}
		Deque<Attribute> todo = new LinkedList<Attribute>();
		if (result != null)
			todo.push(result);
		for (Concept c : getNamedOutConcepts("extends")) {
			result = c.getAttribute(name);
			if (result != null) {
				result = (Attribute) result.clone();
				result.setConcept(this);
				todo.push(result);
			}
		}
		if (todo.size() == 0)
			return null;
		result = todo.pop();
		while (!todo.isEmpty()) {
			Attribute toadd = todo.pop();
			String op = toadd.getProperty("~extends");
			if (!"=".equals(op)) {
				if (op.equals("|") || op.equals("&"))
					op = op + op;
				result.setDefaultCode("(" + result.getDefaultCode() + ") " + op
						+ " (" + toadd.getDefaultCode() + ")");
			}
			// TODO: add proper inheritance of attribute properties
			for (Map.Entry<String, String> entry : toadd.getProperties()
					.entrySet())
				result.setProperty(entry.getKey(), entry.getValue());
			if (toadd.getEventCode() != null
					&& !"".equals(toadd.getEventCode()))
				result.setEventCode(toadd.getEventCode());
		}
		localAttrCache.put(name, result);
		return result;
	}

	private String eventCode = null;

	public String getEventCode() {
		return eventCode;
	}

	public void setEventCode(String eventCode) {
		this.eventCode = eventCode;
	}

	private Map<String, String> properties = new Hashtable<String, String>();

	public Map<String, String> getProperties() {
		return properties;
	}

	protected void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public String getProperty(String key) {
		StringBuilder sb = new StringBuilder();
		if ("+".equals(properties.get("~extends." + key)))
			for (Concept c : getNamedOutConcepts("extends"))
				sb.append(c.getProperty(key));
		if (properties.containsKey(key))
			sb.append(properties.get(key));
		else if ("event".equals(key))
			sb.append(getEventCode());
		return sb.toString();
	}

	public void setProperty(String key, String value) {
		if (value == null)
			properties.remove(key);
		else
			properties.put(key, value);
	}

	private Set<ConceptRelation> outcr = new HashSet<ConceptRelation>();

	public Set<ConceptRelation> getOutCR() {
		return outcr;
	}

	public void setOutCR(Set<ConceptRelation> outcr) {
		this.outcr = outcr;
	}

	public Set<ConceptRelation> getNamedOutCR(String crname) {
		Set<ConceptRelation> result = new HashSet<ConceptRelation>();
		for (ConceptRelation cr : outcr)
			if (cr.getName().equals(crname))
				result.add(cr);
		return result;
	}

	public Set<Concept> getNamedOutConcepts(String crname) {
		Set<Concept> result = new HashSet<Concept>();
		for (ConceptRelation cr : getNamedOutCR(crname))
			result.add(cr.getOutConcept());
		return result;
	}

	private Set<ConceptRelation> incr = new HashSet<ConceptRelation>();

	public Set<ConceptRelation> getInCR() {
		return incr;
	}

	public void setInCR(Set<ConceptRelation> incr) {
		this.incr = incr;
	}

	public Set<ConceptRelation> getNamedInCR(String crname) {
		Set<ConceptRelation> result = new HashSet<ConceptRelation>();
		for (ConceptRelation cr : incr)
			if (cr.getName().equals(crname))
				result.add(cr);
		return result;
	}

	public Set<Concept> getNamedInConcepts(String crname) {
		Set<Concept> result = new HashSet<Concept>();
		for (ConceptRelation cr : getNamedInCR(crname))
			result.add(cr.getInConcept());
		return result;
	}

	public String getType() {
		return getProperty("type");
	}

	public String getTitle() {
		String result = getProperty("title");
		if (result == null || "".equals(result)) {
			result = uri.getPath();
			return (result.contains("/") ? result.substring(result
					.lastIndexOf("/") + 1) : result);
		} else
			return result;
	}

	public String getApplication() {
		String path = uri.getPath();
		if (path == null || "".equals(path.trim()) || path.indexOf("/", 1) < 0)
			return null;
		int i = 1;
		String result = path.substring(i, path.indexOf("/", i));
		if (result.equals("aha3")) {
			i = path.indexOf("/", i) + 1;
			result = path.substring(i, path.indexOf("/", i));
		}
		return result;
	}

	public boolean equals(Object other) {
		if (other == this)
			return true;
		if (!(other instanceof Concept))
			return false;
		final Concept concept = (Concept) other;
		boolean result = true;
		result &= (getUri() == null ? concept.getUri() == null : getUri()
				.equals(concept.getUri()));
		result &= (getProperties().equals(concept.getProperties()));
		result &= (getAttributes().equals(concept.getAttributes()));
		result &= (getInCR().equals(concept.getInCR()));
		result &= (getOutCR().equals(concept.getOutCR()));
		return result;
	}

	public int hashCode() {
		if (getUri() == null)
			return 0;
		else
			return getUri().hashCode();
	}

	public String toString() {
		return "Concept(" + uri + ")";
	}

	public static class comparator implements Comparator<Concept> {
		private String field = "name";

		public comparator() {
		}

		public comparator(String field) {
			this.field = field;
		}

		public int compare(Concept o1, Concept o2) {
			String s1 = null;
			String s2 = null;
			if ("name".equals(field)) {
				s1 = o1.getUriString();
				s2 = o2.getUriString();
			} else {
				s1 = o1.getProperty(field);
				s2 = o2.getProperty(field);
			}
			return (s1 == null ? (s2 == null ? 0 : 1) : (s2 == null ? -1 : s1
					.compareTo(s2)));
		}
	}

	public static URI getConceptURI(URI uri) {
		try {
			return URIs.of(uri.getScheme(), null, uri.getHost(), uri.getPort(),
					uri.getPath(), null, null);
		} catch (Exception e) {
			throw new IllegalArgumentException("unable to create concept URI",
					e);
		}
	}

	public static List<String> toEvent(Concept object) {
		if (object == null)
			return new LinkedList<String>();
		List<String> result = new LinkedList<String>();

		EventHash eh = new EventHash("concept");
		eh.put("uri", object.getUriString());
		eh.put("eventcode", object.getEventCode());
		for (Map.Entry<String, String> entry : object.getProperties()
				.entrySet())
			eh.put("properties." + entry.getKey(), entry.getValue());
		result.add(eh.toString());

		for (Attribute a : object.getAttributes())
			result.add(toEvent(a));
		for (ConceptRelation cr : object.getOutCR())
			result.add(toEvent(cr));
		for (ConceptRelation cr : object.getInCR())
			result.add(toEvent(cr));

		return result;
	}

	private static String toEvent(Attribute a) {
		EventHash eh = new EventHash("attribute");
		eh.put("uri", a.getUri().toString());
		eh.put("eventcode", a.getEventCode());
		eh.put("defaultcode", a.getDefaultCode());
		eh.put("type", a.getType());
		for (Map.Entry<String, String> entry : a.getProperties().entrySet())
			eh.put("properties." + entry.getKey(), entry.getValue());
		return eh.toString();
	}

	private static String toEvent(ConceptRelation cr) {
		EventHash eh = new EventHash("conceptrelation");
		eh.put("name", cr.getName());
		eh.put("inconcept", cr.getInConcept().getUriString());
		eh.put("outconcept", cr.getOutConcept().getUriString());
		for (Map.Entry<String, String> entry : cr.getProperties().entrySet())
			eh.put("properties." + entry.getKey(), entry.getValue());
		return eh.toString();
	}

	public static Map<URI, Concept> fromEvent(List<String> events,
			Cache<Concept> cache) {
		Map<URI, Concept> result = new HashMap<URI, Concept>();
		List<EventHash> relations = new LinkedList<EventHash>();
		for (String event : events) {
			EventHash eh = new EventHash(event);
			if (eh.getName().equals("concept")) {
				Concept c = new Concept();
				c.setUriString(eh.get("uri"));
				c.setEventCode(eh.get("eventcode"));
				for (Map.Entry<String, String> entry : eh.entrySet())
					if (entry.getKey().startsWith("properties."))
						c.setProperty(
								entry.getKey().substring(
										entry.getKey().indexOf(".") + 1),
								entry.getValue());
				result.put(c.getUri(), c);
			}
			if (eh.getName().equals("attribute")) {
				Attribute a = new Attribute();
				URI uri = URIs.of(eh.get("uri"));
				a.setUri(uri);
				result.get(getConceptURI(uri)).addAttribute(a);
				a.setEventCode(eh.get("eventcode"));
				a.setDefaultCode(eh.get("defaultcode"));
				a.setType(eh.get("type"));
				for (Map.Entry<String, String> entry : eh.entrySet())
					if (entry.getKey().startsWith("properties."))
						a.setProperty(
								entry.getKey().substring(
										entry.getKey().indexOf(".") + 1),
								entry.getValue());
			}
			if (eh.getName().equals("conceptrelation")) {
				relations.add(eh);
			}
		}
		for (EventHash eh : relations) {
			URI inconcept = URIs.of(eh.get("inconcept"));
			URI outconcept = URIs.of(eh.get("outconcept"));
			ConceptRelation cr = new ConceptRelation(false, eh.get("name"));
			cr.setEqualsString(eh.get("name") + ";" + inconcept + ";"
					+ outconcept);
			setCRProperties(cr, eh);
			if (result.containsKey(inconcept) && result.containsKey(outconcept)) {
				cr.changeInConcept(result.get(inconcept));
				cr.setOutConcept(cache.getProxy(Concept.class, outconcept));
				cr = new ConceptRelation(false, eh.get("name"));
				cr.setEqualsString(eh.get("name") + ";" + inconcept + ";"
						+ outconcept);
				setCRProperties(cr, eh);
				cr.setInConcept(cache.getProxy(Concept.class, inconcept));
				cr.changeOutConcept(result.get(outconcept));
			} else if (!result.containsKey(inconcept)
					&& !result.containsKey(outconcept)) {
			} else {
				if (result.containsKey(inconcept)) {
					cr.changeInConcept(result.get(inconcept));
					cr.setOutConcept(cache.getProxy(Concept.class, outconcept));
				} else /* result.containsKey(outconcept) */{
					cr.changeOutConcept(result.get(outconcept));
					cr.setInConcept(cache.getProxy(Concept.class, inconcept));
				}
			}
		}
		return result;
	}

	private static void setCRProperties(ConceptRelation cr, EventHash eh) {
		for (Map.Entry<String, String> entry : eh.entrySet())
			if (entry.getKey().startsWith("properties."))
				cr.setProperty(
						entry.getKey().substring(
								entry.getKey().indexOf(".") + 1),
						entry.getValue());
	}

	private transient Map<String, Object> transientData = new HashMap<String, Object>();

	public void addTransientData(String name, Object data) {
		transientData.put(name, data);
	}

	public Object getTransientData(String name) {
		return transientData.get(name);
	}

	private volatile Set<String> virtualAttributeNames = null;

	private Set<String> getVirtualAttributeNames() {
		Set<String> result = virtualAttributeNames;
		if (result == null) {
			result = new HashSet<String>();
			for (Concept c : getNamedOutConcepts("extends"))
				result.addAll(c.getVirtualAttributeNames());
			for (Attribute a : getAttributes())
				result.add(a.getName());
			virtualAttributeNames = result;
		}
		return result;
	}

	public Set<Attribute> getVirtualAttributes() {
		Set<Attribute> result = new HashSet<Attribute>();
		for (String name : getVirtualAttributeNames()) {
			Attribute attr = getAttribute(name);
			if (attr != null)
				result.add(getAttribute(name));
		}
		return result;
	}

	public void refresh() {
		transientData.clear();
		virtualAttributeNames = null;
		localAttrCache.clear();
	}
}