package com.gigaspaces.eviction;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import com.gigaspaces.server.eviction.EvictableServerEntry;
import com.gigaspaces.server.eviction.EvictionStrategy;
import com.gigaspaces.server.eviction.SpaceCacheInteractor;

/**
 * this class contains some useful methods that are common
 * for different the strategies
 * 
 * @author Sagi Bernstein
 * @since 9.1.0
 */
public abstract class AbstractClassBasedEvictionStrategy extends EvictionStrategy {

	private SpaceCacheInteractor spaceCacheInteractor;
	private Properties spaceProperties;
	protected Integer cacheSize;
	protected AtomicLong amountInSpace;
	protected static final Logger logger = Logger.getLogger(com.gigaspaces.logger.Constants.LOGGER_CACHE );

	public void init(SpaceCacheInteractor spaceCacheInteractor, Properties spaceProperties){
		logger.config("started custom eviction strategy " + this.getClass().getSimpleName());
		
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
