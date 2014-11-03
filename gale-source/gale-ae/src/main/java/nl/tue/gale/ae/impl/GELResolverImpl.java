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
 * CodeResolverImpl.java
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

import java.util.Arrays;
import java.util.List;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.cache.CacheSession;
import nl.tue.gale.common.code.Argument;
import nl.tue.gale.common.code.GELResolver;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.dm.data.Attribute;
import nl.tue.gale.dm.data.Concept;
import nl.tue.gale.um.GaleCode;
import nl.tue.gale.um.data.EntityValue;

public class GELResolverImpl implements GELResolver {
	@Override
	public void resolveStateChange(List<Argument> oldValues,
			List<Argument> newValues) {
		GaleContext gale = (GaleContext) getArgument(oldValues,
				"nl.tue.gale.ae.GaleContext").getValue();
		@SuppressWarnings("unchecked")
		CacheSession<EntityValue> session = (CacheSession<EntityValue>) getArgument(
				oldValues, "nl.tue.gale.common.cache.CacheSession").getValue();
		try {
			for (int i = 0; i < oldValues.size(); i++) {
				if (oldValues.get(i).getName().startsWith("_v_")
						&& !GaleUtil.safeEquals(oldValues.get(i).getValue(),
								newValues.get(i).getValue()))
					storeStateChange(gale, session, newValues.get(i));
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"unable to reconcile state change", e);
		}
	}

	private void storeStateChange(GaleContext gale,
			CacheSession<EntityValue> session, Argument argument) {
		if (!(argument.getUserData() instanceof Attribute))
			throw new IllegalStateException(
					"the value of a non user-model variable changed");
		Attribute attr = (Attribute) argument.getUserData();
		URI resolved = gale.conceptUri().resolve("/");
		if (!attr.getUri().toString().startsWith(resolved.toString()))
			throw new IllegalArgumentException(
					"can only set user model variables within the same application: '"
							+ attr.getUri() + "'");
		URI evURI = GaleUtil.addUserInfo(attr.getUri(), gale.userId());
		session.put(evURI, new EntityValue(evURI, argument.getValue()));
	}

	@Override
	public Argument resolveGaleVariable(String name, List<Argument> params) {
		GaleContext gale = (GaleContext) getArgument(params,
				"nl.tue.gale.ae.GaleContext").getValue();
		@SuppressWarnings("unchecked")
		CacheSession<EntityValue> session = (CacheSession<EntityValue>) getArgument(
				params, "nl.tue.gale.common.cache.CacheSession").getValue();
		try {
			Object o = GaleCode.resolve(
					gale.dm().get(Concept.getConceptURI(session.getBaseUri())),
					gale.dm(), name);
			if (o == null)
				throw new IllegalArgumentException("variable not found: '"
						+ name + "'");
			int max = -1;
			for (Argument arg : params) {
				if (arg.getName().startsWith("_v_"))
					max = Math.max(max,
							Integer.parseInt(arg.getName().substring(3)));
				if (o.equals(arg.getUserData()))
					return arg;
			}
			max++;
			Object userData = o;
			if (o instanceof Attribute) {
				o = getAttributeValue(session, (Attribute) o, gale.userId());
			} else if (o instanceof Attribute[]) {
				Attribute[] attrArray = (Attribute[]) o;
				attrArray = Arrays.copyOf(attrArray, attrArray.length);
				o = new Object[attrArray.length];
				for (int i = 0; i < attrArray.length; i++)
					((Object[]) o)[i] = getAttributeValue(session,
							attrArray[i], gale.userId());
			}
			Argument result = Argument.of("_v_" + max, o.getClass()
					.getSimpleName(), o);
			result.setUserData(userData);
			return result;
		} catch (Exception e) {
			throw new IllegalArgumentException("unable to resolve variable '"
					+ name + "' (base: " + session.getBaseUri(), e);
		}
	}

	private Object getAttributeValue(CacheSession<EntityValue> session,
			Attribute attr, String userId) {
		return session.get(GaleUtil.addUserInfo(attr.getUri(), userId))
				.getValue();
	}

	private Argument getArgument(List<Argument> arguments, String className) {
		for (Argument arg : arguments)
			if (arg.getType().equals(className))
				return arg;
		return null;
	}
}
