package com.gigaspaces.eviction.specificorder;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import com.gigaspaces.eviction.AbstractClassBasedEvictionStrategy;
import com.gigaspaces.eviction.singleorder.Priority;
import com.gigaspaces.server.eviction.EvictableServerEntry;
import com.gigaspaces.server.eviction.SpaceCacheInteractor;


public class ClassSpecificEvictionStrategy extends AbstractClassBasedEvictionStrategy{
	public static final int MAX_THREADS = 100;
	ConcurrentSkipListMap<Priority, ConcurrentHashMap<Integer, ClassSpecificEvictionStrategyAdaptor>> priorities;

	public void init(SpaceCacheInteractor spaceCacheInteractor, Properties spaceProperties){
		super.init(spaceCacheInteractor, spaceProperties);
		priorities = new ConcurrentSkipListMap<Priority, ConcurrentHashMap<Integer, ClassSpecificEvictionStrategyAdaptor>>();
	}

	public void onInsert(EvictableServerEntry entry)
	{
		int classHash = getEntryClassHash(entry);
		Priority priority = getPriority(entry);

		//handle class is first inserted to space
		getPriorities().putIfAbsent(priority, new ConcurrentHashMap<Integer, ClassSpecificEvictionStrategyAdaptor>());

		if(!getPriorities().get(priority).containsKey(classHash)){
			if(!getPriorities().get(priority).containsKey(classHash)){
				switch(getOrderBy(entry)){
				case FIFO:
					getPriorities().get(priority).putIfAbsent(
							classHash, new ClassSpecificEvictionFIFOAdaptor(getSpaceCacheInteractor()));
					break;
				case LRU:
					getPriorities().get(priority).putIfAbsent(
							classHash, new ClassSpecificEvictionLRUAdaptor(getSpaceCacheInteractor()));
					break;
				case NONE:
					getPriorities().get(priority).putIfAbsent(
							classHash, new ClassSpecificEvictionStrategyAdaptor());
				}
			}

			getSpecificStrategy(entry).onInsert(entry);
		}
	}

	protected ClassSpecificEvictionStrategyAdaptor getSpecificStrategy(EvictableServerEntry entry) {
		return getPriorities().get(getPriority(entry)).get(getEntryClassHash(entry));
	}


	public void onLoad(EvictableServerEntry entry){ 
		getSpecificStrategy(entry).onLoad(entry);
	}

	public void touchOnRead(EvictableServerEntry entry){
		getSpecificStrategy(entry).touchOnRead(entry);
	}

	public void touchOnModify(EvictableServerEntry entry){
		getSpecificStrategy(entry).touchOnModify(entry);
	}

	public void remove(EvictableServerEntry entry){
		getSpecificStrategy(entry).remove(entry);
	}

	public int  evict (int evictionQuota){ 
		int counter = 0;
		//priority with a lower value should be removed later
		while(counter < evictionQuota) {
				for(ClassSpecificEvictionStrategyAdaptor adaptor : getPriorities().firstEntry().getValue().values())
					counter += adaptor.evict(evictionQuota - counter);
		}
		return counter;
	}


	public ConcurrentSkipListMap<Priority, ConcurrentHashMap<Integer, ClassSpecificEvictionStrategyAdaptor>> getPriorities() {
		return priorities;
	}

	private int getEntryClassHash(EvictableServerEntry entry) {
		return entry.getSpaceTypeDescriptor().getObjectClass().hashCode();
	}

}
