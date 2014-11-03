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
 * Exec.java
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
package nl.tue.gale.tools.servlets;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.tue.gale.tools.AHAStatic;
import nl.tue.gale.tools.GaleToolsUtil;

public class Exec extends HttpServlet {

	private Hashtable handlers = null;

	public void init() {
		handlers = new Hashtable();
		handlers.put("authordir", new AuthorDirHandler(getServletContext()));
	}

	public void service(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		Hashtable resinfo = new Hashtable();
		try {
			ObjectInputStream ois = new ObjectInputStream(req.getInputStream());
			Hashtable reqinfo = (Hashtable) ois.readObject();
			ois.close();
			if (!reqinfo.containsKey("name")) {
				System.out.println("Invalid request table in Exec call");
				return;
			}
			ExecHandler handler = (ExecHandler) handlers.get((String) reqinfo
					.get("name"));
			if (handler == null)
				return;
			resinfo = handler.handle(reqinfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ObjectOutputStream oos = new ObjectOutputStream(res.getOutputStream());
		oos.writeObject(resinfo);
	}

	private static interface ExecHandler {
		public Hashtable handle(Hashtable reqinfo);
	}

	private static class AuthorDirHandler implements ExecHandler {
		private ServletContext sc;

		public AuthorDirHandler(ServletContext sc) {
			this.sc = sc;
		}

		public Hashtable handle(Hashtable reqinfo) {
			Vector files = new Vector();
			Hashtable resinfo = new Hashtable();
			resinfo.put("files", files);
			File root = new File(GaleToolsUtil.getHomeDir(sc),
					AHAStatic.AUTHORFILESPATH.substring(1));
			File dir = new File(root, (String) reqinfo.get("dir"));
			File[] filearray = dir.listFiles();
			for (int i = 0; i < filearray.length; i++) {
				if (!filearray[i].isDirectory())
					files.add(filearray[i].getName());
			}
			return resinfo;
		}
	}
}