package dk.nykredit.jackson.dataformat.hal.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@link dk.nykredit.jackson.dataformat.hal.HALLink} instance or collection for inclusion in the _links of the resource.
 */
@Target({ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Link {
    /**
     * Relation name - if not set the property name will be used.
     */
    String value() default "";
}
