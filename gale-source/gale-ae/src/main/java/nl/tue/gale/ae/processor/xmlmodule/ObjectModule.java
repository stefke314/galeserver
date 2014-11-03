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
 * ObjectModule.java
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
package nl.tue.gale.ae.processor.xmlmodule;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.ProcessorException;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.common.uri.URIs;

import org.dom4j.Element;

public class ObjectModule extends AbstractModule {
	private List<String> mimeToHandle = Arrays.asList(new String[] {
			"text/xhtml", "text/xml", "application/xml", "application/smil" });

	public List<String> getMimeToHandle() {
		return mimeToHandle;
	}

	public void setMimeToHandle(List<String> mimeToHandle) {
		this.mimeToHandle = mimeToHandle;
	}

	public Element traverse(Element element, Resource resource)
			throws ProcessorException {
		try {
			GaleContext gc = GaleContext.of(resource);
			processor.traverseChildren(element, resource);
			Element xml = null;
			if (getDepth(resource) > MAX_DEPTH)
				throw new IllegalArgumentException(
						"maximum object inclusion reached (possible recursion detected)");
			if (element.attributeValue("data") != null) {
				if (element.attributeValue("data").trim().equals(""))
					throw new IllegalArgumentException(
							"object inclusion with empty 'data' attribute");
				URI orgURI = URIs.of(resource.get("original-url").toString());
				URI uri = orgURI.resolve(element.attributeValue("data"));
				URL location = GaleUtil.generateURL(uri.toString(), gc.gc()
						.getHomeDir());
				resource.put("original-url", uri.toString());
				String orgMime = gc.mime();
				resource.put("mime", GaleUtil.getMime(location.toString()));
				String orgObject = (String) resource.get("nl.tue.gale.object");
				resource.put("nl.tue.gale.object", "true");
				xml = GaleUtil.parseXML(location).getRootElement();
				Element root = GaleUtil.createNSElement("root", "");
				root.add(xml);
				incDepth(resource);
				processor.traverse(root, resource);
				decDepth(resource);
				xml = (Element) root.elements().get(0);
				resource.put("mime", orgMime);
				resource.put("original-url", orgURI.toString());
				if (orgObject == null)
					resource.remove("nl.tue.gale.object");
				else
					resource.put("nl.tue.gale.object", orgObject);
			} else if (element.attributeValue("name") != null) {
				URI objectURI = gc.conceptUri().resolve(
						element.attributeValue("name"));
				URL url = new URL(GaleUtil.getContextURL(gc.req()).toString()
						+ "/concept/" + objectURI.toString());
				objectURI = URIs.builder().uri(objectURI).query(null).build();
				Resource objectResource = gc.pm().createResource(
						GaleUtil.wrappedRequest(gc.req(), url),
						GaleUtil.wrappedResponse(gc.resp()));
				objectResource.put("nl.tue.gale.userUri", gc.userUri());
				objectResource.put("nl.tue.gale.conceptUri", objectURI);
				objectResource.put("nl.tue.gale.parentConceptUri",
						gc.conceptUri());
				objectResource.put("nl.tue.gale.object", "true");
				objectResource.setUsed("response");
				setDepth(objectResource, getDepth(resource) + 1);

				gc.pm().processResource(objectResource);
				if (GaleContext.resp(objectResource).getBufferSize() != 0)
					throw new IllegalArgumentException("object not found");
				xml = GaleContext.xml(objectResource);
			}
			GaleUtil.replaceNode(element, xml);
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return (Element) GaleUtil.replaceNode(element,
					GaleUtil.createErrorElement("[" + e.getMessage() + "]"));
		}
	}

	private static final String DEPTH_KEY = "nl.tue.gale.ae.processor.module.ObjectModule.depth";
	private static final int MAX_DEPTH = 10;

	private void incDepth(Resource resource) {
		setDepth(resource, getDepth(resource) + 1);
	}

	private void decDepth(Resource resource) {
		setDepth(resource, getDepth(resource) - 1);
	}

	private int getDepth(Resource resource) {
		if (resource == null)
			return 0;
		String depthString = (String) resource.get(DEPTH_KEY);
		if (depthString == null)
			return 0;
		return Integer.parseInt(depthString);
	}

	private void setDepth(Resource resource, int depth) {
		if (resource == null)
			return;
		resource.put(DEPTH_KEY, Integer.toString(depth));
	}
}