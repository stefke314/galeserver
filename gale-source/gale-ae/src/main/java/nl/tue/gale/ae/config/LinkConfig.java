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
 * LinkConfig.java
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

import java.util.LinkedList;
import java.util.List;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.common.uri.URIs;
import nl.tue.gale.um.data.EntityValue;

import com.google.common.collect.ImmutableList;

public class LinkConfig implements ConfigResolver {
	private static final URI configIdentifier = URIs
			.of("gale://gale.tue.nl/config/link");

	public URI getConfigIdentifier() {
		return configIdentifier;
	}

	private String defaultExpr = "(${#suitability}?(${#visited}>0?\"neutral\":\"good\"):\"bad\")";

	public String getDefaultExpr() {
		return defaultExpr;
	}

	public void setDefaultExpr(String defaultExpr) {
		this.defaultExpr = defaultExpr;
	}

	private List<String> iconList = new LinkedList<String>();

	public List<String> getIconList() {
		return iconList;
	}

	public void setIconList(List<String> iconList) {
		this.iconList = iconList;
	}

	public Object getObject_hide(Resource resource) {
		GaleContext gc = GaleContext.of(resource);
		URI uri = gc.conceptUri();
		uri = GaleUtil.addUserInfo(uri, gc.userId());
		uri = GaleUtil.setURIPart(uri, GaleUtil.URIPart.FRAGMENT, "link.hide");
		EntityValue ev = gc.um().get(uri);
		if (ev != null)
			return ev.getValueString();
		return null;
	}

	public Object getObject_remove(Resource resource) {
		GaleContext gc = GaleContext.of(resource);
		URI uri = gc.conceptUri();
		uri = GaleUtil.addUserInfo(uri, gc.userId());
		uri = GaleUtil
				.setURIPart(uri, GaleUtil.URIPart.FRAGMENT, "link.remove");
		EntityValue ev = gc.um().get(uri);
		if (ev != null)
			return ev.getValueString();
		return null;
	}

	public Object getObject_classexpr(Resource resource) {
		GaleContext gc = GaleContext.of(resource);
		URI uri = gc.conceptUri();
		uri = GaleUtil.addUserInfo(uri, gc.userId());
		uri = GaleUtil.setURIPart(uri, GaleUtil.URIPart.FRAGMENT,
				"link.classexpr");
		EntityValue ev = gc.um().get(uri);
		if (ev != null)
			return ev.getValueString();
		return defaultExpr;
	}

	public Object getObject_iconlist(Resource resource) {
		GaleContext gc = GaleContext.of(resource);
		URI uri = gc.conceptUri();
		uri = GaleUtil.addUserInfo(uri, gc.userId());
		uri = GaleUtil.setURIPart(uri, GaleUtil.URIPart.FRAGMENT,
				"link.iconlist");
		EntityValue resultObject = gc.um().get(uri);
		if (resultObject != null) {
			Object tempResult = resultObject.getValue();
			if (tempResult instanceof String) {
				if ("".equals((String) tempResult))
					tempResult = new String[] {};
				else
					tempResult = new String[] { (String) tempResult };
			}
			if (tempResult instanceof String[])
				return ImmutableList.copyOf((String[]) tempResult);
		}
		return iconList;
	}

	@Override
	public Object getObject(String name, Resource resource) {
		if ("iconlist".equals(name))
			return getObject_iconlist(resource);
		if ("remove".equals(name))
			return getObject_remove(resource);
		if ("hide".equals(name))
			return getObject_hide(resource);
		if ("classexpr".equals(name))
			return getObject_classexpr(resource);
		throw new UnsupportedOperationException("'" + name + "' not supported");
	}
}
