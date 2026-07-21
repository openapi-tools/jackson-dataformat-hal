package io.openapitools.jackson.hal.ser;

import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.ValueSerializer;
import io.openapitools.jackson.dataformat.hal.annotation.Resource;
import tools.jackson.databind.ser.ValueSerializerModifier;
import tools.jackson.databind.ser.bean.BeanSerializerBase;

/**
 * Modifier ensuring that beans annotated with {@link Resource} is handled by the {@link HALBeanSerializer}.
 */
public class HALBeanSerializerModifier extends ValueSerializerModifier {
    @Override
    public ValueSerializer<?> modifySerializer(
        SerializationConfig config,
        BeanDescription.Supplier beanDesc,
        ValueSerializer<?> serializer
    ) {
        Resource ann = beanDesc.getClassAnnotations().get(Resource.class);
        if (ann != null && serializer instanceof BeanSerializerBase) {
            return new HALBeanSerializer((BeanSerializerBase) serializer, beanDesc);
        }
        return serializer;
    }
}
