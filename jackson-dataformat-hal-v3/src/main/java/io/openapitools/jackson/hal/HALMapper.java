package io.openapitools.jackson.hal;

import tools.jackson.databind.json.JsonMapper;

public class HALMapper {

    private HALMapper() {}

    public static JsonMapper.Builder builder() {
        return JsonMapper.builder()
            .addModule(new JacksonHALModule());
    }

    public static JsonMapper create() {
        return builder() .build();
    }
}
