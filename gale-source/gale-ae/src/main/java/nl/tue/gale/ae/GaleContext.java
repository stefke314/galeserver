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
 * GaleContext.java
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
package nl.tue.gale.ae;

import static com.google.common.base.Preconditions.checkNotNull;
import static nl.tue.gale.common.GaleUtil.decodeURL;
import static nl.tue.gale.common.GaleUtil.encodeURL;

import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.tue.gale.ae.config.ConfigManager;
import nl.tue.gale.ae.event.EventManager;
import nl.tue.gale.ae.impl.UserEntityCache;
import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.cache.CacheSession;
import nl.tue.gale.common.code.Argument;
import nl.tue.gale.common.code.CodeManager;
import nl.tue.gale.common.code.CodeResolver;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.dm.DMCache;
import nl.tue.gale.dm.data.Concept;
import nl.tue.gale.um.UMCache;
import nl.tue.gale.um.data.EntityValue;
import nl.tue.gale.um.data.UserEntity;

import org.dom4j.Element;
import org.springframework.context.ApplicationContext;

import com.google.common.collect.ImmutableMap;

public class GaleContext {
	private Resource resource = null;
	private Concept concept = null;
	private URI conceptUri = null;

	private static ApplicationContext ac = null;
	private static UMCache um = null;
	private static DMCache dm = null;
	private static CodeManager cm = null;
	private static ProcessorManager pm = null;
	private static ConfigManager cfgm = null;
	private static EventManager em = null;
	private static UserEntityCache uec = null;
	private static EventBusClient ebc = null;
	private static GaleConfig gc = null;
	private static LogManager log = null;
	private static CodeResolver cr = null;

	private GaleContext(Resource resource) {
		this.resource = resource;
	}

	public static GaleContext of(Resource resource) {
		return new GaleContext(resource);
	}

	public Resource getResource() {
		return resource;
	}

	public ServletContext sc() {
		return sc(resource);
	}

	public static ServletContext sc(Resource resource) {
		return resource.getTyped(ServletContext.class, "servletContext");
	}

	public GaleServletBean gsb() {
		return gsb(resource);
	}

	public static GaleServletBean gsb(Resource resource) {
		return (GaleServletBean) ac(resource).getBean("galeServletBean");
	}

	public ApplicationContext ac() {
		return ac(resource);
	}

	public static ApplicationContext ac(Resource resource) {
		if (ac == null)
			ac = resource.getTyped(ApplicationContext.class,
					"applicationContext");
		return ac;
	}

	public HttpServletRequest req() {
		return req(resource);
	}

	public static HttpServletRequest req(Resource resource) {
		return resource.getTyped(HttpServletRequest.class, "request");
	}

	public HttpServletResponse resp() {
		return resp(resource);
	}

	public static HttpServletResponse resp(Resource resource) {
		return resource.getTyped(HttpServletResponse.class, "response");
	}

	private volatile String userId = null;

	public String userId() {
		if (userId == null)
			userId = userId(resource);
		return userId;
	}

	public static String userId(Resource resource) {
		return UserEntity.getIdFromUri(resource.getTyped(URI.class,
				"nl.tue.gale.userUri"));
	}

	public UMCache um() {
		return um(resource);
	}

	public static UMCache um(Resource resource) {
		if (um == null)
			um = (UMCache) ac(resource).getBean("umCache");
		return um;
	}

	public DMCache dm() {
		return dm(resource);
	}

	public static DMCache dm(Resource resource) {
		if (dm == null)
			dm = (DMCache) ac(resource).getBean("dmCache");
		return dm;
	}

	public CodeManager cm() {
		return cm(resource);
	}

	public static CodeManager cm(Resource resource) {
		if (cm == null)
			cm = (CodeManager) ac(resource).getBean("codeManager");
		return cm;
	}

	public ProcessorManager pm() {
		return pm(resource);
	}

