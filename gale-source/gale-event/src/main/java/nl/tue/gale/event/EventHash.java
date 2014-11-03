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
 * EventHash.java
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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class EventHash extends TreeMap<String, String> {
	private static final long serialVersionUID = -8586979665213686834L;
	private int keynum = 0;
	private String name = null;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name.indexOf(":") != -1)
			throw new IllegalArgumentException("name may not contain colon");
		this.name = name;
	}

	public EventHash(String eventcode) {
		super();
		parse(eventcode);
	}

	private void parse(String eventcode) {
		int i = eventcode.indexOf(":");
		if (i == -1) {
			// just the name
			name = eventcode;
		} else {
			// name and hash
			name = eventcode.substring(0, i);
			parseHash(eventcode.substring(i + 1));
		}
	}

	private void parseHash(String hashcode) {
		int j = 0;
		int newkn = 0;
		for (int i = 0; i < hashcode.length(); i++) {
			if (hashcode.charAt(i) == ';') {
				if ((i == 0) || (i > 0 && hashcode.charAt(i - 1) != '\\')) {
					addHash(hashcode.substring(j, i), newkn);
					newkn++;
					j = i + 1;
				} else if (i > 0 && hashcode.charAt(i - 1) == '\\') {
					hashcode = hashcode.substring(0, i - 1)
							+ hashcode.substring(i);
					i--;
				}
			}
		}
		addHash(hashcode.substring(j), newkn);
	}

	private void addHash(String hashcode, int newkn) {
		int i = hashcode.indexOf(":");
		if (i == -1) {
			put("key" + addZeros(newkn, 3), hashcode);
			return;
		}
		put(hashcode.substring(0, i), hashcode.substring(i + 1));
	}

	private String addZeros(int i, int count) {
		StringBuilder result = new StringBuilder();
		result.append(i);
		while (result.length() < count)
			result.insert(0, "0");
		return result.toString();
	}

	public EventHash addItem(String item) {
		put("key" + addZeros(keynum, 3), item);
		keynum++;
		return this;
	}

	public List<String> getItems() {
		List<String> result = new LinkedList<String>();
		for (Map.Entry<String, String> entry : entrySet())
			result.add(entry.getValue());
		return result;
	}

	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(name);
		if (size() > 0) {
			result.append(":");
			int i = 0;
			for (Map.Entry<String, String> entry : entrySet()) {
				if (i != 0)
					result.append(";");
				result.append(fixSemicolon(entry.getKey()));
				result.append(":");
				result.append(fixSemicolon(entry.getValue()));
				i++;
			}
		}
		return result.toString();
	}

	private String fixSemicolon(String s) {
		StringBuilder result = new StringBuilder(s == null ? "" : s);
		int i = 0;
		while (i < result.length()) {
			if (result.charAt(i) == ';') {
				result.insert(i, '\\');
				i++;
			}
			i++;
		}
		return result.toString();
	}

	public static EventHash createSingleEvent(String name, String event) {
		EventHash result = new EventHash(name);
		result.addItem(event);
		return result;
	}
}
