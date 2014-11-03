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
 * JavaCodeManager.java
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

import static nl.tue.gale.common.code.JavaCodeUtil.untrusted;

import java.io.FilePermission;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PropertyPermission;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

public class JavaCodeManager implements CodeManager {
	private static final Logger log = Logger.getLogger(JavaCodeManager.class);

	private String importCode = "";

	public void setImportCode(List<String> importList) {
		if (importList.size() == 0)
			return;
		StringBuilder result = new StringBuilder();
		for (String s : importList)
			result.append("import " + s + ";\n");
		importCode = result.toString();
	}

	@SuppressWarnings("unchecked")
	public void setGaleConfig(Object gc) {
		String libDir = (String) ((HashMap<String, Object>) gc).get("libDir");
		if (libDir != null && !untrusted.elements().hasMoreElements()) {
			if (!libDir.endsWith("/"))
				libDir += "/";
			untrusted.add(new FilePermission(libDir + "-", "read"));
			untrusted.add(new RuntimePermission("accessDeclaredMembers"));
			untrusted.add(new PropertyPermission("*", "read"));
		}
	}

	private final Map<String, Class<?>> cache = new MapMaker().softValues()
			.maximumSize(500)
			.makeComputingMap(new Function<String, Class<?>>() {
				@Override
				public Class<?> apply(String code) {
					StringBuilder sb = new StringBuilder(code);
					String className = addClassCode(sb);
					return JavaCodeUtil.classFromString(className,
							sb.toString());
				}
			});

	private final AtomicInteger count = new AtomicInteger(0);

	private String addClassCode(StringBuilder code) {
		String name = "CodeManagerPart" + count.getAndIncrement();
		StringBuilder sb = new StringBuilder();
		sb.append("package nl.tue.gale.common.code;\n\n");
		sb.append(importCode);
		sb.append("\npublic class ");
		sb.append(name);
		sb.append(" implements JavaCodeResolver.JavaCodeVariableStore {\n");
		code.insert(0, sb);
		code.append("\n}");
		return "nl.tue.gale.common.code." + name;
	}

	private final ExecutorService executor = Executors.newCachedThreadPool();

	private Object run(Class<?> clazz, List<Argument> params,
			ResolveCallback callback) {
		try {
			final Object instance = clazz.newInstance();
			Method foundMethod = null;
			for (Method m : clazz.getMethods())
				if (m.getName().equals("main")) {
					foundMethod = m;
					break;
				}
			final Method method = foundMethod;
			final List<Object> args = new ArrayList<Object>(params.size());
			for (Argument argument : params)
				args.add(argument.getValue());
			Future<Object> future = executor.submit(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					return method.invoke(instance, args.toArray());
				}
			});
			Object result = null;
			try {
				result = future.get(3, TimeUnit.SECONDS);
			} catch (TimeoutException e) {
				throw new IllegalStateException(
						"unable to run code due to timeout", e);
			}
			return callback.callback(instance, result);
		} catch (Exception e) {
			throw new IllegalArgumentException("unable to run code", e);
		}
	}

	@Override
	public Object evaluate(CodeResolver resolver, String code,
			Collection<Argument> params) {
		if (!(resolver instanceof JavaCodeResolver))
			throw new IllegalArgumentException(
					"resolver must be JavaCodeResolver");
		try {
			List<Argument> args = new LinkedList<Argument>(params);
			StringBuilder sb = new StringBuilder(code.length());
			if (code.startsWith("~"))
				sb.append(code, 1, code.length());
			else {
				sb.append("return ");
				sb.append(code);
				sb.append(';');
			}
			ResolveCallback callback = resolver.resolve(sb, args);
			Class<?> clazz = cache.get(sb.toString());
			return run(clazz, args, callback);
		} catch (Exception e) {
			log.debug(e, e);
			throw new IllegalArgumentException("unable to evaluate code: '\n"
					+ code + "\n': "
					+ replaceNumber(e.getMessage(), params.size() * 2));
		}
	}

	@Override
	public void execute(CodeResolver resolver, String code,
			Collection<Argument> params) {
		if (!(resolver instanceof JavaCodeResolver))
			throw new IllegalArgumentException(
					"resolver must be JavaCodeResolver");
		try {
			List<Argument> args = new LinkedList<Argument>(params);
			StringBuilder sb = new StringBuilder(code);
			sb.append("\n    return null;");
			ResolveCallback callback = resolver.resolve(sb, args);
			Class<?> clazz = cache.get(sb.toString());
			run(clazz, args, callback);
		} catch (Exception e) {
			log.debug(e, e);
			throw new IllegalArgumentException("unable to execute code: '\n"
					+ code + "\n': "
					+ replaceNumber(e.getMessage(), params.size() * 2));
		}
	}

	private String replaceNumber(String message, int i) {
		int index = message.indexOf("File Main.java, Line ");
		if (index < 0)
			return message;
		StringBuilder sb = new StringBuilder(message);
		sb.delete(index, index + 21);
		int line = Integer
				.parseInt(sb.substring(index, sb.indexOf(",", index)));
		sb.delete(index, sb.indexOf(",", index));
		sb.insert(index, line - i - 17);
		sb.insert(index, "Line ");
		return sb.toString();
	}
}
