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
 * Concept.java
 * Last modified: $Date$
 * In revision:   $Revision$
 * Modified by:   $Author: dsmits $
 *
 * Copyright (c) 2008-2011 Eindhoven University of Technology.
 * All Rights Reserved.
 *
 * This software is proprietary information of the Eindhoven University
 * of Technology. It may be used according to the GNU LGPL license.
 */
package nl.tue.gale.conversion.aha3.data;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 * This class defines a single concept.
 */
public class Concept implements Serializable {
	private static final long serialVersionUID = -4964610326951993592L;

	/**
	 * An identifier used to track this object in the database.
	 */
	public long id = -1;

	/**
	 * Constructs a <code>Concept</code> with the specified fully qualified
	 * name.
	 * 
	 * @param name
	 *            the fully qualified name of this concept
	 * @see #setName(String)
	 */
	public Concept(String name) {
		setName(name);
	}

	private String name = null;

	/**
	 * Returns the fully qualified name of this concept.
	 * 
	 * @return the fully qualified name of this concept
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the fully qualified name of this concept. Each part of the name must
	 * start with a letter and may contain letters, digits, periods and
	 * underscores.
	 * <p>
	 * It must start with the application name followed by a period and then the
	 * rest of the concept name. The application may not be named 'personal'
	 * because that name is reserved and has a specific meaning in AHA! (it is
	 * used to store attributes in the user profile that are not part of a
	 * particular application but contain general user information, like his
	 * username, password, etc.).
	 * <p>
	 * If the name does match the requirements then an <code>
	 * IllegalArgumentException</code> is thrown.
	 * 
	 * @param name
	 *            the fully qualified name of this concept
	 * @throws IllegalArgumentException
	 *             thrown when the name does not match the requirements
	 */
	public void setName(String name) {
		checkName(name);
		this.name = name;
	}

	/**
	 * Returns whether <code>name</code> is a valid concept name.
	 * 
	 * @param name
	 *            the concept name to check
	 * @return whether the specified name is a valid concept name
	 * @see #setName(String)
	 */
	public static boolean isValidName(String name) {
		try {
			checkName(name);
		} catch (IllegalArgumentException e) {
			return false;
		}
		return true;
	}

	private static void checkName(String name) {
		if (name == null)
			throw new IllegalArgumentException("Concept name may not be null");
		if ("".equals(name))
			throw new IllegalArgumentException("Concept name may not be empty");
		if (name.indexOf(".") == -1)
			throw new IllegalArgumentException(
					"Concept name must start with application name followed by a period ("
							+ name + ")");
		String[] parts = name.split("\\.");
		if (parts[0].equals("personal"))
			throw new IllegalArgumentException(
					"Application name may not be 'personal' (" + name + ")");
		for (int i = 0; i < parts.length; i++) {
			if (parts[i].length() == 0)
				throw new IllegalArgumentException(
						"A concept name may not have empty parts (" + name
								+ ")");
			if (!Character.isLetter(parts[i].charAt(0)))
				throw new IllegalArgumentException(
						"Every part of a concept name must start with a letter ("
								+ name + ")");
			for (int j = 1; j < parts[i].length(); j++)
				if (!(Character.isLetter(parts[i].charAt(j))
						|| Character.isDigit(parts[i].charAt(j)) || '_' == parts[i]
							.charAt(j)))
					throw new IllegalArgumentException(
							"A concept may only contain letters, digits, periods and underscores ("
									+ name + ")");
		}
	}

	/**
	 * Returns the application name of this concept. This is the first part of
	 * the concept name. To be specific, the part before the first period.
	 * 
	 * @return the application name of this concept
	 */
	public String getApplication() {
		return name.substring(0, name.indexOf("."));
	}

	private Hashtable<String, Attribute> attributes = new Hashtable<String, Attribute>();

	/**
	 * Returns the names of all attributes that this concept contains.
	 * 
	 * @return the names of all attributes in this concept
	 */
	public List<String> getAttributes() {
		return new LinkedList<String>(attributes.keySet());
	}

