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
 * TAMtFile.java
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
package nl.tue.gale.tools.amt;

/**
 * Type representing a file, used for remote file display purposes.
 * 
 * @author T.J. Dekker
 * @version 1.0.0
 */
public class TAMtFile implements java.io.Serializable, Cloneable {

	private String FPath;
	private String FName;
	private long FSize;
	private long FDate; // date last modified
	private boolean FDir;
	private boolean FApp; // representing an AHA! application

	/**
	 * Default Constructor
	 * 
	 * @param APath
	 *            path of the file
	 * @param AName
	 *            name of the file
	 * @param ASize
	 *            size of the file
	 * @param ADate
	 *            last modified date of the file
	 * @param ADir
	 *            the file is a directory
	 * @param AApp
	 *            the file represents an AHA! application.
	 */
	public TAMtFile(String APath, String AName, long ASize, long ADate,
			boolean ADir, boolean AApp) {
		FPath = APath;
		FName = AName;
		FSize = ASize;
		FDate = ADate;
		FDir = ADir;
		FApp = AApp;
	}

	/**
	 * Constructor used to create a directory. Size and Date will both be set to
	 * <Code>0</Code>. The file will not be set as being an AHA! Application.
	 * 
	 * @param APath
	 *            path of the file
	 * @param AName
	 *            name of the file
	 * @param ADir
	 *            the file is a directory
	 */
	public TAMtFile(String APath, String AName, boolean ADir) {
		long l = new Long("0").longValue();
		FPath = APath;
		FName = AName;
		FSize = l;
		FDate = l;
		FDir = ADir;
		FApp = false;
	}

	/**
	 * Constructor used to create an AHA! application. Size and Date will both
	 * be set to <Code>0</Code>.
	 * 
	 * @param APath
	 *            path of the file
	 * @param AName
	 *            name of the file
	 * @param ADir
	 *            the file is a directory
	 * @param AApp
	 *            the file represents an AHA! application.
	 */
	public TAMtFile(String APath, String AName, boolean ADir, boolean AApp) {
		long l = new Long("0").longValue();
		FPath = APath;
		FName = AName;
		FSize = l;
		FDate = l;
		FDir = ADir;
		FApp = AApp;
	}

	/**
	 * Retrieves the path of this file
	 * 
	 * @return The path of this file (the FPath field)
	 */
	public String getPath() {
		return FPath;
	}

	/**
	 * Retrieves the name of this file
	 * 
	 * @return The name of this file (the FName field)
	 */
	public String getName() {
		return FName;
	}

	/**
	 * Retrieves the size of this file
	 * 
	 * @return The size of this file (the FSize field)
	 */
	public long getSize() {
		return FSize;
	}

	/**
	 * Retrieves the last modified date of this file
	 * 
	 * @return The last modified date of this file (the FDate field)
	 */
	public long getDate() {
		return FDate;
	}

	/**
	 * Retrieves if this file is a directory
	 * 
	 * @return <Code>true</Code> if and only if this file is a directory
	 */
	public boolean isDirectory() {
		return FDir;
	}

	/**
	 * Retrieves if this file represents an AHA! application
	 * 
	 * @return <Code>true</Code> if and only if this file is an AHA! application
	 */
	public boolean isApp() {
		return FApp;
	}

	/**
	 * Sets the path of this file in FPath
	 * 
	 * @param apath
	 *            the name for this file
	 */
	public void setPath(String apath) {
		FPath = apath;
	}

	/**
	 * Sets the name of this file in FName
	 * 
	 * @param aname
	 *            the name for this file
	 */
	public void setName(String aname) {
		FName = aname;
	}

	/**
	 * Sets the size of this file in FSize
	 * 
	 * @param asize
	 *            the size for this file
	 */
	public void setSize(long asize) {
		FSize = asize;
	}

	/**
	 * Sets the date last modified of this file in FDate
	 * 
	 * @param adate
	 *            the date for this file
	 */
	public void setDate(long adate) {
		FDate = adate;
	}

	/**
	 * Sets the directory status for this file in FDir
	 * 
	 * @param isdir
	 *            this file is a directory
	 */
	public void setDir(boolean isdir) {
		FDir = isdir;
	}

	/**
	 * Sets the application status for this file in FApp
	 * 
	 * @param isapp
	 *            this file is an AHA! application
	 */
	public void setApp(boolean isapp) {
		FApp = isapp;
	}

	/**
	 * Retrieves a string representation of this object, being the name of the
	 * file
	 * 
	 * @return name of the file (the FName field)
	 */
	public String toString() {
		return FName;
	}

	public Object clone() {
		try {
			Object newobj = super.clone();
			TAMtFile f = (TAMtFile) newobj;
			f.setPath(FPath);
			f.setName(FName);
			f.setSize(FSize);
			f.setDate(FDate);
			f.setDir(FDir);
			f.setApp(FApp);

			return f;
		} catch (Exception e) {
			System.out.println("TAMtFile.clone(): " + e.toString());
		}
		return null;
	}

	public boolean equals(Object aObject) {
		if (aObject == null)
			return false;
		if (!(aObject instanceof TAMtFile))
			return false;

		TAMtFile f = (TAMtFile) aObject;
		boolean same = true;

		same &= (FApp == f.isApp());
		same &= (FDate == f.getDate());
		same &= (FDir == f.isDirectory());
		same &= (FName.equals(f.getName()));
		same &= (FPath.equals(f.getPath()));
		same &= (FSize == f.getSize());

		return same;
	}
};