package io.openapitools.jackson.dataformat.hal.ser;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import io.openapitools.jackson.dataformat.hal.annotation.Resource;

/**
 * Modifier ensuring that beans annotated with {@link Resource} is handled by the {@link HALBeanSerializer}.
 */
public class HALBeanSerializerModifier extends BeanSerializerModifier {
    @Override
    public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
        Resource ann = beanDesc.getClassAnnotations().get(Resource.class);
        if (ann != null) {
            return new HALBeanSerializer((BeanSerializer) serializer);
        }
        return serializer;
    }
}
