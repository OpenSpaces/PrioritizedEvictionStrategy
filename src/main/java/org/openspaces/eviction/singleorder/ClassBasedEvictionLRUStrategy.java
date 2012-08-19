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

package org.openspaces.eviction.singleorder;


import java.util.logging.Level;

import org.openspaces.eviction.IndexValue;

import com.gigaspaces.server.eviction.EvictableServerEntry;


/**
 * This strategy evicts objects from the space, first according to the priority
 * as indicated in the object's class and then by LRU of all objects with the indicated priority
 * 
 * @author Sagi Bernstein
 * @since 9.1.0
 */
public class ClassBasedEvictionLRUStrategy extends ClassBasedEvictionFIFOStrategy {
	
	public void touchOnRead(EvictableServerEntry entry){
		updateEntryIndex(entry);
	}


	public void touchOnModify(EvictableServerEntry entry){
		updateEntryIndex(entry);
	}

	
	protected void updateEntryIndex(EvictableServerEntry entry) {
		if(getPriorities().get(getPriority(entry)).remove(entry.getEvictionPayLoad(), entry)){
			IndexValue key = getIndex().incrementAndGet();
			getPriorities().get(getPriority(entry)).put(key, entry);
			if(logger.isLoggable(Level.FINEST))
				logger.finest("updated entry with UID: " + entry.getUID() +
						" in prioirty " + getPriority(entry) + " with old key index: " +
						entry.getEvictionPayLoad() + " to new key index: " + key);
			entry.setEvictionPayLoad(key);
		}
	}



}
