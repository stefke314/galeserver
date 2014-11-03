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
 * GEBManager.java
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

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.geb.service.EventGEBListenerService;
import nl.tue.gale.geb.service.GEBListenerService;

import org.apache.commons.collections.map.LRUMap;
import org.netbeans.xml.schema.entlistenerdefinition.EventRequestMsg;
import org.netbeans.xml.schema.eventresponsemsg.EventResponseMsg;
import org.netbeans.xml.schema.registereventlistenerrequestmsg.Methods;
import org.netbeans.xml.schema.registereventlistenerrequestmsg.RegisterEventListenerRequestMsg;
import org.netbeans.xml.schema.registereventlistenerresponsemsg.RegisterEventListenerResponseMsg;

@SuppressWarnings("unchecked")
public class GEBManager {
	private URL gebURL = null;
	private URL baseURL = null;
	private boolean initialized = false;
	private GEBListenerService gebListenerService = null;
	private EventGEBListenerService eventGEBListenerService = null;
	private Map<String, String> autoRegister = new HashMap<String, String>();

	public void setAutoRegister(Map<String, String> autoRegister) {
		this.autoRegister = autoRegister;
	}

	public URL getGebURL() {
		return gebURL;
	}

	public void setGebURL(URL gebURL) {
		this.gebURL = gebURL;
	}

	public URL getBaseURL() {
		return baseURL;
	}

	public void setBaseURL(URL baseURL) {
		this.baseURL = baseURL;
	}

	private void init() {
		if (!"true".equals(GaleUtil.getProperty("useGEB")))
			return;
		if (initialized)
			return;
		try {
			URL url;
			Service service;
			url = new URL(gebURL, "gebListenerService?wsdl");
			service = Service.create(url, new QName(
					"http://j2ee.netbeans.org/wsdl/GEB/gebListener",
					"gebListenerService"));
			gebListenerService = (GEBListenerService) service.getPort(
					new QName("http://j2ee.netbeans.org/wsdl/GEB/gebListener",
							"gebListenerPort"), GEBListenerService.class);
			url = new URL(gebURL, "eventGEBListenerService?wsdl");
			service = Service.create(url, new QName(
					"http://j2ee.netbeans.org/wsdl/GEB/eventGEBListener",
					"eventGEBListenerService"));
			eventGEBListenerService = (EventGEBListenerService) service
					.getPort(
							new QName(
									"http://j2ee.netbeans.org/wsdl/GEB/eventGEBListener",
									"eventGEBListenerPort"),
							EventGEBListenerService.class);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"unable to initialize GEBManager: " + e.getMessage(), e);
		}
		initialized = true;
		try {
			for (Map.Entry<String, String> entry : autoRegister.entrySet()) {
				if (!registerService(entry.getValue().trim(),
						Arrays.asList(entry.getKey().trim().split(";"))))
					System.out
							.println("unable to register (assuming already registered)");
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"unable to auto-register the EventListener on GEB: "
							+ e.getMessage(), e);
		}
	}

	public String sendEvent(String previousId, String method, String body) {
		if (!"true".equals(GaleUtil.getProperty("useGEB")))
			return null;
		init();
		System.out.println("GEB: sending: " + previousId + ", " + method + ", "
				+ body);
		EventRequestMsg msg = new EventRequestMsg();
		msg.setPreviousIdEvent(previousId);
		msg.setMethod(method);
		msg.setBody(body);
		EventResponseMsg result = eventGEBListenerService
				.eventGEBListenerOperation(msg);
		return result.getIdAssignedEvent();
	}

	public boolean registerService(String service, List<String> methods) {
		if (!"true".equals(GaleUtil.getProperty("useGEB")))
			return false;
		init();
		try {
			service = baseURL.toURI().resolve(service).toString() + "?wsdl";
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(
					"unable to resolve service register location: " + service,
					e);
		}
		System.out.println("registering GEB service: " + service);
		RegisterEventListenerRequestMsg msg = new RegisterEventListenerRequestMsg();
		msg.setEventListenerID(service);
		for (String method : methods) {
			Methods m = new Methods();
			m.setDescription(method);
			m.setMethod(method);
			msg.getMethods().add(m);
		}
		RegisterEventListenerResponseMsg result = gebListenerService
				.gebRegisterListenerOperation(msg);
		return result.isResponseMsg();
	}

	private Map<String, CallbackListener> callbackMap = (Map<String, CallbackListener>) new LRUMap(
			100);

	public void registerCallback(String eventId, CallbackListener listener) {
		if (eventId != null && listener != null)
			callbackMap.put(eventId, listener);
	}

	public void responseEvent(String eventId, String previousEventId,
			String method, String body) {
		CallbackListener listener = callbackMap.get(previousEventId);
		if (listener != null)
			listener.callback(new EventObject(body));
		else
			System.out.println("unhandled response event " + eventId + " on "
					+ previousEventId + " (" + method + ": " + body);
	}
}
