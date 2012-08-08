package com.gigaspaces.data;

import com.gigaspaces.eviction.SpaceEvictionPriority;

@SpaceEvictionPriority(priority = 1)
public class SilverMedal extends Medal {
	
	public SilverMedal(){}
	
	public SilverMedal(int id){
		super(id);
	}
}
