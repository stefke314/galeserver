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
 * DefinitionLocator.java
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
package nl.tue.gale.dm;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import nl.tue.gale.common.cache.Cache;
import nl.tue.gale.common.parser.ParseNode;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.common.uri.URIs;
import nl.tue.gale.dm.data.Concept;

import org.apache.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapMaker;

/**
 * The {@code DefinitionLocator} is a singleton class that allows easy retrieval
 * of domain definitions stored in .gam files. The .gam files can be discovered
 * based on the URI of a concept. The list of possible names of .gam files
 * (concepts.gam by default) is used to recursively find a URL of .gam file,
 * starting with the path of the URI and moving upwards until the root path is
 * reached.
 * 
 * The {@link loadDefinition} method allows the easy retrieval of information in
 * a particular .gam file. A list is maintained of all .gam files ever
 * requested. If an {@link UpdateListener} is registered, it is informed when
 * the locator suspects a concept's definition might have changed.
 * 
 * @author dsmits
 * 
 */
final class DefinitionLocator {
	private final static Logger log = Logger.getLogger(DefinitionLocator.class);
	private final static long cacheTimeout = 10;
	private final static long threadDelay = 10;
	private Cache<Concept> cache = null;

	private final ScheduledExecutorService executor = Executors
			.newSingleThreadScheduledExecutor();

	private DefinitionLocator() {
		executor.scheduleWithFixedDelay(new Runnable() {
			public void run() {
				updateDefinitions();
			}
		}, threadDelay, threadDelay, TimeUnit.SECONDS);
	}

	public void destroy() {
		executor.shutdownNow();
	}

	private static final DefinitionLocator locator = new DefinitionLocator();

	/**
	 * Returns a reference to the singleton <code>DefinitionLocator</code>.
	 * 
	 * @return a reference to the singleton <code>DefinitionLocator</code>.
	 */
	public static DefinitionLocator instance() {
		return locator;
	}

	public void setCache(Cache<Concept> cache) {
		if (this.cache == null)
			this.cache = cache;
		else
			throw new IllegalStateException("cache has already been set");
	}

	private ImmutableList<URI> names = ImmutableList
			.of(URIs.of("concepts.gam"));

	/**
	 * Sets the names of possible .gam files to search for.
	 * 
	 * @param names
	 *            the names of possible .gam files to search for.
	 */
	public void setNames(Iterable<String> names) {
		checkNotNull(names);
		List<URI> list = new LinkedList<URI>();
		for (String name : names)
			list.add(URIs.of(name));
		this.names = ImmutableList.copyOf(list);
	}

	/**
	 * Returns the URL of a .gam file that should contain the definition of the
	 * concept identified by the specified URI. To improve performance, caching
	 * of the relationship between URI and resulting URL is performed. These
	 * relationships expire after a set time.
	 * 
	 * The actual concepts defined a .gam file are cached and the result URL is
	 * checked against this cache as well. If there is no .gam file found for
	 * the specified concept, or if the .gam file did not contain a definition
	 * for the concept, the result is <code>null</code>.
	 * 
	 * @param conceptUri
	 *            the URI identifying the concept for which a .gam definition
	 *            file should be found
	 * @return the URL of a definition file or <code>null</code> if such a file
	 *         does not exist
	 */
	public URL getDefinitionURL(URI conceptUri) {
		checkNotNull(conceptUri);
		if (!"http".equals(conceptUri.getScheme())
				&& !"file".equals(conceptUri.getScheme()) && !"https".equals(conceptUri.getScheme()))
			return null;
		String conceptUriString = conceptUri.toString();
		String result = conceptMap.get(conceptUriString);
		if (result == null) {
			// entry does not exist in the cache, calculate it
			result = calculateResult(conceptUri);
			if (result == null)
				result = nullURL;
			conceptMap.put(conceptUri.toString(), result);
		}
		if (nullURL.equals(result)) {
			// there is a null entry in the cache
			return null;
		} else {
			// check against the possible outcome of loading the definition file
			Set<String> definitionList = definitionMap.get(result);
			if (definitionList == null || definitionList.contains(conceptUri))
				try {
					return new URL(result);
				} catch (MalformedURLException e) {
					throw new IllegalStateException("unable to generate URL", e);
				}
			return null;
		}
	}

	private String calculateResult(URI conceptUri) {
		URI next = conceptUri;
		URI current;
		URI up = URIs.of("..");
		do {
			current = next;
			for (URI name : names) {
				URI fileUri = current.resolve(name);
				String fileUriString = fileUri.toString();
				if (!notExistsMap.containsKey(fileUriString)) {
					if (existsURI(fileUri))
						return fileUri.toString();
					else
						notExistsMap.put(fileUriString, true);
				}
			}
		} while (!current.equals(next = current.resolve(up)));
		return null;
	}

