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

import org.openspaces.eviction.Index;
import org.openspaces.eviction.IndexValue;

import com.gigaspaces.server.eviction.EvictableServerEntry;
import com.gigaspaces.server.eviction.EvictionStrategy;
import com.gigaspaces.server.eviction.SpaceCacheInteractor;

/**
 * This is an extension of the {@link EvictionStrategy} class
 * it provides a class specific FIFO mechanism
 * to be used with {@link ClassSpecificEvictionStrategy} 
 * 
 * @author Sagi Bernstein
 * @since 9.1.0
 */
public class ClassSpecificEvictionFIFOStrategy extends EvictionStrategy {
	private SpaceCacheInteractor spaceCacheInteractor;
	private ConcurrentSkipListMap<IndexValue, EvictableServerEntry> queue;
	AtomicLong amountInSpace;
	private Index index; 

	public ClassSpecificEvictionFIFOStrategy(SpaceCacheInteractor spaceCacheInteractor, AtomicLong amountInSpace) {
		this.spaceCacheInteractor = spaceCacheInteractor;
		this.queue= new  ConcurrentSkipListMap<IndexValue, EvictableServerEntry>();
		this.amountInSpace = amountInSpace;
		this.index = new Index();
	}

	@Override
	public void onInsert(EvictableServerEntry entry){
		IndexValue key = getIndex().incrementAndGet();
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
				if(getSpaceCacheInteractor().grantEvictionPermissionAndRemove(
						iterator.next())){
					counter++;
				}
			}
		}
		return counter;
	}

	public SpaceCacheInteractor getSpaceCacheInteractor() {
		return spaceCacheInteractor;
	}

	public ConcurrentSkipListMap<IndexValue, EvictableServerEntry> getQueue() {
		return queue;
	}

	public Index getIndex() {
		return index;
	}

	public AtomicLong getAmountInSpace() {
		return this.amountInSpace;
	}
}
