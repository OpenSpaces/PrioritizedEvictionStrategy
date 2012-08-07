package com.gigaspaces.eviction;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import com.gigaspaces.server.eviction.EvictableServerEntry;

public class LRUMapping {
	private ConcurrentHashMap<EvictableServerEntry , Long> byEntry;
	private ConcurrentSkipListMap<Long, EvictableServerEntry> byTime;
	
	
	
	public LRUMapping() {
		super();
		this.byEntry = new ConcurrentHashMap<EvictableServerEntry , Long>();
		this.byTime = new ConcurrentSkipListMap<Long, EvictableServerEntry>();
	}

	public LRUMapping(ConcurrentHashMap<EvictableServerEntry, Long> byEntry,
			ConcurrentSkipListMap<Long, EvictableServerEntry> byTime) {
		super();
		this.setByEntry(byEntry);
		this.setByTime(byTime);
	}
	
	public Long get(EvictableServerEntry entry){
		return getByEntry().get(entry);
	}
	
	public EvictableServerEntry get(Long index){
		return getByTime().get(index);
	}
	
	public void put(EvictableServerEntry entry, Long index){
		Long prev = getByEntry().put(entry, index);
		if(prev != null)
			getByTime().remove(prev);
		getByTime().put(index, entry);
	}
	
	public boolean isEmpty(){
		return getByEntry().isEmpty() && getByTime().isEmpty();
	}

	public void remove(EvictableServerEntry entry){
		Long indexToRemove = getByEntry().remove(entry);
		if(indexToRemove != null)
			getByTime().remove(indexToRemove);
	}

	
	public ConcurrentSkipListMap<Long, EvictableServerEntry> getByTime() {
		return byTime;
	}

	public void setByTime(ConcurrentSkipListMap<Long, EvictableServerEntry> byTime) {
		this.byTime = byTime;
	}

	public ConcurrentHashMap<EvictableServerEntry , Long> getByEntry() {
		return byEntry;
	}

	public void setByEntry(ConcurrentHashMap<EvictableServerEntry , Long> byEntry) {
		this.byEntry = byEntry;
	}
	
	
}
