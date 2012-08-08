package com.gigaspaces.eviction;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import com.gigaspaces.eviction.singleorder.Priority;
import com.gigaspaces.server.eviction.EvictableServerEntry;
import com.gigaspaces.server.eviction.EvictionStrategy;
import com.gigaspaces.server.eviction.SpaceCacheInteractor;

public abstract class AbstractClassBasedEvictionStrategy extends EvictionStrategy {

	private SpaceCacheInteractor spaceCacheInteractor;
	private Properties spaceProperties;
	protected Integer cacheSize;
	protected AtomicLong amountInSpace;

	public void init(SpaceCacheInteractor spaceCacheInteractor, Properties spaceProperties){
		this.spaceCacheInteractor = spaceCacheInteractor;
		this.spaceProperties = spaceProperties;
		cacheSize = (Integer) spaceProperties.get("CACHE_SIZE");
		amountInSpace = new AtomicLong(0);
	}
	
	protected Priority getPriority(EvictableServerEntry entry) {
		int priority = entry.getSpaceTypeDescriptor().getObjectClass()
				.getAnnotation(SpaceEvictionPriority.class).priority();
		if(priority < 0)
				throw new IllegalArgumentException("priority values should be greater than 0");
		return new Priority(priority);
	}

	protected OrderBy getOrderBy(EvictableServerEntry entry) {
		return entry.getSpaceTypeDescriptor().getObjectClass()
				.getAnnotation(SpaceEvictionPriority.class).orderBy();
	}
	public SpaceCacheInteractor getSpaceCacheInteractor() {
		return spaceCacheInteractor;
	}

	public Properties getSpaceProperties() {
		return spaceProperties;
	}

	public AtomicLong getAmountInSpace() {
		return amountInSpace;
	}

	public Integer getCacheSize() {
		return cacheSize;
	}

	public void setCacheSize(Integer cacheSize) {
		this.cacheSize = cacheSize;
	}
	
}
