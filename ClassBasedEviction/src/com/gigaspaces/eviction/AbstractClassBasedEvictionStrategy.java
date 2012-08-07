package com.gigaspaces.eviction;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import com.gigaspaces.server.eviction.EvictableServerEntry;
import com.gigaspaces.server.eviction.EvictionStrategy;
import com.gigaspaces.server.eviction.SpaceCacheInteractor;

public abstract class AbstractClassBasedEvictionStrategy extends EvictionStrategy {

	private SpaceCacheInteractor spaceCacheInteractor;
	private Properties spaceProperties;
	protected Integer cacheSize;
	protected static final int PRIORITIES_SIZE = 3;
	

	protected AtomicInteger amountInSpace;

	public void init(SpaceCacheInteractor spaceCacheInteractor, Properties spaceProperties){
		this.spaceCacheInteractor = spaceCacheInteractor;
		this.spaceProperties = spaceProperties;
		cacheSize = (Integer) spaceProperties.get("CACHE_SIZE");
		amountInSpace = new AtomicInteger(0);
	}
	
	protected Integer getPriority(EvictableServerEntry entry) {
		int priority = entry.getSpaceTypeDescriptor().getObjectClass()
				.getAnnotation(SpaceEvictionPriority.class).priority();
		if(priority < 0 || priority >= PRIORITIES_SIZE)
				throw new IllegalArgumentException("priority values should be between 0 and " + (PRIORITIES_SIZE - 1));
		return priority;
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

	public AtomicInteger getAmountInSpace() {
		return amountInSpace;
	}

	public Integer getCacheSize() {
		return cacheSize;
	}

	public void setCacheSize(Integer cacheSize) {
		this.cacheSize = cacheSize;
	}
	
}
