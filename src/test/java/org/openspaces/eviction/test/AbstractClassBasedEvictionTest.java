package org.openspaces.eviction.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openspaces.core.GigaSpace;
import org.openspaces.eviction.data.BronzeMedal;
import org.openspaces.eviction.data.GoldMedal;
import org.openspaces.eviction.data.Medal;
import org.openspaces.eviction.data.SilverMedal;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractClassBasedEvictionTest {

	protected static final int NUM_OF_THREADS = 30;
	@Autowired
	protected GigaSpace gigaSpace;
	protected int cacheSize = 1000;
	protected static Logger logger = Logger.getLogger(new Object(){}.getClass().getEnclosingClass());

	@BeforeClass
	public static void garbageCollection() {
		System.gc();
	}

	public AbstractClassBasedEvictionTest() {
		super();
	}

	@Before
	public void cleanSpace() {
		gigaSpace.clear(new Object());
	}

	public int getCacheSize() {
		return cacheSize;
	}

	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
	}
	
	@Test
	public void priorityOrderTest() throws Exception  {
		logger.info("write high priority object");
		gigaSpace.write(new GoldMedal(0));

		logger.info("fill the cache with ten times its size of lower priority objects");
		for (int i = 1; i <= cacheSize * 10; i++) {
			if(i % 2 == 0) 
				gigaSpace.write(new SilverMedal(i));
			else
				gigaSpace.write(new BronzeMedal(i));
		}

		Assert.assertEquals("amount of objects in space is larger than cache size",
				gigaSpace.count(new Object()), cacheSize);

		logger.info("assert the original object is still in cache");
		Assert.assertNotNull("gold medal 0 is not in space",
				gigaSpace.read(new GoldMedal(0)));
		logger.info("Test Passed");

	}
	
	@Test
	public void byPriorityOrderEvictionTest() {
		logger.info("fill the space with ten times cache size of different priority objects");
		for (int i = 0; i < cacheSize * 10; i++) {
			if(i % 2 == 0)
				gigaSpace.write(new GoldMedal(i));
			else
				gigaSpace.write(new SilverMedal(i));
		}
		assertCacheSizeEqualsCountInSpace();
		//space will not evict an inserted object that calls to evict
		//so if the last insert is not gold it will stay in space
		logger.info("assert no more than one object of lower priority is in space");
		Assert.assertTrue("not all objects in space are of the highest priority",
				gigaSpace.count(new Object()) == gigaSpace.count(new GoldMedal()) + 1
				|| gigaSpace.count(new Object()) == gigaSpace.count(new GoldMedal()));

		logger.info("Test Passed");
	}

	protected abstract void assertCacheSizeEqualsCountInSpace();
	
	@Test
	public void multiThreadedOperationsTest() throws InterruptedException {
		logger.info("fill the space with entries");		
		ExecutorService threadPool = Executors.newFixedThreadPool(NUM_OF_THREADS);
		for (int i = 0; i < NUM_OF_THREADS; i++) {
			threadPool.execute(new Runnable() {

				@Override
				public void run() {
					for (int i = 0; i < cacheSize * 10; i++) {
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
		assertCacheSizeEqualsCountInSpace();
		assertMultiThreadedOperationsTest();
		logger.info("Test Passed");
	}

	protected abstract void assertMultiThreadedOperationsTest();
	
	@Test
	public void multiThreadedWithTakeOperationsTest() throws InterruptedException {
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
		threadPool.awaitTermination(1, TimeUnit.MINUTES);
		Assert.assertTrue("more silver or bronze than gold",
				gigaSpace.count(new GoldMedal()) > gigaSpace.count(new SilverMedal()) 
				&& gigaSpace.count(new GoldMedal()) > gigaSpace.count(new BronzeMedal()));
		logger.info("Test Passed");
	}
	
	@Test
	public void loadTest() throws InterruptedException {
		logger.info("fill the space with double the cache size");		
		final AtomicInteger id = new AtomicInteger(0);
		final long start = System.currentTimeMillis();
		final int minutes = 1;
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
		logger.info("Test Passed");
	}
	
	@Test
	public void memoryShortageTest() throws InterruptedException {
		logger.info("memory shortage test");
		logger.info("this test should only pass with a jvm heap size of 256MB");
		
		final int mega = 1 << 20;
		ExecutorService threadPool = Executors.newFixedThreadPool(NUM_OF_THREADS);
		for (int i = 0; i < NUM_OF_THREADS; i++)
			threadPool.execute(new Runnable(){			
				@Override
				public void run() {
					for(int i = 0; i < cacheSize * 10; i++){
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
		assertMemoryShortageTest();
	}

	protected abstract void assertMemoryShortageTest();
	
	
}