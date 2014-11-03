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
 * GaleUtil.java
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
package nl.tue.gale.common;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.AccessControlException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import nl.tue.gale.common.uri.URI;
import nl.tue.gale.common.uri.URIs;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * A utility class used throughout Gale. This class contains several static
 * convenience methods and classes.
 * 
 * @author David Smits
 */
public final class GaleUtil {
	public static <T> Iterable<T> enumIterable(final Enumeration<T> enumeration) {
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return new Iterator<T>() {
					@Override
					public boolean hasNext() {
						return enumeration.hasMoreElements();
					}

					@Override
					public T next() {
						return enumeration.nextElement();
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}

	public static final class HashComparator<T> implements
			Comparator<Hashtable<?, ?>> {
		private Comparator<T> comp;
		private Object index;

		public HashComparator(Object index, Comparator<T> comp) {
			this.index = index;
			this.comp = comp;
		}

		@Override
		@SuppressWarnings("unchecked")
		public int compare(Hashtable<?, ?> o1, Hashtable<?, ?> o2) {
			return comp.compare((T) o1.get(index), (T) o2.get(index));
		}
	}

	public static Double avg(Object... args) {
		double total = 0;
		double count = 0;
		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			if (arg instanceof Number) {
				count++;
				total += ((Number) arg).doubleValue();
			} else
				for (int j = 0; j < ((Object[]) arg).length; j++) {
					count++;
					total += ((Number) ((Object[]) arg)[j]).doubleValue();
				}
		}
		return total / count;
	}

	public static Double sum(Object... args) {
		Double result = 0d;
		for (Object arg : args) {
			if (arg instanceof Number)
				result += ((Number) arg).doubleValue();
			else if (arg instanceof Iterable<?>)
				result += sum((Iterable<?>) arg);
			else if (arg.getClass().isArray())
				result += sum((Object[]) arg);
			else
				throw new IllegalArgumentException(
						"only numbers allowed in 'sum'");
		}
		return result;
	}

	public static Double sum(Number number) {
		return number.doubleValue();
	}

	public static Boolean and(Object... args) {
		boolean result = true;
		for (Object arg : args) {
			if (arg instanceof Boolean)
				result &= (Boolean) arg;
			else if (arg instanceof Iterable<?>)
				result &= and((Iterable<?>) arg);
			else if (arg.getClass().isArray())
				result &= and((Object[]) arg);
			else
				throw new IllegalArgumentException(
						"only booleans allowed in 'and'");
		}
		return result;
	}

	public static Boolean and(Boolean bool) {
		return bool;
	}

	public static boolean safeEquals(Object a, Object b) {
		if (a == null)
			return b == null;
		if (b == null)
			return false;
		return a.equals(b);
	}

	/**
	 * reconstructs the original request URL, including parameters.
	 * 
	 * @param req
	 *            the current <code>HttpServletRequest</code>
	 * @return the original request URL
	 */
	@SuppressWarnings("unchecked")
	public static String getRequestURL(HttpServletRequest req) {
		UrlEncodedQueryString qs = UrlEncodedQueryString.create();
		Map<String, String[]> parameters = req.getParameterMap();
		for (Map.Entry<String, String[]> entry : parameters.entrySet())
			for (String value : entry.getValue())
				qs.append(entry.getKey(), value);
		return qs.apply(URIs.of(req.getRequestURL().toString())).toString();
	}

	/**
	 * reconstructs the original request URL, including parameters as specified.
	 * 
	 * @param req
	 *            the current <code>HttpServletRequest</code>
	 * @param parameters
	 *            a map of parameters that should be included
	 * @return the original request URL
	 */
	public static String getRequestURL(HttpServletRequest req,
			Map<String, String[]> parameters) {
		UrlEncodedQueryString qs = UrlEncodedQueryString.create();
		for (Map.Entry<String, String[]> entry : parameters.entrySet())
			for (String value : entry.getValue())
				qs.append(entry.getKey(), value);
		return qs.apply(URIs.of(req.getRequestURL().toString())).toString();
	}

	public static String getServletName(HttpServletRequest req) {
		String result = req.getRequestURI();
		result = result.substring(req.getContextPath().length() + 1);
		result = result.substring(0, result.indexOf("/"));
		return result;
	}

	public static Element createHTMLElement(String name) {
		return createNSElement(name, xhtmlns);
	}

	public static Element createNSElement(String name, String ns) {
		return DocumentFactory.getInstance().createElement(
				DocumentFactory.getInstance().createQName(name, "", ns));
	}

	public static Map<String, String> getQueryParameters(String query) {
		Map<String, String> result = new HashMap<String, String>();
		if (query == null)
			return result;
		for (String param : query.split("&"))
			result.put(param.substring(0, param.indexOf("=")),
					param.substring(param.indexOf("=") + 1));
		return result;
	}

	public static String getQueryString(Map<String, String> params) {
		StringBuffer sb = new StringBuffer();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			if (sb.length() != 0)
				sb.append("&");
			sb.append(entry.getKey());
			sb.append("=");
			sb.append(entry.getValue());
		}
		return sb.toString();
	}

