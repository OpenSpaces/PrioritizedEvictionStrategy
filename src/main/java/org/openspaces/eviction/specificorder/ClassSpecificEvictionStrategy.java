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

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.openspaces.eviction.AbstractClassBasedEvictionStrategy;
import org.openspaces.eviction.OrderBy;
import org.openspaces.eviction.Priority;
import org.openspaces.eviction.SpaceEvictionPriority;

import com.gigaspaces.server.eviction.EvictableServerEntry;
import com.gigaspaces.server.eviction.EvictionStrategy;
import com.gigaspaces.server.eviction.SpaceCacheInteractor;

/**
 * This class enables a class specific eviction mechanism
 * this means eviction is first by the priority indicated by the class,
 * if there are several classes with the same priority classes will be picked by
 * the hash code number of their class object.
 * After a class was picked for eviction the eviction strategy to be used is according
 * to what has been indicated in the classes' {@link SpaceEvictionPriority} {@link OrderBy} property 
 * 
 * @author Sagi Bernstein
 * @since 9.1.0
 */
public class ClassSpecificEvictionStrategy extends AbstractClassBasedEvictionStrategy{
	public static final int MAX_THREADS = 100;
	ConcurrentSkipListMap<Priority, ConcurrentHashMap<Integer, EvictionStrategy>> priorities;

	public void init(SpaceCacheInteractor spaceCacheInteractor, Properties spaceProperties){
		super.init(spaceCacheInteractor, spaceProperties);
		priorities = new ConcurrentSkipListMap<Priority, ConcurrentHashMap<Integer, EvictionStrategy>>();
	}

	public void onInsert(EvictableServerEntry entry)
	{
		int classHash = getEntryClassHash(entry);
		Priority priority = getPriority(entry);

		//handle class is first inserted to space
		getPriorities().putIfAbsent(priority, new ConcurrentHashMap<Integer, EvictionStrategy>());

		if(!getPriorities().get(priority).containsKey(classHash)){
			if(!getPriorities().get(priority).containsKey(classHash)){
				switch(getOrderBy(entry)){
				case FIFO:
					getPriorities().get(priority).putIfAbsent(
							classHash, new ClassSpecificEvictionFIFOStrategy(getSpaceCacheInteractor()));
					break;
				case LRU:
					getPriorities().get(priority).putIfAbsent(
							classHash, new ClassSpecificEvictionLRUStrategy(getSpaceCacheInteractor()));
					break;
				case NONE:
					getPriorities().get(priority).putIfAbsent(
							classHash, new ClassSpecificEvictionNoneStrategy());
				}
			}

			getSpecificStrategy(entry).onInsert(entry);
		}
	}

	protected EvictionStrategy getSpecificStrategy(EvictableServerEntry entry) {
		return getPriorities().get(getPriority(entry)).get(getEntryClassHash(entry));
	}


	public void onLoad(EvictableServerEntry entry){ 
		getSpecificStrategy(entry).onLoad(entry);
	}

	public void touchOnRead(EvictableServerEntry entry){
		getSpecificStrategy(entry).touchOnRead(entry);
	}

	public void touchOnModify(EvictableServerEntry entry){
		getSpecificStrategy(entry).touchOnModify(entry);
	}

	public void remove(EvictableServerEntry entry){
		getSpecificStrategy(entry).remove(entry);
	}

	public int  evict (int evictionQuota){ 
		int counter = 0;

		for(ConcurrentHashMap<Integer, EvictionStrategy> priorityLevel : getPriorities().values()){
			if(counter == evictionQuota)
				break;
			if(priorityLevel.isEmpty())
				continue;
			for (EvictionStrategy strategy : priorityLevel.values()) {
				counter += strategy.evict(evictionQuota - counter);
				if(counter == evictionQuota)
					break;
			}
		}
		return counter;
	}


	public ConcurrentSkipListMap<Priority, ConcurrentHashMap<Integer, EvictionStrategy>> getPriorities() {
		return priorities;
	}

	private int getEntryClassHash(EvictableServerEntry entry) {
		return entry.getSpaceTypeDescriptor().getObjectClass().hashCode();
	}

}
