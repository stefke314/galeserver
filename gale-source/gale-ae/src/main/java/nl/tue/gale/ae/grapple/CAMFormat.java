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
 * CAMFormat.java
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
package nl.tue.gale.ae.grapple;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.cache.Cache;
import nl.tue.gale.common.cache.Caches;
import nl.tue.gale.common.parser.ParseNode;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.common.uri.URIs;
import nl.tue.gale.dm.GAMFormat;
import nl.tue.gale.dm.data.Attribute;
import nl.tue.gale.dm.data.Concept;
import nl.tue.gale.dm.data.ConceptRelation;

import org.dom4j.Element;

import com.google.common.collect.ImmutableList;

public class CAMFormat {
	private Map<URI, Concept> conceptMap = new HashMap<URI, Concept>();
	private Map<String, CRT> crtMap = new HashMap<String, CRT>();
	private Cache<Concept> dm = Caches.newCache(10);
	private Map<URI, URI> conceptIdToName = new HashMap<URI, URI>();

	public static List<Concept> getConcepts(String cam) {
		return getConcepts(GaleUtil.parseXML(new StringReader(cam))
				.getRootElement());
	}

	public static List<Concept> getConcepts(Element cam) {
		CAMFormat camFormat = new CAMFormat();
		try {
			return camFormat.getInternalConcepts(cam);
		} catch (NullPointerException npe) {
			throw new IllegalArgumentException("invalid CAM", npe);
		}
	}

	@SuppressWarnings("unchecked")
	private List<Concept> getInternalConcepts(Element cam) {
		URI baseURI = URIs.of("gale://gale.tue.nl/cam/"
				+ replaceSpace(cam.element("header").elementText("title"))
				+ "/");

		List<Element> models = cam.element("body").element("cam")
				.element("camInternal").element("domainModel")
				.elements("model");
		for (Element model : models) {
			Element vdex = model.element("body").element("dm").element("vdex");
			for (Element termElement : (Collection<Element>) vdex
					.elements("term")) {
				Concept c = new Concept(
						baseURI.resolve(replaceSpace(termElement.element(
								"caption").elementText("langstring"))));
				conceptIdToName.put(baseURI.resolve(termElement
						.elementText("termIdentifier")), c.getUri());
				conceptMap.put(c.getUri(), c);
				c.setProperty("cam.model.guid", cam.element("header")
						.elementText("modeluuid"));
				c.setProperty("cam.concept.guid",
						termElement.elementText("termIdentifier"));
				Attribute a;
				try {
					a = makeAttribute("resource", resourceCode(termElement),
							"", "java.lang.String", false);
					addResourceAttributes(a, termElement);
					c.addAttribute(a);
				} catch (Exception e) {
				}
				c.setProperty("title", termElement.element("caption")
						.elementText("langstring"));
				if (termElement.elements("metadata").size() > 0)
					addConceptProperties(c, termElement.element("metadata"));
			}
			for (Element relElement : (Collection<Element>) vdex
					.elements("relationship")) {
				ConceptRelation cr = new ConceptRelation(
						relElement.elementText("relationshipType"));
				cr.changeInConcept(conceptMap.get(conceptIdToName.get(baseURI
						.resolve(relElement.elementText("sourceTerm")))));
				cr.changeOutConcept(conceptMap.get(conceptIdToName.get(baseURI
						.resolve(relElement.elementText("targetTerm")))));
			}
		}
		Element crts = cam.element("body").element("cam")
				.element("camInternal").element("crtModel");
		for (Element crtElement : (Collection<Element>) crts.elements("model")) {
			CRT crt = CRT.parse(crtElement);
			crtMap.put(crt.getUid(), crt);
		}
		for (Element crtElement : (Collection<Element>) cam.element("body")
				.element("cam").element("camInternal").elements("crt"))
			interpretCRT(crtElement, baseURI);
		List<Concept> resultList = new LinkedList<Concept>();
		resultList.addAll(conceptMap.values());
		for (Concept c : resultList) {
			if (c.getAttribute("visited") == null) {
				Attribute a = makeAttribute("visited", "0", "",
						"java.lang.Integer", true);
				c.addAttribute(a);
			}
			if (c.getAttribute("suitability") == null) {
				Attribute a = makeAttribute("suitability", "true", "",
						"java.lang.Boolean", false);
				c.addAttribute(a);
			}
			for (Attribute a : c.getAttributes()) {
				String pubString = a.getProperty("public");
				if (pubString != null && !"false".equals(pubString))
					a.setProperty("persistent", "true");
			}
		}
		return resultList;
	}

