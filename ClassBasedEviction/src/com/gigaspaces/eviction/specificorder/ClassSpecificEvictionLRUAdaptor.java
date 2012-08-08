package com.gigaspaces.eviction.specificorder;

import java.util.concurrent.ConcurrentSkipListMap;

import com.gigaspaces.eviction.Index;
import com.gigaspaces.eviction.IndexValue;
import com.gigaspaces.server.eviction.EvictableServerEntry;
import com.gigaspaces.server.eviction.SpaceCacheInteractor;

public class ClassSpecificEvictionLRUAdaptor extends ClassSpecificEvictionStrategyAdaptor {
	private SpaceCacheInteractor spaceCacheInteractor;
	private ConcurrentSkipListMap<IndexValue, EvictableServerEntry> mapping;
	private Index index;

	public ClassSpecificEvictionLRUAdaptor(SpaceCacheInteractor spaceCacheInteractor) {
		this.spaceCacheInteractor = spaceCacheInteractor;
		this.mapping = new ConcurrentSkipListMap<IndexValue, EvictableServerEntry>();
		this.index = new Index(); 
	}

	@Override
	public void onInsert(EvictableServerEntry entry) {
		IndexValue key = getIndex().incrementAndGet();
		entry.setEvictionPayLoad(key);
		getMapping().put(key, entry);
	}

	@Override
	public void onLoad(EvictableServerEntry entry){
		onInsert(entry);
	}

	@Override
	public void touchOnRead(EvictableServerEntry entry){
		if(getMapping().remove(entry.getEvictionPayLoad(), entry)){
			IndexValue key = getIndex().incrementAndGet();
			getMapping().put(key, entry);
			entry.setEvictionPayLoad(key);
		}
	}

	@Override
	public void touchOnModify(EvictableServerEntry entry){
		touchOnRead(entry);	
	}

	@Override
	public void remove(EvictableServerEntry entry){
		if(getMapping().remove(entry.getEvictionPayLoad()) == null)
			throw new RuntimeException("entry " + entry + "should be in the map");
	}

	@Override
	public int  evict (int evictionQuota){ 
		int counter = 0;
		int mappingsSize = getMapping().size();
		for(int i = 0; i < Math.min(mappingsSize, evictionQuota) 
				&& counter < evictionQuota; i++)
			if(getSpaceCacheInteractor().grantEvictionPermissionAndRemove(
					getMapping().pollFirstEntry().getValue()))
				counter++;
		return counter;
	}
	
	public SpaceCacheInteractor getSpaceCacheInteractor() {
		return spaceCacheInteractor;
	}

	private ConcurrentSkipListMap<IndexValue, EvictableServerEntry> getMapping() {
		return mapping;
	}

	public Index getIndex() {
		return index;
	}

}
