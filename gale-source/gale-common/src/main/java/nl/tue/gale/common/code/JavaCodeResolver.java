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
 * JavaCodeResolver.java
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
package nl.tue.gale.common.code;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

public final class JavaCodeResolver implements CodeResolver {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(JavaCodeResolver.class);
	private GELResolver gel = null;

	public JavaCodeResolver() {
	}

	public JavaCodeResolver(GELResolver gel) {
		this.gel = gel;
	}

	public GELResolver getGel() {
		return gel;
	}

	public void setGel(GELResolver gel) {
		this.gel = gel;
	}

	public interface JavaCodeVariableStore {
		public Object[] getVariables();
	}

	@Override
	public ResolveCallback resolve(StringBuilder code, List<Argument> params) {
		/*
		 * String debugStr = null; boolean debug =
		 * log.getEffectiveLevel().equals(Level.DEBUG); if (debug) debugStr =
		 * code.toString();
		 */
		if (gel == null)
			throw new IllegalStateException("GELResolver not set");
		convertLegacySet(code);
		resolveGaleCode(code, params);
		StringBuilder sb = new StringBuilder();
		Collections.sort(params);
		for (Argument param : params) {
			sb.append("public ");
			sb.append(param.toJavaString());
			sb.append(";\n");
		}
		sb.append("\npublic Object main(");
		boolean first = true;
		for (Argument param : params) {
			if (!first)
				sb.append(", ");
			sb.append(param.getType());
			sb.append(" _p_");
			sb.append(param.getName());
			first = false;
		}
		sb.append(") throws Exception {\n");
		final List<Argument> orgParams = new ArrayList<Argument>(params.size());
		for (Argument param : params) {
			sb.append(param.getName());
			sb.append(" = _p_");
			sb.append(param.getName());
			sb.append(";\n");
			orgParams.add(param.copy());
		}
		code.insert(0, sb);
		code.append("\n}\n\npublic Object[] getVariables() {\nreturn new Object[] {");
		first = true;
		for (Argument param : params) {
			if (!first)
				code.append(", ");
			code.append(param.getName());
			first = false;
		}
		code.append("};\n}");
		/*
		 * if (debug) log.debug("translated '" + debugStr + "' to '" + code);
		 */
		return new ResolveCallback() {
			@Override
			public Object callback(Object resultInstance, Object resultValue) {
				List<Argument> newParams = new ArrayList<Argument>(
						orgParams.size());
				Object[] variables = ((JavaCodeVariableStore) resultInstance)
						.getVariables();
				for (int i = 0; i < orgParams.size(); i++) {
					Argument orgParam = orgParams.get(i);
					Argument newParam = Argument.of(orgParam.getName(),
							orgParam.getType(), variables[i]);
					newParam.setUserData(orgParam.getUserData());
					newParams.add(newParam);
				}
				gel.resolveStateChange(orgParams, newParams);
				return resultValue;
			}
		};
	}

	protected void resolveGaleCode(StringBuilder code, List<Argument> params) {
		int i = 0;
		while ((i = code.indexOf("${", i)) >= 0) {
			int j = code.indexOf("}", i);
			Argument found = gel.resolveGaleVariable(code.substring(i + 2, j),
					params);
			if (!params.contains(found))
				params.add(found);
			code.delete(i, j + 1);
			code.insert(i, found.getName());
		}
	}

	private void convertLegacySet(StringBuilder code) {
		int i = 0;
		while ((i = code.indexOf("#{", i)) >= 0) {
			int j = code.indexOf(",", i);
			int k = code.indexOf("}", j);
			int l = j;
			int m = code.indexOf("{", l);
			while (m >= 0 && m < k) {
				l = k;
				k = code.indexOf("}", l + 1);
				m = code.indexOf("{", l);
			}
			code.delete(k, k + 1);
			code.setCharAt(j, '=');
			code.insert(j, '}');
			code.setCharAt(i, '$');
			i = k;
		}
	}

}