	public static class GUID {
		private long guid = 0;

		public synchronized long get() {
			guid++;
			return guid;
		}
	}

	public static enum URIPart {
		SCHEME, USERINFO, HOST, PORT, AUTHORITY, PATH, QUERY, FRAGMENT
	}

	public static final String adminpw = "6129a449d4877a0cdcac8b1dcfa774df";
	public static int debuglevel = 0;
	private static Map<String, String> mimetable = new HashMap<String, String>();
	public static final String xhtmlns = "http://www.w3.org/1999/xhtml";
	public static final String gdomns = "http://gale.tue.nl/gdom";
	public static final String adaptns = "http://gale.tue.nl/adaptation";
	public static final boolean throwOnAttributeNotFound = true;
	public static final Set<String> omitCloseSet = new HashSet<String>();

	static {
		mimetable.put("txt", "text/plain");
		mimetable.put("html", "text/html");
		mimetable.put("htm", "text/html");
		mimetable.put("xml", "text/xml");
		mimetable.put("xhtml", "text/xhtml");
		mimetable.put("xhtm", "text/xhtml");
		mimetable.put("frm", "text/xhtml");
		mimetable.put("smil", "application/smil");
		mimetable.put("smi", "application/smil");
		mimetable.put("css", "text/css");
		mimetable.put("sgml", "text/sgml");
		mimetable.put("sgm", "text/sgml");
		mimetable.put("wml", "text/vnd.wap.wml");
		mimetable.put("wmls", "text/vnd.wap.wmlscript");

		// tex
		mimetable.put("dvi", "application/x-dvi");
		mimetable.put("tex", "application/x-tex");

		// MS-office
		mimetable.put("doc", "application/msword");
		mimetable.put("xls", "application/ms-excel");
		mimetable.put("ppt", "application/ms-powerpoint");

		// pdf etc.
		mimetable.put("pdf", "application/pdf");
		mimetable.put("ps", "application/postscript");
		mimetable.put("eps", "application/postscript");
		mimetable.put("ai", "application/postscript");

		// binary files
		mimetable.put("bin", "octet-stream");
		mimetable.put("dms", "octet-stream");
		mimetable.put("lha", "octet-stream");
		mimetable.put("lzh", "octet-stream");
		mimetable.put("exe", "octet-stream");

		// class files
		mimetable.put("class", "octet-stream");

		// zips
		mimetable.put("gz", "application/x-gzip");
		mimetable.put("zip", "application/zip");

		// special stuff
		mimetable.put("swf", "application/x-shockware-flash");

		// images
		mimetable.put("gif", "image/gif");
		mimetable.put("bmp", "image/bmp");
		mimetable.put("jpeg", "image/jpeg");
		mimetable.put("jpg", "image/jpeg");
		mimetable.put("jpe", "image/jpeg");
		mimetable.put("tif", "image/tif");
		mimetable.put("tiff", "image/tif");
		mimetable.put("xbm", "image/x-xbitmap");
		mimetable.put("xpm", "image/x-xpixmap");

		// windows media
		mimetable.put("wmv", "video/x-ms-wmv");

		// others from tomcat
		mimetable.put("abs", "audio/x-mpeg");
		mimetable.put("aif", "audio/x-aiff");
		mimetable.put("aifc", "audio/x-aiff");
		mimetable.put("aiff", "audio/x-aiff");
		mimetable.put("aim", "application/x-aim");
		mimetable.put("art", "image/x-jg");
		mimetable.put("asf", "video/x-ms-asf");
		mimetable.put("asx", "video/x-ms-asf");
		mimetable.put("au", "audio/basic");
		mimetable.put("avi", "video/x-msvideo");
		mimetable.put("avx", "video/x-rad-screenplay");
		mimetable.put("bcpio", "application/x-bcpio");
		mimetable.put("bin", "application/octet-stream");
		mimetable.put("body", "text/html");
		mimetable.put("cdf", "application/x-cdf");
		mimetable.put("cer", "application/x-x509-ca-cert");
		mimetable.put("class", "application/java");
		mimetable.put("cpio", "application/x-cpio");
		mimetable.put("csh", "application/x-csh");
		mimetable.put("dib", "image/bmp");
		mimetable.put("dtd", "text/plain");
		mimetable.put("dv", "video/x-dv");
		mimetable.put("etx", "text/x-setext");
		mimetable.put("exe", "application/octet-stream");
		mimetable.put("gtar", "application/x-gtar");
		mimetable.put("gz", "application/x-gzip");
		mimetable.put("hdf", "application/x-hdf");
		mimetable.put("hqx", "application/mac-binhex40");
		mimetable.put("ief", "image/ief");
		mimetable.put("jad", "text/vnd.sun.j2me.app-descriptor");
		mimetable.put("jar", "application/java-archive");
		mimetable.put("java", "text/plain");
		mimetable.put("jnlp", "application/x-java-jnlp-file");
		mimetable.put("js", "text/javascript");
		mimetable.put("kar", "audio/x-midi");
		mimetable.put("m3u", "audio/x-mpegurl");
		mimetable.put("mac", "image/x-macpaint");
		mimetable.put("man", "application/x-troff-man");
		mimetable.put("me", "application/x-troff-me");
		mimetable.put("mid", "audio/x-midi");
		mimetable.put("midi", "audio/x-midi");
		mimetable.put("mif", "application/x-mif");
		mimetable.put("mov", "video/quicktime");
		mimetable.put("movie", "video/x-sgi-movie");
		mimetable.put("mp1", "audio/x-mpeg");
		mimetable.put("mp2", "audio/x-mpeg");
		mimetable.put("mp3", "audio/x-mpeg");
		mimetable.put("mpa", "audio/x-mpeg");
		mimetable.put("mpe", "video/mpeg");
		mimetable.put("mpeg", "video/mpeg");
		mimetable.put("mpega", "audio/x-mpeg");
		mimetable.put("mpg", "video/mpeg");
		mimetable.put("mpv2", "video/mpeg2");
		mimetable.put("ms", "application/x-wais-source");
		mimetable.put("nc", "application/x-netcdf");
		mimetable.put("oda", "application/oda");
		mimetable.put("pbm", "image/x-portable-bitmap");
		mimetable.put("pct", "image/pict");
		mimetable.put("pgm", "image/x-portable-graymap");
		mimetable.put("pic", "image/pict");
		mimetable.put("pict", "image/pict");
		mimetable.put("pls", "audio/x-scpls");
		mimetable.put("png", "image/png");
		mimetable.put("pnm", "image/x-portable-anymap");
		mimetable.put("pnt", "image/x-macpaint");
		mimetable.put("ppm", "image/x-portable-pixmap");
		mimetable.put("psd", "image/x-photoshop");
		mimetable.put("qt", "video/quicktime");
		mimetable.put("qti", "image/x-quicktime");
		mimetable.put("qtif", "image/x-quicktime");
		mimetable.put("ras", "image/x-cmu-raster");
		mimetable.put("rgb", "image/x-rgb");
		mimetable.put("rm", "application/vnd.rn-realmedia");
		mimetable.put("roff", "application/x-troff");
		mimetable.put("rtf", "application/rtf");
		mimetable.put("rtx", "text/richtext");
		mimetable.put("sh", "application/x-sh");
		mimetable.put("shar", "application/x-shar");
		mimetable.put("smf", "audio/x-midi");
		mimetable.put("snd", "audio/basic");
		mimetable.put("src", "application/x-wais-source");
		mimetable.put("sv4cpio", "application/x-sv4cpio");
		mimetable.put("sv4crc", "application/x-sv4crc");
		mimetable.put("swf", "application/x-shockwave-flash");
		mimetable.put("t", "application/x-troff");
		mimetable.put("tar", "application/x-tar");
		mimetable.put("tcl", "application/x-tcl");
		mimetable.put("texi", "application/x-texinfo");
		mimetable.put("texinfo", "application/x-texinfo");
		mimetable.put("tr", "application/x-troff");
		mimetable.put("tsv", "text/tab-separated-values");
		mimetable.put("ulw", "audio/basic");
		mimetable.put("ustar", "application/x-ustar");
		mimetable.put("xwd", "image/x-xwindowdump");
		mimetable.put("wav", "audio/x-wav");
		mimetable.put("wbmp", "image/vnd.wap.wbmp");
		mimetable.put("wml", "text/vnd.wap.wml");
		mimetable.put("wmlc", "text/vnd.wap.wmlc");
		mimetable.put("wmls", "text/vnd.wap.wmls");
		mimetable.put("wmlscript", "application/vnd.wap.wmlscript");
		mimetable.put("wrl", "x-world/x-vrml");
		mimetable.put("Z", "application/x-compress");
		mimetable.put("z", "application/x-compress");
		mimetable.put("zip", "application/zip");
		omitCloseSet.addAll(Arrays.asList(new String[] { "BASE", "BR", "COL",
				"HR", "IMG", "INPUT", "LINK", "META", "P", "PARAM" }));
	}

