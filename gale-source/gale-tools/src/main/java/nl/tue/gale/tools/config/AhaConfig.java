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
 * AhaConfig.java
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import nl.tue.gale.tools.GaleToolsUtil;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AhaConfig {
	private Map<String, String> confighash = new HashMap<String, String>();
	private Map<String, AhaManager> managerhash = new HashMap<String, AhaManager>();
	private ServletContext sc = null;
	private Element ahaconfig = null;
	private static final String fs = System.getProperty("file.separator");

	public AhaConfig(ServletContext sc) throws ServletException {
		this.sc = sc;
		ahaconfig = (Element) sc.getAttribute("ahaconfig");
		// loadConfig();
	}

	@SuppressWarnings("unchecked")
	public String Get(String key) {
		if (key == null)
			return null;
		String result = (String) confighash.get(key);
		if (result != null)
			return result;
		if (key.equals("AHAROOT"))
			return GaleToolsUtil.getHomeDir(sc).toString() + fs;
		if (key.equals("XMLROOT"))
			return Get("AHAROOT") + "xmlroot";
		try {
			if (key.equals("CONTEXTPATH"))
				return sc.getContextPath();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			throw new Exception("Invalid config variable: " + key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void Put(String key, String value) {
		confighash.put(key, value);
	}

	public String All() {
		return confighash.toString();
	}

	public AhaManager GetManager(String l) {
		return managerhash.get(l);
	}

	public void PutManager(AhaManager m) {
		managerhash.put(m.login, m);
	}

	public void RemoveManager(String l) {
		managerhash.remove(l);
	}

	public void storeConfig() throws IOException {
		if (1 == 1)
			throw new IllegalStateException("this method should not be called");
		synchronized (ahaconfig.getOwnerDocument()) {
			Node n = ahaconfig.getFirstChild();
			while (n != null) {
				Node c = n.getNextSibling();
				if (n instanceof Element) {
					String s = ((Element) n).getTagName();
					if (s.equals("variable") || s.equals("user"))
						ahaconfig.removeChild(n);
				}
				n = c;
			}
			for (Map.Entry<String, String> entry : confighash.entrySet()) {
				Element e = ahaconfig.getOwnerDocument().createElement(
						"variable");
				ahaconfig.appendChild(e);
				e.setAttribute("id", entry.getKey());
				e.appendChild(e.getOwnerDocument().createTextNode(
						entry.getValue()));
			}
			for (Map.Entry<String, AhaManager> entry : managerhash.entrySet()) {
				AhaManager man = entry.getValue();
				Element e = ahaconfig.getOwnerDocument().createElement("user");
				ahaconfig.appendChild(e);
				e.setAttribute("username", man.getLogin());
				e.setAttribute("password", man.getHashed());
				e.appendChild(e.getOwnerDocument()
						.createTextNode(man.getName()));
			}
			File configfile = new File(sc.getRealPath("/WEB-INF/ahaconfig.xml"));
			FileWriter fw = new FileWriter(configfile);
			fw.write(GaleToolsUtil.serializeElement(ahaconfig));
			fw.close();
		}
	}

	public void loadConfig() {
		synchronized (ahaconfig.getOwnerDocument()) {
			confighash = new Hashtable<String, String>();
			managerhash = new Hashtable<String, AhaManager>();
			NodeList nl = ahaconfig.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++)
				if (nl.item(i) instanceof Element)
					configElement((Element) nl.item(i));
		}
	}

	private void configElement(Element el) {
		if (el.getTagName().equals("variable"))
			configVariable(el);
		if (el.getTagName().equals("user"))
			configUser(el);
	}

	private void configVariable(Element el) {
		confighash
				.put(el.getAttribute("id"), el.getFirstChild().getNodeValue());
	}

	private void configUser(Element el) {
		AhaManager man = new AhaManager(el.getAttribute("username"), el
				.getFirstChild().getNodeValue());
		man.setHashed(el.getAttribute("password"));
		managerhash.put(el.getAttribute("username"), man);
	}
}
