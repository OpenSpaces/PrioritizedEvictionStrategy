package com.gigaspaces.eviction.specificorder;

import java.util.Properties;

import com.gigaspaces.server.eviction.EvictableServerEntry;
import com.gigaspaces.server.eviction.SpaceCacheInteractor;


public class ClassSpecificEvictionStrategyAdaptor{
	public void init(SpaceCacheInteractor spaceCacheInteractor, Properties spaceProperties){}
	public void onInsert(EvictableServerEntry entry){ }
	public void onLoad(EvictableServerEntry entry){ }
	public void touchOnRead(EvictableServerEntry entry){}
	public void touchOnModify(EvictableServerEntry entry){}
	public void remove(EvictableServerEntry entry){}
	public int  evict (int evictionQuota){ return 0;}
	public void close(){}

}