	/**
	 * Generates an html span element with the specified error text. The element
	 * will have its 'class' attribute set to 'error'.
	 * 
	 * @param text
	 *            the text to use in the generated element
	 * @param doc
	 *            the <code>Document</code> that the generated element will be a
	 *            part of
	 * @return the generated span <code>org.w3c.dom.Element</code>
	 */
	public static Element createErrorElement(String text) {
		Element result = DocumentFactory.getInstance().createElement(
				DocumentFactory.getInstance().createQName("span", "",
						"http://www.w3.org/1999/xhtml"));
		result.addText(text);
		result.addAttribute("style", "color:#D02020");
		return result;
	}

	public static SAXReader createSAXReader(boolean validation, boolean loaddtd) {
		SAXReader result = new SAXReader();
		result.setValidation(validation);
		result.setIncludeExternalDTDDeclarations(loaddtd);
		result.setIncludeInternalDTDDeclarations(loaddtd);
		return result;
	}

	private static final ThreadLocal<XPPEntityReader> _entityReader = new ThreadLocal<XPPEntityReader>() {
		@Override
		protected XPPEntityReader initialValue() {
			return new XPPEntityReader();
		}
	};

	public static XPPEntityReader createXPPReader() {
		return _entityReader.get();
	}

	public static boolean debug(int lvl) {
		return lvl <= debuglevel;
	}

