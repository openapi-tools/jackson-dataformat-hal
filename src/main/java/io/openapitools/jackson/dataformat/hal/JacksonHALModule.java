package io.openapitools.jackson.dataformat.hal;

import com.fasterxml.jackson.databind.module.SimpleModule;
import io.openapitools.jackson.dataformat.hal.annotation.EmbeddedResource;
import io.openapitools.jackson.dataformat.hal.annotation.Link;
import io.openapitools.jackson.dataformat.hal.annotation.Resource;
import io.openapitools.jackson.dataformat.hal.deser.HALBeanDeserializerModifier;
import io.openapitools.jackson.dataformat.hal.ser.HALBeanSerializerModifier;

/**
 * Module registering handlers for the {@link Resource} and {@link EmbeddedResource} annonations as well as the
 * {@link Link} annotation.
 */
public class JacksonHALModule extends SimpleModule {

    private final CURIEProvider curieProvider;

    public JacksonHALModule() {
        super("JacksonHALModule", PackageVersion.VERSION);
        curieProvider = null;
    }

    public JacksonHALModule(CURIEProvider curieProvider) {
        super("JacksonHALModule", PackageVersion.VERSION);
        this.curieProvider = curieProvider;
    }

    @Override
    public void setupModule(SetupContext context) {
        context.addBeanSerializerModifier(new HALBeanSerializerModifier(curieProvider));
        context.addBeanDeserializerModifier(new HALBeanDeserializerModifier());
    }
}
