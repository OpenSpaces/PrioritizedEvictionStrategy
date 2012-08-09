package com.gigaspaces.eviction;
/**
 * This enumeration indicates the strategy by which an annotated class will be evicted
 * 
 * @author Sagi Bernstein
 * @since 9.1.0
 */
public enum OrderBy {
	LRU,
	FIFO,
	NONE
}
