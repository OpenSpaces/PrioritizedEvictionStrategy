package com.gigaspaces.feeder;



import javax.annotation.PostConstruct;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.annotation.Autowired;

import com.gigaspaces.data.BronzeMedal;
import com.gigaspaces.data.GoldMedal;

public class Feeder {
	@Autowired
	private GigaSpace gigaSpace;
	private int cacheSize;
	
	//mean trick
	private static Logger logger = Logger.getLogger(new Object(){}.getClass().getEnclosingClass());

	@PostConstruct
	public void startFeeding()  {
		logger.info("Feeder started");
		gigaSpace.write(new BronzeMedal());
		for (int i = 0; i < cacheSize; i++) {
			gigaSpace.write(new GoldMedal());
		}
		Assert.assertNull("BronzeMedal was not evicted", gigaSpace.read(new BronzeMedal()));
		logger.info("Test Passed!!");
	}



	public int getCacheSize() {
		return cacheSize;
	}

	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
	}
}
