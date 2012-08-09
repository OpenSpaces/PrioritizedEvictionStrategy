package com.gigaspaces.eviction;

import java.util.concurrent.atomic.AtomicLong;

/**
 * this class is used to produce {@link IndexValue} objects
 * to help map LRU
 * 
 * @author Sagi Bernstein
 * @since 9.1.0
 */
public class Index{
	private long major;
	private AtomicLong minor;
	
	public Index() {
		this.major = 0l;
		this.minor = new AtomicLong(0);
		
	}

	public IndexValue incrementAndGet(){
		Long minorValue = getMinor().incrementAndGet();
		
		if(minorValue < 0)
			synchronized (this) {
				if(getMinor().get() < 0){
					return new IndexValue(this.major++, 0l);
				}
			}
		else return new IndexValue(getMajor(), minorValue);
		//in the case the minor index was negative and upon entering the lock it 
		//already is positive we get this thread to have another go at getting the index
		return incrementAndGet();
	}


	public long getMajor() {
		return major;
	}


	public AtomicLong getMinor() {
		return minor;
	}
}
