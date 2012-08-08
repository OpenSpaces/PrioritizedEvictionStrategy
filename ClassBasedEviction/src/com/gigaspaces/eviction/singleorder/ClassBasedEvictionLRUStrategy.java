package com.gigaspaces.eviction.singleorder;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

import com.gigaspaces.eviction.AbstractClassBasedEvictionStrategy;
import com.gigaspaces.server.eviction.EvictableServerEntry;
import com.gigaspaces.server.eviction.SpaceCacheInteractor;


public class ClassBasedEvictionLRUStrategy extends AbstractClassBasedEvictionStrategy {
	List<ConcurrentSkipListMap<Long, EvictableServerEntry>> priorities;
	AtomicLong index;

	public void init(SpaceCacheInteractor spaceCacheInteractor, Properties spaceProperties){
		super.init(spaceCacheInteractor, spaceProperties);
		this.index = new AtomicLong(0);
		priorities = new ArrayList<ConcurrentSkipListMap<Long, EvictableServerEntry>>();
		for (int i = 0; i < PRIORITIES_SIZE; i++) {
			priorities.add(new ConcurrentSkipListMap<Long, EvictableServerEntry>());
		}
	}

	public void onInsert(EvictableServerEntry entry){
		//keep track of number of objects in space
		getAmountInSpace().incrementAndGet();

		put(entry);

		//explicitly evict when there are more objects in space the the cache size
		int diff = getAmountInSpace().intValue() - getCacheSize();
		if(diff > 0)
			evict(diff);
	}


	public void onLoad(EvictableServerEntry entry){ 
		onInsert(entry);
	}


	public void touchOnRead(EvictableServerEntry entry){
		put(entry);
	}


	public void touchOnModify(EvictableServerEntry entry){
		put(entry);
	}


	public void remove(EvictableServerEntry entry){
		getPriorities().get(getPriority(entry)).remove(entry.getEvictionPayLoad());

		//keep track of number of objects in space
		getAmountInSpace().decrementAndGet();
	}


	public int evict(int evictionQuota){ 
		int counter = 0;
		//priority with a lower value should be removed later
		for(int i = getPriorities().size() - 1; i >= 0  && (counter < evictionQuota); i--) {
			if(getSpaceCacheInteractor().grantEvictionPermissionAndRemove(
					getPriorities().get(i).pollFirstEntry().getValue()))
				counter++;
		}
		return counter;
	}


	public void close(){}

	protected void put(EvictableServerEntry entry) {
		Long key = getIndex().incrementAndGet();
		getPriorities().get(getPriority(entry)).remove(entry.getEvictionPayLoad(), entry);
		entry.setEvictionPayLoad(key);
		getPriorities().get(getPriority(entry)).put(key, entry);
	}

	public AtomicLong getIndex() {
		return index;
	}

	public List<ConcurrentSkipListMap<Long, EvictableServerEntry>> getPriorities() {
		return priorities;
	}

}