	public static String digest(String value) {
		try {
			byte[] bytes = java.security.MessageDigest.getInstance("MD5")
					.digest(value.getBytes());
			return (new java.math.BigInteger(bytes)).toString(16);
		} catch (Exception e) {
			throw new IllegalArgumentException("unable to digest message: "
					+ e.getMessage(), e);
		}
	}

	/**
	 * Generates a proper <code>URL</code> from a string representing the
	 * location of a resource. Resource-locations can use a variety of protocols
	 * that are not supported by the standard <code>URL</code>. The 'gale'
	 * protocol is used to locate resources inside the servers Gale home
	 * directory (should be translated to a 'file:' URL). Other locations are
	 * assumed to be true URLs.
	 * 
	 * @param location
	 *            the <code>String</code> representing the resource location
	 * @param homedir
	 *            the <code>File</code> representing the Gale home directory
	 * @return the generated <code>URL</code>
	 * @throws IllegalArgumentException
	 *             if the location specified does not result in a proper URL
	 */
	public static URL generateURL(String location, File homedir) {
		try {
			if (location.startsWith("gale:"))
				return new File(homedir, location.substring(5)).toURI().toURL();
			if (location.startsWith("file:"))
				throw new AccessControlException("access denied accessing '"
						+ location + "'");
			return new URL(location);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("unable to generate URL for '"
					+ location + "': " + e.getMessage(), e);
		}
	}

	public static void setMimeTable(Map<String, String> table) {
		mimetable.clear();
		mimetable.putAll(table);
	}

	public static String getMime(String name) {
		if (name.contains("?"))
			name = name.substring(0, name.indexOf("?"));
		if (name.indexOf(".") != -1) {
			return mimetable.get(name.substring(name.lastIndexOf('.') + 1));
		} else {
			return mimetable.get(name);
		}
	}

