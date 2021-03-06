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

package org.openspaces.eviction;

import java.util.concurrent.atomic.AtomicLong;

/**
 * this class is used to produce {@link IndexValue} objects
 * to help map LRU
 * 
 * @author Sagi Bernstein
 * @since 9.1.0
 */
public class Index{
	private volatile long major;
	private AtomicLong minor;
	
	public Index() {
		this.major = 0;
		this.minor = new AtomicLong(0);
		
	}

	public IndexValue incrementAndGet(){
		long majorValue = getMajor(); 
		long minorValue = getMinor().incrementAndGet();
		
		if(minorValue < 0){
			synchronized (this) {
				if(getMinor().get() < 0){
					getMinor().set(0);
					this.major += 1;
					return new IndexValue(getMajor(), 0l);
				}
			}
		}
		else{
			long currentMajor = getMajor();
			if(currentMajor == majorValue)
				return new IndexValue(majorValue, minorValue);
		}
		//in the case the minor index was negative and upon entering the lock it
		//already is positive, or in the case the major was switched
		//we send this thread to have another go at getting the index
		return incrementAndGet();
	}


	public long getMajor() {
		return major;
	}


	public AtomicLong getMinor() {
		return minor;
	}
}
