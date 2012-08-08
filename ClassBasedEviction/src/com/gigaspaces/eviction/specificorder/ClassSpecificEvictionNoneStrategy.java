package com.gigaspaces.eviction.specificorder;

import com.gigaspaces.server.eviction.EvictionStrategy;

/**
 * This is an empty extension of the {@link EvictionStrategy} class
 * it is solely to make it possible to instantiate an empty implementation
 * this is needed in {@link ClassSpecificEvictionStrategy} to provide the none strategy
 * 
 * @author Sagi Bernstein
 * @since 9.1.0
 */
public class ClassSpecificEvictionNoneStrategy extends EvictionStrategy{

}
