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

package org.openspaces.eviction.specificorder;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.gigaspaces.server.eviction.SpaceEvictionStrategy;

/**
 * This is an empty extension of the {@link EvictionStrategy} class
 * it is solely to make it possible to instantiate an empty implementation
 * this is needed in {@link ClassSpecificEvictionStrategy} to provide the none strategy
 * 
 * @author Sagi Bernstein
 * @since 9.1.0
 */
public class ClassSpecificEvictionNoneStrategy extends SpaceEvictionStrategy{
	protected static final Logger logger = Logger.getLogger(com.gigaspaces.logger.Constants.LOGGER_CACHE);
	
	public ClassSpecificEvictionNoneStrategy() {
		if(logger.isLoggable(Level.CONFIG))
			logger.config("instantiated new Class Specific None Strategy: " + this.hashCode());
	}
	

	@Override
	public int evict(int numOfEntries) {
		return 0;
	}

}
