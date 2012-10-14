package org.openspaces.test.db;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.openspaces.core.GigaSpace;
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
	
	
	
}
