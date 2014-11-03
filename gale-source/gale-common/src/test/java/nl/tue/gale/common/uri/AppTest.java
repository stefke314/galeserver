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
 * AppTest.java
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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
	/**
	 * Create the test case
	 * 
	 * @param testName
	 *            name of the test case
	 */
	public AppTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(AppTest.class);
	}

	/**
	 * Rigourous Test :-)
	 */
	public void testApp() {
		assertTrue(FastURI.normalizePath("../test").equals("../test"));
		assertTrue(FastURI.normalizePath("/../test").equals("/../test"));
		assertTrue(FastURI.normalizePath("../test/").equals("../test/"));
		assertTrue(FastURI.normalizePath("./temp/../test/").equals("test/"));
		assertTrue(FastURI.normalizePath("../hello/../test").equals("../test"));
		assertTrue(FastURI.normalizePath(
				"mixbang/hello/.././../bye/../whoishere").equals("whoishere"));
		assertTrue(FastURI.normalizePath("../../similar/../././hello stupid")
				.equals("../../hello stupid"));
		assertTrue(URIs.of("http://gale.tue.nl/test/hello")
				.resolve("#fragment").toString()
				.equals("http://gale.tue.nl/test/hello#fragment"));
	}
}
