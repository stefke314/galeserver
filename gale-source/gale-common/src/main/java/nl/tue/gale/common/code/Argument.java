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
 * Argument.java
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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public final class Argument implements Comparable<Argument> {
	private final String name;
	private final String type;
	private final Object value;

	private Argument(String name, String type, Object value) {
		checkNotNull(name);
		checkNotNull(type);
		this.name = name;
		this.type = type;
		this.value = value;
	}

	public static Argument of(String name, String type, Object value) {
		return new Argument(name, type, value);
	}

	public static List<Argument> of(String n1, String t1, Object v1, String n2,
			String t2, Object v2) {
		return listBuilder().arg(n1, t1, v1).arg(n2, t2, v2).build();
	}

	public static List<Argument> of(String n1, String t1, Object v1, String n2,
			String t2, Object v2, String n3, String t3, Object v3) {
		return listBuilder().arg(n1, t1, v1).arg(n2, t2, v2).arg(n3, t3, v3)
				.build();
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public Object getValue() {
		return value;
	}

	public static ListBuilder listBuilder() {
		return new ListBuilder();
	}

	public static class ListBuilder {
		private List<Argument> list = new LinkedList<Argument>();

		public ListBuilder arg(Argument arg) {
			checkNotNull(arg);
			list.add(arg);
			return this;
		}

		public ListBuilder arg(String name, String type, Object value) {
			list.add(of(name, type, value));
			return this;
		}

		public List<Argument> build() {
			return list;
		}
	}

	public static SetBuilder setBuilder() {
		return new SetBuilder();
	}

	public static class SetBuilder {
		private Set<Argument> set = new TreeSet<Argument>();

		public SetBuilder arg(Argument arg) {
			checkNotNull(arg);
			set.add(arg);
			return this;
		}

		public SetBuilder arg(String name, String type, Object value) {
			set.add(of(name, type, value));
			return this;
		}

		public Set<Argument> build() {
			return set;
		}
	}

	@Override
	public int compareTo(Argument o) {
		if (o == null)
			return -1;
		if (this == o)
			return 0;
		return name.compareTo(o.name);
	}

	private volatile int hashCode = 0;

	@Override
	public int hashCode() {
		if (hashCode == 0) {
			int hash = 248384;
			hash = 31 * hash + name.hashCode();
			hash = 31 * hash + type.hashCode();
			if (value != null)
				hash = 31 * hash + value.hashCode();
			if (hash == 0)
				hash = 1;
			hashCode = hash;
		}
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Argument))
			return false;
		Argument other = (Argument) obj;
		if (!name.equals(other.name))
			return false;
		if (!type.equals(other.type))
			return false;
		if (value == null)
			return other.value == null;
		return value.equals(other.value);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(type);
		sb.append(' ');
		sb.append(name);
		sb.append('(');
		sb.append((value == null ? "null" : value.toString()));
		sb.append(')');
		return sb.toString();
	}

	public String toJavaString() {
		StringBuilder sb = new StringBuilder(name.length() + type.length() + 1);
		sb.append(type);
		sb.append(' ');
		sb.append(name);
		return sb.toString();
	}

	public Argument copy() {
		Argument result = new Argument(name, type, value);
		result.setUserData(data);
		return result;
	}

	private Object data = null;

	public Object getUserData() {
		return data;
	}

	public void setUserData(Object data) {
		this.data = data;
	}
}
