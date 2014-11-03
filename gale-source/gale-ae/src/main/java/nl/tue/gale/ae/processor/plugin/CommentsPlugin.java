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
 * CommentsPlugin.java
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
package nl.tue.gale.ae.processor.plugin;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.ProcessorException;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.ae.processor.xmlmodule.CreoleParser;
import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.cache.CacheSession;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.common.uri.URIs;
import nl.tue.gale.event.EventHash;
import nl.tue.gale.um.data.EntityValue;

import org.dom4j.Element;

import com.google.common.collect.ImmutableList;
import com.google.gson.reflect.TypeToken;

public class CommentsPlugin extends AbstractPlugin {
	@Override
	public void doPost(Resource resource) throws ProcessorException {
		GaleContext gale = GaleContext.of(resource);
		if (gale.req().getParameter("delete") != null)
			deleteComments(gale);
		else {
			String comment = gale.req().getParameter("comment");
			if (comment != null)
				addComment(gale, comment, false);
		}
		String redirect = gale.req().getParameter("redirect");
		if (redirect != null) {
			try {
				String context = GaleUtil.getContextURL(
						GaleContext.req(resource)).toString();
				gale.resp().sendRedirect(context + "/concept/" + redirect);
				GaleContext.usedResponse(resource);
			} catch (Exception e) {
				throw new ProcessorException("unable to redirect", e);
			}
		} else
			doGet(resource);
	}

	private void deleteComments(GaleContext gale) throws ProcessorException {
		if (!gale.userId().equals(gale.concept().getProperty("author")))
			return;
		CacheSession<EntityValue> session = gale.um().openSession();
		for (EntityValue ev : getComments(gale))
			session.put(ev.getUri(),
					EntityValue.create(ev.getUri(), new String[] {}));
		session.commit();
	}

	private void addComment(GaleContext gale, String comment, boolean personal) {
		String listname = (personal ? "#comments-personal" : "#comments");
		URI uri = GaleUtil.addUserInfo(gale.conceptUri(), gale.userId())
				.resolve(listname);
		EntityValue ev = gale.um().get(uri);
		if (ev == null)
			ev = new EntityValue(uri, new String[] {});
		List<String> list = new ArrayList<String>(
				ImmutableList.copyOf((String[]) ev.getValue()));
		list.add(Comment.of(gale.userId(), System.currentTimeMillis(), comment)
				.toString());
		ev = new EntityValue(uri, list.toArray(new String[] {}));
		CacheSession<EntityValue> session = gale.um().openSession();
		session.put(uri, ev);
		session.commit();
	}

	private List<EntityValue> getComments(GaleContext gale)
			throws ProcessorException {
		String queryString = "select ev from EntityValue ev where ev.attributeUri = '"
				+ gale.conceptUri()
				+ "#comments' or ev.attributeUri = '"
				+ gale.conceptUri() + "#comments-personal'";
		List<String> qresult;
		try {
			qresult = gale.ebc().event(
					"queryum",
					ImmutableList.of(EventHash.createSingleEvent("query",
							queryString).toString()));
			List<EntityValue> evs = GaleUtil.gson().fromJson(qresult.get(1),
					new TypeToken<List<EntityValue>>() {
					}.getType());
			return evs;
		} catch (Exception e) {
			throw new ProcessorException("unable to retrieve comments: "
					+ e.getMessage(), e);
		}
	}

	@Override
	public void doGet(Resource resource) throws ProcessorException {
		GaleContext gale = GaleContext.of(resource);
		List<Comment> comments = new ArrayList<Comment>();
		for (EntityValue ev : getComments(gale)) {
			String[] values = (String[]) ev.getValue();
			for (String value : values)
				comments.add(Comment.parse(value));
		}
		Collections.sort(comments);
		String url = GaleUtil.getRequestURL(gale.req());
		Element html = GaleUtil.createHTMLElement("html");
		Element body = html.addElement("body");
		if (gale.userId().equals(gale.concept().getProperty("author")))
			body.add(createAuthorOptions(gale));
		Element div = body.addElement("div").addAttribute("class", "comment");
		Element form = div.addElement("form").addAttribute("method", "POST")
				.addAttribute("action", url).addAttribute("style", "margin:0")
				.addAttribute("id", "newcomment");
		form.addElement("a")
				.addAttribute("href", "#")
				.addAttribute("onClick",
						"document.forms['newcomment'].submit(); return false;")
				.addAttribute("class", "comment-submit good").addText("submit");
		form.addElement("span").addAttribute("class", "comment-name")
				.addText(findName(gale, gale.userId()));
		form.addElement("span").addAttribute("class", "comment-text")
				.addElement("textarea").addAttribute("name", "comment")
				.addAttribute("rows", "10");
		boolean dimComments = (comments.size() > 0
				&& comments.get(0).getUserId().equals(gale.userId()) ? true
				: false);
		for (Comment comment : comments) {
			Element cdiv = body.addElement("div").addAttribute("class",
					(dimComments ? "comment comment-read" : "comment"));
			cdiv.addElement("span").addAttribute("class", "comment-time")
					.addText(comment.getDateString());
			cdiv.addElement("span").addAttribute("class", "comment-name")
					.addText(findName(gale, comment.getUserId()));
			cdiv.add(createCommentText(comment.getValue()));
		}
		resource.put("xml", html);
		resource.put("mime", "text/xhtml");
		resource.put("original-url", gale.conceptUri().toString());
		try {
			resource.put("url", gale.conceptUri().toURL());
		} catch (MalformedURLException e) {
		}
		gale.usedStream();
	}

