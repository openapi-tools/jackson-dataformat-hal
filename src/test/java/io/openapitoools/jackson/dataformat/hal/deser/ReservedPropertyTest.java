package io.openapitoools.jackson.dataformat.hal.deser;

import io.openapitools.jackson.dataformat.hal.deser.ReservedProperty;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import io.openapitools.jackson.dataformat.hal.annotation.Link;
import java.lang.reflect.Field;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReservedPropertyTest {

    @Test
    public void testAlternateNameNone() {
        BeanPropertyDefinition bpd = mock(BeanPropertyDefinition.class);

        String alternateName = ReservedProperty.LINKS.alternateName(bpd, "orig-name");

        assertEquals("orig-name", alternateName);
    }

    @Test
    public void testAlternateNameFieldNoAnnotation() throws Exception {
        BeanPropertyDefinition bpd = mock(BeanPropertyDefinition.class);
        AnnotatedField af = new AnnotatedField(null, POJO.class.getDeclaredField("bareField"), null);
        when(bpd.getField()).thenReturn(af);

        String alternateName = ReservedProperty.LINKS.alternateName(bpd, "orig-name");

        assertEquals("orig-name", alternateName);
    }

    @Test
    public void testAlternateNameFieldNoValue() throws Exception {
        BeanPropertyDefinition bpd = mock(BeanPropertyDefinition.class);

        final Field field = POJO.class.getDeclaredField("annotatedField");
        AnnotationMap am = new AnnotationMap();
        am.addIfNotPresent(field.getAnnotation(Link.class));
        AnnotatedField af = new AnnotatedField(null, field, am);
        when(bpd.getField()).thenReturn(af);

        String alternateName = ReservedProperty.LINKS.alternateName(bpd, "orig-name");

        assertTrue(alternateName.matches(".+:orig-name"));
    }

    @Test
    public void testAlternateNameField() throws Exception {
        BeanPropertyDefinition bpd = mock(BeanPropertyDefinition.class);

        final Field field = POJO.class.getDeclaredField("annotatedFieldWithValue");
        AnnotationMap am = new AnnotationMap();
        am.addIfNotPresent(field.getAnnotation(Link.class));
        AnnotatedField af = new AnnotatedField(null, field, am);
        when(bpd.getField()).thenReturn(af);

        String alternateName = ReservedProperty.LINKS.alternateName(bpd, "orig-name");

        assertTrue(alternateName.matches(".+:alternate-name"));
    }

    static class POJO {

        String bareField;

        @Link
        String annotatedField;

        @Link(value = "alternate-name")
        String annotatedFieldWithValue;
    }

}
