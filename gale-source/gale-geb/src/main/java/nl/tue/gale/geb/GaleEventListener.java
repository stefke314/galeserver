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
 * GaleEventListener.java
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
package nl.tue.gale.geb;

import java.util.List;

import javax.jws.WebService;

import nl.tue.gale.ae.EventBusClient;
import nl.tue.gale.ae.grapple.CourseListService;
import nl.tue.gale.ae.grapple.UpdateContentService;
import nl.tue.gale.event.EventHash;
import nl.tue.gale.geb.service.EventEventListenerService;

import org.netbeans.xml.schema.eventlistenerdefinition.EventRequestMsg;

@WebService(endpointInterface = "nl.tue.gale.geb.service.EventEventListenerService", serviceName = "eventEventListenerService", portName = "eventEventListenerPort", targetNamespace = "http://j2ee.netbeans.org/wsdl/GEB/eventEventListener")
public class GaleEventListener implements EventEventListenerService {
	private UpdateContentService updateContentService = null;
	private CourseListService courseListService = null;
	private GEBManager gebManager = null;
	private EventBusClient eventBusClient = null;

	public EventBusClient getEventBusClient() {
		return eventBusClient;
	}

	public void setEventBusClient(EventBusClient eventBusClient) {
		this.eventBusClient = eventBusClient;
	}

	public UpdateContentService getUpdateContentService() {
		return updateContentService;
	}

	public void setUpdateContentService(
			UpdateContentService updateContentService) {
		this.updateContentService = updateContentService;
	}

	public CourseListService getCourseListService() {
		return courseListService;
	}

	public void setCourseListService(CourseListService courseListService) {
		this.courseListService = courseListService;
	}

	public GEBManager getGebManager() {
		return gebManager;
	}

	public void setGebManager(GEBManager gebManager) {
		this.gebManager = gebManager;
	}

	public void eventEventListenerOperation(EventRequestMsg eventRequestMsg) {
		String previousEventId = eventRequestMsg.getPreviousIdEvent();
		String method = eventRequestMsg.getMethod();
		String body = eventRequestMsg.getBody();
		/*
		 * System.out.println("GEB: received: " + eventRequestMsg.getEventId() +
		 * ": " + method + ": " + body);
		 */
		if (previousEventId == null || "".equals(previousEventId.trim())) {
			if ("updateCAMModel".equals(method)) {
				String result = updateCAMModel(body);
				gebManager.sendEvent(eventRequestMsg.getEventId(),
						"updateCAMModelResponse", result);
			} else if ("getCourses".equals(method)) {
				String result = getCourses(body);
				gebManager.sendEvent(eventRequestMsg.getEventId(),
						"getCoursesResponse", result);
			} else if ("getCourseCount".equals(method)) {
				Integer result = getCourseCount(body);
				gebManager.sendEvent(eventRequestMsg.getEventId(),
						"getCourseCountResponse", result.toString());
			} else if ("sendGaleEvent".equals(method)) {
				String result = sendGaleEvent(body);
				gebManager.sendEvent(eventRequestMsg.getEventId(),
						"sendGaleEventResponse", result);
			}
		} else {
			gebManager.responseEvent(eventRequestMsg.getEventId(),
					eventRequestMsg.getPreviousIdEvent(), method, body);
		}
	}

	private String sendGaleEvent(String body) {
		if (body.indexOf(':') < 0)
			return "result:no method found in event";
		String method = body.substring(0, body.indexOf(':'));
		body = body.substring(body.indexOf(':') + 1);
		try {
			List<String> result = eventBusClient.event(method,
					stringToList(body));
			return listToString(result);
		} catch (Exception e) {
			e.printStackTrace();
			return "result:" + e.getMessage();
		}
	}

	private int getCourseCount(String body) {
		return courseListService.getCourseCount();
	}

	private String getCourses(String body) {
		if (body == null || "".equals(body.trim()))
			return courseListService.getCourses();
		else {
			String[] s = body.split(";");
			if (s.length < 2 || s.length > 2)
				return courseListService.getCourses();
			return courseListService.getCoursesSelection(
					Integer.parseInt(s[0]), Integer.parseInt(s[1]));
		}
	}

	private String updateCAMModel(String body) {
		return updateContentService.updateCAMModel(body);
	}

	private static String listToString(List<String> list) {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		for (String s : list) {
			if (!first)
				sb.append(";");
			sb.append(s.replace(";", "\\;"));
			first = false;
		}
		return sb.toString();
	}

	private static List<String> stringToList(String list) {
		EventHash eh = new EventHash("test:" + list);
		return eh.getItems();
	}
}