	private static final ThreadLocal<Random> _random = new ThreadLocal<Random>() {
		@Override
		protected Random initialValue() {
			return new Random();
		}
	};

	public static String newGUID() {
		byte[] bytes = new byte[18];
		_random.get().nextBytes(bytes);
		char[] chars = new char[36];
		byte value;
		for (int i = 0; i < 18; i++) {
			value = (byte) (bytes[i] & (byte) 0x0F);
			chars[i * 2] = (char) (value < 10 ? value + 48 : value + 55);
			value = (byte) ((bytes[i] >>> 4) & (byte) 0x0F);
			chars[i * 2 + 1] = (char) (value < 10 ? value + 48 : value + 55);
		}
		chars[8] = '-';
		chars[13] = '-';
		chars[18] = '-';
		chars[23] = '-';
		return new String(chars);
	}

	public static String notnull(Object o) {
		return (o == null ? "" : o.toString());
	}

	public static Document parseXML(File file) {
		try {
			return parseXML(file, "UTF-8");
		} catch (Exception e) {
			throw new IllegalArgumentException("unable to parse xml document: "
					+ e.getMessage(), e);
		}
	}

	public static Document parseXML(InputStream stream) {
		try {
			return parseXML(stream, "UTF-8");
		} catch (Exception e) {
			throw new IllegalArgumentException("unable to parse xml document: "
					+ e.getMessage(), e);
		}
	}

	public static Document parseXML(URL url) {
		try {
			return parseXML(url, "UTF-8");
		} catch (Exception e) {
			throw new IllegalArgumentException("unable to parse xml document: "
					+ e.getMessage(), e);
		}
	}

	public static Document parseXML(Reader reader) {
		try {
			return createXPPReader().read(reader);
		} catch (Exception e) {
			throw new IllegalArgumentException("unable to parse xml document: "
					+ e.getMessage(), e);
		}
	}

	public static Document parseXML(File file, String encoding) {
		try {
			return parseXML(new FileInputStream(file), encoding);
		} catch (Exception e) {
			throw new IllegalArgumentException("unable to parse xml document: "
					+ e.getMessage(), e);
		}
	}

	public static Document parseXML(InputStream stream, String encoding) {
		try {
			return parseXML(new InputStreamReader(stream, encoding));
		} catch (Exception e) {
			throw new IllegalArgumentException("unable to parse xml document: "
					+ e.getMessage(), e);
		}
	}

	public static Document parseXML(URL url, String encoding) {
		try {
			return parseXML(url.openStream(), encoding);
		} catch (Exception e) {
			throw new IllegalArgumentException("unable to parse xml document: "
					+ e.getMessage(), e);
		}
	}

	public static String serializeXML(Element element) {
		StringWriter out = new StringWriter();
		OutputFormat of = new OutputFormat();
		of.setExpandEmptyElements(true);
		XMLWriter writer = new XMLWriter(out, of) {
			@Override
			protected void writeEmptyElementClose(String qualifiedName)
					throws IOException {
				if (omitCloseSet.contains(qualifiedName.toUpperCase())) {
					writer.write(" />");
				} else {
					super.writeEmptyElementClose(qualifiedName);
				}
			}
		};
		try {
			writer.write(element);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("unable to serialize XML: "
					+ e.getMessage());
		}
		return out.toString();
	}

	@SuppressWarnings("unchecked")
	public static Node replaceNode(Node oldnode, Node newnode) {
		List<Node> content = oldnode.getParent().content();
		if (newnode != null)
			content.add(content.indexOf(oldnode), newnode);
		content.remove(oldnode);
		return newnode;
	}

	public static boolean toBoolean(Object o) {
		if (o instanceof Number)
			return ((Number) o).doubleValue() != 0;
		if (o instanceof Boolean)
			return ((Boolean) o).booleanValue();
		return false;
	}

	public static double toDouble(Object o) {
		if (o instanceof Number)
			return ((Number) o).doubleValue();
		if (o instanceof Boolean)
			return (((Boolean) o).booleanValue() ? 1 : 0);
		return 0;
	}

	public static HttpServletRequest wrappedRequest(HttpServletRequest req,
			final URL url, final String method) {
		return new HttpServletRequestWrapper(wrappedRequest(req, url)) {
			@Override
			public String getMethod() {
				return method;
			}
		};
	}

