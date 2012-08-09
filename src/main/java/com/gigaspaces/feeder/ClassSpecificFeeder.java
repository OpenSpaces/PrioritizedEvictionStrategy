package com.gigaspaces.feeder;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.annotation.Autowired;

public class ClassSpecificFeeder {
	@Autowired
	private GigaSpace gigaSpace;
	private int cacheSize;
	
	//mean trick
	private static Logger logger = Logger.getLogger(new Object(){}.getClass().getEnclosingClass());

	@PostConstruct
	public void startFeeding()  {
		logger.info(cacheSize);
	}

}
