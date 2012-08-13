package org.openspaces.eviction.test;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.annotation.Autowired;

public class AbstractClassBasedEvictionTest {

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

}