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
 * SOAPFactory.java
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
package nl.tue.gale.event;

import java.net.URL;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import nl.tue.gale.common.GaleUtil;

public class SOAPFactory implements EventListenerFactory {
	/**
	 * Returns a <code>EventListener</code> proxy to the webservice running on
	 * the specified <code>url</code>. The <code>url</code> should specify the
	 * actual location of the webservice and some additional parameters.
	 * 
	 * Two additional parameters are required in the query string of the
	 * <code>url</code>, 'service' and 'port'. They should refer to the name of
	 * the service and the name of the port respectively. The wsdl will be
	 * assumed to be located at the specified <code>url</code> without query
	 * string and adding '?wsdl'.
	 * 
	 * @param url
	 *            the <code>URL</code> specifying the location of the webservice
	 * @return the <code>EventListener</code> proxy to the specified webservice
	 */
	public EventListener getListener(URL url) {
		try {
			Map<String, String> params = GaleUtil.getQueryParameters(url
					.getQuery());
			String urlstr = url.toString();
			URL wsdl = new URL(urlstr.substring(0, urlstr.indexOf("?"))
					+ "?wsdl");
			Service service = Service.create(wsdl, new QName(
					"http://event.gale.tue.nl/", params.get("service")));
			EventListener result = (EventListener) service.getPort(new QName(
					"http://event.gale.tue.nl/", params.get("port")),
					EventListener.class);
			return result;
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"unable to create EventListener for '" + url + "': "
							+ e.getMessage(), e);
		}
	}
}