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

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;

import org.openspaces.eviction.Index;
import org.openspaces.eviction.IndexValue;

import com.gigaspaces.server.eviction.EvictableServerEntry;
import com.gigaspaces.server.eviction.EvictionStrategy;
import com.gigaspaces.server.eviction.SpaceCacheInteractor;

/**
 * This is an extension of the {@link EvictionStrategy} class
 * it provides a class specific LRU mechanism
 * to be used with {@link ClassSpecificEvictionStrategy} 
 * 
 * @author Sagi Bernstein
 * @since 9.1.0
 */
public class ClassSpecificEvictionLRUStrategy extends EvictionStrategy {
	private SpaceCacheInteractor spaceCacheInteractor;
	private ConcurrentSkipListMap<IndexValue, EvictableServerEntry> mapping;
	private Index index;

	public ClassSpecificEvictionLRUStrategy(SpaceCacheInteractor spaceCacheInteractor) {
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
	public int evict(int evictionQuota){ 
		int counter = 0;
		int mappingsSize = getMapping().size();
		for(int i = 0; i < Math.min(mappingsSize, evictionQuota) 
				&& counter < evictionQuota; i++) {
			Entry<IndexValue, EvictableServerEntry> firstEntry = getMapping().firstEntry();
			while(firstEntry != null){
				if(getSpaceCacheInteractor().grantEvictionPermissionAndRemove(
						firstEntry.getValue())){
					counter++;
					firstEntry = getMapping().firstEntry();
				}
			}
		}
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
