package io.openapitoools.jackson.dataformat.hal.deser;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

import io.openapitools.jackson.dataformat.hal.annotation.Link;
import io.openapitools.jackson.dataformat.hal.deser.CurieMap;
import io.openapitools.jackson.dataformat.hal.deser.ReservedProperty;

public class ReservedPropertyTest {

    @Test
    public void testAlternateNameNone() {
        CurieMap cm = mock(CurieMap.class);
        BeanPropertyDefinition bpd = mock(BeanPropertyDefinition.class);
        when(bpd.getName()).thenReturn("orig-name");

        String alternateName = ReservedProperty.LINKS.alternateName(bpd,cm );

        Assertions.assertEquals("orig-name", alternateName);
    }

    @Test
    public void testAlternateNameFieldNoAnnotation() throws Exception {
        CurieMap cm = mock(CurieMap.class);
        BeanPropertyDefinition bpd = mock(BeanPropertyDefinition.class);
        AnnotatedField af = new AnnotatedField(null, POJO.class.getDeclaredField("bareField"), null);
        when(bpd.getField()).thenReturn(af);
        when(bpd.getName()).thenReturn("orig-name");

        String alternateName = ReservedProperty.LINKS.alternateName(bpd, cm);

        Assertions.assertEquals("orig-name", alternateName);
    }

    @Test
    public void testAlternateNameFieldNoValue() throws Exception {
        CurieMap cm = mock(CurieMap.class);
        BeanPropertyDefinition bpd = mock(BeanPropertyDefinition.class);

        final Field field = POJO.class.getDeclaredField("annotatedField");
        AnnotationMap am = new AnnotationMap();
        am.addIfNotPresent(field.getAnnotation(Link.class));
        AnnotatedField af = new AnnotatedField(null, field, am);
        when(bpd.getField()).thenReturn(af);
        when(bpd.getName()).thenReturn("orig-name");

        String alternateName = ReservedProperty.LINKS.alternateName(bpd, cm);

        Assertions.assertTrue(alternateName.matches(".+:orig-name"));
    }

    @Test
    public void testAlternateNameField() throws Exception {
        CurieMap cm = mock(CurieMap.class);
        BeanPropertyDefinition bpd = mock(BeanPropertyDefinition.class);

        final Field field = POJO.class.getDeclaredField("annotatedFieldWithValue");
        AnnotationMap am = new AnnotationMap();
        am.addIfNotPresent(field.getAnnotation(Link.class));
        AnnotatedField af = new AnnotatedField(null, field, am);
        when(bpd.getField()).thenReturn(af);
        when(bpd.getName()).thenReturn("orig-name");

        String alternateName = ReservedProperty.LINKS.alternateName(bpd, cm);

        Assertions.assertTrue(alternateName.matches(".+:alternate-name"));
    }

    static class POJO {

        String bareField;

        @Link
        String annotatedField;

        @Link(value = "alternate-name")
        String annotatedFieldWithValue;
    }

}
