package io.openapitools.jackson.dataformat.hal.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation specifying a CURIE for use with links
 */
@Target({ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Curie {

    /**
     * CURIE href template e.g. "http://docs.my.site/{rel}"
     */
    String href() default "";

    /**
     * CURIE name used to reference the CURIE in {@link Link} annotations
     * e.g. "mysite"
     */
    String curie() default "";

}
