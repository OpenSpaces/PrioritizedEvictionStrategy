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

public abstract class ClassBasedEvicitionStrategy extends
		AbstractClassBasedEvictionStrategy {

	private ConcurrentSkipListMap<Priority, ConcurrentSkipListMap<IndexValue, EvictableServerEntry>> priorities;
	private Index index;

	public ClassBasedEvicitionStrategy() {
		super();
	}

	public void init(SpaceCacheInteractor spaceCacheInteractor, Properties spaceProperties) {
		super.init(spaceCacheInteractor, spaceProperties);
		priorities = new ConcurrentSkipListMap<Priority, ConcurrentSkipListMap<IndexValue, EvictableServerEntry>>();
		index = new Index();
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
					if(getSpaceCacheInteractor().grantEvictionPermissionAndRemove(
							iterator.next())){
						counter++;
					}
				}
	
			}
		return counter;
	}

	protected ConcurrentSkipListMap<Priority, ConcurrentSkipListMap<IndexValue, EvictableServerEntry>> getPriorities() {
		return priorities;
	}

	public Index getIndex() {
		return index;
	}

}