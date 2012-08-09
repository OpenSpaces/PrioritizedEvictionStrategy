package com.gigaspaces.eviction;

/**
 * A thin wrapper for the {@link Integer} class that reverses
 * Integer's natural order, as priority with lower value is more important
 * 
 * @author Sagi Bernstein
 * @since 9.1.0
 */
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

	@Override
	public String toString() {
		return "[" + priority + "]";
	}
	
}
