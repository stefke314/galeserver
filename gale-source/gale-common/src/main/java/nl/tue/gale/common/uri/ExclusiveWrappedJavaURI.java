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
 * ExclusiveWrappedJavaURI.java
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
package nl.tue.gale.common.uri;

import java.net.URISyntaxException;

class ExclusiveWrappedJavaURI extends WrappedJavaURI {
	protected ExclusiveWrappedJavaURI(java.net.URI javaURI) {
		super(javaURI);
	}

	@Override
	public URI resolve(URI uri) {
		return new ExclusiveWrappedJavaURI(
				javaURI.resolve(((ExclusiveWrappedJavaURI) uri).javaURI));
	}

	@Override
	public URI relativize(URI uri) {
		return new ExclusiveWrappedJavaURI(
				javaURI.relativize(((ExclusiveWrappedJavaURI) uri).javaURI));
	}

	@Override
	public int compareTo(URI that) {
		return javaURI.compareTo(((ExclusiveWrappedJavaURI) that).javaURI);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ExclusiveWrappedJavaURI))
			return false;
		return javaURI.equals(((ExclusiveWrappedJavaURI) obj).javaURI);
	}

	@Override
	public URI parseServerAuthority() throws URISyntaxException {
		return new ExclusiveWrappedJavaURI(javaURI.parseServerAuthority());
	}

	@Override
	public URI normalize() {
		return new ExclusiveWrappedJavaURI(javaURI.normalize());
	}

	@Override
	public URI resolve(String str) {
		return new ExclusiveWrappedJavaURI(javaURI.resolve(str));
	}

	static class Builder extends WrappedJavaURI.Builder {
		@Override
		public URI build() {
			return new ExclusiveWrappedJavaURI(buildJavaURI());
		}
	}
}