	public static ProcessorManager pm(Resource resource) {
		if (pm == null)
			pm = (ProcessorManager) ac(resource).getBean("processorManager");
		return pm;
	}

	public ConfigManager cfgm() {
		return cfgm(resource);
	}

	public static ConfigManager cfgm(Resource resource) {
		if (cfgm == null)
			cfgm = (ConfigManager) ac(resource).getBean("configManager");
		return cfgm;
	}

	public EventManager em() {
		return em(resource);
	}

	public static EventManager em(Resource resource) {
		if (em == null)
			em = (EventManager) ac(resource).getBean("eventManager");
		return em;
	}

	public UserEntityCache uec() {
		return uec(resource);
	}

	public static UserEntityCache uec(Resource resource) {
		if (uec == null)
			uec = (UserEntityCache) ac(resource).getBean("userEntityCache");
		return uec;
	}

	public URI conceptUri() {
		return conceptUri(resource);
	}

	public static URI conceptUri(Resource resource) {
		return (URI) resource.get("nl.tue.gale.conceptUri");
	}

	public Concept concept() {
		if (conceptUri == null || !conceptUri.equals(conceptUri())) {
			conceptUri = conceptUri();
			if (conceptUri != null)
				concept = dm().get(conceptUri);
		}
		return concept;
	}

	public static Concept concept(Resource resource) {
		return GaleContext.dm(resource).get(GaleContext.conceptUri(resource));
	}

	public URI userUri() {
		return userUri(resource);
	}

	public static URI userUri(Resource resource) {
		return (URI) resource.get("nl.tue.gale.userUri");
	}

	public URL url() {
		return url(resource);
	}

	public static URL url(Resource resource) {
		return resource.getTyped(URL.class, "url");
	}

	public GaleConfig gc() {
		return gc(resource);
	}

	public static GaleConfig gc(Resource resource) {
		if (gc == null)
			gc = (GaleConfig) ac(resource).getBean("galeConfig");
		return gc;
	}

	public InputStream stream() {
		return stream(resource);
	}

	public static InputStream stream(Resource resource) {
		return resource.getTyped(InputStream.class, "stream");
	}

	public String encoding() {
		return encoding(resource);
	}

	public static String encoding(Resource resource) {
		String result = resource.getTyped(String.class, "encoding");
		if (result == null)
			return "UTF-8";
		else
			return result;
	}

	public String mime() {
		return mime(resource);
	}

	public static String mime(Resource resource) {
		return resource.getTyped(String.class, "mime");
	}

	public Element xml() {
		return xml(resource);
	}

	public static Element xml(Resource resource) {
		return resource.getTyped(Element.class, "xml");
	}

	public CacheSession<EntityValue> openUmSession() {
		return openUmSession(resource);
	}

	public static CacheSession<EntityValue> openUmSession(Resource resource) {
		CacheSession<EntityValue> session = um(resource).openSession();
		session.setBaseUri(GaleUtil.addUserInfo(conceptUri(resource),
				userId(resource)));
		return session;
	}

	public EventBusClient ebc() {
		return ebc(resource);
	}

	public static EventBusClient ebc(Resource resource) {
		if (ebc == null)
			ebc = (EventBusClient) ac(resource).getBean("eventBusClient");
		return ebc;
	}

	public String currentView() {
		return currentView(resource);
	}

	public static String currentView(Resource resource) {
		String result = resource.getTyped(String.class, "current-view");
		if (result == null)
			return "content";
		return result;
	}

	public boolean servletAccess() {
		return servletAccess(resource);
	}

	public static boolean servletAccess(Resource resource) {
		Boolean result = resource.getTyped(Boolean.class,
				"nl.tue.gale.servletAccess");
		if (result == null)
			return false;
		else
			return result;
	}

	public LogManager log() {
		return log(resource);
	}

	public static LogManager log(Resource resource) {
		if (log == null)
			log = (LogManager) ac(resource).getBean("logManager");
		return log;
	}

