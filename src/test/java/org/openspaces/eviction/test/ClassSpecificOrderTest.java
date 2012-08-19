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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openspaces.eviction.data.GoldMedal;
import org.openspaces.eviction.data.SilverMedal;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/pu-specific.xml"})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class ClassSpecificOrderTest extends AbstractClassBasedEvictionTest{
	
	@Test
	public void noneDoesNotEvictTest() throws Exception  {
		logger.info("fill cache with ten times its amount with none order by");
		for (int i = 0; i < cacheSize * 10; i++) {
			gigaSpace.write(new GoldMedal(i));
		}
		logger.info("assert none order does not evict");
		Assert.assertEquals("GoldMedals were evicted", cacheSize * 10, gigaSpace.count(new GoldMedal()));
		logger.info("Test Passed");
	}


	public void assertMultiThreadedOperationsTest() {
		logger.info("assert only objects with none are left");		
		Assert.assertTrue("not all objects in space are of the highest priority",
				gigaSpace.count(new Object()) == gigaSpace.count(new GoldMedal()) + 1
				|| gigaSpace.count(new Object()) == gigaSpace.count(new GoldMedal()));
	}


	@Test
	public void inClassLRUReadTest() throws Exception  {

		logger.info("write an object");
		gigaSpace.write(new SilverMedal(0));

		logger.info("fill the space with more than cache size object and red the original in the middle");
		for (int i = 1; i <= cacheSize + 10; i++) {
			if(i == (cacheSize/2))
				gigaSpace.read(new SilverMedal(0));
			else
				gigaSpace.write(new SilverMedal(i));
		}
		Assert.assertTrue("amount of objects in space is larger than cache size",
				gigaSpace.count(new Object()) == cacheSize);
		logger.info("assert the original object is still in cache");
		Assert.assertNotNull("silver medal 0 is not in space",
				gigaSpace.read(new SilverMedal(0)));
		logger.info("Test Passed");
	}

	@Test
	public void inClassLRUModifyTest() throws Exception  {
		logger.info("same as inClassLRUTest but modify the object instead of read it");

		SilverMedal silverMedal = new SilverMedal(0);
		gigaSpace.write(silverMedal);
		silverMedal.setContest("Butterfly 100m");
		for (int i = 1; i <= cacheSize + 10; i++) {
			if(i == (cacheSize/2))
				gigaSpace.write(silverMedal);
			else
				gigaSpace.write(new SilverMedal(i));
		}
		Assert.assertEquals("amount of objects in space is larger than cache size",
				gigaSpace.count(new Object()), cacheSize);
		Assert.assertNotNull("silver medal 0 is not in space",
				gigaSpace.read(silverMedal));
	}

	protected void assertMemoryShortageTest(){
		Assert.assertEquals("not only gold medals remain in space",
				gigaSpace.count(new Object()),  gigaSpace.count(new GoldMedal()));
	}

	protected void assertCacheSizeEqualsCountInSpace() {}
}
