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

import org.openspaces.eviction.IndexValue;

import com.gigaspaces.server.eviction.EvictableServerEntry;
import com.gigaspaces.server.eviction.SpaceEvictionManager;

/**
 * This is an extension of the {@link ClassSpecificEvictionFIFOStrategy} class
 * it provides a class specific LRU mechanism
 * to be used with {@link ClassSpecificEvictionStrategy} 
 * 
 * @author Sagi Bernstein
 * @since 9.1.0
 */
public class ClassSpecificEvictionLRUStrategy extends ClassSpecificEvictionFIFOStrategy{

	public ClassSpecificEvictionLRUStrategy(SpaceEvictionManager evictionManager) {
		super(evictionManager);
		if(logger.isLoggable(Level.CONFIG))
			logger.config("instantiated new Class Specific Strategy: " + this.getClass().getName() + " " + this.hashCode());
	}


	@Override
	public void onRead(EvictableServerEntry entry){
		super.onRead(entry);
		updateEntryIndex(entry);
	}


	@Override
	public void onUpdate(EvictableServerEntry entry){
		super.onUpdate(entry);
		updateEntryIndex(entry);
	}

	
	protected void updateEntryIndex(EvictableServerEntry entry) {
		if(getQueue().remove(entry.getEvictionPayLoad(), entry)){
			IndexValue key = getIndex().incrementAndGet();
			getQueue().put(key, entry);
			entry.setEvictionPayLoad(key);
			if(logger.isLoggable(Level.FINEST))
				logger.finest("updated entry with UID: " + entry.getUID() +
						" in class " + entry.getSpaceTypeDescriptor().getClass() + " with old key index: " +
						entry.getEvictionPayLoad() + " to new key index: " + key);
		}
	}


	

	
	
}
