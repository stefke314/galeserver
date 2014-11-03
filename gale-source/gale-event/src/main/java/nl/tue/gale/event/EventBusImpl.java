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
 * EventBusImpl.java
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
package nl.tue.gale.event;

import java.net.URL;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class EventBusImpl extends AbstractEventListener {
	private Map<String, List<EventListener>> services = new Hashtable<String, List<EventListener>>();
	private URL wsdl = null;
	private boolean aSync = false;

	public boolean isASync() {
		return aSync;
	}

	public void setASync(boolean aSync) {
		this.aSync = aSync;
	}

	public EventBusImpl() {
		aSyncThread.start();
	}

	public URL getWsdl() {
		return wsdl;
	}

	public void setWsdl(URL wsdl) {
		this.wsdl = wsdl;
	}

	public List<String> event(String method, List<String> params) {
		// System.out.println("--- event '"+method+"': "+params);
		if (method.equals("register"))
			return event_register(params);
		if (method.startsWith("async:")) {
			method = method.substring(6);
			if (isASync()) {
				synchronized (aSyncQueue) {
					aSyncQueue.offer(new EventMessage(method, params));
				}
				return Arrays.asList(new String[] { "result:ok" });
			}
		}
		List<EventListener> slist = services.get(method);
		if (slist == null) {
			return Arrays
					.asList(new String[] { "result:no service for method '"
							+ method + "'" });
		} else {
			List<String> result = new LinkedList<String>();
			for (EventListener el : slist)
				result.addAll(el.event(method, params));
			// System.out.println("----- result: "+result);
			return result;
		}
	}

	public List<String> event_register(List<String> params) {
		EventHash eventbus = new EventHash("eventbus");
		eventbus.put("address", wsdl.toString());
		EventHash service = null;
		for (EventHash event : EventUtil.parseEvents(params))
			if (event.getName().equals("service"))
				service = event;
		if (service != null) {
			String address = service.getItems().get(0);
			if (address != null) {
				try {
					EventListener el = getEventListenerFactory().getListener(
							new URL(address));
					List<String> methods = el
							.event("register",
									Arrays.asList(new String[] { eventbus
											.toString() }));
					for (EventHash event : EventUtil.parseEvents(methods))
						if (event.getName().equals("methods")) {
							for (String method : event.getItems()) {
								List<EventListener> slist = services
										.get(method);
								if (slist == null) {
									slist = new LinkedList<EventListener>();
									services.put(method, slist);
								}
								slist.add(el);
							}
						}
					return Arrays.asList(new String[] { "result:ok" });
				} catch (Exception e) {
					e.printStackTrace(System.out);
					return Arrays
							.asList(new String[] { "result:error registering service" });
				}
			}
		}
		return Arrays
				.asList(new String[] { "result:no service specified in register" });
	}

	protected void init() {
	}

	protected String getMethods() {
		return "register";
	}

	private Thread aSyncThread = new ASyncThread();
	private Queue<EventMessage> aSyncQueue = new LinkedList<EventMessage>();

	private class ASyncThread extends Thread {
		public boolean running = true;

		public void run() {
			while (running) {
				try {
					sleep(500);
				} catch (InterruptedException e) {
				}
				if (!isASync())
					running = false;
				EventMessage em;
				do {
					synchronized (aSyncQueue) {
						em = aSyncQueue.poll();
					}
					if (em != null) {
						execEvent(em);
					}
				} while (em != null);
			}
		}

		private void execEvent(EventMessage em) {
			List<EventListener> slist = services.get(em.getMethod());
			if (slist == null) {
				System.out.println("--- ASyncEventBus: no service for method '"
						+ em.getMethod() + "'");
			} else {
				for (EventListener el : slist)
					el.event(em.getMethod(), em.getParams());
			}
		}
	}

	private static class EventMessage {
		private String method;
		private List<String> params;

		public String getMethod() {
			return method;
		}

		public List<String> getParams() {
			return params;
		}

		public EventMessage(String method, List<String> params) {
			this.method = method;
			this.params = params;
		}
	}
}
