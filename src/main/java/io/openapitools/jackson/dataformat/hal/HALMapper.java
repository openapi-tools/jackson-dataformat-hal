package io.openapitools.jackson.dataformat.hal;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Customized {@link ObjectMapper} that will serialize objects into the HAL format if properly annotated.
 */
public class HALMapper extends ObjectMapper {
    protected static final JacksonHALModule DEFAULT_HAL_MODULE = new JacksonHALModule();
    
    /**
     * Create new HAL mapper with default configuration.
     */
    public HALMapper() {
        registerModule(DEFAULT_HAL_MODULE);
    }

    /**
     * Create new HAL mapper with CURIE support.
     */
    public HALMapper(CURIEProvider curieProvider) {
        registerModule(new JacksonHALModule(curieProvider));
    }

    /**
     * Copy constructor to support {@link #copy()}.
     * @param mapper Mapper to copy.
     */
    public HALMapper(HALMapper mapper) {
        super(mapper);
        registerModule(DEFAULT_HAL_MODULE);
    }

    @Override
    public ObjectMapper copy() {
        return new HALMapper(this);
    }
}
