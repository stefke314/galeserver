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
 * CacheOptimizer.java
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
package nl.tue.gale.event;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableList;

final class CacheOptimizer {
	private int minutesDelay = 10;
	private boolean initialized = false;
	private static final ScheduledExecutorService executor = Executors
			.newSingleThreadScheduledExecutor();
	private EventBusImpl eventBus = null;

	public int getMinutesDelay() {
		return minutesDelay;
	}

	public void setMinutesDelay(int delay) {
		if (initialized)
			throw new IllegalStateException(
					"cannot set delay after initialization");
		minutesDelay = delay;
	}

	public EventBusImpl getEventBus() {
		return eventBus;
	}

	public void setEventBus(EventBusImpl eventBus) {
		if (initialized)
			throw new IllegalStateException(
					"cannot set delay after initialization");
		this.eventBus = eventBus;
	}

	public void init() {
		if (eventBus == null)
			throw new IllegalStateException("eventBus not set");
		executor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				sendClearCache();
			}
		}, minutesDelay, minutesDelay, TimeUnit.MINUTES);
		initialized = true;
	}

	public void destroy() {
		executor.shutdownNow();
	}

	private void sendClearCache() {
		try {
			eventBus.event("ccdm", ImmutableList.<String> of());
			eventBus.event("ccum", ImmutableList.<String> of());
			eventBus.event("ccae", ImmutableList.<String> of());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
