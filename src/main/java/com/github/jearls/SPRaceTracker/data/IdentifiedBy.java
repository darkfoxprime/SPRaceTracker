package com.github.jearls.SPRaceTracker.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This tells the importer and exporter how to recognize or generate an ID for
 * an object, since we don't want to necessarily store the internal IDs.
 * 
 * @author jearls
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface IdentifiedBy {
    String[] value();
}
