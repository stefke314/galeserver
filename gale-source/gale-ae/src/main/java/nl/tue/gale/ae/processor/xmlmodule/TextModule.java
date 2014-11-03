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
 * TextModule.java
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

import static nl.tue.gale.common.GaleUtil.createHTMLElement;
import static nl.tue.gale.common.GaleUtil.replaceNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.ProcessorException;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.cache.CacheSession;
import nl.tue.gale.common.code.Argument;
import nl.tue.gale.common.uri.URIs;
import nl.tue.gale.um.data.EntityValue;

import org.dom4j.Element;
import org.dom4j.Node;

import com.google.common.collect.ImmutableList;

public class TextModule extends AbstractModule {
	private ImmutableList<String> mimeToHandle = ImmutableList.of("text/xhtml",
			"text/xml", "application/xml", "application/smil");

	@Override
	public List<String> getMimeToHandle() {
		return mimeToHandle;
	}

	public void setMimeToHandle(List<String> mimeToHandle) {
		this.mimeToHandle = ImmutableList.copyOf(mimeToHandle);
	}

	private final Map<String, TextHandler> handlerMap = new HashMap<String, TextHandler>();

	public void setHandlerList(List<TextHandler> handlers) {
		handlerMap.clear();
		for (TextHandler handler : handlers)
			handlerMap.put(handler.getType(), handler);
	}

	public List<TextHandler> getHandlerList() {
		return ImmutableList.copyOf(handlerMap.values());
	}

	@Override
	public Element traverse(Element element, Resource resource)
			throws ProcessorException {
		GaleContext gale = GaleContext.of(resource);

		if (!includeBlock(element, resource))
			return (Element) replaceNode(element, null);

		// find possible class
		String classexpr = null;
		EntityValue ev = gale.um().get(
				URIs.builder().uri(gale.conceptUri()).userInfo(gale.userId())
						.fragment("tags.class").build());
		if (ev != null)
			classexpr = ev.getValueString();
		if (classexpr != null && !"".equals(classexpr)) {
			CacheSession<EntityValue> session = gale.um().openSession();
			try {
				String cl = (String) gale.cm().evaluate(
						gale.cr(),
						classexpr,
						Argument.of("gale", "nl.tue.gale.ae.GaleContext", gale,
								"session",
								"nl.tue.gale.common.cache.CacheSession",
								session, "element", "org.dom4j.Element",
								element));
				if (cl != null)
					element.addAttribute("class", cl);
			} catch (Exception e) {
				e.printStackTrace();
				return (Element) replaceNode(element,
						GaleUtil.createErrorElement("[" + e.getMessage() + "]"));
			}
		}

		// create result element
		Element result;
		if (element.attributeValue("class") != null)
			result = createHTMLElement("div").addAttribute("class",
					element.attributeValue("class"));
		else if (element.attributeValue("id") != null)
			result = createHTMLElement("div").addAttribute("id",
					element.attributeValue("id"));
		else
			result = createHTMLElement("span");

		// handle textual content
		TextHandler handler = handlerMap.get(element.attributeValue("type"));
		if (handler != null) {
			processor.traverseChildren(element, resource);
			handler.handleTextElement(element);
		}

		// copy the content to the new node
		@SuppressWarnings("unchecked")
		List<Node> content = ImmutableList.copyOf((List<Node>) element
				.content());
		for (Node node : content) {
			node.detach();
			result.add(node);
		}
		replaceNode(element, result);
		return result;
	}

	/*
	 * Returns true if and only if the block respresented by 'element' should be
	 * included in the result of this repository.
	 */
	private boolean includeBlock(Element element, Resource resource) {
		GaleContext gale = GaleContext.of(resource);
		String expr = element.attributeValue("expr");
		if (expr != null)
			if (!(Boolean) gale.eval(expr))
				return false;
		String curTags = element.attributeValue("tag");
		if (curTags == null)
			return true;
		String tags = gale.req().getParameter("tags");
		if (tags == null || "".equals(tags))
			tags = "*";
		else
			return containsTag(curTags, tags);
		EntityValue ev = gale.um().get(
				URIs.builder().uri(gale.conceptUri()).userInfo(gale.userId())
						.fragment("tags").build());
		if (ev != null)
			tags = ev.getValueString();
		return containsTag(curTags, tags);
	}

	/*
	 * Returns true if one of the tags in curTags is contained in the tags in
	 * allowedTags, and no tag in curTags is negated in allowedTags. The tags in
	 * curTags and allowedTags are separated by semicolon. A tag starting with a
	 * '-' is a negated tag. The '*' is used as a wildcard in allowedTags,
	 * matching any tag, often used in conjunction with negated tags.
	 */
	private boolean containsTag(String curTags, String allowedTags) {
		List<String> allowed = ImmutableList.copyOf(allowedTags.split(";"));
		boolean contains = false;
		for (String tag : curTags.split(";")) {
			if (allowed.contains("-" + tag))
				return false;
			if (allowed.contains("*"))
				contains = true;
			contains |= allowed.contains(tag);
		}
		return contains;
	}
}