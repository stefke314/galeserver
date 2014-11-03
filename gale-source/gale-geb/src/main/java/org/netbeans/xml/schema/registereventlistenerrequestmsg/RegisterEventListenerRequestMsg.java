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
 * RegisterEventListenerRequestMsg.java
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
package org.netbeans.xml.schema.registereventlistenerrequestmsg;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for registerEventListenerRequestMsg complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="registerEventListenerRequestMsg">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="eventListenerID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="methods" type="{http://xml.netbeans.org/schema/registerEventListenerRequestMsg}methods" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "registerEventListenerRequestMsg", propOrder = {
		"eventListenerID", "methods" })
public class RegisterEventListenerRequestMsg {

	@XmlElement(required = true)
	protected String eventListenerID;
	@XmlElement(required = true)
	protected List<Methods> methods;

	/**
	 * Gets the value of the eventListenerID property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getEventListenerID() {
		return eventListenerID;
	}

	/**
	 * Sets the value of the eventListenerID property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setEventListenerID(String value) {
		this.eventListenerID = value;
	}

	/**
	 * Gets the value of the methods property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the methods property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getMethods().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Methods }
	 * 
	 * 
	 */
	public List<Methods> getMethods() {
		if (methods == null) {
			methods = new ArrayList<Methods>();
		}
		return this.methods;
	}

}
