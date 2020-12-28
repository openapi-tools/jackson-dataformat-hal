package io.openapitools.jackson.dataformat.hal;

import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.cfg.Annotations;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

@Named
@Provider
@Produces(JacksonHALJsonProvider.MEDIA_TYPE_APPLICATION_HAL_JSON)
@Consumes(JacksonHALJsonProvider.MEDIA_TYPE_APPLICATION_HAL_JSON)
public class JacksonHALJsonProvider extends JacksonJsonProvider{
    public static final String MEDIA_TYPE_APPLICATION_HAL_JSON = "application/hal+json";
    public static final JacksonHALModule JACKSON_HAL_MODULE = new JacksonHALModule();

    public JacksonHALJsonProvider() {
	super();
	modifyDefaultMapperForHALJson();
    }

    public JacksonHALJsonProvider(Annotations[] annotationsToUse) {
	super(annotationsToUse);
	modifyDefaultMapperForHALJson();
    }

    public JacksonHALJsonProvider(ObjectMapper mapper) {
	super(mapper);
	modifyConfiguredMapperForHALJson();
    }

    public JacksonHALJsonProvider(ObjectMapper mapper, Annotations[] annotationsToUse) {
	super(mapper, annotationsToUse);
	modifyConfiguredMapperForHALJson();
    }

    /**
     * Always return a copy of the ObjectMapper provided by the JAX-RS provider.
     *
     * This is necessary since this JacksonHALJsonProvider reconfigures the mapper
     * to specifically output the HAL+JSON format. Using a copy of the existing
     * mapper allows the original mapper to still be used for plain JSON output.
     */
    @Override
    protected ObjectMapper _locateMapperViaProvider(Class<?> type, MediaType mediaType)
    {
	ObjectMapper mapper = super._locateMapperViaProvider(type, mediaType);
	if(mapper == null) {
	    return null;
	}
	return mapper.copy();
    }

    private void modifyDefaultMapperForHALJson() {
        _mapperConfig.setMapper(_mapperConfig.getDefaultMapper().registerModule(JACKSON_HAL_MODULE));
    }

    private void modifyConfiguredMapperForHALJson() {
        _mapperConfig.setMapper(_mapperConfig.getConfiguredMapper().registerModule(JACKSON_HAL_MODULE));
    }
}
