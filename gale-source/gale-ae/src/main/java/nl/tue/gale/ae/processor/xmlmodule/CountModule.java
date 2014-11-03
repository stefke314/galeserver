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
 * CountModule.java
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
package nl.tue.gale.ae.processor.xmlmodule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.ProcessorException;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.common.uri.URIs;
import nl.tue.gale.dm.DMCache;
import nl.tue.gale.dm.data.Concept;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;

public class CountModule extends AbstractModule {
	private List<String> mimeToHandle = Arrays.asList(new String[] {
			"text/xhtml", "text/xml", "application/xml", "application/smil" });
	private static DMCache dm = null;

	public List<String> getMimeToHandle() {
		return mimeToHandle;
	}

	public void setMimeToHandle(List<String> mimeToHandle) {
		this.mimeToHandle = mimeToHandle;
	}

	private static final Map<URI, URI[]> uriCache = new MapMaker().softValues()
			.maximumSize(150).expireAfterWrite(30, TimeUnit.SECONDS)
			.makeComputingMap(new Function<URI, URI[]>() {
				@Override
				public URI[] apply(URI input) {
					Set<Concept> conceptBuffer = new HashSet<Concept>();
					Set<Concept> newBuffer = new HashSet<Concept>();
					newBuffer.add(dm.get(input));
					do {
						conceptBuffer.addAll(newBuffer);
						Set<Concept> addBuffer = new HashSet<Concept>();
						for (Concept c : newBuffer) {
							for (Concept add : c.getNamedInConcepts("parent"))
								if (!conceptBuffer.contains(add))
									addBuffer.add(add);
							for (Concept add : c.getNamedOutConcepts("parent"))
								if (!conceptBuffer.contains(add))
									addBuffer.add(add);
						}
						newBuffer = addBuffer;
					} while ((newBuffer.size() > 0)
							&& (conceptBuffer.size() < 1024));
					List<URI> result = new ArrayList<URI>();
					for (Concept c : conceptBuffer)
						if (!"false".equals(c.getProperty("count")))
							result.add(c.getUri());
					return result.toArray(new URI[] {});
				}
			});

	public static final URI[] getUriCache(URI uri, GaleContext gale) {
		dm = gale.dm();
		try {
			return uriCache.get(uri);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"unable to obtain list of related concepts for '" + uri
							+ "': " + e.getMessage(), e);
		}
	}

	public static final URI[] getVisitedUriCache(URI uri, GaleContext gale) {
		dm = gale.dm();
		try {
			URI[] conceptList = uriCache.get(uri);
			List<URI> result = new ArrayList<URI>(conceptList.length);
			for (URI concept : conceptList) {
				Number num = (Number) gale
						.um()
						.get(URIs.builder().scheme(concept.getScheme())
								.host(concept.getHost())
								.port(concept.getPort())
								.path(concept.getPath())
								.userInfo(gale.userId()).fragment("visited")
								.build()).getValue();
				if (num.longValue() > 0)
					result.add(concept);
			}
			return result.toArray(new URI[] {});
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"unable to obtain visitedUriCache: " + e.getMessage(), e);
		}
	}

	public static final URI[] getNonVisitedUriCache(URI uri, GaleContext gale) {
		dm = gale.dm();
		try {
			URI[] conceptList = uriCache.get(uri);
			List<URI> result = new ArrayList<URI>(conceptList.length);
			for (URI concept : conceptList)
				result.add(concept);
			for (URI concept : getVisitedUriCache(uri, gale))
				result.remove(concept);
			return result.toArray(new URI[] {});
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"unable to obtain visitedUriCache: " + e.getMessage(), e);
		}
	}

	private static final Function<URI, String> toStringFunction = new Function<URI, String>() {
		@Override
		public String apply(URI input) {
			return input.toString();
		}
	};

	public static final String[] getVisitedUriCache(String uri, GaleContext gale) {
		return Lists.<URI, String> transform(
				ImmutableList.copyOf(getVisitedUriCache(URIs.of(uri), gale)),
				toStringFunction).toArray(new String[] {});
	}

	public static final String[] getNonVisitedUriCache(String uri,
			GaleContext gale) {
		return Lists
				.<URI, String> transform(
						ImmutableList.copyOf(getNonVisitedUriCache(
								URIs.of(uri), gale)), toStringFunction)
				.toArray(new String[] {});
	}

	public Element traverse(Element element, Resource resource)
			throws ProcessorException {
		try {
			processor.traverseChildren(element, resource);
			GaleContext gale = GaleContext.of(resource);
			URI[] conceptList = getUriCache(gale.conceptUri(), gale);
			URI[] visitedConceptList = getVisitedUriCache(gale.conceptUri(),
					gale);
			boolean todo = "todo".equals(element.attributeValue("method"));
			Integer number = (todo ? conceptList.length
					- visitedConceptList.length : visitedConceptList.length);
			GaleUtil.replaceNode(element, DocumentFactory.getInstance()
					.createText(number.toString()));
			return null;
		} catch (Exception e) {
			throw new IllegalArgumentException("unable to count concepts: "
					+ e.getMessage(), e);
		}
	}
}