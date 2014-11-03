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
 * AdaptLinkModule.java
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
package nl.tue.gale.ae.processor.xmlmodule;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.ProcessorException;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.cache.CacheSession;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.common.uri.URIs;
import nl.tue.gale.dm.data.Concept;
import nl.tue.gale.um.data.EntityValue;

import org.apache.commons.collections.LRUMap;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;

public class AdaptLinkModule extends AbstractModule {
	private static final Logger log = Logger.getLogger(AdaptLinkModule.class);

	private List<String> mimeToHandle = Arrays
			.asList(new String[] { "text/xhtml" });

	@Override
	public List<String> getMimeToHandle() {
		return mimeToHandle;
	}

	public void setMimeToHandle(List<String> mimeToHandle) {
		this.mimeToHandle = mimeToHandle;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Element traverse(Element element, Resource resource)
			throws ProcessorException {
		GaleContext gale = GaleContext.of(resource);
		CacheSession<EntityValue> session = gale.openUmSession();
		try {
			// initialization
			processor.traverseChildren(element, resource);
			boolean useParent = !(element.getName().equals("a"));
			Element a = (useParent ? element.getParent() : element);
			String href = a.attributeValue("href");
			URI uri = URIs.of(href);
			//System.out.println(href);
			String anchorid = uri.getFragment();
			href = URIs.builder().uri(uri).fragment(null).build().toString();
			session.setBaseUri(session.resolve(href));

			// possibly add content to the link, based on previous processing
			Node n = resource
					.getTyped(Node.class,
							"nl.tue.gale.ae.processor.xmlmodule.AdaptLinkModule.content");
			if (n != null) {
				n = (Node) n.clone();
				a.add(n);
			}

			// should we adapt the class attribute or hide the link
			boolean adapt = (!"false".equals(a.attributeValue("adapt")));
			a.addAttribute("adapt", null);

			// should the link be removed or hidden
			if (adapt) {
				String removeexpr = (String) gale.cfgm().getObject(
						"gale://gale.tue.nl/config/link#remove", resource);
				if (removeexpr != null) {
					boolean remove = false;
					try {
						remove = (Boolean) gale.eval(session, removeexpr);
					} catch (Exception e) {
						log.debug(e, e);
					}
					if (remove) {
						GaleUtil.replaceNode(a, null);
						return null;
					}
				}

				String hideexpr = (String) gale.cfgm().getObject(
						"gale://gale.tue.nl/config/link#hide", resource);
				if (hideexpr != null) {
					boolean hide = false;
					try {
						hide = (Boolean) gale.eval(session, hideexpr);
					} catch (Exception e) {
						log.debug(e, e);
					}
					if (hide) {
						GaleUtil.replaceNode(a, DocumentFactory.getInstance()
								.createText(a.getText()));
						return null;
					}
				}
			}

			// determine and set css class
			if (adapt) {
				EntityValue ev = session.get(session.resolve("#link.class"));
				String cssclass = (ev != null ? ev.getValue().toString() : null);
				if (cssclass == null) {
					String classexpr = (String) gale.cfgm().getObject(
							"gale://gale.tue.nl/config/link#classexpr",
							resource);
					try {
						cssclass = gale.eval(session, classexpr).toString();
					} catch (Exception e) {
						log.debug(e, e);
						cssclass = "unknown";
					}
				}
				a.addAttribute("class", cssclass);
			}
	
			// add possible exec code
			String exec = element.attributeValue("exec");
			String query = URIs.of(href).getQuery();
			if (exec != null) {
				String guid = GaleUtil.newGUID();
				storeInSession(gale, guid, exec);
				Map<String, String> params = GaleUtil.getQueryParameters(query);
				params.put("plugin", "exec");
				params.put("guid", guid);
				query = GaleUtil.getQueryString(params);
			}

			// add proper link to concept
			String conceptName = (href.startsWith("?") ? gale.concept()
					.getUriString() : Concept.getConceptURI(
					session.getBaseUri()).toString());
			uri = URIs.of(gale.gsb().getConceptManager()
					.getConceptLink(URIs.of(conceptName), gale.req(), query));
			
			// Added by Vinicius Ramos to log the currentView: "?view=" + gale.currentView()
			a.addAttribute("href", uri.toString()
					+ (anchorid != null ? "#" + anchorid : "") + (uri.toString().contains("?") ? "" : "?view=" + gale.currentView()));
			a.setQName(DocumentFactory.getInstance().createQName(a.getName(),
					"", GaleUtil.xhtmlns));
			if (useParent)
				a.remove(element);

			// add possible icons
			if (adapt) {
				List<String> iconList = (List<String>) gale.cfgm().getObject(
						"gale://gale.tue.nl/config/link#iconlist", resource);
				Element span = null;
				for (String iconExpr : iconList) {
					try {
						String icon = (String) gale.eval(session, iconExpr);
						if (icon == null || "".equals(icon))
							continue;
						boolean pre = false;
						if (icon.startsWith("pre:")) {
							icon = icon.substring(4);
							pre = true;
						}
						URI iconUri = URIs.of(icon);
						if (iconUri.isAbsolute()) {
							// absolute uri
							if (iconUri.getScheme().equals("gale")) {
								icon = GaleUtil.getContextURL(gale.req()) + "/"
										+ GaleUtil.getServletName(gale.req())
										+ "/${home}" + iconUri.getPath();
							}
						} else {
							// relative uri

						}
						if (span == null) {
							span = DocumentFactory.getInstance().createElement(
									"span", GaleUtil.xhtmlns);
							a.getParent()
									.content()
									.add(a.getParent().content().indexOf(a),
											span);
							a.getParent().content().remove(a);
							span.add(a);
						}
						Element img = DocumentFactory.getInstance()
								.createElement("img", GaleUtil.xhtmlns);
						img.addAttribute("src", icon);
						img.addAttribute("align", "bottom");
						img.addAttribute("alt", "");
						if (pre)
							span.content().add(0, img);
						else
							span.add(img);
					} catch (Exception e) {
						log.debug(e, e);
					}
				}
			}
			return null;
		} catch (Exception e) {
			log.debug(e, e);
			return (Element) GaleUtil.replaceNode(element,
					GaleUtil.createErrorElement("[" + e.getMessage() + "]"));
		} finally {
			session.rollback();
		}
	}

	@SuppressWarnings("unchecked")
	private void storeInSession(GaleContext gale, String guid, String exec) {
		Map<String, String[]> lru = (Map<String, String[]>) gale.req()
				.getSession().getAttribute("ExecPlugin:map");
		if (lru == null) {
			lru = Collections
					.synchronizedMap((Map<String, String[]>) new LRUMap(50));
			gale.req().getSession().setAttribute("ExecPlugin:map", lru);
		}
		lru.put(guid, new String[] { gale.conceptUri().toString(), exec });
	}
}
