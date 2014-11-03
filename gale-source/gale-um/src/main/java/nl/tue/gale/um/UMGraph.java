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
 * UMGraph.java
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
package nl.tue.gale.um;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.dm.DMCache;
import nl.tue.gale.dm.data.Attribute;
import nl.tue.gale.dm.data.Concept;

public class UMGraph {
	private Map<URI, Set<URI>> lookup = new HashMap<URI, Set<URI>>();
	private Map<URI, Set<URI>> reverseLookup = new HashMap<URI, Set<URI>>();
	private DMCache dm = null;

	public UMGraph(DMCache dm) {
		this.dm = dm;
	}

	private Set<URI> getInternalReverseSet(URI uri) {
		Set<URI> set = reverseLookup.get(uri);
		if (set == null) {
			set = new HashSet<URI>();
			reverseLookup.put(uri, set);
		}
		return set;
	}

	public Set<URI> getReverseSet(URI uri) {
		Set<URI> result = new HashSet<URI>();
		result.addAll(getInternalReverseSet(uri));
		return result;
	}

	private Set<URI> getInternalLinkSet(URI uri) {
		Set<URI> set = lookup.get(uri);
		if (set == null) {
			set = new HashSet<URI>();
			lookup.put(uri, set);
		}
		return set;
	}

	public Set<URI> getLinkSet(URI uri) {
		Set<URI> result = new HashSet<URI>();
		result.addAll(getInternalLinkSet(uri));
		return result;
	}

	public void addLink(URI source, URI target) {
		getInternalLinkSet(source).add(target);
		getInternalReverseSet(target).add(source);
	}

	public void removeLink(URI source, URI target) {
		getInternalLinkSet(source).remove(target);
		getInternalReverseSet(target).remove(source);
	}

	public boolean hasLink(URI source, URI target) {
		return getInternalLinkSet(source).contains(target);
	}

	public boolean hasReverseLink(URI source, URI target) {
		return getInternalReverseSet(source).contains(target);
	}

	public void expandSet(Set<URI> uriSet) {
		Set<URI> added = new HashSet<URI>();
		do {
			Set<URI> todo = new HashSet<URI>();
			todo.addAll(uriSet);
			todo.removeAll(added);
			added.addAll(uriSet);
			for (URI uri : todo) {
				uriSet.addAll(getInternalLinkSet(uri));
				uriSet.addAll(getExtendsLinkSet(uri));
			}
		} while (uriSet.size() != added.size());
	}

	private Set<URI> getExtendsLinkSet(URI uri) {
		Set<URI> result = new HashSet<URI>();
		String attr = uri.getFragment();
		if (attr == null || "".equals(attr))
			return result;
		Set<Concept> extendsSet = getExtendsLinkSet(
				dm.get(Concept.getConceptURI(uri)), attr);
		if (extendsSet != null)
			for (Concept c : extendsSet)
				result.add(GaleUtil.setURIPart(c.getUri(),
						GaleUtil.URIPart.FRAGMENT, attr));
		return result;
	}

	private Set<Concept> getExtendsLinkSet(Concept root, String attr) {
		if (root == null)
			return null;
		Set<Concept> clist = root.getNamedInConcepts("extends");
		if (clist.size() == 0)
			return null;
		Set<Concept> result = new HashSet<Concept>();
		for (Concept c : clist) {
			boolean contains = false;
			for (Attribute a : c.getAttributes())
				contains |= a.getName().equals(attr);
			if (!contains) {
				result.add(c);
				Set<Concept> children = getExtendsLinkSet(c, attr);
				if (children != null)
					result.addAll(children);
			}
		}
		return result;
	}

	public void invalidate() {
		lookup.clear();
		reverseLookup.clear();
	}
}