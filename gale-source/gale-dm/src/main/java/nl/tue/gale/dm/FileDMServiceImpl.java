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
 * FileDMServiceImpl.java
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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import nl.tue.gale.common.uri.URI;
import nl.tue.gale.common.uri.URIs;
import nl.tue.gale.dm.data.Concept;
import nl.tue.gale.event.AbstractEventListener;
import nl.tue.gale.event.EventHash;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.MapMaker;

public abstract class FileDMServiceImpl extends AbstractEventListener {
	private static final ScheduledExecutorService executor = Executors
			.newSingleThreadScheduledExecutor();
	private final Map<URI, Concept> cache = new MapMaker().makeMap();
	private final Map<String, Long> timeMap = new HashMap<String, Long>();

	@Override
	protected void init() {
		doCheck();
		executor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				doCheck();
			}
		}, 30, 30, TimeUnit.SECONDS);
	}

	private void doCheck() {
		for (File file : getFiles()) {
			Long oldTime = timeMap.get(file.getAbsolutePath());
			Long curTime = file.lastModified();
			if (!curTime.equals(oldTime)) {
				doFile(file);
				timeMap.put(file.getAbsolutePath(), curTime);
			}
		}
	}

	public void destroy() {
		executor.shutdownNow();
	}

	@Override
	protected String getMethods() {
		return "getdm;ccdm";

	}

	public List<String> event_getdm(List<String> params) {
		try {
			EventHash eh = new EventHash(params.get(0));
			if (!eh.getName().equals("uri"))
				throw new IllegalArgumentException("first argument is no uri");
			URI uri = URIs.of(eh.getItems().get(0));
			Concept concept = cache.get(uri);
			if (concept == null)
				return ImmutableList.of("result:ok");
			List<String> result = new ArrayList<String>();
			result.add("result:ok");
			result.addAll(Concept.toEvent(concept));
			return result;
		} catch (Exception e) {
			return error(e);
		}
	}

	public List<String> event_ccdm(List<String> params) {
		cache.clear();
		timeMap.clear();
		doCheck();
		return ImmutableList.of("result:ok");
	}

	protected void addConcepts(List<Concept> concepts) {
		List<String> events = new LinkedList<String>();
		for (Concept concept : concepts) {
			cache.put(concept.getUri(), concept);
			events.addAll(Concept.toEvent(concept));
		}
		final List<String> event = events;
		executor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					getEventBus().event("updatedm", event);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
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

	protected abstract List<File> getFiles();

	protected abstract void doFile(File file);
}