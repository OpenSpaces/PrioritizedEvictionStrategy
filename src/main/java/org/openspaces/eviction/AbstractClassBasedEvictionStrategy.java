/*******************************************************************************
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All
 rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package org.openspaces.eviction;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.gigaspaces.server.eviction.EvictableServerEntry;
import com.gigaspaces.server.eviction.SpaceEvictionManager;
import com.gigaspaces.server.eviction.SpaceEvictionStrategy;
import com.gigaspaces.server.eviction.SpaceEvictionStrategyConfig;

/**
 * this class contains some useful methods that are common
 * for different the strategies
 * 
 * @author Sagi Bernstein
 * @since 9.1.0
 */
public abstract class AbstractClassBasedEvictionStrategy extends SpaceEvictionStrategy {
	final protected AtomicLong amountInSpace;
	protected static final Logger logger = Logger.getLogger(com.gigaspaces.logger.Constants.LOGGER_CACHE);

	public AbstractClassBasedEvictionStrategy(){
		super();
		this.amountInSpace = new AtomicLong(0);
	}
	
	public void initialize(SpaceEvictionManager evictionManager, SpaceEvictionStrategyConfig config){
		super.initialize(evictionManager, config);
		if(logger.isLoggable(Level.CONFIG))
			logger.config("started custom eviction strategy " + this.getClass().getSimpleName());
	}
	
	protected Priority getPriority(EvictableServerEntry entry) {
		int priority = entry.getSpaceTypeDescriptor().getObjectClass()
				.getAnnotation(SpaceEvictionPriority.class).priority();
		if(priority < 0)
				throw new IllegalArgumentException("priority values should be equal or greater than 0");
		return new Priority(priority);
	}

	protected OrderBy getOrderBy(EvictableServerEntry entry) {
		return entry.getSpaceTypeDescriptor().getObjectClass()
				.getAnnotation(SpaceEvictionPriority.class).orderBy();
	}

	public AtomicLong getAmountInSpace() {
		return amountInSpace;
	}
	
}
