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
 * UserEntityCacheResolver.java
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
package nl.tue.gale.ae.impl;

import java.util.Arrays;
import java.util.List;

import nl.tue.gale.common.uri.URI;
import nl.tue.gale.event.EventHash;
import nl.tue.gale.um.data.UserEntity;

public class UserEntityCacheResolver extends EventBusCacheResolver<UserEntity> {
	public UserEntityCacheResolver() {
		super(UserEntity.class);
	}

	protected List<String> getEvents(URI uri) {
		try {
			return getEventBusClient().event(
					"getentity",
					Arrays.asList(new String[] { EventHash.createSingleEvent(
							"uri", uri.toString()).toString() }));
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"unable to retrieve objects from event bus", e);
		}
	}

	protected List<String> putEvents(List<String> events) {
		try {
			return getEventBusClient().event("async:setentity", events);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"unable to send objects to event bus", e);
		}
	}
}
