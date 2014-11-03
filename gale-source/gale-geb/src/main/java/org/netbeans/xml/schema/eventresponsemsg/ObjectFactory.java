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
 * ObjectFactory.java
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
package org.netbeans.xml.schema.eventresponsemsg;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the org.netbeans.xml.schema.eventresponsemsg
 * package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the
 * Java representation for XML content. The Java representation of XML content
 * can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory
 * methods for each of these are provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

	private final static QName _EventResponseMsg_QNAME = new QName(
			"http://xml.netbeans.org/schema/eventResponseMsg",
			"eventResponseMsg");

	/**
	 * Create a new ObjectFactory that can be used to create new instances of
	 * schema derived classes for package:
	 * org.netbeans.xml.schema.eventresponsemsg
	 * 
	 */
	public ObjectFactory() {
	}

	/**
	 * Create an instance of {@link EventResponseMsg }
	 * 
	 */
	public EventResponseMsg createEventResponseMsg() {
		return new EventResponseMsg();
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link EventResponseMsg }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://xml.netbeans.org/schema/eventResponseMsg", name = "eventResponseMsg")
	public JAXBElement<EventResponseMsg> createEventResponseMsg(
			EventResponseMsg value) {
		return new JAXBElement<EventResponseMsg>(_EventResponseMsg_QNAME,
				EventResponseMsg.class, null, value);
	}

}
