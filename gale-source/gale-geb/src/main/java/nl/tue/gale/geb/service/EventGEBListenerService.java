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
 * EventGEBListenerService.java
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
package nl.tue.gale.geb.service;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.netbeans.xml.schema.entlistenerdefinition.EventRequestMsg;
import org.netbeans.xml.schema.eventresponsemsg.EventResponseMsg;

@WebService(name = "eventGEBListenerPortType", targetNamespace = "http://j2ee.netbeans.org/wsdl/GEB/eventGEBListener")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface EventGEBListenerService {
	@WebMethod(action = "eventGEBListenerOperation_action")
	@WebResult(name = "part1", partName = "part1")
	public EventResponseMsg eventGEBListenerOperation(
			@WebParam(name = "part1", partName = "part1") EventRequestMsg part1);
}
