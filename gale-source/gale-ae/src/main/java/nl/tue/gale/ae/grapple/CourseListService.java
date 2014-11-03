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
 * CourseListService.java
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
package nl.tue.gale.ae.grapple;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import nl.tue.gale.ae.EventBusClient;
import nl.tue.gale.ae.GaleConfig;
import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.common.uri.URIs;
import nl.tue.gale.dm.DMCache;
import nl.tue.gale.dm.data.Concept;
import nl.tue.gale.event.EventHash;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import com.google.gson.reflect.TypeToken;

@WebService(serviceName = "CourseList", portName = "CourseListPort", targetNamespace = "http://gale.win.tue.nl/services")
public class CourseListService {
	private EventBusClient eventBusClient = null;
	private DMCache dmCache = null;
	private GaleConfig galeConfig = null;

	@WebMethod(exclude = true)
	public GaleConfig getGaleConfig() {
		return galeConfig;
	}

	@WebMethod(exclude = true)
	public void setGaleConfig(GaleConfig galeConfig) {
		this.galeConfig = galeConfig;
	}

	@WebMethod(exclude = true)
	public DMCache getDmCache() {
		return dmCache;
	}

	@WebMethod(exclude = true)
	public void setDmCache(DMCache dmCache) {
		this.dmCache = dmCache;
	}

	@WebMethod(exclude = true)
	public EventBusClient getEventBusClient() {
		return eventBusClient;
	}

	@WebMethod(exclude = true)
	public void setEventBusClient(EventBusClient eventBusClient) {
		this.eventBusClient = eventBusClient;
	}

	@WebMethod
	@WebResult(name = "getCoursesResult", targetNamespace = "http://gale.win.tue.nl/services")
	public String getCourses() {
		return getCoursesSelection(0, getCourseCount());
	}

	@WebMethod
	@WebResult(name = "getCoursesResult", targetNamespace = "http://gale.win.tue.nl/services")
	public String getCoursesSelection(
			@WebParam(name = "first", targetNamespace = "http://gale.win.tue.nl/services") int first,
			@WebParam(name = "count", targetNamespace = "http://gale.win.tue.nl/services") int count) {
		loadConcepts();
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<learnerinformation xmlns=\"http://www.imsglobal.org/xsd/imslip_v1p0\" xmlns:imslip=\"http://www.imsproject.org/xsd/ims_lip_rootv1p0\" xmlns:xsi=\"http://www.w3.org/2000/10/XMLSchema-instance\">\n");
		for (URI uri : startConcepts) {
			sb.append(createIMSLIP(dmCache.get(uri)).asXML());
			sb.append("\n");
		}
		sb.append("</learnerinformation>\n");
		return sb.toString();
	}

	@WebMethod
	@WebResult(name = "getCourseCountResult", targetNamespace = "http://gale.win.tue.nl/services")
	public int getCourseCount() {
		loadConcepts();
		return startConcepts.size();
	}

	@WebMethod(exclude = true)
	public List<Concept> getStartConcepts() {
		loadConcepts();
		List<Concept> result = new LinkedList<Concept>();
		for (URI uri : startConcepts)
			result.add(dmCache.get(uri));
		return result;
	}

	private List<URI> startConcepts = new LinkedList<URI>();
	private long lastLoad = 0;

	private Element createIMSLIP(Concept c) {
		DocumentFactory df = DocumentFactory.getInstance();
		Element sourcedid = df.createElement("sourcedid");
		Element result = df.createElement("activity");
		Element contentype = result.addElement("contentype");
		contentype.addElement("referential").add(sourcedid);
		sourcedid.addElement("source").addText(
				galeConfig.getRootGaleUrl() + c.getUriString());
		sourcedid.addElement("id").addText(c.getTitle());
		String camguid = c.getProperty("cam.model.guid");
		if (camguid != null && !"".equals(camguid)) {
			Element field = contentype.addElement("temporal").addElement(
					"temporalfield");
			field.addElement("fieldlabel").addElement("typename")
					.addElement("tyvalue").addText("camguid");
			field.addElement("fielddata").addText(camguid);
		}
		return result;
	}

	private void loadConcepts() {
		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			@Override
			public Object run() {
				if ((System.currentTimeMillis() - lastLoad) < 15000)
					return null;
				lastLoad = System.currentTimeMillis();
				try {
					String query = "select c.uriString from Concept c where c.properties['start'] = 'true'";
					List<String> queryResult = eventBusClient.event("querydm",
							Arrays.asList(new String[] { EventHash
									.createSingleEvent("query", query)
									.toString() }));
					String result = null;
					for (String s : queryResult)
						if (result == null && (!s.startsWith("result:")))
							result = s;
					List<String> concepts = GaleUtil.gson().fromJson(result,
							new TypeToken<List<String>>() {
							}.getType());
					startConcepts.clear();
					if (concepts == null)
						return null;
					for (String uri : concepts)
						startConcepts.add(URIs.of(uri));
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		});
	}
}
