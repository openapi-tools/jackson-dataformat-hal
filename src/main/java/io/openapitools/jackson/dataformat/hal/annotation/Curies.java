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
public @interface Curies {

    /**
     * Annotation grouping a list of {@link Curies} for convenience/readability
     */
    Curie[] value() default {};

}
