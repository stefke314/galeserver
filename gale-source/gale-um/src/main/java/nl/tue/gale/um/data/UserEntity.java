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
 * UserEntity.java
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
package nl.tue.gale.um.data;

import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.tue.gale.common.cache.Cache;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.common.uri.URIs;
import nl.tue.gale.event.EventHash;

public class UserEntity {
	public static final UserEntity nullValue = new UserEntity(null);

	public UserEntity() {
	}

	public UserEntity(String id) {
		setId(id);
	}

	private String id = null;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public URI getUri() {
		return getUriFromId(getId());
	}

	public String getDecodedId() {
		try {
			return URLDecoder.decode(id, "UTF-8");
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"unable to decode URI for userEntity '" + getId() + "'", e);
		}
	}

	private UserEntity parent = null;

	public UserEntity getParent() {
		return parent;
	}

	public void setParent(UserEntity parent) {
		this.parent = parent;
	}

	private Set<UserEntity> users = new HashSet<UserEntity>();

	public Set<UserEntity> getUsers() {
		return users;
	}

	public void setUsers(Set<UserEntity> users) {
		this.users = users;
	}

	private Map<String, String> properties = new HashMap<String, String>();

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public String getProperty(String key) {
		return properties.get(key);
	}

	public void setProperty(String key, String value) {
		if (value == null)
			properties.remove(key);
		else
			properties.put(key, value);
	}

	public static List<String> toEvent(UserEntity object) {
		if (object == null)
			return Arrays.asList(new String[] { "null" });
		EventHash eh = new EventHash("userEntity");
		eh.put("userid", object.getId());
		for (Map.Entry<String, String> entry : object.getProperties()
				.entrySet())
			eh.put("properties." + entry.getKey(), entry.getValue());
		return Arrays.asList(new String[] { eh.toString() });
	}

	public static Map<URI, UserEntity> fromEvent(List<String> events,
			Cache<?> cache) {
		Map<URI, UserEntity> result = new HashMap<URI, UserEntity>();
		for (String event : events) {
			EventHash eh = new EventHash(event);
			if ("userEntity".equals(eh.getName())) {
				UserEntity ue = new UserEntity(eh.get("userid"));
				for (Map.Entry<String, String> entry : eh.entrySet())
					if (entry.getKey().startsWith("properties."))
						ue.setProperty(
								entry.getKey().substring(
										entry.getKey().indexOf(".") + 1),
								entry.getValue());
				result.put(ue.getUri(), ue);
			}
		}
		return result;
	}

	public static String getIdFromUri(URI uri) {
		String id = uri.getSchemeSpecificPart();
		id = id.substring(id.indexOf(":") + 1);
		return id;
	}

	public static URI getUriFromId(String id) {
		try {
			return URIs.of("gale:entity:" + id);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"unable to create URI for userEntity '" + id + "'", e);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 429;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result
				+ ((properties == null) ? 0 : properties.hashCode());
		result = prime * result + ((users == null) ? 0 : users.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserEntity other = (UserEntity) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (properties == null) {
			if (other.properties != null)
				return false;
		} else if (!properties.equals(other.properties))
			return false;
		if (users == null) {
			if (other.users != null)
				return false;
		} else if (!users.equals(other.users))
			return false;
		return true;
	}
}