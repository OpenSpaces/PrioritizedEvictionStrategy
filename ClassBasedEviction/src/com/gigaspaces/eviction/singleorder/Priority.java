package com.gigaspaces.eviction.singleorder;

public class Priority implements Comparable<Priority> {
	Integer priority;
	
	public Priority(Integer priority) {
		this.priority = priority;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	@Override
	public int compareTo(Priority o) {
		return o.getPriority() - this.getPriority();
	}
	
	
}
