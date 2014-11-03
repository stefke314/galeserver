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
 * ForModule.java
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
package nl.tue.gale.ae.processor.xmlmodule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.ProcessorException;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.cache.CacheSession;
import nl.tue.gale.common.code.Argument;
import nl.tue.gale.dm.data.Concept;
import nl.tue.gale.um.data.EntityValue;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.Node;

public class ForModule extends AbstractModule {
	private List<String> mimeToHandle = Arrays.asList(new String[] {
			"text/xhtml", "text/xml", "application/xml", "application/smil" });

	public List<String> getMimeToHandle() {
		return mimeToHandle;
	}

	public void setMimeToHandle(List<String> mimeToHandle) {
		this.mimeToHandle = mimeToHandle;
	}

	@SuppressWarnings("unchecked")
	public Element traverse(Element element, Resource resource)
			throws ProcessorException {
		try {
			GaleContext gale = GaleContext.of(resource);
			String expr = element.attributeValue("expr");
			String var = element.attributeValue("var");
			List<Object> alist = new LinkedList<Object>();
			Object list = gale.eval(expr);
			if (list.getClass().isArray())
				for (Object o : (Object[]) list)
					alist.add(o);
			else
				for (Object o : (Iterable<Object>) list)
					alist.add(o);
			if (alist.size() == 0) {
				element.getParent().remove(element);
				return null;
			}

			// optionally sort the array
			String sort = element.attributeValue("sort");
			if (sort != null && !"".equals(sort)) {
				final boolean ascending = ("false".equals(element
						.attributeValue("ascending")) ? false : true);
				List<Object[]> slist = new ArrayList<Object[]>();
				for (Object o : alist) {
					Object result = null;
					if (o instanceof Concept) {
						CacheSession<EntityValue> session = gale
								.openUmSession();
						session.setBaseUri(((Concept) o).getUri());
						result = gale
								.cm()
								.evaluate(
										gale.cr(),
										sort,
										Argument.of(
												"gale",
												"nl.tue.gale.ae.GaleContext",
												gale,
												"session",
												"nl.tue.gale.common.cache.CacheSession",
												session, "value",
												"nl.tue.gale.dm.data.Concept",
												o));
					} else {
						CacheSession<EntityValue> session = gale
								.openUmSession();
						result = gale
								.cm()
								.evaluate(
										gale.cr(),
										sort,
										Argument.of(
												"gale",
												"nl.tue.gale.ae.GaleContext",
												gale,
												"session",
												"nl.tue.gale.common.cache.CacheSession",
												session, "value", o.getClass()
														.getName(), o));
					}
					slist.add(new Object[] { result, o });
				}
				Collections.sort(slist, new Comparator<Object[]>() {
					public int compare(Object[] arg0, Object[] arg1) {
						Object o1 = arg0[0];
						Object o2 = arg1[0];
						if (!ascending) {
							Object temp = o1;
							o1 = o2;
							o2 = temp;
						}
						if (o1 == null)
							return (o2 == null ? 0 : -1);
						if (o2 == null)
							return 1;
						if (o1 instanceof Number)
							return (new Double(((Number) o1).doubleValue()))
									.compareTo(new Double(((Number) o2)
											.doubleValue()));
						return o1.toString().compareTo(o2.toString());
					}
				});
				alist.clear();
				for (Object[] o : slist)
					alist.add(o[1]);
			}

			// process for-loop
			String guid = GaleUtil.newGUID();
			resource.put(guid, alist.toArray());

			Element parent = element.getParent();
			int index = parent.indexOf(element);
			Pattern p = Pattern.compile("\\Q%" + var + "\\E\\W");
			for (int i = 0; i < alist.size(); i++) {
				Element clone = element.createCopy();
				Object object = ((Object[]) resource.get(guid))[i];
				String type = object.getClass().getName();
				if (type.indexOf("_$$_") >= 0)
					type = type.substring(0, type.indexOf("_$$_"));
				if (object instanceof Concept) {
					replace(clone, p, ((Concept) object).getUri().toString());
				} else if (object instanceof String) {
					replace(clone, p, (String) object);
				} else if (object instanceof Number
						|| object instanceof Boolean) {
					replace(clone, p, object.toString());
				} else {
					replace(clone, p, "((" + type
							+ ")((Object[])gale.getResource().get(\"" + guid
							+ "\"))[" + i + "])");
				}
				processor.traverseChildren(clone, resource);
				for (Node n : (List<Node>) clone.content()) {
					parent.content().add(index, n);
					index++;
				}
			}
			element.detach();
		} catch (Exception e) {
			e.printStackTrace();
			return (Element) GaleUtil.replaceNode(element,
					GaleUtil.createErrorElement("[" + e.getMessage() + "]"));
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private void replace(Element element, Pattern p, String replace) {
		for (Attribute a : (List<Attribute>) element.attributes()) {
			a.setValue(replace(a.getValue(), p, replace));
		}
		for (Element e : (List<Element>) element.elements())
			replace(e, p, replace);
	}

	private String replace(String str, Pattern p, String replace) {
		StringBuffer sb = new StringBuffer();
		str = str + "!";
		Matcher m = p.matcher(str);
		while (m.find()) {
			m.appendReplacement(sb, "");
			sb.append(replace);
			sb.append(str.charAt(m.end() - 1));
		}
		m.appendTail(sb);
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
}
