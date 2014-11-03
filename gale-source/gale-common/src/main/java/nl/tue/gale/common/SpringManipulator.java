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
 * SpringManipulator.java
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
package nl.tue.gale.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

public class SpringManipulator {
	private Object bean = null;

	public void setBean(Object bean) {
		this.bean = bean;
	}

	private String property = null;

	public void setProperty(String property) {
		this.property = property;
	}

	public <E> void setList(List<E> list) {
		BeanWrapper wrapper = new BeanWrapperImpl(bean);
		@SuppressWarnings("unchecked")
		List<E> beanList = new ArrayList<E>(
				(List<E>) wrapper.getPropertyValue(property));
		beanList.addAll(list);
		wrapper.setPropertyValue(property, beanList);
	}

	public <K, V> void setMap(Map<K, V> map) {
		BeanWrapper wrapper = new BeanWrapperImpl(bean);
		@SuppressWarnings("unchecked")
		Map<K, V> beanMap = new HashMap<K, V>(
				(Map<K, V>) wrapper.getPropertyValue(property));
		beanMap.putAll(map);
		wrapper.setPropertyValue(property, beanMap);
	}
}
