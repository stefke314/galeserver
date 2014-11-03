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
 * EventRequestMsg.java
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
package org.netbeans.xml.schema.eventlistenerdefinition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for eventRequestMsg complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="eventRequestMsg">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="eventId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="previousIdEvent" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="method" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="body" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "eventRequestMsg", propOrder = { "eventId", "previousIdEvent",
		"method", "body" })
public class EventRequestMsg {

	@XmlElement(required = true)
	protected String eventId;
	protected String previousIdEvent;
	@XmlElement(required = true)
	protected String method;
	@XmlElement(required = true)
	protected String body;

	/**
	 * Gets the value of the eventId property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getEventId() {
		return eventId;
	}

	/**
	 * Sets the value of the eventId property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setEventId(String value) {
		this.eventId = value;
	}

	/**
	 * Gets the value of the previousIdEvent property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getPreviousIdEvent() {
		return previousIdEvent;
	}

	/**
	 * Sets the value of the previousIdEvent property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setPreviousIdEvent(String value) {
		this.previousIdEvent = value;
	}

	/**
	 * Gets the value of the method property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * Sets the value of the method property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setMethod(String value) {
		this.method = value;
	}

	/**
	 * Gets the value of the body property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getBody() {
		return body;
	}

	/**
	 * Sets the value of the body property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setBody(String value) {
		this.body = value;
	}

}
