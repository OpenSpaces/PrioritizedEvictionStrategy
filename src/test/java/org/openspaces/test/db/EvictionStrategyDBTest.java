package org.openspaces.test.db;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openspaces.core.GigaSpace;
import org.openspaces.eviction.test.data.BronzeMedal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/pu-db.xml"})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class EvictionStrategyDBTest {
	protected static final int NUM_OF_THREADS = 30;
	protected static final int ENTRY_NUM = 20;
	private static final long RUNNING_TIME = 1;
	@Autowired
	protected GigaSpace gigaSpace;
	protected int CACHE_MAX_SIZE = 5;
	protected static Logger logger = Logger.getLogger(new Object(){}.getClass().getEnclosingClass());

	@BeforeClass
	public static  void callGC() {
		System.gc();
	}

	@Before
	public void cleanSpace() {
		gigaSpace.clear(new Object());
	}
	
	@Test
	public void readFromDBTest(){
		for (int i = 0; i < CACHE_MAX_SIZE + 1; i++) {
			gigaSpace.write(new BronzeMedal(i));
		}
		Assert.assertNotNull(gigaSpace.read(new BronzeMedal(CACHE_MAX_SIZE + 1)));
	}

	//@Test
	public void multiThreadedMultiOperationsTest() throws InterruptedException {
		logger.info("fill the space with entries");		
		ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(NUM_OF_THREADS);
		List<ScheduledFuture<?>> futuresList = new ArrayList<ScheduledFuture<?>>();
		for (int i = 0; i < NUM_OF_THREADS; i++) {
			ScheduledFuture<?> scheduleWithFixedDelay = threadPool.scheduleWithFixedDelay(new Runnable() {

				@Override
				public void run(){
					/*switch(i % 3){
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
				}*/
				}
			}, 0, 100, TimeUnit.MILLISECONDS);
			futuresList.add(scheduleWithFixedDelay);
		}
		TimeUnit.MINUTES.sleep(RUNNING_TIME);
		for (ScheduledFuture<?> scheduledFuture : futuresList) {
			Assert.assertTrue(scheduledFuture.cancel(false));
		}

		logger.info("Test Passed");
	}



}
