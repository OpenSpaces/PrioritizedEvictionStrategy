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
import org.openspaces.core.GigaSpace;
import org.openspaces.eviction.data.BronzeMedal;
import org.openspaces.eviction.data.GoldMedal;
import org.openspaces.eviction.data.Medal;
import org.openspaces.eviction.data.SilverMedal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/pu.xml"})
public class ClassSpecificOrderTest{
	private static final int NUM_OF_THREADS = 10;
	@Autowired
	private GigaSpace gigaSpace;
	private int cacheSize = 1000;

	//mean trick
	private static Logger logger = Logger.getLogger(new Object(){}.getClass().getEnclosingClass());

	@Before
	public void cleanSpace(){
		gigaSpace.clear(new Object());
	}

	@Test
	public void test1() throws Exception  {
		logger.info("test 1 - assert none order does not evict");
		logger.info("fill cache with ten times its amount with none order by");
		for (int i = 0; i < cacheSize * 10; i++) {
			gigaSpace.write(new GoldMedal(i));
		}
		logger.info("assert none order does not evict");
		Assert.assertEquals("GoldMedals were evicted", cacheSize * 10, gigaSpace.count(new GoldMedal()));
		logger.info("Test Passed 1");
	}

	@Test
	public void test2() {
		logger.info("test 2 - assert only silver remains");

		logger.info("fill the space with ten times cache size of different priority objects");
		for (int i = 0; i < cacheSize * 10; i++) {
			if(i % 2 == 0)
				gigaSpace.write(new BronzeMedal(i));
			else
				gigaSpace.write(new SilverMedal(i));
		}
		Assert.assertTrue("amount of objects in space is larger then cache size",
				gigaSpace.count(new Object()) == cacheSize);
		//space will not evict an inserted object that calls to evict
		//so if the last insert is not gold it will stay in space
		logger.info("assert no more than one object of lower priority is in space");
		Assert.assertTrue("not all objects in space are of the highest priority",
				gigaSpace.count(new Object()) == gigaSpace.count(new GoldMedal()) + 1
				|| gigaSpace.count(new Object()) == gigaSpace.count(new GoldMedal()));

		logger.info("Test Passed 2");
	}

	@Test
	public void test3() throws InterruptedException {
		gigaSpace.clear(new Object());

		logger.info("test 3 - multi threaded");
		logger.info("fill the space with entries");		
		ExecutorService threadPool = Executors.newFixedThreadPool(NUM_OF_THREADS);
		for (int i = 0; i < NUM_OF_THREADS; i++) {
			threadPool.execute(new Runnable() {

				@Override
				public void run() {
					for (int i = 0; i < (cacheSize * 10); i++) {
						if(i % 3 == 0)
							gigaSpace.write(new GoldMedal(i));
						else if (i % 3 == 1)
							gigaSpace.write(new SilverMedal(i));
						else
							gigaSpace.write(new BronzeMedal(i));
					}
				}
			});
		}
		threadPool.shutdown();
		threadPool.awaitTermination(60, TimeUnit.SECONDS);
		logger.info("assert only objects only amount to cache size");		
		Assert.assertTrue("amount of objects in space is larger then cache size",
				gigaSpace.count(new Object()) == cacheSize);
	}


	@Test
	public void test4() throws Exception  {
		logger.info("test 4 - lru logics");
		logger.info("write an object");
		gigaSpace.write(new SilverMedal(0));

		logger.info("fill the space with more then cache size object and red the original in the middle");
		for (int i = 1; i <= cacheSize + 10; i++) {
			if(i == (cacheSize/2))
				gigaSpace.read(new SilverMedal(0));
			else
				gigaSpace.write(new SilverMedal(i));
		}
		Assert.assertTrue("amount of objects in space is larger then cache size",
				gigaSpace.count(new Object()) == cacheSize);
		logger.info("assert the original object is still in cache");
		Assert.assertNotNull("silver medal 0 is not in space",
				gigaSpace.read(new SilverMedal(0)));
	}

	@Test
	public void test5() throws Exception  {
		logger.info("test 5 - lru logics");
		logger.info("same as 4 but modify the object instead of read it");

		SilverMedal silverMedal = new SilverMedal(0);
		gigaSpace.write(silverMedal);
		silverMedal.setContest("Butterfly 100m");
		for (int i = 1; i <= cacheSize + 10; i++) {
			if(i == (cacheSize/2))
				gigaSpace.write(silverMedal);
			else
				gigaSpace.write(new SilverMedal(i));
		}
		Assert.assertTrue("amount of objects in space is larger then cache size",
				gigaSpace.count(new Object()) == cacheSize);
		Assert.assertNotNull("silver medal 0 is not in space",
				gigaSpace.read(silverMedal));
	}

	@Test
	public void test6() throws Exception  {
		logger.info("test 6 - lru logics");
		logger.info("write high priority object");
		gigaSpace.write(new GoldMedal(0));

		logger.info("fille the cache with ten times its size of lower priority objects");
		for (int i = 1; i <= cacheSize * 10; i++) {
			if(i % 2 == 0) 
				gigaSpace.write(new SilverMedal(i));
			else
				gigaSpace.write(new BronzeMedal(i));
		}

		Assert.assertTrue("amount of objects in space is larger then cache size",
				gigaSpace.count(new Object()) == cacheSize);

		logger.info("assert the original object is still in cache");
		Assert.assertNotNull("silver medal 0 is not in space",
				gigaSpace.read(new GoldMedal(0)));


	}


	@Test
	public void test10() throws Exception  {
		logger.info("test 10 - memory shortage");
		final int mega = (int)Math.pow(2, 20);
		ExecutorService threadPool = Executors.newFixedThreadPool(NUM_OF_THREADS);
		for (int i = 0; i < NUM_OF_THREADS; i++)
			threadPool.execute(new Runnable(){			
				@Override
				public void run() {
					for(int i = 0; i < cacheSize; i++){
						Medal toWrite;	
						if(i % 3 == 0)
							toWrite = new GoldMedal(i);
						else if (i % 3 == 1)
							toWrite = new SilverMedal(i);
						else
							toWrite = new BronzeMedal(i);
						toWrite.setWeight(new byte[mega]);
						gigaSpace.write(toWrite);
					}					
				}
			});
		threadPool.shutdown();
		threadPool.awaitTermination(60, TimeUnit.SECONDS);
		Assert.assertTrue("amount of objects in space is larger then cache size",
				gigaSpace.count(new Object()) <= cacheSize);
	}




	public int getCacheSize() {
		return cacheSize;
	}

	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
	}

}