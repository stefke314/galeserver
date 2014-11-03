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
 * UpdateContentService.java
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
package nl.tue.gale.ae.grapple;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

@WebService(serviceName = "UpdateContent", portName = "UpdateContentPort", targetNamespace = "http://gale.win.tue.nl/services")
public class UpdateContentService {
	private UpdateContentManager updateContentManager = null;

	@WebMethod(exclude = true)
	public UpdateContentManager getUpdateContentManager() {
		return updateContentManager;
	}

	@WebMethod(exclude = true)
	public void setUpdateContentManager(
			UpdateContentManager updateContentManager) {
		this.updateContentManager = updateContentManager;
	}

	@WebMethod
	@WebResult(name = "updateCAMModelResult", targetNamespace = "http://gale.win.tue.nl/services")
	public String updateCAMModel(
			@WebParam(name = "model", targetNamespace = "http://gale.win.tue.nl/services") String model) {
		System.out.println("model received: " + model);
		UpdateContentResponse response = updateContentManager
				.updateCAMModel(model);
		if (response.getException() != null)
			response.getException().printStackTrace();
		return response.toString();
	}
}
