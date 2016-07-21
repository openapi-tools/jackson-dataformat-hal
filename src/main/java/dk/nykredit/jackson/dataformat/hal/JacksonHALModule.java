package dk.nykredit.jackson.dataformat.hal;

import com.fasterxml.jackson.databind.module.SimpleModule;
import dk.nykredit.jackson.dataformat.hal.annotation.EmbeddedResource;
import dk.nykredit.jackson.dataformat.hal.annotation.Link;
import dk.nykredit.jackson.dataformat.hal.annotation.Resource;
import dk.nykredit.jackson.dataformat.hal.ser.HALBeanSerializerModifier;

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
        context.addBeanSerializerModifier(new HALBeanSerializerModifier());
    }
}
