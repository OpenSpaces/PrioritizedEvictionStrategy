/*******************************************************************************
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All
 rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package org.openspaces.eviction.specificorder;

import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openspaces.eviction.Index;
import org.openspaces.eviction.IndexValue;

import com.gigaspaces.server.eviction.EvictableServerEntry;
import com.gigaspaces.server.eviction.SpaceEvictionManager;
import com.gigaspaces.server.eviction.SpaceEvictionStrategy;

/**
 * This is an extension of the {@link EvictionStrategy} class
 * it provides a class specific FIFO mechanism
 * to be used with {@link ClassSpecificEvictionStrategy} 
 * 
 * @author Sagi Bernstein
 * @since 9.1.0
 */
public class ClassSpecificEvictionFIFOStrategy extends SpaceEvictionStrategy {
	final private ConcurrentSkipListMap<IndexValue, EvictableServerEntry> queue;
	final private AtomicLong amountInSpace;
	final private Index index; 
	final protected static Logger logger = Logger.getLogger(com.gigaspaces.logger.Constants.LOGGER_CACHE);

	public ClassSpecificEvictionFIFOStrategy(SpaceEvictionManager evictionManager, AtomicLong amountInSpace) {
		super();
		this.queue= new ConcurrentSkipListMap<IndexValue, EvictableServerEntry>();
		this.index = new Index();
		this.amountInSpace = amountInSpace;
		if(logger.isLoggable(Level.CONFIG))
			logger.config("instantiated new Class Specific Strategy: " + this.getClass().getName() + " " + this.hashCode());
	}
	
	@Override
	public void onInsert(EvictableServerEntry entry){
		super.onInsert(entry);
		add(entry);
	}

	@Override
	public void onLoad(EvictableServerEntry entry){
		super.onLoad(entry);
		add(entry);
	}

	
	protected void add(EvictableServerEntry entry) {
		IndexValue key = getIndex().incrementAndGet();
		entry.setEvictionPayLoad(key);
		getQueue().put(key, entry);
		if(logger.isLoggable(Level.FINEST))
			logger.finest("inserted entry with UID: " + entry.getUID() +
					" in class " + entry.getSpaceTypeDescriptor().getClass() + " with key index: " + key);
	}

	@Override
	public void onRemove(EvictableServerEntry entry){
		super.onRemove(entry);
		if(getQueue().remove(entry.getEvictionPayLoad()) == null)
			throw new RuntimeException("entry " + entry + "should be in the queue");

		if(logger.isLoggable(Level.FINEST))
			logger.finest("removed entry with UID: " + entry.getUID() +
					" in class " + entry.getSpaceTypeDescriptor().getClass() + " with key index: " + entry.getEvictionPayLoad());
		
		//keep track of number of objects in space
		getAmountInSpace().decrementAndGet();
	}

	@Override
	public int evict(int evictionQuota){ 
		int counter = 0;
		int mappingsSize = getQueue().size();
		for(int i = 0; i < Math.min(mappingsSize, evictionQuota) 
				&& counter < evictionQuota; i++) {
			Iterator<EvictableServerEntry> iterator = getQueue().values().iterator();
			while(iterator.hasNext() && counter < evictionQuota){
				EvictableServerEntry next = iterator.next();
				if(logger.isLoggable(Level.FINEST))
					logger.finest("trying to evict entry with UID: " + next.getUID() +
						", in class " + next.getSpaceTypeDescriptor().getClass() + " and key index: " + next.getEvictionPayLoad());
				if(getEvictionManager().tryEvict(next)){
					counter++;	
				}
			}
		}
		
		if(logger.isLoggable(Level.FINEST))
			logger.finest("got request to evict " + evictionQuota + " entries, evicted " + counter);
		
		return counter;
	}

	protected ConcurrentSkipListMap<IndexValue, EvictableServerEntry> getQueue() {
		return queue;
	}

	protected Index getIndex() {
		return index;
	}

	protected AtomicLong getAmountInSpace() {
		return this.amountInSpace;
	}
}
