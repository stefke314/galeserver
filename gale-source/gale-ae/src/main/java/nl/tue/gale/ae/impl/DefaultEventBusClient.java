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
 * DefaultEventBusClient.java
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

import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.List;

import nl.tue.gale.ae.EventBusClient;
import nl.tue.gale.event.EventHash;
import nl.tue.gale.event.EventListener;
import nl.tue.gale.event.EventListenerFactory;

public class DefaultEventBusClient implements EventBusClient {
	private EventListener eb = null;
	private boolean initialized = false;
	private URL[] register = null;
	private URL eventbus = null;
	private EventListenerFactory eventListenerFactory = null;

	public List<String> event(final String method, final List<String> params)
			throws IOException {
		initialize();
		return AccessController
				.doPrivileged(new PrivilegedAction<List<String>>() {
					public List<String> run() {
						return eb.event(method, params);
					}
				});
	}

	private synchronized void initialize() throws IOException {
		if (initialized)
			return;
		try {
			eb = eventListenerFactory.getListener(eventbus);
			for (URL service : register) {
				List<String> result = eb.event("register", Arrays
						.asList(new String[] { EventHash.createSingleEvent(
								"service", service.toString()).toString() }));
				if (!result.contains("result:ok"))
					throw new IllegalArgumentException(
							"service registration error: " + result);
			}
			initialized = true;
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"unable to initialize EventBusClient", e);
		}
	}

	public URL[] getRegister() {
		return register;
	}

	public void setRegister(URL[] register) {
		this.register = register;
	}

	public URL getEventBus() {
		return eventbus;
	}

	public void setEventBus(URL eventbus) {
		this.eventbus = eventbus;
	}

	public EventListenerFactory getEventListenerFactory() {
		return eventListenerFactory;
	}

	public void setEventListenerFactory(
			EventListenerFactory eventListenerFactory) {
		this.eventListenerFactory = eventListenerFactory;
	}
}