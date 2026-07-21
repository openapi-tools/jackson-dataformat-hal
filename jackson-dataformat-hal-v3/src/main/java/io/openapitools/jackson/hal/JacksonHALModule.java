package io.openapitools.jackson.hal;

import io.openapitools.jackson.dataformat.hal.annotation.EmbeddedResource;
import io.openapitools.jackson.dataformat.hal.annotation.Link;
import io.openapitools.jackson.dataformat.hal.annotation.Resource;
import io.openapitools.jackson.hal.deser.HALBeanDeserializerModifier;
import io.openapitools.jackson.hal.ser.HALBeanSerializerModifier;
import tools.jackson.databind.module.SimpleModule;

/**
 * Module registering handlers for the {@link Resource} and {@link EmbeddedResource} annonations as well as the
 * {@link Link} annotation.
 */
public class JacksonHALModule extends SimpleModule {

    public JacksonHALModule() {
        super("JacksonHALModule", PackageVersion.VERSION);
    }

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        context.addSerializerModifier(new HALBeanSerializerModifier());
        context.addDeserializerModifier(new HALBeanDeserializerModifier());
    }
}
