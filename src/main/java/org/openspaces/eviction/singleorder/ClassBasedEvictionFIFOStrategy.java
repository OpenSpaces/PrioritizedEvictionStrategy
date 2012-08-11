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

import java.util.concurrent.ConcurrentSkipListMap;

import org.openspaces.eviction.IndexValue;

import com.gigaspaces.server.eviction.EvictableServerEntry;

/**
 * This strategy evicts objects from the space, first according to the priority
 * as indicated in the object's class and then by FIFO of all objects with the indicated priority
 * 
 * @author Sagi Bernstein
 * @since 9.1.0
 */
public class ClassBasedEvictionFIFOStrategy extends ClassBasedEvicitionStrategy {
	public void onInsert(EvictableServerEntry entry){
		//keep track of number of objects in space
		getAmountInSpace().incrementAndGet();

		IndexValue key = getIndex().incrementAndGet();
		getPriorities().putIfAbsent(getPriority(entry),
				new ConcurrentSkipListMap<IndexValue, EvictableServerEntry>());
		getPriorities().get(getPriority(entry)).put(key, entry);
		entry.setEvictionPayLoad(key);
		
		logger.finest("insterted entry with UID: " + entry.getUID() +
				" to prioirty " + getPriority(entry) + " and key index: " + key);
		//explicitly evict when there are more objects in space the the cache size
		int diff = getAmountInSpace().intValue() - getCacheSize();
		if(diff > 0)
			evict(diff);
	}


	public void onLoad(EvictableServerEntry entry){ 
		onInsert(entry);
	}


	public void remove(EvictableServerEntry entry){
		if(getPriorities().get(getPriority(entry)).remove(entry.getEvictionPayLoad()) == null)
			throw new RuntimeException("entry " + entry + "should be in the queue");
		//keep track of number of objects in space
		getAmountInSpace().decrementAndGet();
	}


}
