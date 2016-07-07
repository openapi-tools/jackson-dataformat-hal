package dk.nykredit.jackson.dataformat.hal;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import dk.nykredit.jackson.dataformat.hal.ser.HALBeanSerializerModifier;

/**
 * Module registering handlers for the {@link Resource} and {@link EmbeddedResource} annonations as well as the
 * NIF {@link dk.nykredit.nif.ws.rs.Link} class.
 */
public class JacksonHALModule extends SimpleModule {
    public JacksonHALModule() {
        super("json-hal-module", new Version(1, 0, 0, null, "dk.nykredit.nif", "nif-ws"));
        setSerializerModifier(new HALBeanSerializerModifier());
    }
}
