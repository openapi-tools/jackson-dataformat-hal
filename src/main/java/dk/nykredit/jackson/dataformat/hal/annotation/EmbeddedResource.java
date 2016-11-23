package dk.nykredit.jackson.dataformat.hal.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marking field in a resource as embedded resource.
 */
@Target({ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface EmbeddedResource {

    /**
     * Relation name - if not set the property name will be used.
     * @return The name of the relation realized by the embedded resource.
     */
    String value() default "";

}
