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

/**
 * Specifies the value of index to be used for LRU mapping
 * since the index is updated on each operation it is necessary
 * to use two longs since 64 bit of a single one could not be enough
 * 
 * @author Sagi Bernstein
 * @since 9.1.0
 */
public class IndexValue implements Comparable<IndexValue>{
	private long major;
	private long minor;

	public IndexValue(Long major, Long minor) {
		this.setMajor(major);
		this.setMinor(minor);
	}

	public long getMajor() {
		return major;
	}

	public void setMajor(long major) {
		this.major = major;
	}

	public long getMinor() {
		return minor;
	}

	public void setMinor(long minor) {
		this.minor = minor;
	}

	@Override
	public int compareTo(IndexValue o) {
		long ans = this.getMajor() - o.getMajor();
		return (int) ((ans == 0)? this.getMinor() - o.getMinor() : ans);
	}

	@Override
	public String toString() {
		return "[" + major + ", " + minor + "]";
	}

}