	public static class URLRequestWrapper extends HttpServletRequestWrapper {
		private final URL url;
		private final Map<String, String[]> parameterMap;

		public URLRequestWrapper(HttpServletRequest request, URL url) {
			super(request);
			this.url = url;
			try {
				ImmutableMap.Builder<String, String[]> builder = ImmutableMap
						.builder();
				Multimap<String, String> content = HashMultimap.create();
				String query = url.toURI().getQuery();
				if (query != null) {
					for (String s : query.split("[&;]")) {
						String[] param = s.split("=");
						if (param.length == 1)
							content.put(param[0], "");
						else
							content.put(param[0],
									URLDecoder.decode(param[1], "UTF-8"));
					}
				}
				for (Map.Entry<String, Collection<String>> entry : content
						.asMap().entrySet())
					builder.put(entry.getKey(),
							entry.getValue().toArray(new String[] {}));
				parameterMap = builder.build();
			} catch (Exception e) {
				throw new IllegalArgumentException(
						"unable to create wrapped HttpRequest", e);
			}
		}

		@Override
		public String getParameter(String name) {
			String[] values = parameterMap.get(name);
			if (values == null)
				return null;
			return values[0];
		}

		@Override
		@SuppressWarnings({ "rawtypes" })
		public Map getParameterMap() {
			return parameterMap;
		}

		@Override
		@SuppressWarnings("rawtypes")
		public Enumeration getParameterNames() {
			final Object[] keys = parameterMap.keySet().toArray();
			return new Enumeration() {
				private int i = 0;

				@Override
				public boolean hasMoreElements() {
					return i < keys.length;
				}

				@Override
				public Object nextElement() {
					i++;
					return keys[i - 1];
				}
			};
		}

		@Override
		public String[] getParameterValues(String name) {
			return parameterMap.get(name);
		}

		@Override
		public String getPathInfo() {
			return url.getPath().substring(
					url.getPath().indexOf("/concept") + 8);
		}

		@Override
		public String getPathTranslated() {
			return "";
		}

		@Override
		public String getQueryString() {
			return (url.toString().indexOf("?") != -1 ? url.toString()
					.substring(url.toString().indexOf("?") + 1) : "");
		}

		@Override
		public String getRequestURI() {
			return url.getPath();
		}

		@Override
		public StringBuffer getRequestURL() {
			return new StringBuffer((url.toString().indexOf("?") != -1 ? url
					.toString().substring(0, url.toString().indexOf("?"))
					: url.toString()));
		}
	}

	public static HttpServletRequest wrappedRequest(HttpServletRequest req,
			final URL url) {
		return new URLRequestWrapper(req, url);
	}

