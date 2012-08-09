package com.gigaspaces.data;

import com.gigaspaces.eviction.SpaceEvictionPriority;

@SpaceEvictionPriority(priority = 2)
public class BronzeMedal extends Medal {
	
	public BronzeMedal(){}
	
	public BronzeMedal(int id){
		super(id);
	}
}
