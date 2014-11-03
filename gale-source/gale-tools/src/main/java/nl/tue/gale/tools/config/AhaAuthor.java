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
 * AhaAuthor.java
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

import java.util.Vector;

import nl.tue.gale.common.GaleUtil;

public class AhaAuthor {

	public String passwd; // this is hashed or encrypted password
	public String name;
	public String login;
	public boolean admin = false;
	private Vector courseList = new Vector();

	public String showHashString(String unhashed) {
		return getHashString(unhashed);
	}

	//
	// advised are MD5 of SHA1
	private String getHashString(String unhashed) {
		return GaleUtil.digest(unhashed);
		// generate a string hash-representation of a string
		/*
		 * try { MessageDigest md=MessageDigest.getInstance("MD5"); StringBuffer
		 * hexdigest = new StringBuffer(); hexdigest.append("MD5:"); // state
		 * the used algorithm String byteHex; byte[]
		 * digest=md.digest(unhashed.getBytes());
		 * 
		 * for (int i=0; i < digest.length; i++) { byteHex =
		 * Integer.toHexString(digest[i] & 0xFF); // know no (unsigned) cast if
		 * (byteHex.length() == 1) hexdigest.append("0" + byteHex); else
		 * hexdigest.append(byteHex); } return hexdigest.toString();
		 * 
		 * } catch (Exception e) { System.err.println(
		 * "MD5 could not be instantiated, using standard hash which is XTREMELY UNSAFE"
		 * ); return "HC:"+Integer.toHexString(unhashed.hashCode()); }
		 */
	}

	public AhaAuthor() {

	}

	// the reason the password is not set is
	// the risk of entering the unhash password
	// instead of the hashed, or vice-versa

	public AhaAuthor(String l, String n) {
		login = l;
		name = n;
		passwd = null;
	}

	public void setUnHashed(String unhashed) {
		passwd = getHashString(unhashed);
	}

	public void setHashed(String hashed) {
		passwd = hashed;
	}

	// this function is usefull for writing down
	// the user-information
	public String getHashed() {
		return passwd;
	}

	public boolean checkPasswd(String unhashed) {
		return passwd.equals(getHashString(unhashed));
	}

	public void setLogin(String l) {
		login = l;
	}

	public String getLogin() {
		return login;
	}

	public void setName(String n) {
		name = n;
	}

	public String getName() {
		return name;
	}

	public boolean getAdmin() {
		return admin;
	}

	public void setCourseList(Vector v) {
		courseList = v;
	}

	public Vector getCourseList() {
		return courseList;
	}

}
