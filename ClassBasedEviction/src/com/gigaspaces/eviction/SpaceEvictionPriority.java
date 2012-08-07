package com.gigaspaces.eviction;


import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation for setting the eviction priority of a class 
 * 
 * @author Sagi Bernstein
 * @since 9.1.0
 *
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface SpaceEvictionPriority {
	int priority();
	OrderBy orderBy() default OrderBy.LRU;
}

