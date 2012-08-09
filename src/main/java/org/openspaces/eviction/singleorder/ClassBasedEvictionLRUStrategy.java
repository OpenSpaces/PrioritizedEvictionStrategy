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

import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.ConcurrentSkipListMap;

import org.openspaces.eviction.AbstractClassBasedEvictionStrategy;
import org.openspaces.eviction.Index;
import org.openspaces.eviction.IndexValue;
import org.openspaces.eviction.Priority;

import com.gigaspaces.server.eviction.EvictableServerEntry;
import com.gigaspaces.server.eviction.SpaceCacheInteractor;


/**
 * This strategy evicts objects from the space, first according to the priority
 * as indicated in the object's class and then by LRU of all objects with the indicated priority
 * 
 * @author Sagi Bernstein
 * @since 9.1.0
 */
public class ClassBasedEvictionLRUStrategy extends AbstractClassBasedEvictionStrategy {
	ConcurrentSkipListMap<Priority, ConcurrentSkipListMap<IndexValue, EvictableServerEntry>> priorities;
	Index index;

	public void init(SpaceCacheInteractor spaceCacheInteractor, Properties spaceProperties){
		super.init(spaceCacheInteractor, spaceProperties);
		this.index = new Index();
		priorities = new ConcurrentSkipListMap<Priority, ConcurrentSkipListMap<IndexValue, EvictableServerEntry>>();
	}

	public void onInsert(EvictableServerEntry entry){
		//keep track of number of objects in space
		getAmountInSpace().incrementAndGet();

		//handle new priority value in space
		getPriorities().putIfAbsent(getPriority(entry), new ConcurrentSkipListMap<IndexValue, EvictableServerEntry>());

		IndexValue key = getIndex().incrementAndGet();
		entry.setEvictionPayLoad(key);
		getPriorities().get(getPriority(entry)).put(key, entry);

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


	public void touchOnRead(EvictableServerEntry entry){
		if(getPriorities().get(getPriority(entry)).remove(entry.getEvictionPayLoad(), entry)){
			IndexValue key = getIndex().incrementAndGet();
			getPriorities().get(getPriority(entry)).put(key, entry);
			entry.setEvictionPayLoad(key);
		}
	}


	public void touchOnModify(EvictableServerEntry entry){
		touchOnRead(entry);
	}


	public void remove(EvictableServerEntry entry){
		if(getPriorities().get(getPriority(entry)).remove(entry.getEvictionPayLoad()) == null)
			throw new RuntimeException("entry " + entry + "should be in the map");
		//keep track of number of objects in space
		getAmountInSpace().decrementAndGet();
	}


	public int evict(int evictionQuota){ 
		int counter = 0;

		for(ConcurrentSkipListMap<IndexValue, EvictableServerEntry> map : getPriorities().values())
			if(counter == evictionQuota)
				break;
			else if(map.isEmpty())
				continue;
			else {
				Iterator<EvictableServerEntry> iterator = map.values().iterator();
				while(iterator.hasNext() && counter < evictionQuota){
					if(getSpaceCacheInteractor().grantEvictionPermissionAndRemove(
							iterator.next())){
						counter++;
					}
				}

			}
		return counter;
	}


	public Index getIndex() {
		return index;
	}

	public ConcurrentSkipListMap<Priority, ConcurrentSkipListMap<IndexValue, EvictableServerEntry>> getPriorities() {
		return priorities;
	}

}
