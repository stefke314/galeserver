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
 * EventBusCacheResolver.java
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

import nl.tue.gale.ae.EventBusClient;
import nl.tue.gale.event.EventCacheResolver;

public abstract class EventBusCacheResolver<T> extends EventCacheResolver<T> {
	public EventBusCacheResolver(Class<T> clazz) {
		super(clazz);
	}

	private EventBusClient eventBusClient = null;

	public EventBusClient getEventBusClient() {
		return eventBusClient;
	}

	public void setEventBusClient(EventBusClient eventBusClient) {
		this.eventBusClient = eventBusClient;
	}
}