	public CodeResolver cr() {
		return cr(resource);
	}

	public static CodeResolver cr(Resource resource) {
		if (cr == null)
			cr = (CodeResolver) ac(resource).getBean("codeResolver");
		return cr;
	}

	public Object eval(String expr) {
		CacheSession<EntityValue> session = openUmSession();
		try {
			return eval(session, expr);
		} finally {
			session.commit();
		}
	}

	public Object eval(CacheSession<EntityValue> session, String expr) {
		return cm().evaluate(
				cr(),
				expr,
				Argument.of("gale", "nl.tue.gale.ae.GaleContext", this,
						"session", "nl.tue.gale.common.cache.CacheSession",
						session));
	}

	public void exec(String expr) {
		CacheSession<EntityValue> session = openUmSession();
		try {
			exec(session, expr);
		} finally {
			session.commit();
		}
	}

	public void exec(CacheSession<EntityValue> session, String expr) {
		cm().execute(
				cr(),
				expr,
				Argument.of("gale", "nl.tue.gale.ae.GaleContext", this,
						"session", "nl.tue.gale.common.cache.CacheSession",
						session));
	}

	public void usedResponse() {
		usedResponse(resource);
	}

	public static void usedResponse(Resource resource) {
		usedXml(resource);
		resource.setUsed("response");
	}

	public void usedXml() {
		usedXml(resource);
	}

	public static void usedXml(Resource resource) {
		usedStream(resource);
		resource.setUsed("xml");
	}

	public void usedStream() {
		usedStream(resource);
	}

	public static void usedStream(Resource resource) {
		usedRequest(resource);
		resource.setUsed("stream");
	}

	public void usedRequest() {
		usedRequest(resource);
	}

	public static void usedRequest(Resource resource) {
		resource.setUsed("request");
	}

	public void addCookie(String name, String value) {
		addCookie(resource, name, value);
	}

	public static void addCookie(Resource resource, String name, String value) {
		addCookie(resource, name, value, 63072000);
	}

	public void addCookie(String name, String value, int age) {
		addCookie(resource, name, value, age);
	}

	public static void addCookie(Resource resource, String name, String value,
			int age) {
		checkNotNull(name);
		if (value == null)
			value = "Null";
		String host = "";
		String path = sc(resource).getContextPath();
		/*
		 * if
		 * (req(resource).getHeader("User-Agent").contains("compatible; MSIE")
		 * && age == 0) { host =
		 * URIs.of(req(resource).getRequestURL().toString()) .getHost(); path =
		 * "/"; }
		 */
		String expires = null;
		if (age >= 0) {
			if (age == 0)
				age = -86400;
			DateFormat df = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss z",
					Locale.US);
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"),
					Locale.US);
			c.add(Calendar.SECOND, age);
			df.setCalendar(c);
			expires = df.format(c.getTime());
		} else
			expires = "";
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : ImmutableMap.of(name,
				encodeURL(value), "Expires", expires, "Path", path, "Domain",
				host).entrySet())
			if (!"".equals(entry.getValue())) {
				sb.append(entry.getKey());
				sb.append("=");
				sb.append(entry.getValue());
				sb.append("; ");
			}
		sb.delete(sb.length() - 2, sb.length());
		resp(resource).setContentType("text/html");
		resp(resource).addHeader("Set-Cookie", sb.toString());
	}

	public String getCookie(String name) {
		return getCookie(resource, name);
	}

	public static String getCookie(Resource resource, String name) {
		Cookie[] cookies = req(resource).getCookies();
		if (cookies == null)
			return null;
		for (Cookie cookie : cookies)
			if (cookie.getName().equalsIgnoreCase(name))
				return decodeURL(cookie.getValue());
		return null;
	}

	public boolean isObject() {
		return isObject(resource);
	}

	public static boolean isObject(Resource resource) {
		return "true".equals(resource.get("nl.tue.gale.object"));
	}
}
