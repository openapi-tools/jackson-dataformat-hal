package io.openapitools.jackson.dataformat.hal.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation specifying an array of curies to be used in defining link relations.
 */
@Target({ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Curies {

    /**
     * Annotation grouping a list of {@link Curies} for convenience/readability
     * @return an array of curies
     */
    Curie[] value() default {};

}
