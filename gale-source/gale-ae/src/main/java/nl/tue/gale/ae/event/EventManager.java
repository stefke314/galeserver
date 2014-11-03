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
 * EventManager.java
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
package nl.tue.gale.ae.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.tue.gale.ae.Resource;

public class EventManager {
	private Map<String, EventHandler> handlerMap = new HashMap<String, EventHandler>();

	public void setHandlerList(List<EventHandler> handlerList) {
		handlerMap.clear();
		for (EventHandler eh : handlerList)
			handlerMap.put(eh.getType(), eh);
	}

	public void fireEvent(String type, Object source, Resource resource) {
		if (!handlerMap.containsKey(type))
			System.out.println("--- no handler for event '" + type + "'");
		else
			handlerMap.get(type).fireEvent(source, resource);
	}
}