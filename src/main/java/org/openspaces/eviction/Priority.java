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