	public static HttpServletResponse wrappedResponse(HttpServletResponse resp) {
		return new HttpServletResponseWrapper(resp) {
			private String characterencoding = "ISO-8859-1";

			private String contenttype = null;

			private CharArrayWriter output = new CharArrayWriter();

			private PrintWriter pout = null;

			private int error = 0;

			@Override
			public void addCookie(Cookie cookie) {
			}

			@Override
			public void addDateHeader(String name, long date) {
			}

			@Override
			public void addHeader(String name, String value) {
			}

			@Override
			public void addIntHeader(String name, int value) {
			}

			@Override
			public void flushBuffer() throws IOException {
			}

			@Override
			public int getBufferSize() {
				return error;
			}

			@Override
			public String getCharacterEncoding() {
				return characterencoding;
			}

			@Override
			public String getContentType() {
				return contenttype;
			}

			@Override
			public ServletOutputStream getOutputStream() throws IOException {
				return new ServletOutputStream() {
					@Override
					public void close() throws IOException {
						getWriter().close();
					}

					@Override
					public void flush() throws IOException {
						getWriter().flush();
					}

					@Override
					public void print(boolean b) throws IOException {
						getWriter().print(b);
					}

					@Override
					public void print(char c) throws IOException {
						getWriter().print(c);
					}

					@Override
					public void print(double d) throws IOException {
						getWriter().print(d);
					}

					@Override
					public void print(float f) throws IOException {
						getWriter().print(f);
					}

					@Override
					public void print(int i) throws IOException {
						getWriter().print(i);
					}

					@Override
					public void print(long l) throws IOException {
						getWriter().print(l);
					}

					@Override
					public void print(String s) throws IOException {
						getWriter().print(s);
					}

					@Override
					public void println() throws IOException {
						getWriter().println();
					}

					@Override
					public void println(boolean b) throws IOException {
						getWriter().println(b);
					}

					@Override
					public void println(char c) throws IOException {
						getWriter().println(c);
					}

					@Override
					public void println(double d) throws IOException {
						getWriter().println(d);
					}

					@Override
					public void println(float f) throws IOException {
						getWriter().println(f);
					}

					@Override
					public void println(int i) throws IOException {
						getWriter().println(i);
					}

					@Override
					public void println(long l) throws IOException {
						getWriter().println(l);
					}

					@Override
					public void println(String s) throws IOException {
						getWriter().println(s);
					}

					@Override
					public void write(byte[] b) throws IOException {
						getWriter().write(
								(new String(b, "ISO-8859-1")).toCharArray());
					}

					@Override
					public void write(byte[] b, int off, int len)
							throws IOException {
						getWriter().write(
								(new String(b, "ISO-8859-1")).toCharArray(),
								off, len);
					}

					@Override
					public void write(int b) throws IOException {
						getWriter().write(b);
					}
				};
			}

			@Override
			public PrintWriter getWriter() throws IOException {
				if (pout == null)
					pout = new PrintWriter(output);
				return pout;
			}

			@Override
			public boolean isCommitted() {
				return false;
			}

			@Override
			public void reset() {
				output = new CharArrayWriter();
				pout = new PrintWriter(output);
			}

			@Override
			public void resetBuffer() {
			}

			@Override
			public void sendError(int sc) throws IOException {
				error = sc;
			}

			@Override
			public void sendError(int sc, String msg) throws IOException {
			}

			@Override
			public void sendRedirect(String location) throws IOException {
			}

			@Override
			public void setBufferSize(int size) {
			}

			@Override
			public void setCharacterEncoding(String charset) {
				characterencoding = charset;
			}

			@Override
			public void setContentLength(int len) {
			}

			@Override
			public void setContentType(String type) {
				this.contenttype = type;
			}

			@Override
			public void setDateHeader(String name, long date) {
			}

			@Override
			public void setHeader(String name, String value) {
			}

			@Override
			public void setIntHeader(String name, int value) {
			}

			@Override
			public void setStatus(int sc) {
			}

			@Override
			public void setStatus(int sc, String sm) {
			}

			@Override
			public String toString() {
				return output.toString();
			}
		};
	}

	public static URL getContextURL(HttpServletRequest req) {
		try {
			return new URL(req.getScheme(), req.getServerName(),
					req.getServerPort(), req.getContextPath());
		} catch (Exception e) {
			throw new IllegalArgumentException("unable to create context URL: "
					+ e.getMessage(), e);
		}
	}

