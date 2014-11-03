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
 * AuthorLogin.java
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
package nl.tue.gale.tools.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JFrame;

public class AuthorLogin {

	public URL home;
	public String username = "";
	public String password = "";

	public AuthorLogin(URL home) {
		this.home = home;
	}

	public boolean login() {
		boolean userValid = false;
		LoginDialog login = new LoginDialog(new JFrame(""));
		if (login.id) {
			username = login.username.getText();
			setUserName(username);
			char[] passwd = login.password.getPassword();
			password = new String(passwd);
			userValid = validateUser(username, password);
		} else
			System.out.println("Cancel was pressed");
		login.dispose();
		return userValid;
	}

	private boolean validateUser(String usr, String pwd) {

		boolean exists = false;
		String contextpath = "";
		StringBuffer responce = new StringBuffer();
		try {
			String path = home.getPath();
			String temp = path.substring(1, path.length());
			int index = temp.indexOf("/");
			index++;
			contextpath = path.substring(0, index);
			// Create an object we can use to communicate with the servlet
			URL servletURL = new URL(home.getProtocol() + "://"
					+ home.getHost() + ":" + home.getPort() + contextpath
					+ "/authorservlets/AppletLogin");
			URLConnection servletConnection = servletURL.openConnection();
			servletConnection.setDoOutput(true); // to allow us to write to the
													// URL
			servletConnection.setUseCaches(false); // to ensure that we do
													// contact
			// the servlet and don't get
			// anything from the browser's
			// cache
			// Write the message to the servlet
			PrintStream out = new PrintStream(
					servletConnection.getOutputStream());
			out.println(usr + "\n" + pwd);
			out.close();

			// Now read in the response
			InputStream in = servletConnection.getInputStream();
			int chr;
			while ((chr = in.read()) != -1) {
				responce.append((char) chr);
			}
			in.close();
			if (responce.toString().trim().equals("true"))
				exists = true;
		} catch (IOException ioe) {
			System.out.println("AuthorLogin: login: Error: " + ioe.toString());
		}

		return exists;
	}

	public void setUserName(String name) {
		username = name;
	}

	public String getUserName() {
		return username;
	}

}
