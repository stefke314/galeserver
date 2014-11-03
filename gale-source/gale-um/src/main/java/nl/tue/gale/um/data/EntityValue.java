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
 * EntityValue.java
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.sql.Blob;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.rowset.serial.SerialBlob;

import net.iharder.Base64;
import nl.tue.gale.common.cache.Cache;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.common.uri.URIs;
import nl.tue.gale.dm.data.Attribute;
import nl.tue.gale.event.EventHash;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class EntityValue implements JsonSerializer<EntityValue>,
		JsonDeserializer<EntityValue> {
	public static final EntityValue nullValue = new EntityValue(
			URIs.of("null://gale.tue.nl/null"));

	private URI uri = null;
	private Object value = null;

	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	public EntityValue() {
	}

	public EntityValue(URI uri) {
		setUri(uri);
	}

	public EntityValue(URI uri, Object value) {
		setUri(uri);
		setValue(value);
	}

	public EntityValue(String uri) {
		setUriString(uri);
	}

	public void setUriString(String uri) {
		try {
			this.uri = URIs.of(uri);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"unable to create URI for String '" + uri + "'", e);
		}
	}

	public String getUriString() {
		return uri.toString();
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Blob getValueData() {
		try {
			return new SerialBlob(getValueBytes());
		} catch (Exception e) {
			throw new IllegalArgumentException("unable to serialize object '"
					+ uri + "'", e);
		}
	}

	public void setValueData(Blob data) {
		try {
			ObjectInputStream oi = new ObjectInputStream(data.getBinaryStream());
			value = oi.readObject();
		} catch (Exception e) {
			throw new IllegalArgumentException("unable to deserialize object '"
					+ uri + "'", e);
		}
	}

	private byte[] getValueBytes() {
		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			ObjectOutputStream oo = new ObjectOutputStream(bo);
			oo.writeObject(value);
			return bo.toByteArray();
		} catch (Exception e) {
			throw new IllegalArgumentException("unable to serialize object '"
					+ uri + "'", e);
		}
	}

	private void setValueBytes(byte[] data) {
		try {
			ByteArrayInputStream bi = new ByteArrayInputStream(data);
			ObjectInputStream oi = new ObjectInputStream(bi);
			value = oi.readObject();
		} catch (Exception e) {
			throw new IllegalArgumentException("unable to deserialize object '"
					+ uri + "'", e);
		}
	}

	public Double getValueNumber() {
		if (value instanceof Number)
			return ((Number) value).doubleValue();
		else
			return null;
	}

	public void setValueNumber(Double number) {
	}

	public String getValueString() {
		String result = value.toString();
		int max = (result.length() > 1024 ? 1024 : result.length());
		return value.toString().substring(0, max);
	}

	public void setValueString(String string) {
	}

	public String getUserId() {
		return uri.getUserInfo();
	}

	public void setUserId(String userId) {
	}

	public String getAttributeUri() {
		return Attribute.getAttributeURI(uri).toString();
	}

	public void setAttributeUri(String uri) {
	}

	private static boolean serializable(Object value) {
		try {
			Constructor<?> c = value.getClass().getConstructor(String.class);
			Object newvalue = c.newInstance(value.toString());
			return newvalue.equals(value);
		} catch (Exception e) {
			return false;
		}
	}

	private static final Charset charset = Charset.forName("ISO-8859-1");

	public static List<String> toEvent(EntityValue object) {
		EventHash eh = new EventHash("umvalue");
		eh.put("key", object.getUriString());
		if (object.getValue() == null) {
			eh.put("remove", "true");
			return Arrays.asList(new String[] { eh.toString() });
		}
		String strvalue;
		boolean ser = serializable(object.getValue());
		if (ser) {
			strvalue = object.getValue().toString();
		} else {
			strvalue = new String(object.getValueBytes(), charset);
		}
		eh.put("type", object.getValue().getClass().getName());
		eh.put("value", strvalue);
		eh.put("serialized", "" + !ser);
		return Arrays.asList(new String[] { eh.toString() });
	}

	public static Map<URI, EntityValue> fromEvent(List<String> events,
			Cache<?> cache) {
		Map<URI, EntityValue> result = new HashMap<URI, EntityValue>();
		for (String event : events) {
			EventHash eh = new EventHash(event);
			if (eh.getName().equals("umvalue")) {
				EntityValue ev = new EntityValue();
				ev.setUriString(eh.get("key"));
				if (!"true".equals(eh.get("remove"))) {
					String strvalue = eh.get("value");
					boolean b = (new Boolean(eh.get("serialized")));
					if (b) {
						ev.setValueBytes(strvalue.getBytes(charset));
					} else {
						// use String constructor
						String type = eh.get("type");
						try {
							ev.setValue(Class.forName(type)
									.getConstructor(String.class)
									.newInstance(strvalue));
						} catch (Exception e) {
							throw new IllegalArgumentException(
									"unable to create object using String constructor",
									e);
						}
					}
					result.put(ev.getUri(), ev);
				} else {
					result.put(ev.getUri(), null);
				}
			}
		}
		return result;
	}

	public boolean equals(Object other) {
		if (other == this)
			return true;
		if (!(other instanceof EntityValue))
			return false;
		final EntityValue ev = (EntityValue) other;
		if (uri == null)
			return ev.getUri() == null;
		if (uri.equals(ev.getUri())) {
			if (value == null)
				return ev.getValue() == null;
			return value.equals(ev.getValue());
		} else
			return false;
	}

	public static EntityValue create(URI uri, Object value) {
		EntityValue result = new EntityValue();
		result.setUri(uri);
		result.setValue(value);
		return result;
	}

	@Override
	public EntityValue deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		EntityValue ev = new EntityValue();
		JsonObject obj = (JsonObject) json;
		try {
			ev.setUriString(obj.getAsJsonPrimitive("uri").getAsString());
			ev.setValueBytes(Base64.decode(obj.getAsJsonPrimitive("value")
					.getAsString()));
		} catch (Exception e) {
			throw new JsonParseException("unable to deserialize EntityValue: "
					+ json.toString(), e);
		}
		return ev;
	}

	@Override
	public JsonElement serialize(EntityValue src, Type typeOfSrc,
			JsonSerializationContext context) {
		JsonObject result = new JsonObject();
		result.addProperty("uri", src.uri.toString());
		result.addProperty("value", Base64.encodeBytes(src.getValueBytes()));
		return result;
	}

	public String toString() {
		return "[" + getUri() + " -> " + value + "]";
	}
}