	public static URI setURIPart(URI original, URIPart part, String value) {
		try {
			switch (part) {
			case SCHEME:
				return URIs.builder().uri(original).scheme(value).build();
			case USERINFO:
				return URIs.builder().uri(original).userInfo(value).build();
			case HOST:
				return URIs.builder().uri(original).host(value).build();
			case PORT:
				return URIs.builder().uri(original)
						.port(Integer.parseInt(value)).build();
			case AUTHORITY:
				return URIs.builder().uri(original).authority(value).build();
			case PATH:
				return URIs.builder().uri(original).path(value).build();
			case QUERY:
				return URIs.builder().uri(original).query(value).build();
			case FRAGMENT:
				return URIs.builder().uri(original).fragment(value).build();
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("uanble to set URI part: "
					+ e.getMessage(), e);
		}
		return null;
	}

	public static URI addUserInfo(URI uri, String userInfo) {
		return setURIPart(uri, URIPart.USERINFO, userInfo);
	}

	@SuppressWarnings("unchecked")
	public static Element findElement(Element xml, String string) {
		if (xml.getName().equals(string))
			return xml;
		for (Element e : (List<Element>) xml.elements()) {
			Element result = findElement(e, string);
			if (result != null)
				return result;
		}
		return null;
	}

	public static String getParam(Object[] params, String name) {
		for (Object p : params) {
			String v = p.toString();
			if (v.startsWith(name + "="))
				return v.substring(v.indexOf("=") + 1);
		}
		return null;
	}

	public static boolean checkEquals(Object o1, Object o2) {
		return (o1 == null ? o2 == null : o1.equals(o2));
	}

	public static void sendToClient(HttpServletResponse resp,
			InputStream stream, String mime, String encoding) {
		byte[] buffer = new byte[8192];
		int nr = 0;
		try {
			resp.setContentType(mime);
			resp.setCharacterEncoding(encoding);
			resp.setBufferSize(8192);
			if (stream == null)
				throw new ServletException("no data to send to client");
			OutputStream os = resp.getOutputStream();
			do {
				nr = stream.read(buffer);
				if (nr > 0)
					os.write(buffer, 0, nr);
			} while (nr != -1);
			os.close();
			stream.close();
		} catch (Exception e) {
			throw new IllegalArgumentException("unable to send data to client",
					e);
		}
	}

	private static boolean openCorpus = false;

	public static void setOpenCorpus(boolean open) {
		openCorpus = open;
	}

	public static boolean useOpenCorpus() {
		return openCorpus;
	}

	private static Map<String, String> properties = new HashMap<String, String>();

	public static String getProperty(String key) {
		return properties.get(key);
	}

	public static void setProperty(String key, String value) {
		properties.put(key, value);
	}

	public static String nullToEmpty(String param) {
		if (param == null)
			return "";
		else
			return param;
	}

	public static Object typedObject(String typeClass, String value) {
		try {
			Class<?> clazz = Class.forName(typeClass);
			if (Number.class.isAssignableFrom(clazz)) {
				Double dvalue = Double.parseDouble(value);
				if (Double.class.isAssignableFrom(clazz))
					return dvalue;
				if (Float.class.isAssignableFrom(clazz))
					return dvalue.floatValue();
				if (Integer.class.isAssignableFrom(clazz))
					return dvalue.intValue();
				if (Short.class.isAssignableFrom(clazz))
					return dvalue.shortValue();
				if (Byte.class.isAssignableFrom(clazz))
					return dvalue.byteValue();
				if (Long.class.isAssignableFrom(clazz))
					return dvalue.longValue();
			}
			return Class.forName(typeClass).getConstructor(String.class)
					.newInstance(value);
		} catch (Exception e) {
			throw new IllegalArgumentException("unable to typecast value '"
					+ value + "' to class '" + typeClass + "'");
		}
	}

	public static String encodeURL(String url) {
		try {
			return URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static String decodeURL(String url) {
		try {
			return URLDecoder.decode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static String pick(int count, String list) {
		String[] seperated = list.split(";");
		int index = Math.max(Math.min(seperated.length - 1, count - 1), 0);
		return seperated[index];
	}

	private static final Map<Class<?>, Object> DEFAULTS;

	static {
		Map<Class<?>, Object> map = new HashMap<Class<?>, Object>();
		defaultsPut(map, boolean.class, false);
		defaultsPut(map, char.class, '\0');
		defaultsPut(map, byte.class, (byte) 0);
		defaultsPut(map, short.class, (short) 0);
		defaultsPut(map, int.class, 0);
		defaultsPut(map, long.class, 0L);
		defaultsPut(map, float.class, 0f);
		defaultsPut(map, double.class, 0d);
		defaultsPut(map, Boolean.class, false);
		defaultsPut(map, Character.class, '\0');
		defaultsPut(map, Byte.class, (byte) 0);
		defaultsPut(map, Short.class, (short) 0);
		defaultsPut(map, Integer.class, 0);
		defaultsPut(map, Long.class, 0L);
		defaultsPut(map, Float.class, 0f);
		defaultsPut(map, Double.class, 0d);
		DEFAULTS = ImmutableMap.copyOf(map);
	}

	private static <T> void defaultsPut(Map<Class<?>, Object> map,
			Class<T> type, T value) {
		map.put(type, value);
	}

	public static <T> T defaultValue(Class<T> type) {
		return type.cast(DEFAULTS.get(type));
	}

	private static final Gson gson;

	static {
		GsonBuilder builder = new GsonBuilder();
		try {
			builder.registerTypeAdapter(Class
					.forName("nl.tue.gale.um.data.EntityValue"),
					Class.forName("nl.tue.gale.um.data.EntityValue")
							.newInstance());
		} catch (Exception e) {
			e.printStackTrace();
		}
		gson = builder.create();
	}

	public static Gson gson() {
		return gson;
	}
}
