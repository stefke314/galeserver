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
 * JavaCodeUtil.java
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

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.SecureClassLoader;
import java.security.cert.Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.jci.compilers.CompilationResult;
import org.apache.commons.jci.compilers.JavaCompiler;
import org.apache.commons.jci.compilers.JavaCompilerFactory;
import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.stores.ResourceStore;

final class JavaCodeUtil {
	static final PermissionCollection trusted = new Permissions();
	static final PermissionCollection untrusted = new Permissions();

	static {
		trusted.add(new AllPermission());
		Policy.setPolicy(new CodeManagerPolicy());
		System.setSecurityManager(new SecurityManager());
	}

	private static final class CodeManagerPolicy extends Policy {
		public PermissionCollection getPermissions(CodeSource codesource) {
			if (codesource == null)
				return trusted;
			if (codesource.getLocation() == null)
				return trusted;
			if (codesource.getLocation().toString()
					.equals("http://gale.win.tue.nl"))
				return untrusted;
			return trusted;
		}

		public void refresh() {
		}
	}

	private static final CodeSource cs;

	static {
		try {
			Certificate[] cert = null;
			cs = new CodeSource(new URL("http://gale.win.tue.nl"), cert);
		} catch (Exception e) {
			throw new IllegalStateException(
					"unable to initialize JavaCodeManager", e);
		}
	}

	private static byte[] cbytes;

	private static class ProtectedClassLoader extends SecureClassLoader {
		public ProtectedClassLoader(ClassLoader parent) {
			super(parent);
		}

		protected Class<?> findClass(String name) throws ClassNotFoundException {
			try {
				return super.findClass(name);
			} catch (ClassNotFoundException e) {
			}
			byte[] b = cbytes;
			return this.defineClass(name, b, 0, b.length, cs);
		}
	}

	private static final ClassLoader loader = new ProtectedClassLoader(
			JavaCodeUtil.class.getClassLoader());

	private static final Lock lock = new ReentrantLock();

	public static Class<?> classFromBytes(String name, byte[] bytes) {
		lock.lock();
		try {
			cbytes = bytes;
			return loader.loadClass(name);
		} catch (Exception e) {
			throw new IllegalArgumentException("unable to create Class", e);
		} finally {
			lock.unlock();
		}
	}

	private static final JavaCompiler compiler = new JavaCompilerFactory()
			.createCompiler("nl.tue.gale.common.code.JaninoJavaCompiler");

	private static byte[] compile(final String code) {
		final List<byte[]> result = new LinkedList<byte[]>();
		CompilationResult cr = compiler.compile(new String[] { "Main.java" },
				new ResourceReader() {
					public byte[] getBytes(String arg0) {
						try {
							if ("Main.java".equals(arg0))
								return code.getBytes("UTF-8");
							else
								return null;
						} catch (UnsupportedEncodingException e) {
							return null;
						}
					}

					public boolean isAvailable(String arg0) {
						return true;
					}
				}, new ResourceStore() {
					public byte[] read(String arg0) {
						return null;
					}

					public void remove(String arg0) {
					}

					public void write(String arg0, byte[] arg1) {
						result.add(arg1);
					}
				});
		if (cr.getErrors().length > 0)
			throw new IllegalArgumentException("unable to compile code: "
					+ cr.getErrors()[0].getMessage());
		return result.get(0);
	}

	public static Class<?> classFromString(String name, String code) {
		byte[] bytes = compile(code);
		return classFromBytes(name, bytes);
	}
}
