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
 * GaleServletBean.java
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
package nl.tue.gale.ae;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.um.data.UserEntity;

public class GaleServletBean {
	private ProcessorManager processorManager = null;
	private LoginManager loginManager = null;
	private ConceptManager conceptManager = null;

	public LoginManager getLoginManager() {
		return loginManager;
	}

	public void setLoginManager(LoginManager loginManager) {
		this.loginManager = loginManager;
	}

	public ConceptManager getConceptManager() {
		return conceptManager;
	}

	public void setConceptManager(ConceptManager conceptManager) {
		this.conceptManager = conceptManager;
	}

	public ProcessorManager getProcessorManager() {
		return processorManager;
	}

	public void setProcessorManager(ProcessorManager processorManager) {
		this.processorManager = processorManager;
	}

	public void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// set headers to prevent caching in the browser
		resp.setDateHeader("Expires", 0);
		resp.setHeader("Cache-Control",
				"no-store, no-cache, private, must-revalidate");
		resp.addHeader("Cache-Control",
				"post-check=0, max-stale=0, pre-check=0");
		resp.setHeader("Pragma", "no-cache");

		// main processing here
		Resource resource = processorManager.createResource(req, resp);

		// get user id
		String userId = null;
		try {
			userId = loginManager.getLoginName(resource);
			if (userId == null)
				return;
		} catch (Exception e) {
			throw new IllegalArgumentException("error running LoginManager", e);
		}
		resource.put("nl.tue.gale.userUri", UserEntity.getUriFromId(userId));

		// call ConceptManager
		URI conceptId = null;
		try {
			conceptId = conceptManager.getConceptId(resource);
		} catch (Exception e) {
			throw new IllegalArgumentException("error running ConceptManager",
					e);
		}
		resource.put("nl.tue.gale.conceptUri", conceptId);

		resource.put("nl.tue.gale.servletAccess", true);
		try {
			processorManager.processResource(resource);
		} catch (ProcessorException e) {
			throw new ServletException("error processing resource: "
					+ e.getMessage(), e);
		}

		// try to load resource if necessary
		if (!resource.isUsed("request")) {
			try {
				String surl = (GaleContext.url(resource) == null ? null
						: GaleContext.url(resource).toString());
				if (surl == null)
					surl = "gale:/empty.xhtml";
				URL url = GaleUtil.generateURL(surl, GaleContext.gc(resource)
						.getHomeDir());
				URLConnection con = url.openConnection();
				InputStream stream = con.getInputStream();
				resource.put("encoding", con.getContentEncoding());
				resource.put("stream", stream);
				resource.put("mime", GaleUtil.getMime(url.toString()));
				if ("text/xhtml".equals(GaleContext.mime(resource)))
					resource.put("mime", "text/html");
			} catch (Exception e) {
				throw new ServletException("unable to load resource: "
						+ e.getMessage(), e);
			}
		}

		// try to send resource to client if necessary
		if (!resource.isUsed("response")) {
			String mime = (String) resource.get("mime");
			if (mime == null)
				mime = "text/html";
			GaleUtil.sendToClient(resp, (InputStream) resource.get("stream"),
					mime, GaleContext.encoding(resource));
		}
	}
}
