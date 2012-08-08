package com.gigaspaces.data;

import com.gigaspaces.eviction.SpaceEvictionPriority;

@SpaceEvictionPriority(priority = 0)
public class GoldMedal extends Medal{
	
	public GoldMedal(){}
	
	public GoldMedal(int id){
		super(id);
	}
}
