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
 * LoadProcessor.java
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
package nl.tue.gale.ae.processor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import nl.tue.gale.ae.AbstractResourceProcessor;
import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.ProcessorException;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.ae.impl.ConceptStability;
import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.um.data.EntityValue;

public class LoadProcessor extends AbstractResourceProcessor {
	public void processResource(Resource resource) throws ProcessorException {
		if (resource.isUsed("request"))
			return;
		GaleContext gale = GaleContext.of(resource);
		try {
			String surl;
			if (gale.concept() != null) {
				URI resourceAttr = GaleUtil.setURIPart(gale.conceptUri(),
						GaleUtil.URIPart.FRAGMENT, "resource");
				resourceAttr = GaleUtil
						.addUserInfo(resourceAttr, gale.userId());
				EntityValue ev = gale.um().get(resourceAttr);
				if (ev == null)
					surl = "gale:/empty.xhtml";
				else
					surl = ev.getValueString();
			} else {
				throw new ProcessorException("unknown concept '"
						+ gale.conceptUri() + "'");
			}
			URL url = null;
			if (surl.startsWith("content:")) {
				surl = surl.substring(8);
			} else {
				url = GaleUtil.generateURL(surl, gale.gc().getHomeDir());
			}
			resource.put("url", url);
			resource.put("original-url", surl);

			String stableData = ConceptStability.getStableData(gale);
			if (stableData != null) {
				resource.put("encoding", "UTF-8");
				resource.putUsed("stream",
						new ByteArrayInputStream(stableData.getBytes("UTF-8")));
				resource.put("mime", "text/html");
			} else {
				if (url != null) {
					URLConnection con = url.openConnection();
					InputStream stream = con.getInputStream();
					String mime = GaleUtil.getMime(url.toString());
					if (mime == null || "".equals(mime)) {
						mime = con.getContentType();
						if (mime.indexOf(";") >= 0)
							mime = mime.substring(0, mime.indexOf(";"));
					}
					resource.put("encoding", con.getContentEncoding());
					resource.put("stream", stream);
					resource.put("mime", mime);
				} else {
					try {
						URI attr = gale.conceptUri().resolve(surl);
						attr = GaleUtil.addUserInfo(attr, gale.userId());
						EntityValue ev = gale.um().get(attr);
						String value = ev.getValueString().trim();
						if (!value.startsWith("<") || !value.endsWith(">"))
							value = "<span>" + value + "</span>";
						resource.put(
								"stream",
								new ByteArrayInputStream(value
										.getBytes("UTF-8")));
						resource.put("encoding", "UTF-8");
						resource.put("mime", "text/xhtml");
					} catch (Exception e) {
						throw new ProcessorException(
								"unable to inline content from URL: '" + surl
										+ "'", e);
					}
				}
			}
		} catch (Exception e) {
			if (e instanceof ProcessorException)
				throw (ProcessorException) e;
			e.printStackTrace();
			try {
				gale.resp().sendError(404);
				gale.usedResponse();
			} catch (IOException ie) {
				throw new ProcessorException("unable to load resource", e);
			}
		}
	}
}