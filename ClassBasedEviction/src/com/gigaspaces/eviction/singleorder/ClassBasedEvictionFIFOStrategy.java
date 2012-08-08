package com.gigaspaces.eviction.singleorder;

import java.util.Properties;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

import com.gigaspaces.eviction.AbstractClassBasedEvictionStrategy;
import com.gigaspaces.server.eviction.EvictableServerEntry;
import com.gigaspaces.server.eviction.SpaceCacheInteractor;


public class ClassBasedEvictionFIFOStrategy extends AbstractClassBasedEvictionStrategy {
	private ConcurrentSkipListMap<Priority, ConcurrentSkipListMap<Long, EvictableServerEntry>> priorities;
	private AtomicLong index; 

	public void init(SpaceCacheInteractor spaceCacheInteractor, Properties spaceProperties){
		priorities = new ConcurrentSkipListMap<Priority, ConcurrentSkipListMap<Long, EvictableServerEntry>>();
		index = new AtomicLong(0);
	}

	public void onInsert(EvictableServerEntry entry){
		//keep track of number of objects in space
		getAmountInSpace().incrementAndGet();

		Long key = getIndex().incrementAndGet();
		getPriorities().putIfAbsent(getPriority(entry),
				new ConcurrentSkipListMap<Long, EvictableServerEntry>());
		getPriorities().get(getPriority(entry)).put(key, entry);
		entry.setEvictionPayLoad(key);

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


	public int evict(int evictionQuota){ 
		int counter = 0;

		for(ConcurrentSkipListMap<Long, EvictableServerEntry> queue : getPriorities().values()){
			if(counter == evictionQuota)
				break;
			if(queue.isEmpty())
				continue;
			if(getSpaceCacheInteractor().grantEvictionPermissionAndRemove(
					getPriorities().firstEntry().getValue().firstEntry().getValue()))
				counter++;
		}
		return counter;
	}


	ConcurrentSkipListMap<Priority, ConcurrentSkipListMap<Long, EvictableServerEntry>> getPriorities() {
		return priorities;
	}

	public AtomicLong getIndex() {
		return index;
	}

	public void setIndex(AtomicLong index) {
		this.index = index;
	}

}
