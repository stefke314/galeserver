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
 * OpenCorpusServiceImpl.java
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.cache.Cache;
import nl.tue.gale.common.cache.Caches;
import nl.tue.gale.common.parser.ParseNode;
import nl.tue.gale.common.uri.URIs;
import nl.tue.gale.dm.data.Concept;
import nl.tue.gale.event.AbstractEventListener;
import nl.tue.gale.event.EventHash;

import com.google.common.collect.ImmutableList;

public class OpenCorpusServiceImpl extends AbstractEventListener {
	private static final String methods = "getdm;ccdm";
	private File galeHome = null;
	private Cache<Concept> dm = Caches.newCache(10);

	public void setGaleConfig(Map<String, Object> galeConfig) {
		this.galeHome = (File) galeConfig.get("homeDir");
	}

	protected String getMethods() {
		return methods;
	}

	public List<String> event_ccdm(List<String> params) {
		dm.invalidate();
		return ImmutableList.of("result:ok");
	}

	public List<String> event_getdm(List<String> params) {
		try {
			List<String> result = new LinkedList<String>();
			result.add("result:ok");
			EventHash eh = new EventHash(params.get(0));
			if (!eh.getName().equals("uri"))
				throw new IllegalArgumentException("first argument is no uri");
			if (!GaleUtil.useOpenCorpus())
				return result;

			boolean done;
			ParseNode gamNode = null;
			String reqUriString = eh.getItems().get(0);
			String uriString = reqUriString;
			do {
				InputStream is = null;
				try {
					URL url = GaleUtil.generateURL(uriString, galeHome);
					is = url.openConnection().getInputStream();
				} catch (Exception e) {
					return result;
				}

				String gamCode = (uriString.toLowerCase().endsWith(".gam") ? readGAM(is)
						: readBuffer(is));
				if (gamCode == null)
					return result;
				gamCode = replaceEscape(gamCode);
				if (gamCode.startsWith("redirect:")) {
					String redirect = gamCode.substring(9);
					uriString = URIs.of(uriString).resolve(redirect).toString();
					done = false;
				} else {
					gamNode = GAMFormat.parseGAM(gamCode);
					done = true;
				}
			} while (!done);
			List<Concept> concepts = GAMFormat.readGAM(
					(GAMFormat.BlocksNode) gamNode, URIs.of(uriString), dm);
			Concept concept = null;
			for (Concept c : concepts)
				if (c.getUriString().equals(reqUriString))
					concept = c;
			if (concept == null)
				return result;
			result.addAll(Concept.toEvent(concept));

			return result;
		} catch (Exception e) {
			return error(e);
		}
	}

	private String readGAM(InputStream is) {
		try {
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(is));
			String s;
			while ((s = reader.readLine()) != null) {
				builder.append(s);
				builder.append("\n");
			}
			return builder.toString();
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"unable to read from InputStream", e);
		}
	}

	private String replaceEscape(String gamCode) {
		gamCode = gamCode.replace("&lt;", "<");
		gamCode = gamCode.replace("&gt;", ">");
		gamCode = gamCode.replace("&quot;", "\"");
		gamCode = gamCode.replace("&amp;", "&");
		gamCode = gamCode.replace("&apos;", "'");
		return gamCode;
	}

	private String readBuffer(InputStream is) throws IOException {
		byte[] buf = new byte[512];
		int cur = 0;
		int read = 0;
		int pos = -1;
		while (((read = is.read(buf, cur, 512 - cur)) >= 0) && (pos == -1)) {
			cur += read;
			if (cur == 512) {
				pos = parseBuffer(buf, 512);
				if (pos == -1) {
					for (int i = 0; i < 40; i++)
						buf[i] = buf[472 + i];
					cur = 40;
				}
			}
		}
		if (pos == -1)
			pos = parseBuffer(buf, cur);
		if (pos < 0)
			return null;
		StringBuilder sb = new StringBuilder();
		pos += meta.length;
		// buf = Arrays.copyOfRange(buf, pos + 1, cur);
		sb.append(new String(buf, pos + 1, cur - pos - 1));
		if (read >= 0) {
			buf = new byte[512];
			cur = 0;
			read = 0;
			while ((read = is.read(buf, cur, 512 - cur)) >= 0) {
				cur += read;
				if (cur == 512) {
					sb.append(new String(buf));
					cur = 0;
					if (sb.indexOf("\"") != sb.lastIndexOf("\""))
						break;
				}
			}
			if (cur != 0) {
				System.arraycopy(buf, 0, buf, 0, cur);
				// buf = Arrays.copyOfRange(buf, 0, cur);
				sb.append(new String(buf));
			}
		}
		try {
			return sb.substring(sb.indexOf("\"") + 1,
					sb.indexOf("\"", sb.indexOf("\"") + 1));
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"unable to properly read gale.dm metadata", e);
		}
	}

	private static final byte[] meta = "<meta name=\"gale.dm\"".getBytes();
	private static final byte[] body = "<body".getBytes();

	private int parseBuffer(byte[] buf, int size) {
		int metaCount = 0;
		int bodyCount = 0;
		int cur = 0;
		while (cur < size) {
			if (buf[cur] == meta[metaCount])
				metaCount++;
			else
				metaCount = 0;
			if (metaCount == meta.length)
				return cur - meta.length;
			if (buf[cur] == body[bodyCount])
				bodyCount++;
			else
				bodyCount = 0;
			if (bodyCount == body.length)
				return -2;
			cur++;
		}
		return -1;
	}

	protected void init() {
	}

	@Override
	public List<String> event(String method, List<String> params) {
		List<String> result = super.event(method, params);
		if (result != null)
			return result;
		try {
			if ("getdm".equals(method))
				return event_getdm(params);
			if ("ccdm".equals(method))
				return event_ccdm(params);
		} catch (Exception e) {
			return error(e);
		}
		throw new UnsupportedOperationException("'" + method
				+ "' method not supported");
	}
}