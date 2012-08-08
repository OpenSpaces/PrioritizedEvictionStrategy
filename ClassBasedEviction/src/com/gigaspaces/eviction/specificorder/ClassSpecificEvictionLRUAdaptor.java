package com.gigaspaces.eviction.specificorder;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

import com.gigaspaces.server.eviction.EvictableServerEntry;
import com.gigaspaces.server.eviction.SpaceCacheInteractor;

public class ClassSpecificEvictionLRUAdaptor extends
ClassSpecificEvictionStrategyAdaptor {
	private SpaceCacheInteractor spaceCacheInteractor;
	private ConcurrentSkipListMap<Long, EvictableServerEntry> mapping;
	private AtomicLong index;

	public ClassSpecificEvictionLRUAdaptor(SpaceCacheInteractor spaceCacheInteractor) {
		this.spaceCacheInteractor = spaceCacheInteractor;
		this.mapping = new ConcurrentSkipListMap<Long, EvictableServerEntry>();
		this.index = new AtomicLong(0); 
	}

	@Override
	public void onInsert(EvictableServerEntry entry) {
		put(entry);
	}

	@Override
	public void onLoad(EvictableServerEntry entry){
		put(entry);
	}

	@Override
	public void touchOnRead(EvictableServerEntry entry){
		put(entry);
	}

	@Override
	public void touchOnModify(EvictableServerEntry entry){
		put(entry);	
	}

	@Override
	public void remove(EvictableServerEntry entry){
		getMapping().remove(entry.getEvictionPayLoad());
	}

	@Override
	public int  evict (int evictionQuota){ 
		int counter = 0;
		for(int i = 0; i < Math.min(getMapping().size(), evictionQuota) 
				&& counter < evictionQuota; i++)
			if(getSpaceCacheInteractor().grantEvictionPermissionAndRemove(
					getMapping().pollFirstEntry().getValue()))
				counter++;
		return counter;
	}
	
	protected void put(EvictableServerEntry entry) {
		Long key = getIndex().incrementAndGet();
		getMapping().remove(entry.getEvictionPayLoad(), entry);
		entry.setEvictionPayLoad(key);
		getMapping().put(key, entry);
	}

	public SpaceCacheInteractor getSpaceCacheInteractor() {
		return spaceCacheInteractor;
	}

	private ConcurrentSkipListMap<Long, EvictableServerEntry> getMapping() {
		return mapping;
	}

	public AtomicLong getIndex() {
		return index;
	}

}
