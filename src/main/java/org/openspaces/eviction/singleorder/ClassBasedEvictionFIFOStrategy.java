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
 * as indicated in the object's class and then by FIFO of all objects with the indicated priority
 * 
 * @author Sagi Bernstein
 * @since 9.1.0
 */
public class ClassBasedEvictionFIFOStrategy extends AbstractClassBasedEvictionStrategy {

	private ConcurrentSkipListMap<Priority, ConcurrentSkipListMap<IndexValue, EvictableServerEntry>> priorities;
	private Index index;

	public ClassBasedEvictionFIFOStrategy() {
		super();
	}

	public void init(SpaceCacheInteractor spaceCacheInteractor, Properties spaceProperties) {
		super.init(spaceCacheInteractor, spaceProperties);
		priorities = new ConcurrentSkipListMap<Priority, ConcurrentSkipListMap<IndexValue, EvictableServerEntry>>();
		index = new Index();
	}
	
	public void onInsert(EvictableServerEntry entry) {
		//keep track of number of objects in space
		getAmountInSpace().incrementAndGet();
	
		//handle new priority value in space
		if(getPriorities().putIfAbsent(getPriority(entry), new ConcurrentSkipListMap<IndexValue, EvictableServerEntry>())
				!= null)
			logger.finest("opened new priority listing for priority: " + getPriority(entry));
	
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

	public void onLoad(EvictableServerEntry entry) { 
		logger.finest("loading entry with UID: " + entry.getUID() + " calling onInsert");
		onInsert(entry);
	}

	public void remove(EvictableServerEntry entry) {
		if(getPriorities().get(getPriority(entry)).remove(entry.getEvictionPayLoad()) == null)
			throw new RuntimeException("entry " + entry + "should be in the queue");
		logger.finest("removed entry with UID: " + entry.getUID() +
				", prioirty " + getPriority(entry) + " and key index: " + entry.getEvictionPayLoad());
		//keep track of number of objects in space
		getAmountInSpace().decrementAndGet();
	}

	public int evict(int evictionQuota) { 
		int counter = 0;
	
		for(ConcurrentSkipListMap<IndexValue, EvictableServerEntry> priority : getPriorities().values())
			if(counter == evictionQuota)
				break;
			else if(priority.isEmpty())
				continue;
			else {
				Iterator<EvictableServerEntry> iterator = priority.values().iterator();
				while(iterator.hasNext() && counter < evictionQuota){
					EvictableServerEntry next = iterator.next();
					logger.finest("trying to evict entry with UID: " + next.getUID() +
							", prioirty " + getPriority(next) + " and key index: " + next.getEvictionPayLoad());
					if(getSpaceCacheInteractor().grantEvictionPermissionAndRemove(next)){
						counter++;
					}
				}
	
			}
		logger.finest("got request to evict " + evictionQuota + " entries, evicted " + counter);
		return counter;
	}

	protected ConcurrentSkipListMap<Priority, ConcurrentSkipListMap<IndexValue, EvictableServerEntry>> getPriorities() {
		return priorities;
	}

	public Index getIndex() {
		return index;
	}

}