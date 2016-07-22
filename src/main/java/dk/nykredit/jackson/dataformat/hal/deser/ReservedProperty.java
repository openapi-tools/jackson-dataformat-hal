package dk.nykredit.jackson.dataformat.hal.deser;

import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import dk.nykredit.jackson.dataformat.hal.annotation.EmbeddedResource;
import dk.nykredit.jackson.dataformat.hal.annotation.Link;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Modelling reserved HAL properties namely <code>_links</code> and <code>_embedded</code>.
 */
public enum ReservedProperty {
    
    LINKS("_links", Link.class), EMBEDDED("_embedded", EmbeddedResource.class);
    
    private final String name;
    private final UUID prefix = UUID.randomUUID();
    private final Class annotation;
    private final Method valueMethod;

    private ReservedProperty(String name, Class annotation) {
        this.name = name;
        this.annotation = annotation;
        try {
            valueMethod = annotation.getDeclaredMethod("value");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
    
    public String getPropertyName() {
        return name;
    }
    
    public String alternateName(AnnotatedField af, String originalName) {
        Object o = af.getAnnotation(annotation);
        if (o != null) {
            try {
                String alternateName = (String) valueMethod.invoke(o);
                return alternateName(alternateName.isEmpty() ? originalName : alternateName);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return originalName;
    }
    
    public String alternateName(String originalName) {
        return prefix.toString() + ":" + originalName;
    }
    
}
