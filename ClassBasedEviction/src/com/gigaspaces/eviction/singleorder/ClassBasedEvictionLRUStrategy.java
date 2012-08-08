package com.gigaspaces.eviction.singleorder;

import java.util.Properties;
import java.util.concurrent.ConcurrentSkipListMap;

import com.gigaspaces.eviction.AbstractClassBasedEvictionStrategy;
import com.gigaspaces.eviction.Index;
import com.gigaspaces.eviction.IndexValue;
import com.gigaspaces.server.eviction.EvictableServerEntry;
import com.gigaspaces.server.eviction.SpaceCacheInteractor;


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

		getPriorities().putIfAbsent(getPriority(entry), new ConcurrentSkipListMap<IndexValue, EvictableServerEntry>());

		IndexValue key = getIndex().incrementAndGet();
		entry.setEvictionPayLoad(key);
		getPriorities().get(getPriority(entry)).put(key, entry);

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
				else if(getSpaceCacheInteractor().grantEvictionPermissionAndRemove(
						map.firstEntry().getValue()))
						counter++;
		return counter;
	}


	public Index getIndex() {
		return index;
	}

	public ConcurrentSkipListMap<Priority, ConcurrentSkipListMap<IndexValue, EvictableServerEntry>> getPriorities() {
		return priorities;
	}

}