	private void addResourceAttributes(Attribute a, Element termElement) {
		@SuppressWarnings("unchecked")
		List<Element> rlist = (List<Element>) termElement
				.elements("mediaDescriptor");
		rlist = ImmutableList.copyOf(rlist);
		for (Element urlElement : rlist) {
			String label = getResourceProperty(termElement, urlElement, "label");
			if (label != null)
				a.setProperty(label, urlElement.elementText("mediaLocator"));
		}
	}

	private String getResourceProperty(Element termElement,
			Element resourceElement, String name) {
		String guid = null;
		try {
			guid = resourceElement.element("interpretationNote").elementText(
					"langstring");
		} catch (Exception e) {
		}
		String result = null;
		try {
			@SuppressWarnings("unchecked")
			List<Element> metadata = (List<Element>) termElement.element(
					"metadata").elements("resource");
			for (Element metaElement : metadata)
				if ((metaElement.attributeValue("id").equals(guid))
						&& (metaElement.element("lom").element("general")
								.element("title").elementText("langstring")
								.equals(name))) {
					result = metaElement.element("lom").element("general")
							.element("description").elementText("langstring");
				}
		} catch (Exception e) {
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private String resourceCode(Element termElement) {
		List<Element> rlist = new ArrayList<Element>();
		rlist.addAll(termElement.elements("mediaDescriptor"));
		Collections.reverse(rlist);
		if (rlist.size() == 0)
			return "\"gale:/empty.xhtml\"";
		if (rlist.size() == 1)
			return "\"" + rlist.get(0).elementText("mediaLocator") + "\"";
		StringBuilder result = new StringBuilder();
		result.append("\"gale:/empty.xhtml\"");
		for (Element urlElement : rlist) {
			String expr = getResourceProperty(termElement, urlElement, "expr");
			if (expr == null)
				expr = "true";

			// build part of resource string
			result.insert(0, "\":");
			result.insert(0, urlElement.elementText("mediaLocator"));
			result.insert(0, "?\"");
			result.insert(0, expr);
			result.insert(0, "(");
			result.append(")");
		}
		return result.toString();
	}

	@SuppressWarnings("unchecked")
	private void addConceptProperties(Concept c, Element element) {
		for (Element prop : (List<Element>) element.elements("concept")) {
			Element entry = prop.element("lom").element("general")
					.element("catalogentry");
			String name = entry.elementText("catalog");
			String value = entry.element("entry").elementText("langstring");
			c.setProperty(name, value);
		}
	}

	private String replaceSpace(String s) {
		// TODO: replace all strange characters
		try {
			return /* URLEncoder.encode( */s.replace(" ", "_")/* , "UTF-8") */;
		} catch (Exception e) {
			e.printStackTrace();
			return s.replace(" ", "_");
		}
	}

	@SuppressWarnings("unchecked")
	private void interpretCRT(Element crtElement, URI baseURI) {
		CRT crt = crtMap.get(crtElement.elementText("uuid"));
		if (crt == null)
			throw new IllegalArgumentException("the CRT '"
					+ crtElement.elementText("uuid")
					+ "' is referenced but not included");
		Map<String, Set<URI>> socketData = new HashMap<String, Set<URI>>();
		for (Element crtSocketElement : (Collection<Element>) crtElement
				.elements("camSocket")) {
			CRTsocket socket = crt.getSocketById(crtSocketElement
					.elementText("socketid"));
			Set<URI> cList = new HashSet<URI>();
			socketData.put(socket.getUid(), cList);
			for (Element entityElement : (Collection<Element>) crtSocketElement
					.elements("entity")) {
				Concept c = conceptMap.get(conceptIdToName.get(baseURI
						.resolve(entityElement.elementText("dmId"))));
				if (c == null)
					throw new IllegalArgumentException(
							"unable to find DM concept with ID '"
									+ entityElement.elementText("dmId")
									+ "' mentioned in CRT '" + crt.getName()
									+ "'");
				cList.add(c.getUri());
			}
		}
		for (CRTvar var : crt.getVariables()) {
			Set<URI> cList = socketData.get(crt.getSocketByName(
					var.getSocketName()).getUid());
			if (cList == null)
				throw new IllegalArgumentException(
						"the CRT '"
								+ crt.getName()
								+ "' has UM variables that refer to non-existing sockets");
			for (URI cname : cList) {
				Concept c = conceptMap.get(cname);
				if (c.getAttribute(var.getName()) == null) {
					Attribute attr = makeAttribute(var.getName(),
							var.getDefaultVar(), "", var.getJavaType(),
							var.isPersistent());
					if (var.getRange() != null)
						attr.setProperty("gumf.range", var.getRange());
					if (var.isPublicVar()) {
						attr.setProperty("persistent", "true");
						attr.setProperty("public",
								c.getUri().resolve("#" + attr.getName())
										.toString());
						attr.setProperty("authorative",
								(var.isPersistent() ? "true" : "false"));
					}
					c.addAttribute(attr);
				} else {
					if (!var.getJavaType().equals(
							c.getAttribute(var.getName()).getType()))
						throw new IllegalArgumentException(
								"attribute type mismatch in CRT '"
										+ crt.getName()
										+ "' ("
										+ var.getName()
										+ "): '"
										+ var.getJavaType()
										+ "' cannot be assigned to '"
										+ c.getAttribute(var.getName())
												.getType() + "'");
				}
			}
		}
		Pointer<String, URI> socketPointer = null;
		try {
			socketPointer = new Pointer<String, URI>(socketData);
		} catch (Exception e) {
			throw new IllegalArgumentException("empty socket in CRT '"
					+ crt.getName() + "'", e);
		}
		String crtGUID = GaleUtil.newGUID();
		while (socketPointer.hasPointer()) {
			String code = crt.getCode();
			Map<String, URI> current = socketPointer.current();
			for (String socketId : current.keySet()) {
				CRTsocket s = crt.getSocketById(socketId);
				try {
					code = code.replace("%" + s.getName() + "%",
							current.get(socketId).toString());
				} catch (Exception e) {
					throw new IllegalArgumentException("the CRT '"
							+ crt.getName()
							+ "' has code that refers to socket '"
							+ s.getName()
							+ "' that does not exist in the definition", e);
				}
			}
			for (Element param : (List<Element>) crtElement
					.elements("parameter")) {
				String pname = param.attributeValue("name");
				String pvalue = param.getText();
				code = code.replace("%" + pname + "%", pvalue);
			}
			try {
				List<Concept> codeConcepts = GAMFormat.readGAM(code, baseURI,
						dm);
				for (Concept c : codeConcepts)
					interpretConceptAdditions(c, crtGUID);
			} catch (Exception e) {
				throw new IllegalArgumentException("the CRT '" + crt.getName()
						+ "' has errors in its code: " + e.getMessage(), e);
			}
			socketPointer.next();
		}
	}

	private void interpretConceptAdditions(Concept c, String crtGUID) {
		Concept original = conceptMap.get(c.getUri());
		if (original == null) {
			original = new Concept(c.getUri());
			conceptMap.put(original.getUri(), original);
		}
		// properties
		Set<String> props = new HashSet<String>();
		for (String s : c.getProperties().keySet())
			if (!s.startsWith("~"))
				props.add(s);
		if (c.getEventCode() != null && !"".equals(c.getEventCode())) {
			props.add("event");
			c.setProperty("event", c.getEventCode());
		}
		compileProperties(original.getProperties(), props, c.getProperties());
		// relations
		for (ConceptRelation cr : c.getInCR()) {
			if (conceptMap.containsKey(cr.getInConcept().getUri()))
				compileCR(cr.getInConcept().getUri(), cr.getName(), c.getUri(),
						cr.getProperties());
		}
		for (ConceptRelation cr : c.getOutCR()) {
			if (conceptMap.containsKey(cr.getOutConcept().getUri()))
				compileCR(c.getUri(), cr.getName(),
						cr.getOutConcept().getUri(), cr.getProperties());
		}
		// attributes
		for (Attribute a : c.getAttributes()) {
			Attribute orgAttr = original.getAttribute(a.getName());
			if (orgAttr == null) {
				orgAttr = makeAttribute(a.getName(), "", "", a.getType(),
						a.isPersistent());
				original.addAttribute(orgAttr);
			}
			interpretAttributeAdditions(orgAttr, a, crtGUID);
		}
	}

	private void interpretAttributeAdditions(Attribute original, Attribute a,
			String crtGUID) {
		// some checks
		ParseNode node = (ParseNode) a.getTransientData("node");
		String nodeName = (String) node.get("name");
		if (nodeName.indexOf(":") < 0) // untyped attribute addition
			a.setType(original.getType());
		a.getProperties().remove("~extends");
		// check for type mismatch
		if (!original.getType().equals(a.getType()))
			throw new IllegalArgumentException("attribute type mismatch ("
					+ original.getName() + "): '" + a.getType()
					+ "' cannot be assigned to '" + original.getType() + "'");
		// default code
		try {
			String nodeValue = (String) node.get("value");
			if (nodeValue != null && !"".equals(nodeValue.trim())) {
				char operation = (Character) node.get("operation");
				if (operation == '=')
					original.setDefaultCode(a.getDefaultCode());
				else {
					String opstr = ((operation == '|' || operation == '&') ? " "
							+ operation + operation
							: " " + operation)
							+ " ";
					StringBuilder newDefaultCode = new StringBuilder(
							original.getDefaultCode() == null ? "" : original
									.getDefaultCode().trim());
					if (original.getType().equals("java.lang.Boolean")) {
						boolean initialized = "true".equals(original
								.getTransientData(crtGUID));
						if (!initialized) {
							if (newDefaultCode.length() > 0)
								newDefaultCode.append(" && ");
							newDefaultCode.append("(");
							newDefaultCode.append(a.getDefaultCode());
							newDefaultCode.append(")");
							original.addTransientData(crtGUID, "true");
						} else {
							newDefaultCode
									.deleteCharAt(newDefaultCode.length() - 1);
							newDefaultCode.append(opstr);
							newDefaultCode.append(a.getDefaultCode());
							newDefaultCode.append(")");
						}
					} else if (newDefaultCode.length() > 0) {
						newDefaultCode.insert(0, "new " + original.getType()
								+ "(");
						newDefaultCode.append(opstr);
						newDefaultCode.append(a.getDefaultCode());
						newDefaultCode.append(")");
					} else
						newDefaultCode.append(a.getDefaultCode());
					original.setDefaultCode(newDefaultCode.toString());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// properties
		Set<String> props = new HashSet<String>();
		for (String s : a.getProperties().keySet())
			if (!s.startsWith("~") && !"persistent".equals(s))
				props.add(s);
		if (a.getEventCode() != null && !"".equals(a.getEventCode())) {
			props.add("event");
			a.setProperty("event", a.getEventCode());
		}
		compileProperties(original.getProperties(), props, a.getProperties());
	}

	private void compileCR(URI source, String name, URI target,
			Map<String, String> properties) {
		if (!conceptMap.containsKey(source) || !conceptMap.containsKey(target))
			return;
		ConceptRelation cr = new ConceptRelation(false);
		cr.setEqualsString(name + ";" + source + ";" + target);
		if (conceptMap.get(source).getOutCR().contains(cr)) {
			for (ConceptRelation loopcr : conceptMap.get(source).getOutCR())
				if (loopcr.getOutConcept().getUri().equals(target))
					cr = loopcr;
		} else
			cr = new ConceptRelation(name, conceptMap.get(source),
					conceptMap.get(target));
		Set<String> props = new HashSet<String>();
		for (String s : properties.keySet())
			if (!s.startsWith("~"))
				props.add(s);
		compileProperties(cr.getProperties(), props, properties);
	}

	private void compileProperties(Map<String, String> orgProperties,
			Set<String> props, Map<String, String> properties) {
		for (String s : props) {
			String opStr = properties.get("~extends." + s);
			char operation = '=';
			if (opStr != null && opStr.length() > 0)
				operation = opStr.charAt(0);
			orgProperties.put(
					s,
					compileValue(orgProperties.get(s), operation,
							properties.get(s)));
		}
	}

	private String compileValue(String orgValue, char operation, String newValue) {
		if (operation == '=')
			return (newValue == null ? "" : newValue);
		StringBuffer sb = new StringBuffer();
		if (orgValue != null)
			sb.append(orgValue);
		if (sb.length() > 0 && operation != '+') {
			sb.append(" ");
			sb.append(operation);
			sb.append(operation);
			sb.append(" ");
		}
		if (newValue != null)
			sb.append(newValue);
		return sb.toString();
	}

	private static Attribute makeAttribute(String name, String defaultCode,
			String eventCode, String type, boolean persistent) {
		Attribute result = new Attribute(name);
		result.setDefaultCode(defaultCode);
		result.setEventCode(eventCode);
		result.setType(type);
		try {
			if (Number.class.isAssignableFrom(Class.forName(type)))
				result.setDefaultCode("new " + type + "(" + defaultCode + ")");
			else
				result.setDefaultCode(defaultCode);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		result.setProperty("persistent", (persistent ? "true" : "false"));
		return result;
	}

	@SuppressWarnings("unused")
	private static class CRT {
		private String uid = null;
		private String name = null;
		private String code = null;
		private Map<String, CRTsocket> socketById = new HashMap<String, CRTsocket>();
		private Map<String, CRTsocket> socketByName = new HashMap<String, CRTsocket>();
		private Collection<CRTvar> variables = new LinkedList<CRTvar>();

		public String getUid() {
			return uid;
		}

		public void setUid(String uid) {
			this.uid = uid;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public Map<String, CRTsocket> getSocketById() {
			return socketById;
		}

		public CRTsocket getSocketById(String id) {
			return socketById.get(id);
		}

		public void setSocketById(Map<String, CRTsocket> socketById) {
			this.socketById = socketById;
		}

		public Map<String, CRTsocket> getSocketByName() {
			return socketByName;
		}

		public CRTsocket getSocketByName(String name) {
			return socketByName.get(name);
		}

		public void setSocketByName(Map<String, CRTsocket> socketByName) {
			this.socketByName = socketByName;
		}

		public Collection<CRTvar> getVariables() {
			return variables;
		}

		public void setVariables(Collection<CRTvar> variables) {
			this.variables = variables;
		}

		@SuppressWarnings("unchecked")
		public static CRT parse(Element crt) {
			CRT result = new CRT();
			result.setUid(crt.element("header").elementText("modeluuid"));
			result.setName(crt.element("header").elementText("title"));
			crt = crt.element("body").element("crt");
			result.setCode(crt.element("adaptationbehaviour").elementText(
					"galcode"));
			for (Element codeElement : (List<Element>) crt.element(
					"adaptationbehaviour").elements("code")) {
				if ("gale".equals(codeElement.attributeValue("type")))
					result.setCode(codeElement.getText());
			}
			for (Element var : (Collection<Element>) crt
					.element("adaptationbehaviour").element("usermodel")
					.elements("umvariable"))
				result.getVariables().add(CRTvar.parse(var));
			for (Element socket : (Collection<Element>) crt.element(
					"crtsockets").elements("socket")) {
				CRTsocket s = CRTsocket.parse(socket);
				result.getSocketById().put(s.getUid(), s);
				result.getSocketByName().put(s.getName(), s);
			}
			return result;
		}
	}

	private static class CRTsocket {
		private String uid = null;
		private String name = null;

		public String getUid() {
			return uid;
		}

		public void setUid(String uid) {
			this.uid = uid;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public static CRTsocket parse(Element socket) {
			CRTsocket result = new CRTsocket();
			result.setUid(socket.elementText("uuid"));
			result.setName(socket.elementText("name"));
			return result;
		}

		public boolean equals(Object o) {
			if (o == null)
				return false;
			if (!(o instanceof CRTsocket))
				return false;
			CRTsocket s = (CRTsocket) o;
			boolean result = true;
			result &= (uid == null ? s.uid == null : uid.equals(s.uid));
			result &= (name == null ? s.name == null : name.equals(s.name));
			return result;
		}
	}

	@SuppressWarnings("unused")
	private static class CRTvar {
		private String name = null;
		private String socketName = null;
		private boolean publicVar = false;
		private boolean persistent = false;
		private String type = null;
		private String defaultVar = null;
		private String range = null;

		public String getRange() {
			return range;
		}

		public void setRange(String range) {
			this.range = range;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getSocketName() {
			return socketName;
		}

		public void setSocketName(String socketName) {
			this.socketName = socketName;
		}

		public boolean isPublicVar() {
			return publicVar;
		}

		public void setPublicVar(boolean publicVar) {
			this.publicVar = publicVar;
		}

		public boolean isPersistent() {
			return persistent;
		}

		public void setPersistent(boolean persistent) {
			this.persistent = persistent;
		}

		public String getType() {
			return type;
		}

		public String getJavaType() {
			if ("integer".equals(type))
				return "java.lang.Integer";
			if ("float".equals(type))
				return "java.lang.Float";
			if ("boolean".equals(type))
				return "java.lang.Boolean";
			return "java.lang.String";
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getDefaultVar() {
			return defaultVar;
		}

		public void setDefaultVar(String defaultVar) {
			this.defaultVar = defaultVar;
		}

		public static CRTvar parse(Element var) {
			CRTvar result = new CRTvar();
			result.setName(var.elementText("umvarname"));
			result.setDefaultVar(var.elementText("default"));
			result.setPersistent(new Boolean(var.elementText("persistent")));
			result.setPublicVar(new Boolean(var.elementText("public")));
			result.setSocketName(var.elementText("socket"));
			result.setType(var.elementText("type"));
			try {
				result.setRange(var.element("range").elementText("from") + "-"
						+ var.element("range").elementText("to"));
			} catch (Exception e) {
			}
			return result;
		}
	}

	private static class Pointer<T, U> {
		private Map<T, List<U>> data = new HashMap<T, List<U>>();
		private Map<T, U> current = new HashMap<T, U>();
		private List<T> indexList = new ArrayList<T>();

		public Pointer(Map<T, Set<U>> data) {
			for (Map.Entry<T, Set<U>> entry : data.entrySet()) {
				List<U> list = new ArrayList<U>();
				list.addAll(entry.getValue());
				this.data.put(entry.getKey(), list);
			}
			for (T t : data.keySet())
				indexList.add(t);
			for (T t : indexList)
				current.put(t, this.data.get(t).get(0));
		}

		public Map<T, U> current() {
			Map<T, U> result = new HashMap<T, U>();
			result.putAll(current);
			return result;
		}

		public void next() {
			int x = indexList.size() - 1;
			boolean done = false;
			while (x >= 0 && !done) {
				done = true;
				T t = indexList.get(x);
				int y = data.get(t).indexOf(current.get(t));
				y++;
				if (y >= data.get(t).size()) {
					y = 0;
					done = false;
				}
				current.put(t, data.get(t).get(y));
				x--;
			}
			if (x < 0 && !done)
				current = null;
		}

		public boolean hasPointer() {
			return (current != null);
		}
	}
}
