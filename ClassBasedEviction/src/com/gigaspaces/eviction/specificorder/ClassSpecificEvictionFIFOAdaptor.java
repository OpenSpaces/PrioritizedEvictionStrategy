package com.gigaspaces.eviction.specificorder;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

import com.gigaspaces.server.eviction.EvictableServerEntry;
import com.gigaspaces.server.eviction.SpaceCacheInteractor;


public class ClassSpecificEvictionFIFOAdaptor extends ClassSpecificEvictionStrategyAdaptor {
	private SpaceCacheInteractor spaceCacheInteractor;
	private ConcurrentSkipListMap<Long, EvictableServerEntry> queue;
	private AtomicLong index; 
	
	public ClassSpecificEvictionFIFOAdaptor(SpaceCacheInteractor spaceCacheInteractor) {
		this.spaceCacheInteractor = spaceCacheInteractor;
		this.queue= new  ConcurrentSkipListMap<Long, EvictableServerEntry>();
		this.index = new AtomicLong(0);
	}
	
	@Override
	public void onInsert(EvictableServerEntry entry){
		long key = getIndex().incrementAndGet();
		entry.setEvictionPayLoad(key);
		getQueue().put(key, entry);
	}
	
	@Override
	public void onLoad(EvictableServerEntry entry){
		onInsert(entry);
	}
	
	@Override
	public void remove(EvictableServerEntry entry){
		if(getQueue().remove(entry.getEvictionPayLoad()) == null)
			throw new RuntimeException("entry " + entry + "should be in the queue");
	}
	
	@Override
	public int evict(int evictionQuota){ 
		int counter = 0;
		int queueSize = getQueue().size();
		
		for(int i = 0; i < Math.min(queueSize, evictionQuota - counter); i++)
			if(getSpaceCacheInteractor().grantEvictionPermissionAndRemove(
					getQueue().firstEntry().getValue()))
				counter++;
		return counter;
	}

	public SpaceCacheInteractor getSpaceCacheInteractor() {
		return spaceCacheInteractor;
	}

	public ConcurrentSkipListMap<Long, EvictableServerEntry> getQueue() {
		return queue;
	}

	public AtomicLong getIndex() {
		return index;
	}
}
