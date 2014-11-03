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
 * GaleConfig.java
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
package nl.tue.gale.ae;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;

import nl.tue.gale.common.GaleUtil;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;

import com.google.common.collect.ImmutableSet;

public class GaleConfig extends HashMap<String, Object> implements
		ApplicationContextAware {
	private static final long serialVersionUID = -7575701106819625712L;

	private File file = null;
	private ServletContext sc = null;
	private ApplicationContext ac = null;
	private String rootGaleUrl = null;
	private Set<String> safeDomains = null;

	public String getRootGaleUrl() {
		return rootGaleUrl;
	}

	public void setRootGaleUrl(String rootGaleUrl) {
		try {
			new URL(rootGaleUrl);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("unable to set rootGaleUrl: "
					+ e.getMessage(), e);
		}
		this.rootGaleUrl = rootGaleUrl;
		GaleUtil.setProperty("rootGaleUrl", rootGaleUrl);
	}

	public void setMimeTable(Map<String, String> table) {
		GaleUtil.setMimeTable(table);
	}

	public void setSafeDomains(Map<String, String> table) {
		safeDomains = ImmutableSet.copyOf(table.keySet());
		put("safeDomains", safeDomains);
	}

	public boolean isSafeDomain(String domain) {
		return safeDomains.contains(domain.toLowerCase().trim());
	}

	protected void initConfig() {
		String home = System.getenv("GALE_HOME");
		if (home == null)
			home = sc.getRealPath("/");
		if (home == null)
			home = "/usr/work/aha";
		file = new File(home);
		if (!file.exists())
			file.mkdir();
		put("homeDir", file);
		put("libDir", sc.getRealPath("/WEB-INF"));
	}

	public File getHomeDir() {
		return file;
	}

	public void setOpenCorpus(boolean open) {
		GaleUtil.setOpenCorpus(open);
	}

	public void setUseGEB(boolean geb) {
		GaleUtil.setProperty("useGEB", (geb ? "true" : "false"));
	}

	@Override
	public void setApplicationContext(ApplicationContext ac)
			throws BeansException {
		this.sc = ((WebApplicationContext) ac).getServletContext();
		this.ac = ac;
	}

	private final ScheduledExecutorService executor = Executors
			.newSingleThreadScheduledExecutor();
	private ScheduledFuture<?> future = null;

	public void setAutoInit(final int seconds) {
		future = executor.schedule(new Runnable() {
			@Override
			public void run() {
				doAutoInit();
			}
		}, seconds, TimeUnit.SECONDS);
	}

	private void doAutoInit() {
		try {
			((EventBusClient) ac.getBean("eventBusClient")).event("initall",
					new LinkedList<String>());
			if ("true".equals(GaleUtil.getProperty("useGEB"))) {
				Object geb = ac.getBean("gebManager");
				Method initmethod = geb.getClass().getDeclaredMethod("init");
				initmethod.setAccessible(true);
				initmethod.invoke(geb);
			}
		} catch (Exception e) {
			System.out.println("auto init failed: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void destroy() {
		if (future != null)
			future.cancel(true);
		executor.shutdownNow();
	}
}
