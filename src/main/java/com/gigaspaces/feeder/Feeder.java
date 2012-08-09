package com.gigaspaces.feeder;



import javax.annotation.PostConstruct;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.annotation.Autowired;

import com.gigaspaces.data.BronzeMedal;
import com.gigaspaces.data.GoldMedal;
import com.gigaspaces.data.SilverMedal;

public class Feeder {
	@Autowired
	private GigaSpace gigaSpace;
	private int cacheSize;
	
	//mean trick
	private static Logger logger = Logger.getLogger(new Object(){}.getClass().getEnclosingClass());

	@PostConstruct
	public void startFeeding()  {
		logger.info("Feeder started");
		
		
		logger.info("test 1 - assert lru order");
		gigaSpace.write(new BronzeMedal(0));
		for (int i = 1; i < cacheSize; i++) {
			gigaSpace.write(new GoldMedal(i));
		}
		gigaSpace.write(new BronzeMedal(100));
		Assert.assertNull("BronzeMedal 0 was not evicted", gigaSpace.read(new BronzeMedal(0)));
		logger.info("Test Passed 1");
		
		gigaSpace.clear(new Object());
		
		logger.info("test 2 - assert only gold remains");
		for (int i = 0; i < (cacheSize * 10) + 1; i++) {
			if(i % 2 == 0)
				gigaSpace.write(new GoldMedal(i));
			else
				gigaSpace.write(new SilverMedal(i));
		}
		Assert.assertTrue("amount of objects in space is larger then cache size",
				gigaSpace.count(new Object()) == cacheSize);
		Assert.assertTrue("not all objects in space are of the highest priority",
				gigaSpace.count(new Object()) == gigaSpace.count(new GoldMedal()));

		logger.info("Test Passed 2");

		gigaSpace.clear(new Object());
		
		logger.info("test 3 - multi threaded");
		
		logger.info("test 4 - out of memory");
		
		
		
	}



	public int getCacheSize() {
		return cacheSize;
	}

	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
	}
}
