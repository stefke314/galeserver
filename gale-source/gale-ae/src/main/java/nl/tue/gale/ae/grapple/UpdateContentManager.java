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
 * UpdateContentManager.java
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
package nl.tue.gale.ae.grapple;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import nl.tue.gale.ae.EventBusClient;
import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.dm.data.Concept;

import org.dom4j.Element;

public class UpdateContentManager {
	private EventBusClient eventBusClient = null;

	public EventBusClient getEventBusClient() {
		return eventBusClient;
	}

	public void setEventBusClient(EventBusClient eventBusClient) {
		this.eventBusClient = eventBusClient;
	}

	public UpdateContentResponse updateCAMModel(String model) {
		return updateCAMModel(GaleUtil.parseXML(new StringReader(model))
				.getRootElement());
	}

	public UpdateContentResponse updateCAMModel(Element model) {
		UpdateContentResponse response = new UpdateContentResponse();
		List<String> events = new LinkedList<String>();
		List<Concept> concepts = null;
		try {
			concepts = CAMFormat.getConcepts(model);
			for (Concept c : concepts) {
				events.addAll(Concept.toEvent(c));
				if ("true".equals(c.getProperty("start")))
					response.getStartConcepts().add(c.getUriString());
			}
			eventBusClient.event("async:setdm", events);
		} catch (Exception e) {
			response.setException(e);
		}
		if (response.getException() == null) {
			String uri = null;
			if (response.getStartConcepts().size() > 0)
				uri = response.getStartConcepts().get(0);
			else if (concepts.size() > 0)
				uri = concepts.get(0).getUriString();
			else
				uri = "gale://gale.tue.nl/admin/courselist";
			response.setUrl(GaleUtil.getProperty("rootGaleUrl") + uri);
		}
		return response;
	}
}
