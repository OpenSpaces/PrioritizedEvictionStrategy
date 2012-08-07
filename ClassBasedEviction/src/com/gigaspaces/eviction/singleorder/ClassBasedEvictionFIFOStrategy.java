package com.gigaspaces.eviction.singleorder;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.gigaspaces.eviction.AbstractClassBasedEvictionStrategy;
import com.gigaspaces.server.eviction.EvictableServerEntry;
import com.gigaspaces.server.eviction.SpaceCacheInteractor;


public class ClassBasedEvictionFIFOStrategy extends AbstractClassBasedEvictionStrategy {
	private List<ConcurrentLinkedQueue<EvictableServerEntry>> priorities;

	public void init(SpaceCacheInteractor spaceCacheInteractor, Properties spaceProperties){
		priorities = new ArrayList<ConcurrentLinkedQueue<EvictableServerEntry>>();
		for (int i = 0; i < PRIORITIES_SIZE; i++) {
			priorities.add(new ConcurrentLinkedQueue<EvictableServerEntry>());
		}
	}

	public void onInsert(EvictableServerEntry entry){
		//keep track of number of objects in space
		getAmountInSpace().incrementAndGet();

		getPriorities().get(getPriority(entry)).add(entry);

		//explicitly evict when there are more objects in space the the cache size
		int diff = getAmountInSpace().intValue() - getCacheSize();
		if(diff > 0)
			evict(diff);
	}


	public void onLoad(EvictableServerEntry entry){ 
		onInsert(entry);
	}


	public void remove(EvictableServerEntry entry){
		getPriorities().get(getPriority(entry)).remove(entry);

		//keep track of number of objects in space
		getAmountInSpace().decrementAndGet();
	}


	public int evict(int evictionQuota){ 
		int counter = 0;

		//priority with a lower value should be removed later
		for(int i = getPriorities().size() - 1; i >= 0  && (counter < evictionQuota); i--) {
			while(counter < evictionQuota)
				if(getSpaceCacheInteractor().grantEvictionPermissionAndRemove(getPriorities().get(i).peek()))
					counter++;
		}	
		return counter;

	}


	List<ConcurrentLinkedQueue<EvictableServerEntry>> getPriorities() {
		return priorities;
	}

}