	/**
	 * Determines whether the specified attribute exists in this concept. The
	 * name of the attribute may not be the fully qualified attribute name. To
	 * be specific, it may not contain a period.
	 * 
	 * @param name
	 *            the name of the attribute to check
	 * @return if the attribute exists in this concept
	 */
	public boolean hasAttribute(String name) {
		return attributes.containsKey(name);
	}

	/**
	 * Retrieves the specified attribute if it exists in this concept. Otherwise
	 * this method returns <code>null</code>. The name of the attribute may not
	 * be the fully qualified attribute name. To be specific, it may not contain
	 * a period.
	 * 
	 * @param name
	 *            the name of the attribute to retrieve
	 * @return the requested attribute
	 */
	public Attribute getAttribute(String name) {
		return attributes.get(name);
	}

	/**
	 * Stores the specified attribute in this concept. If the attribute already
	 * exists in this concept then it is overwritten.
	 * 
	 * @param attribute
	 *            the <code>Attribute</code> to set
	 */
	public void setAttribute(Attribute attribute) {
		attributes.put(attribute.getName(), attribute);
	}

	/**
	 * Removes the specified attribute from this concept. If the attribute
	 * doesn't exist, this method does nothing.
	 * 
	 * @param name
	 *            the name of the attribute to remove
	 */
	public void removeAttribute(String name) {
		attributes.remove(name);
	}

	private String description = null;

	/**
	 * Returns a description of this concept. This may be <code>null</code> if
	 * there is no description.
	 * 
	 * @return a description of this concept.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets a description for this concept. This may be <code>null</code> to
	 * clear the description.
	 * 
	 * @param description
	 *            a description for this concept
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	private String resource = null;

	/**
	 * Returns the URL of the resource that this concept is linked to, if any.
	 * If this concept is not directly linked to a resource this returns <code>
	 * null</code>.
	 * <p>
	 * A concept may also be linked to a resource by means of the 'showability'
	 * attribute. The preferred way to get the url that is currently (based on a
	 * user profile) active for this concept is via the <code>getActiveURL
	 * </code> method. This method will first check if there is a 'showability'
	 * attribute and if there is it will ignore the resource set via <code>
	 * setResourceURL</code>.
	 * 
	 * @return the URL that this concept is directly linked to
	 * @see #getActiveURL
	 */
	public String getResourceURL() {
		return resource;
	}

	/**
	 * Sets the URL of the resource that this concept is linked to. If this
	 * concept is not directly linked to a resource, set it to <code>null
	 * </code>. If this concept is linked to more than one resource, use the
	 * 'showability' attribute. If there is a 'showability' attribute the value
	 * set here is ignored.
	 * 
	 * @param resource
	 *            the <code>URL</code> of the resource that this concept is
	 *            directly linked to
	 * @see #getActiveURL
	 */
	public void setResourceURL(String resource) {
		this.resource = resource;
	}

	private StableMode stablemode = StableMode.NONE;

	/**
	 * Returns the stability mode used by this concept.
	 * 
	 * @return the stability mode used by this concept.
	 * @see StableMode
	 */
	public StableMode getStableMode() {
		return stablemode;
	}

	/**
	 * Sets the stability mode of this concept. Default is <code>NONE</code>,
	 * meaning no stability is used.
	 * 
	 * @param stable
	 *            the <code>StableMode</code> to use
	 * @see StableMode
	 */
	public void setStableMode(StableMode stablemode) {
		this.stablemode = stablemode;
	}

	private String stableexpression = null;

	/**
	 * Returns the expression used when the stability mode is <code>FREEZE
	 * </code>. This expression should be evaluated to determine if included
	 * objects are to be kept stable. This expression may also be present if the
	 * stability mode is something other than <code>FREEZE</code>, but then it
	 * should be ignored.
	 * 
	 * @return the expression used to determine if stability should be used
	 * @see #setStableMode
	 */
	public String getStableExpression() {
		return stableexpression;
	}

