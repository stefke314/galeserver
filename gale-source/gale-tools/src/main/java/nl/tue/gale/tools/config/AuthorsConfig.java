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
 * AuthorsConfig.java
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
package nl.tue.gale.tools.config;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletContext;

import nl.tue.gale.ae.EventBusClient;
import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.event.EventHash;
import nl.tue.gale.tools.GaleToolsUtil;
import nl.tue.gale.um.data.UserEntity;

public class AuthorsConfig {
	private EventBusClient ebc;
	private Hashtable<String, UserEntity> authors = new Hashtable<String, UserEntity>();
	private Hashtable<String, String> courseindex = new Hashtable<String, String>();

	public AuthorsConfig(ServletContext sc) {
		ebc = (EventBusClient) GaleToolsUtil.getBean(sc, "eventBusClient");
		LoadConfig();
	}

	public synchronized boolean containsCourse(String course) {
		return courseindex.containsKey(course.toLowerCase());
	}

	public synchronized AhaAuthor GetAuthor(String l) {
		if (l == null)
			return null;
		return toAuthor(authors.get(l));
	}

	@SuppressWarnings("unchecked")
	public synchronized void PutAuthor(AhaAuthor a) {
		if (a == null)
			return;
		authors.put(a.login, fromAuthor(a));
		updateIndex((List<String>) a.getCourseList(), a.login);
	}

	public synchronized void RemoveAuthor(String a) {
		if (a == null)
			return;
		authors.remove(a);
	}

	@SuppressWarnings("unchecked")
	public synchronized void reindex() {
		courseindex.clear();
		for (UserEntity ue : authors.values())
			updateIndex((List<String>) toAuthor(ue).getCourseList(), ue.getId());
		courseindex.put("web-inf", "aha_reserved");
		courseindex.put("config", "aha_reserved");
		courseindex.put("database", "aha_reserved");
		courseindex.put("ahastandard", "aha_reserved");
		courseindex.put("images", "aha_reserved");
		courseindex.put("lib", "aha_reserved");
		courseindex.put("meta-inf", "aha_reserved");
		courseindex.put("xmlroot", "aha_reserved");
		courseindex.put("author", "aha_reserved");
	}

	public synchronized void StoreConfig() {
		List<String> call = new LinkedList<String>();
		for (UserEntity ue : authors.values())
			call.addAll(UserEntity.toEvent(ue));
		try {
			ebc.event("setentity", call);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public synchronized void LoadConfig() {
		authors = new Hashtable<String, UserEntity>();
		courseindex = new Hashtable<String, String>();
		List<String> query = null;
		try {
			query = ebc
					.event("queryum",
							Arrays.asList(new String[] { "query:from UserEntity ue where ue.properties['author'] = 'true'" }));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		String resultstr = query.get(0);
		if (!(resultstr.equals("result:ok")))
			throw new RuntimeException(resultstr.substring(7));
		try {
			List<UserEntity> list = GaleUtil.gson().fromJson(query.get(1),
					List.class);
			for (UserEntity ue : list)
				authors.put(ue.getId(), ue);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		reindex();
	}

	private AhaAuthor toAuthor(UserEntity ue) {
		if (ue == null)
			return null;
		AhaAuthor result = new AhaAuthor();
		result.login = ue.getId();
		result.passwd = ue.getProperty("password");
		result.name = ue.getProperty("name");
		result.admin = new Boolean(ue.getProperty("admin"));
		String coursesstr = ue.getProperty("courses");
		if (coursesstr != null) {
			EventHash eh = new EventHash(coursesstr);
			for (String course : eh.getItems())
				result.getCourseList().add(course);
		}
		return result;
	}

	private UserEntity fromAuthor(AhaAuthor author) {
		UserEntity result = authors.get(author.login);
		if (result == null)
			result = new UserEntity(author.login);
		result.setProperty("author", "true");
		result.setProperty("password", author.getHashed());
		result.setProperty("name", author.name);
		result.setProperty("admin", author.admin + "");
		EventHash eh = new EventHash("courses");
		for (String course : (Vector<String>) author.getCourseList())
			eh.addItem(course);
		result.setProperty("courses", eh.toString());
		return result;
	}

	private void updateIndex(List<String> courses, String author) {
		for (String course : courses)
			courseindex.put(course.toLowerCase(), author);
	}

	public synchronized void addAuthor(String author) {
		UserEntity entity = null;
		try {
			// TODO: change code
			// entity = UserEntity.fromEvent(ebc.event("getentity",
			// Arrays.asList(new String[]
			// {"entity:"+author})),null);
		} catch (Exception e) {
		}
		if (entity == null) {
			entity = new UserEntity(author);
			entity.setProperty("password", GaleUtil.digest(""));
			entity.setProperty("name", author);
		}
		entity.setProperty("author", "true");

		try {
			ebc.event("setentity", UserEntity.toEvent(entity));
		} catch (Exception e) {
			e.printStackTrace();
		}
		LoadConfig();
	}
}