	private boolean existsURI(URI gamURI) {
		try {
			gamURI.toURL().openStream();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Returns a set of concepts that are found in the .gam definition file
	 * located at the specified URL, and in any files that are referred through
	 * 'module' and 'parent' directives. The URIs of the concepts that are found
	 * in the files are cached for a set time to quickly determine if the files
	 * contain a definition for a specified URI.
	 * 
	 * @param definitionURL
	 *            the URL of a .gam definition file
	 * @return the set of concepts defined in the specified file and related
	 *         files
	 */
	public Set<Concept> loadDefinition(URL definitionURL) {
		try {
			Set<URI> todo = new HashSet<URI>();
			Set<URI> done = new HashSet<URI>();
			Set<URI> related = new HashSet<URI>();
			todo.add(URIs.of(definitionURL.toString()));
			Set<Concept> result = new HashSet<Concept>();
			while (!todo.isEmpty()) {
				URI uri = todo.iterator().next();
				todo.remove(uri);
				related.clear();
				try {
					result.addAll(loadInternalDefinition(uri.toURL(), related));
				} catch (Exception e) {
					log.error(e, e);
				}
				done.add(uri);
				related.removeAll(done);
				todo.addAll(related);
			}
			return result;
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"unable to load .gam definition from '" + definitionURL
							+ "'", e);
		}
	}

	private Set<Concept> loadInternalDefinition(URL definitionURL,
			Set<URI> related) {
		try {
			URLConnection con = definitionURL.openConnection();
			Set<Concept> result = new HashSet<Concept>();

			// load the file in memory
			StringBuilder sb;
			if (con.getContentLength() < 0)
				sb = new StringBuilder();
			else
				sb = new StringBuilder(con.getContentLength() + 16);
			try {
				Reader in = new BufferedReader(new InputStreamReader(
						con.getInputStream(),
						findEncoding(con.getContentType())));
				char[] buf = new char[1024];
				int read;
				while ((read = in.read(buf)) >= 0)
					sb.append(buf, 0, read);
				in.close();
			} catch (Exception e) {
				throw new IllegalArgumentException("unable to read .gam file '"
						+ definitionURL + "'", e);
			}
			String gamCode = sb.toString();

			// parse the file
			try {
				URI baseUri = URIs.of(definitionURL.toString());
				String fileName = baseUri.getPath().substring(
						baseUri.getPath().lastIndexOf('/') + 1);
				GAMFormat.BlocksNode parsed = (GAMFormat.BlocksNode) GAMFormat
						.parseGAM(gamCode, baseUri);
				if (parsed.getChildren().size() > 0
						&& parsed.getChildren().get(0).getUri()
								.equals("$options"))
					for (ParseNode pn : parsed.getChildren().get(0)
							.getChildren())
						if ("module".equals(pn.get("name"))
								|| "parent".equals(pn.get("name")))
							related.add(baseUri.resolve(pn.get("value") + "/"
									+ fileName));
				for (Concept concept : GAMFormat
						.readGAM(parsed, baseUri, cache))
					result.add(concept);
			} catch (Exception e) {
				throw new IllegalArgumentException(
						"unable to parse .gam file '" + definitionURL + "'", e);
			}

			// update the cache
			fileMap.put(definitionURL.toString(), con.getLastModified());
			ImmutableSet.Builder<String> setBuilder = ImmutableSet.builder();
			for (Concept concept : result)
				setBuilder.add(concept.getUriString());
			definitionMap.put(definitionURL.toString(), setBuilder.build());

			// return the result
			return result;
		} catch (IOException e) {
			throw new IllegalArgumentException(
					"unable to load .gam definition from '" + definitionURL
							+ "'", e);
		}
	}

	private String findEncoding(String contentType) {
		String result = "ISO-8859-1";
		if (contentType != null)
			for (String part : contentType.split(";"))
				if (part.trim().toLowerCase().startsWith("charset"))
					result = part.split("=")[1].toUpperCase();
		return result;
	}

	private UpdateListener updateListener = null;

	public void setUpdateListener(UpdateListener updateListener) {
		this.updateListener = updateListener;
	}

	private void updateDefinitions() {
		try {
			if (updateListener == null)
				return;
			List<URL> todo = new LinkedList<URL>();
			for (Map.Entry<String, Long> entry : fileMap.entrySet()) {
				URL url = new URL(entry.getKey());
				long modified = url.openConnection().getLastModified();
				if (entry.getValue().longValue() != modified) {
					todo.add(url);
					fileMap.put(entry.getKey(), modified);
				}
			}
			ImmutableList.Builder<Concept> builder = ImmutableList.builder();
			for (URL url : todo)
				try {
					builder.addAll(loadDefinition(url));
				} catch (Exception e) {
					e.printStackTrace();
				}
			List<Concept> result = builder.build();
			if (result.size() > 0)
				updateListener.update(builder.build());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static final String nullURL = "http://gale.tue.nl/#null";

	private final Map<String, String> conceptMap = new MapMaker()
			.expireAfterWrite(cacheTimeout, TimeUnit.SECONDS).maximumSize(500)
			.makeMap();

	private final Map<String, ImmutableSet<String>> definitionMap = new MapMaker()
			.expireAfterWrite(cacheTimeout, TimeUnit.SECONDS).maximumSize(50)
			.makeMap();

	private final Map<String, Boolean> notExistsMap = new MapMaker()
			.expireAfterWrite(cacheTimeout, TimeUnit.SECONDS).maximumSize(500)
			.makeMap();

	private final Map<String, Long> fileMap = new MapMaker().makeMap();

	public void invalidate() {
		conceptMap.clear();
		definitionMap.clear();
		notExistsMap.clear();
		fileMap.clear();
	}
}