	/**
	 * Sets the expression that determines if included objects are to be kept
	 * stable. This expression only has effect if the stability mode is set to
	 * <code>FREEZE</code>. It may be set anyway and it may be <code>null
	 * </code>.
	 * 
	 * @param stableexpression
	 *            the expression that is evaluated if the stability mode is
	 *            <code>FREEZE</code>.
	 * @see #setStableMode
	 */
	public void setStableExpression(String stableexpression) {
		this.stableexpression = stableexpression;
	}

	/**
	 * Returns a string representation of this concept. This returns the fully
	 * qualified concept name.
	 * 
	 * @return a string representation of this concept
	 */
	public String toString() {
		return getName();
	}

	private String type = "page";

	/**
	 * Returns the type of this concept. This can be used to group concepts
	 * together. The default type is 'page'. It can not be <code>null</code>.
	 * 
	 * @return a <code>String</code> representing the type of this concept
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the type of this concept. This can be used to group concepts
	 * together. It may not be <code>null</code>.
	 * 
	 * @param type
	 *            a <code>String</code> representing the type of this concept
	 */
	public void setType(String type) {
		if (type == null)
			throw new NullPointerException();
		this.type = type;
	}

	private String title = null;

	/**
	 * Returns the title of this concept. This is a short description that is
	 * shown in for instance the various tree views. If the title is set to
	 * <code>null</code> then this method returns the last part of the qualified
	 * concept name.
	 * 
	 * @return the title of this concept
	 */
	public String getTitle() {
		if (title == null)
			return getName();
		else
			return title;
	}

	/**
	 * Sets the title of this concept. This is a short description that is shown
	 * in for instance the various tree views. The title may be set to
	 * <code>null</code> in which case <code>getTitle</code> will return the
	 * fully qualified concept name instead.
	 * 
	 * @param title
	 *            the title of this concept
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	private String firstchild = null;

	public String getFirstChild() {
		return firstchild;
	}

	public void setFirstChild(String firstchild) {
		this.firstchild = firstchild;
	}

	private String parent = null;

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	private String nextsib = null;

	public String getNextSib() {
		return nextsib;
	}

	public void setNextSib(String nextsib) {
		this.nextsib = nextsib;
	}

	/**
	 * Makes a copy of this concept where every occurence of <code>source
	 * </code> is replaced by <code>destination</code>. This is done
	 * recursively, so it also processes the occurences in expressions contained
	 * in attributes. Occurences in title and description are also replaced.
	 * 
	 * @param source
	 *            a <code>String</code> that represents some part of the source
	 *            concept name
	 * @param destination
	 *            a <code>String</code> that represents some part of the
	 *            destination concept name
	 * @return the new copied concept
	 * @throws IllegalArgumentException
	 *             if the resplacement results in an invalid concept name
	 */
	public Concept copy(String source, String destination) {
		Concept result = new Concept(getName().replaceAll(source, destination));
		result.setDescription((getDescription() == null ? null
				: getDescription().replaceAll(source, destination)));
		result.setResourceURL(getResourceURL());
		result.setStableMode(getStableMode());
		result.setStableExpression((getStableExpression() == null ? null
				: getStableExpression().replaceAll(source, destination)));
		result.setType(type);
		result.setTitle(getTitle().replaceAll(source, destination));
		for (Attribute attribute : attributes.values())
			result.setAttribute(attribute.copy(source, destination));
		return result;
	}

	public static class comparator implements Comparator<Concept> {
		private static final int NAME = 1;
		private static final int TITLE = 2;
		int field = 0;

		public comparator(String field) {
			if ("name".equals(field))
				this.field = NAME;
			if ("title".equals(field))
				this.field = TITLE;
		}

		public int compare(Concept o1, Concept o2) {
			if (field == 0)
				return 0;
			String s1 = null;
			String s2 = null;
			if (field == NAME) {
				s1 = o1.getName();
				s2 = o2.getName();
			}
			if (field == TITLE) {
				s1 = o1.getTitle();
				s2 = o2.getTitle();
			}
			if (s1 == null)
				return (s2 == null ? 0 : -1);
			if (s2 == null)
				return 1;
			return s1.compareTo(s2);
		}
	}
}