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



import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openspaces.eviction.data.BronzeMedal;
import org.openspaces.eviction.data.GoldMedal;
import org.openspaces.eviction.data.Medal;
import org.openspaces.eviction.data.SilverMedal;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/pu-lru.xml"})
public class LRUSingleOrderTest extends FIFOSingleOrderTest{

	//mean trick
	private static Logger logger = Logger.getLogger(new Object(){}.getClass().getEnclosingClass());




	@Test
	public void readKeepsAnObjectTest() throws Exception  {
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
		Assert.assertEquals("amount of objects in space is larger then cache size",
				gigaSpace.count(new Object()), cacheSize);
		
		logger.info("assert the original object is still in cache");
		Assert.assertNotNull("silver medal 0 is not in space",
				gigaSpace.read(new SilverMedal(0)));
	}

	@Test
	public void multiThreaded(){
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

		logger.info("fill the cache with ten times its size of lower priority objects");
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
	public void test7() throws InterruptedException {
		logger.info("test 7 - load test");
		logger.info("fill the space with entries");		
		ExecutorService threadPool = Executors.newFixedThreadPool(NUM_OF_THREADS);
		for (int i = 0; i < NUM_OF_THREADS; i++) {
			threadPool.execute(new Runnable() {

				@Override
				public void run() {
					for (int i = 0; i < cacheSize * 10; i++) {
						switch(i % 3){
						case 0:
							gigaSpace.write(new GoldMedal(i));
							gigaSpace.read(new BronzeMedal());
							if(Math.random() < 0.5)
								gigaSpace.take(new SilverMedal());
							break;
						case 1:
							gigaSpace.write(new SilverMedal(i));
							gigaSpace.read(new GoldMedal());
							if(Math.random() < 0.5)
								gigaSpace.take(new BronzeMedal());
							break;
						case 2:
							gigaSpace.write(new BronzeMedal(i));
							gigaSpace.read(new SilverMedal());
							if(Math.random() < 0.5)
								gigaSpace.take(new GoldMedal());
							break;
						}
					}
				}
			});
		}
		threadPool.shutdown();
		threadPool.awaitTermination(60, TimeUnit.SECONDS);
		Assert.assertTrue("more silver than gold or more bronze than silver",
				gigaSpace.count(new GoldMedal()) > gigaSpace.count(new SilverMedal()) 
				&& gigaSpace.count(new SilverMedal()) > gigaSpace.count(new BronzeMedal()));
	}

	
	@Test
	public void test8() throws InterruptedException {
		logger.info("test 8 - fifo test");
		logger.info("fill the space with double the cache size");		
		final AtomicInteger id = new AtomicInteger(0);
		final long start = System.currentTimeMillis();
		final int minutes = 60;
		ExecutorService threadPool = Executors.newFixedThreadPool(NUM_OF_THREADS);
		for (int i = 0; i < NUM_OF_THREADS; i++) {
			threadPool.execute(new Runnable() {

				@Override
				public void run() {
					while(System.currentTimeMillis() - start < TimeUnit.MINUTES.toMillis(minutes)){
						gigaSpace.write(new GoldMedal(id.getAndIncrement()));
						gigaSpace.read(new BronzeMedal());
						if(Math.random() < 0.5)
							gigaSpace.take(new SilverMedal());
						gigaSpace.write(new SilverMedal(id.getAndIncrement()));
						gigaSpace.read(new GoldMedal());
						if(Math.random() < 0.5)
							gigaSpace.take(new BronzeMedal());
						gigaSpace.write(new BronzeMedal(id.getAndIncrement()));
						gigaSpace.read(new SilverMedal());
						if(Math.random() < 0.5)
							gigaSpace.take(new GoldMedal());
					}
				}
			});
		}
		threadPool.shutdown();
		threadPool.awaitTermination(minutes, TimeUnit.MINUTES);
		Assert.assertTrue("more silver or bronze than gold",
				gigaSpace.count(new GoldMedal()) > gigaSpace.count(new SilverMedal()) 
				&& gigaSpace.count(new GoldMedal()) > gigaSpace.count(new BronzeMedal()));
	}

	@Test
	public void test10() throws Exception  {
		logger.info("test 10 - memory shortage");
		logger.info("this test should only pass with a jvm heap size of 256MB");
		
		final int mega = 1 << 20;
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

}
