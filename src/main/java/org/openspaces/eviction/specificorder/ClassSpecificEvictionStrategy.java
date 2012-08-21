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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.logging.Level;

import org.openspaces.eviction.AbstractClassBasedEvictionStrategy;
import org.openspaces.eviction.OrderBy;
import org.openspaces.eviction.Priority;
import org.openspaces.eviction.SpaceEvictionPriority;

import com.gigaspaces.server.eviction.EvictableServerEntry;
import com.gigaspaces.server.eviction.SpaceEvictionManager;
import com.gigaspaces.server.eviction.SpaceEvictionStrategy;
import com.gigaspaces.server.eviction.SpaceEvictionStrategyConfig;

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
	ConcurrentSkipListMap<Priority, ConcurrentHashMap<Integer, SpaceEvictionStrategy>> priorities;
	
	@Override
	public void initialize(SpaceEvictionManager evictionManager, SpaceEvictionStrategyConfig config){
		super.initialize(evictionManager, config);
		priorities = new ConcurrentSkipListMap<Priority, ConcurrentHashMap<Integer, SpaceEvictionStrategy>>();
	}

	@Override
	public void onInsert(EvictableServerEntry entry){
		int classHash = getEntryClassHash(entry);
		Priority priority = getPriority(entry);

		//handle priority value is first inserted to space
		if(getPriorities().putIfAbsent(priority, new ConcurrentHashMap<Integer, SpaceEvictionStrategy>()) == null)
			if(logger.isLoggable(Level.FINER))
				logger.finer("opened new priority listing for priority: " + getPriority(entry));

		//handle class type is first inserted to space
		if(!getPriorities().get(priority).containsKey(classHash)){
			switch(getOrderBy(entry)){
			case FIFO:
				ClassSpecificEvictionFIFOStrategy fifoStrategy = new ClassSpecificEvictionFIFOStrategy(getEvictionManager(), getAmountInSpace());
				fifoStrategy.initialize(getEvictionManager(), getEvictionConfig());
				getPriorities().get(priority).putIfAbsent(classHash, fifoStrategy);
				if(logger.isLoggable(Level.FINER))
					logger.finer("created new FIFO strategy for class " + 
						entry.getSpaceTypeDescriptor().getObjectClass());
				break;
			case LRU:
				ClassSpecificEvictionLRUStrategy lruStrategy = new ClassSpecificEvictionLRUStrategy(getEvictionManager(), getAmountInSpace());
				lruStrategy.initialize(getEvictionManager(), getEvictionConfig());
				getPriorities().get(priority).putIfAbsent(classHash, lruStrategy);
					if(logger.isLoggable(Level.FINER))
						logger.finer("created new LRU strategy for class " + 
						entry.getSpaceTypeDescriptor().getObjectClass());
				break;
			case NONE:
				getPriorities().get(priority).putIfAbsent(
						classHash, new ClassSpecificEvictionNoneStrategy(getAmountInSpace()));
				if(logger.isLoggable(Level.FINER))
					logger.finer("created new NONE strategy for class " + 
						entry.getSpaceTypeDescriptor().getObjectClass());
			}
		}

			getSpecificStrategy(entry).onInsert(entry);
			
			//keep track of number of objects in space
			getAmountInSpace().incrementAndGet();
			
			int diff = getAmountInSpace().intValue() - getEvictionConfig().getMaxCacheSize();
			if(diff > 0)
				evict(diff);
	}

	@Override
	public void onLoad(EvictableServerEntry entry){ 
		getSpecificStrategy(entry).onLoad(entry);
	}

	@Override
	public void onRead(EvictableServerEntry entry){
		getSpecificStrategy(entry).onRead(entry);
	}

	@Override
	public void onUpdate(EvictableServerEntry entry){
		getSpecificStrategy(entry).onUpdate(entry);
	}

	@Override
	public void onRemove(EvictableServerEntry entry){
		getSpecificStrategy(entry).onRemove(entry);
	}


	@Override
	public int evict(int evictionQuota){ 
		int counter = 0;

		for(ConcurrentHashMap<Integer, SpaceEvictionStrategy> priorityLevel : getPriorities().values()){
			if(counter == evictionQuota)
				break;
			if(priorityLevel.isEmpty())
				continue;
			for (SpaceEvictionStrategy strategy : priorityLevel.values()) {
				counter += strategy.evict(evictionQuota - counter);
				if(counter == evictionQuota)
					break;
			}
		}
		return counter;
	}


	public ConcurrentSkipListMap<Priority, ConcurrentHashMap<Integer, SpaceEvictionStrategy>> getPriorities() {
		return priorities;
	}

	private int getEntryClassHash(EvictableServerEntry entry) {
		return entry.getSpaceTypeDescriptor().getObjectClass().hashCode();
	}
	
	protected SpaceEvictionStrategy getSpecificStrategy(EvictableServerEntry entry) {
		return getPriorities().get(getPriority(entry)).get(getEntryClassHash(entry));
	}

}
