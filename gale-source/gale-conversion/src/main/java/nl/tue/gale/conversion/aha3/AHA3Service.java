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
 * AHA3Service.java
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
package nl.tue.gale.conversion.aha3;

import java.util.List;

import javax.jws.WebService;

@WebService(endpointInterface = "nl.tue.gale.event.EventListener", serviceName = "AHA3", portName = "AHA3Port", targetNamespace = "http://event.gale.tue.nl/")
public class AHA3Service implements nl.tue.gale.event.EventListener {
	private AHA3ServiceImpl impl = null;

	public AHA3ServiceImpl getImpl() {
		return impl;
	}

	public void setImpl(AHA3ServiceImpl impl) {
		this.impl = impl;
	}

	public List<String> event(String method, List<String> params) {
		return impl.event(method, params);
	}
}
