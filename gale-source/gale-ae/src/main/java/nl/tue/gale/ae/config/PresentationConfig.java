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
 * PresentationConfig.java
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
package nl.tue.gale.ae.config;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.ae.processor.view.LayoutView;
import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.cache.CacheSession;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.common.uri.URIs;
import nl.tue.gale.um.data.EntityValue;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;

public class PresentationConfig implements ConfigResolver {
	private static final URI configIdentifier = URIs
			.of("gale://gale.tue.nl/config/presentation");

	private Map<String, LayoutView> viewTable = new HashMap<String, LayoutView>();

	public void setViewTable(Map<String, LayoutView> viewTable) {
		this.viewTable = viewTable;
	}

	public Map<String, LayoutView> getViewTable() {
		return viewTable;
	}

	public URI getConfigIdentifier() {
		return configIdentifier;
	}

	private String defaultCSS = "${home}/gale.css";

	public String getDefaultCSS() {
		return defaultCSS;
	}

	public void setDefaultCSS(String defaultCSS) {
		this.defaultCSS = defaultCSS;
	}

	public Object getObject_css(Resource resource) {
		String css = defaultCSS;
		GaleContext gale = GaleContext.of(resource);
		CacheSession<EntityValue> session = gale.openUmSession();
		EntityValue ev = session.get(URIs.of("#layout.css"));
		if (ev != null)
			css = ev.getValue().toString().trim();
		return css;
	}

	public Object getObject_cssLayout(Resource resource) {
		String cssLayout = null;
		GaleContext gale = GaleContext.of(resource);
		CacheSession<EntityValue> session = gale.openUmSession();
		EntityValue ev = session.get(URIs.of("#layout.content"));
		if (ev != null)
			cssLayout = ev.getValue().toString().trim();
		return cssLayout;
	}

	private static final URI titleFragment = URIs.of("#layout.title");

	public Object getObject_title(Resource resource) {
		String title = null;
		GaleContext gale = GaleContext.of(resource);
		EntityValue ev = gale.openUmSession().get(titleFragment);
		if (ev != null) {
			String titleExpr = ev.getValue().toString().trim();
			title = (String) gale.eval(titleExpr);
		}
		if (title == null)
			title = gale.concept().getTitle();
		return title;
	}

	public Object getObject_layout(Resource resource) {
		Element result = DocumentFactory.getInstance().createElement("struct");
		result.addAttribute("cols", "20%;*");
		result.addElement("view").addAttribute("name", "static-tree-view");
		result.addElement("content");

		GaleContext gale = GaleContext.of(resource);
		CacheSession<EntityValue> session = gale.openUmSession();
		EntityValue ev = session.get(URIs.of("#layout"));

		if (ev == null) {
			/*
			 * if (gale.conceptUri().toString().startsWith(
			 * "gale://gale.tue.nl/aha3/tutorial") &&
			 * "page".equals(gale.concept().getType())) return result;
			 */
			return null;
		}
		if (ev.getValue().toString().trim().equals(""))
			return null;
		return GaleUtil.parseXML(new StringReader(ev.getValue().toString()))
				.getRootElement();
	}

	@Override
	public Object getObject(String name, Resource resource) {
		if ("css".equals(name))
			return getObject_css(resource);
		if ("cssLayout".equals(name))
			return getObject_cssLayout(resource);
		if ("layout".equals(name))
			return getObject_layout(resource);
		if ("title".equals(name))
			return getObject_title(resource);
		if (name.startsWith("view-")) {
			return viewTable.get(name.substring(5));
		}
		throw new UnsupportedOperationException("'" + name + "' not supported");
	}
}
