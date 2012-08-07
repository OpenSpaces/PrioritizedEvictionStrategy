package com.gigaspaces.eviction.specificorder;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import com.gigaspaces.eviction.AbstractClassBasedEvictionStrategy;
import com.gigaspaces.server.eviction.EvictableServerEntry;
import com.gigaspaces.server.eviction.SpaceCacheInteractor;


public class ClassSpecificEvictionStrategy extends AbstractClassBasedEvictionStrategy{
	List<ConcurrentHashMap<Integer, ClassSpecificEvictionStrategyAdaptor>> priorities;
	Semaphore classes;
	Semaphore remove;

	public void init(SpaceCacheInteractor spaceCacheInteractor, Properties spaceProperties){
		super.init(spaceCacheInteractor, spaceProperties);
		priorities = new ArrayList<ConcurrentHashMap<Integer, ClassSpecificEvictionStrategyAdaptor>>();
		classes = new Semaphore(1);
		remove = new Semaphore(1);
		for(int i = 0; i < PRIORITIES_SIZE; i++)
			getPriorities().add(new ConcurrentHashMap<Integer, ClassSpecificEvictionStrategyAdaptor>());
	}

	public void onInsert(EvictableServerEntry entry)
	{
		int classHash = getEntryClassHash(entry);
		int priority = getPriority(entry);

		//handle class is first inserted to space
		if(!getPriorities().get(priority).containsKey(classHash)){
			classes.acquireUninterruptibly();

			if(!getPriorities().get(priority).containsKey(classHash)){
				switch(getOrderBy(entry)){
				case FIFO:
					getPriorities().get(priority).put(
							classHash, new ClassSpecificEvictionFIFOAdaptor());
					break;
				case LRU:
					getPriorities().get(priority).put(
							classHash, new ClassSpecificEvictionLRUAdaptor());
					break;
				case NONE:
					getPriorities().get(priority).put(
							classHash, new ClassSpecificEvictionStrategyAdaptor());
				}
				getPriorities().get(priority).get(classHash)
				.init(getSpaceCacheInteractor(), getSpaceProperties());
			}
			classes.release();

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
		for(int i = getPriorities().size() - 1; i >= 0  && (counter < evictionQuota); i--) {
			ClassSpecificEvictionStrategyAdaptor[] adaptors = new ClassSpecificEvictionStrategyAdaptor[getPriorities().get(i).size()];
			getPriorities().get(i).values().toArray(adaptors);
			for(int j = 0; j < adaptors.length && (counter < evictionQuota); i++)
				counter += adaptors[j].evict(evictionQuota - counter);
		}
		return counter;
	}


	public List<ConcurrentHashMap<Integer, ClassSpecificEvictionStrategyAdaptor>> getPriorities() {
		return priorities;
	}

	private int getEntryClassHash(EvictableServerEntry entry) {
		return entry.getSpaceTypeDescriptor().getObjectClass().hashCode();
	}

}
