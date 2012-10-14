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

package org.openspaces.eviction.test;



import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openspaces.eviction.test.data.BronzeMedal;
import org.openspaces.eviction.test.data.GoldMedal;
import org.openspaces.eviction.test.data.SilverMedal;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/pu-fifo.xml"})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class FIFOSingleOrderTest extends AbstractClassBasedEvictionTest{

	//mean trick
	private static Logger logger = Logger.getLogger(new Object(){}.getClass().getEnclosingClass());

	@Before
	public void cleanSpace(){
		gigaSpace.clear(new Object());
	}

	@Test
	public void priorityEvictionByFIFOTest() throws Exception  {
		logger.info("write low priority object");
		gigaSpace.write(new BronzeMedal(0));
		logger.info("fill cache exactly with hight priority object");
		for (int i = 1; i < cacheSize; i++) {
			gigaSpace.write(new GoldMedal(i));
		}
		logger.info("write another lower priority object");
		gigaSpace.write(new BronzeMedal(1));
		logger.info("assert the first object was removed");
		Assert.assertNull("BronzeMedal 0 was not evicted", gigaSpace.read(new BronzeMedal(0)));
		logger.info("Test Passed");
	}


	

	protected void assertMultiThreadedOperationsTest() {
		Assert.assertTrue("more silver than gold or more bronze than silver", 
				gigaSpace.count(new GoldMedal()) >= gigaSpace.count(new SilverMedal()) 
				&& gigaSpace.count(new SilverMedal()) >= gigaSpace.count(new BronzeMedal()));
	}

	protected void assertMemoryShortageTest() {
		Assert.assertTrue("amount of objects in space is larger than cache size",
				gigaSpace.count(new Object()) <= cacheSize);
	}
	
	protected void assertCacheSizeEqualsCountInSpace() {
		Assert.assertEquals("amount of objects in space is larger than cache size",
				gigaSpace.count(new Object()), cacheSize);
	}

}
