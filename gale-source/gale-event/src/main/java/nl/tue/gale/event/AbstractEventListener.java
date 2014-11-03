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
 * AbstracteventListener.java
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
import java.util.List;

public abstract class AbstractEventListener implements EventListener {
	private EventListenerFactory eventListenerFactory = null;
	private EventListener eventBus = null;

	public EventListenerFactory getEventListenerFactory() {
		return eventListenerFactory;
	}

	public void setEventListenerFactory(
			EventListenerFactory eventListenerFactory) {
		this.eventListenerFactory = eventListenerFactory;
	}

	protected EventListener getEventBus() {
		return eventBus;
	}

	public List<String> event(String method, List<String> params) {
		try {
			if ("register".equals(method))
				return event_register(params);
			if ("implemented".equals(method))
				return event_implemented(params);
		} catch (Exception e) {
			return error(e);
		}
		return null;
	}

	public List<String> event_register(List<String> params) {
		for (EventHash event : EventUtil.parseEvents(params))
			if (event.getName().equals("eventbus")) {
				String address = event.get("address");
				if (address != null)
					try {
						eventBus = eventListenerFactory.getListener(new URL(
								address));
					} catch (Exception e) {
						return error(e);
					}
			}
		if (eventBus == null) {
			return Arrays
					.asList(new String[] { "result:no eventbus found on register" });
		} else {
			try {
				init();
			} catch (Exception e) {
				return error(e);
			}
			return event_implemented(null);
		}
	}

	protected abstract void init();

	public List<String> event_implemented(List<String> params) {
		return Arrays.asList(new String[] { "result:ok",
				"methods:" + getMethods() });
	}

	protected abstract String getMethods();

	protected List<String> error(Exception e) {
		e.printStackTrace();
		return Arrays.asList(new String[] { "result:" + e.getClass().getName()
				+ ":" + e.getMessage() });
	}
}
