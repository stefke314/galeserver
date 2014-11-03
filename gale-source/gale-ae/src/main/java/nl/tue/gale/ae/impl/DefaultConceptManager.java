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
 * DefaultConceptManager.java
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
package nl.tue.gale.ae.impl;

import java.net.URL;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;

import nl.tue.gale.ae.ConceptManager;
import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.common.uri.URIs;

public class DefaultConceptManager implements ConceptManager {
	@Override
	public URI getConceptId(Resource resource) {
		try {
			HttpServletRequest req = GaleContext.req(resource);
			String pathInfo = "";
			pathInfo = URLDecoder.decode(req.getPathInfo(), "UTF-8");
			if (pathInfo.indexOf("${home}/") >= 0) {
				loadHomeDirResource(resource);
				return null;
			}
			if (pathInfo.indexOf("${lib}/") >= 0) {
				loadLibDirResource(resource);
				return null;
			}
			if (pathInfo != null && !"".equals(pathInfo)) {
				pathInfo = pathInfo.substring(1);
				if (pathInfo.indexOf(":/") > 0 && pathInfo.indexOf("://") < 0)
					pathInfo = pathInfo.substring(0, pathInfo.indexOf(':') + 1)
							+ "/"
							+ pathInfo.substring(pathInfo.indexOf(':') + 1);
			}
			if (pathInfo.length() == 0)
				pathInfo = URLDecoder.decode(req.getParameter("concept"),
						"UTF-8");
			return URIs.of(pathInfo);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"unable to retrieve concept URI: " + e.getMessage(), e);
		}
	}

	private void loadHomeDirResource(Resource resource) {
		HttpServletRequest req = GaleContext.req(resource);
		String pathInfo = req.getPathInfo();
		pathInfo = "gale://"
				+ pathInfo.substring(pathInfo.lastIndexOf("${home}/") + 8);
		URL url = GaleUtil.generateURL(pathInfo, GaleContext.gc(resource)
				.getHomeDir());
		try {
			resource.putUsed("stream", url.openConnection().getInputStream());
			resource.put("mime", GaleUtil.getMime(url.toString()));
			resource.setUsed("request", true);
		} catch (Exception e) {
			throw new IllegalArgumentException("unable to open ${home} file", e);
		}
	}

	private void loadLibDirResource(Resource resource) {
		HttpServletRequest req = GaleContext.req(resource);
		String pathInfo = req.getPathInfo();
		pathInfo = pathInfo.substring(pathInfo.lastIndexOf("${lib}/") + 7);
		try {
			resource.putUsed("stream", GaleContext.sc(resource)
					.getResourceAsStream("/WEB-INF/lib/" + pathInfo));
			resource.setUsed("request", true);
		} catch (Exception e) {
			throw new IllegalArgumentException("unable to open ${lib} file", e);
		}
	}

	@Override
	public String getConceptLink(URI uri, HttpServletRequest request,
			String query) {
		URI reqUri = URIs.of(request.getRequestURL().toString());
		return URIs.of(
				reqUri.getScheme(),
				reqUri.getAuthority(),
				request.getContextPath() + "/"
						+ GaleUtil.getServletName(request) + "/" + uri, query,
				null).toString();

	}
}
