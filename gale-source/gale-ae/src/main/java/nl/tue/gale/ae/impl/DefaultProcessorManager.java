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
 * DefaultProcessorManager.java
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
package nl.tue.gale.ae.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.tue.gale.ae.ProcessorException;
import nl.tue.gale.ae.ProcessorManager;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.ae.ResourceProcessor;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;

public class DefaultProcessorManager implements ProcessorManager,
		ApplicationContextAware {
	private ApplicationContext ac = null;
	private ServletContext sc = null;
	private List<ResourceProcessor> processorList = null;

	public void setProcessorList(List<ResourceProcessor> list) {
		processorList = list;
	}

	public Resource createResource(HttpServletRequest req,
			HttpServletResponse resp) {
		Resource result = new ResourceImpl();
		result.put("request", req);
		result.put("response", resp);
		result.put("servletContext", sc);
		result.put("applicationContext", ac);
		if (req != null && "true".equals(req.getParameter("object")))
			result.put("nl.tue.gale.object", "true");
		return result;
	}

	public void processResource(Resource resource) throws ProcessorException {
		for (ResourceProcessor rp : processorList) {
			Boolean go = (Boolean) resource.get(rp.getClass().getName());
			if (go == null)
				go = true;
			if (go)
				rp.processResource(resource);
		}
	}

	public void setApplicationContext(ApplicationContext ac)
			throws BeansException {
		this.ac = ac;
		this.sc = ((WebApplicationContext) ac).getServletContext();
	}

	private static class ResourceImpl implements Resource {
		private Map<String, Object> map = new HashMap<String, Object>();
		private Map<String, Boolean> usedMap = new HashMap<String, Boolean>();

		public Object get(String key) {
			return map.get(key);
		}

		@SuppressWarnings("unchecked")
		public <T> T getTyped(Class<T> c, String name) {
			return (T) get(name);
		}

		public Object put(String key, Object value) {
			if (!map.containsKey(key))
				usedMap.put(key, false);
			return map.put(key, value);
		}

		public Object putUsed(String key, Object value) {
			Object result = map.put(key, value);
			setUsed(key, true);
			return result;
		}

		public Object remove(String key) {
			usedMap.remove(key);
			return map.remove(key);
		}

		public boolean isUsed(String key) {
			if (usedMap.containsKey(key))
				return usedMap.get(key);
			return true;
		}

		public void setUsed(String key, boolean used) {
			if (map.containsKey(key))
				usedMap.put(key, used);
		}

		public void setUsed(String key) {
			setUsed(key, true);
		}
	}
}