	private Element createAuthorOptions(GaleContext gale) {
		Element div = GaleUtil.createHTMLElement("div").addAttribute("class",
				"comment");
		div.addElement("script")
				.addAttribute("type", "text/javascript")
				.addText(
						"\nfunction check() {\nif (confirm(\"Delete comments for this concept?\"))\n{\ndocument.forms['deletecomments'].submit();\n}\nreturn false;}");
		Element form = div.addElement("form").addAttribute("method", "POST")
				.addAttribute("action", GaleUtil.getRequestURL(gale.req()))
				.addAttribute("style", "margin:0")
				.addAttribute("id", "deletecomments");
		form.addElement("a").addAttribute("href", "#")
				.addAttribute("onClick", "check(); return false;")
				.addAttribute("class", "comment-submit good")
				.addText("delete all");
		form.addElement("span").addAttribute("class", "comment-name")
				.addText("Author options");
		form.addElement("input").addAttribute("type", "hidden")
				.addAttribute("name", "delete").addAttribute("value", "true");
		return div;
	}

	private static String findName(GaleContext gale, String userId) {
		try {
			URI uri = GaleUtil.addUserInfo(
					URIs.of("gale://gale.tue.nl/personal#name"), userId);
			return gale.um().get(uri).getValue().toString();
		} catch (Exception e) {
			e.printStackTrace();
			return (userId.startsWith("GALE_") ? "Anonymous user" : userId);
		}
	}

	private static Element createCommentText(String comment) {
		Element result = CreoleParser.instance().parse(comment);
		result.addAttribute("xml:space", "preserve").addAttribute("class",
				"comment-text");
		result.detach();
		return result;
	}

	private static class Comment implements Comparable<Comment> {
		private final String value;
		private final String userId;
		private final long time;
		private final String serialized;
		private final DateFormat df = new SimpleDateFormat();

		private Comment(String userId, long time, String value) {
			checkNotNull(userId);
			checkNotNull(value);
			this.userId = userId;
			this.time = time;
			this.value = value;
			StringBuilder sb = new StringBuilder();
			sb.append(time);
			sb.append(":'");
			sb.append(userId.replace("\\", "\\\\").replace("'", "\\'"));
			sb.append("':");
			sb.append(value);
			serialized = sb.toString();
		}

		public static Comment parse(String serialized) {
			int i = serialized.indexOf(":");
			long time = Long.parseLong(serialized.substring(0, i));
			i += 2;
			StringBuilder sb = new StringBuilder(serialized.length());
			while (serialized.charAt(i) != '\'') {
				if (serialized.charAt(i) == '\\')
					i++;
				sb.append(serialized.charAt(i));
				i++;
			}
			String userId = sb.toString();
			i += 2;
			String value = serialized.substring(i);
			return of(userId, time, value);
		}

		public static Comment of(String userId, long time, String value) {
			return new Comment(userId, time, value);
		}

		public String getValue() {
			return value;
		}

		public String getUserId() {
			return userId;
		}

		@SuppressWarnings("unused")
		public long getTime() {
			return time;
		}

		public Date getDate() {
			return new Date(time);
		}

		public String getDateString() {
			return df.format(getDate());
		}

		@Override
		public String toString() {
			return serialized;
		}

		@Override
		public int compareTo(Comment o) {
			if (time == o.time)
				return 0;
			if (time > o.time)
				return -1;
			return 1;
		}
	}
}
