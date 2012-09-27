package org.openspaces.eviction.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.SpaceMemoryShortageException;
import org.openspaces.eviction.data.BronzeMedal;
import org.openspaces.eviction.data.GoldMedal;
import org.openspaces.eviction.data.Medal;
import org.openspaces.eviction.data.SilverMedal;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractClassBasedEvictionTest {

	protected static final int NUM_OF_THREADS = 30;
	protected static final int ENTRY_NUM = 1000;
	@Autowired
	protected GigaSpace gigaSpace;
	protected int cacheSize = 1000;
	protected static Logger logger = Logger.getLogger(new Object(){}.getClass().getEnclosingClass());

	@BeforeClass
	public static  void callGC() {
		System.gc();
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

	public void setGigaSpace(GigaSpace gigaSpace) {
		this.gigaSpace = gigaSpace;
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
				cacheSize, gigaSpace.count(new Object()));

		logger.info("assert the original object is still in cache");
		Assert.assertNotNull("gold medal 0 is not in space",
				gigaSpace.read(new GoldMedal(0)));
		logger.info("Test Passed");

	}

	@Test
	public void evictionByFifoOrderTest() throws Exception  {
		logger.info("write high priority object");
		logger.info("fill the cache");

		for (int i = 0; i < cacheSize; i++) {
			gigaSpace.write(new BronzeMedal(i));
		}
		for (int i = 0; i < cacheSize; i++) {
			gigaSpace.write(new SilverMedal(i));
			Assert.assertNull("BronzeMedal " + i + " was not evicted from space", gigaSpace.read(new BronzeMedal(i)));
			assertNextOneStillInSpace(i);
		}

		logger.info("Test Passed");

	}

	protected void assertNextOneStillInSpace(int i) {
		if(i < cacheSize -1)
			Assert.assertNotNull("BronzeMedal " + (i + 1) + " was evicted from space", gigaSpace.read(new BronzeMedal(i + 1)));
	}

	@Test
	public void priorityOrderWriteMultiTest() throws Exception  {
		logger.info("same as previous test only uses writeMultiple");
		logger.info("write high priority object");
		gigaSpace.write(new GoldMedal(0));

		logger.info("fill the cache with ten times its size of lower priority objects");

		Medal[] medals = new Medal[ENTRY_NUM/2];
		for (int i = 0; i < cacheSize * 10; i += ENTRY_NUM/2) {
			for (int j = i; j < ENTRY_NUM/2 + i; j++) {
				if(i % 2 == 0) 
					medals[j - i] = new SilverMedal(j);
				else
					medals[j -i] = new BronzeMedal(j);
			}
			gigaSpace.writeMultiple(medals);
		}


		Assert.assertEquals("amount of objects in space is larger than cache size",
				cacheSize, gigaSpace.count(new Object()));

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
		logger.info("assert objects only amount to cache size");		
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
					try{
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
					}catch(SpaceMemoryShortageException e){}
				}
			});
		}
		threadPool.shutdown();
		threadPool.awaitTermination(1, TimeUnit.MINUTES);
		assertAfterMultipleOperations();
		logger.info("Test Passed");
	}

	protected void assertAfterMultipleOperations() {

		int goldCount = gigaSpace.count(new GoldMedal());
		int silverCount = gigaSpace.count(new SilverMedal());
		int bronzeCount = gigaSpace.count(new BronzeMedal());
		logger.info("gold: " + goldCount + ", silver: " + silverCount	+ ", bronze: " + bronzeCount);
		Assert.assertTrue("more silver or bronze than gold, gold: " + goldCount + ", silver: " + silverCount
				+ ", bronze: " + bronzeCount,
				goldCount >= silverCount && 
				//since we do not evict entries that are written to full cache it is possible that up to NUM_OF_THREADS
				//of lower priority would be written instead of higher priority entries
				(silverCount + NUM_OF_THREADS) >= bronzeCount);
	}

	@Test
	public void loadTest() throws InterruptedException {
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
		assertAfterMultipleOperations();
		logger.info("Test Passed");
	}

	@Test
	public void loadMultiOperationsTest() throws InterruptedException {
		logger.info("fill the space with entries");		
		final AtomicInteger id = new AtomicInteger(0);
		final long start = System.currentTimeMillis();
		final int minutes = 1;
		ExecutorService threadPool = Executors.newFixedThreadPool(NUM_OF_THREADS);
		for (int i = 0; i < NUM_OF_THREADS; i++) {
			threadPool.execute(new Runnable() {
				@Override
				public void run() {
					while(System.currentTimeMillis() - start < TimeUnit.MINUTES.toMillis(minutes)){
						Medal[] medals = new Medal[ENTRY_NUM];
						for(int i = 0; i < ENTRY_NUM ; i++){
							switch(i % 3){
							case 0:
								medals[i] = new GoldMedal(id.getAndIncrement());
								break;
							case 1:
								medals[i] = new SilverMedal(id.getAndIncrement());
								break;
							case 2:
								medals[i] = new BronzeMedal(id.getAndIncrement());
								break;
							}
						}
						gigaSpace.writeMultiple(medals);
						gigaSpace.readMultiple(new Medal(), ENTRY_NUM/2);
						if(Math.random() < 0.1){
							double take = Math.random();
							if(take < 0.33)
								gigaSpace.takeMultiple(new GoldMedal(), ENTRY_NUM/4);
							else if(take < 0.66)
								gigaSpace.takeMultiple(new SilverMedal(), ENTRY_NUM/4);
							else
								gigaSpace.takeMultiple(new BronzeMedal(), ENTRY_NUM/4);
						}
						if(Math.random() < 0.1)
							id.set(id.intValue() / 2);
					}
				}
			});
		}
		threadPool.shutdown();
		threadPool.awaitTermination(minutes + 1, TimeUnit.MINUTES);
		assertAfterMultipleOperations();
		logger.info("Test Passed");
	}

	//@Test
	public void memoryShortageTest() throws InterruptedException, ExecutionException {
		logger.info("memory shortage test");
		final AtomicInteger id = new AtomicInteger();
		long maxMemory = Runtime.getRuntime().maxMemory();
		final int weight = (int) (maxMemory  / cacheSize) ;
		final long start = System.currentTimeMillis();
		final int minutes = 1;
		ExecutorService threadPool = Executors.newFixedThreadPool(NUM_OF_THREADS);
		List<Future<Boolean>> results = new ArrayList<Future<Boolean>>();
		for (int i = 0; i < NUM_OF_THREADS; i++){
			Future<Boolean> result = threadPool.submit(new Callable<Boolean>(){			
				@Override
				public Boolean call() {
					boolean ans = false;
					int i = 0;
					while(System.currentTimeMillis() - start < TimeUnit.MINUTES.toMillis(minutes)){
						try{
							i++;
							Medal toWrite;	
							if(i % 3 == 0)
								toWrite = new GoldMedal(id.incrementAndGet());
							else if (i % 3 == 1)
								toWrite = new SilverMedal(id.incrementAndGet());
							else
								toWrite = new BronzeMedal(id.incrementAndGet());
							toWrite.setWeight(new byte[weight]);
							gigaSpace.write(toWrite);
						}
						catch(SpaceMemoryShortageException e){
							ans = true;
							continue;
						}
					}
					return ans;					
				}
			});
			results.add(result);
		}
		threadPool.shutdown();
		threadPool.awaitTermination(minutes, TimeUnit.MINUTES);
		boolean gotShortage = false;
		Iterator<Future<Boolean>> iterator = results.iterator();
		while (iterator.hasNext() && !gotShortage) {
			gotShortage |= iterator.next().get();
		}
		Assert.assertTrue("did not get memory shortage", gotShortage);
		try{
			assertMemoryShortageTest();
		}
		catch(SpaceMemoryShortageException e){}
	}

	protected abstract void assertMemoryShortageTest();


}