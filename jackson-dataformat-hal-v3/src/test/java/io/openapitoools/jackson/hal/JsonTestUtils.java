package io.openapitoools.jackson.hal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import io.openapitools.jackson.hal.HALMapper;

/**
 * Shared test utilities for JSON assertion.
 */
public final class JsonTestUtils {

    private JsonTestUtils() {}

    /**
     * Order-agnostic JSON comparison: parses both strings as maps and compares them.
     * This avoids brittleness from property ordering differences between Jackson versions.
     */
    @SuppressWarnings("unchecked")
    public static void assertJsonEquals(String expected, String actual) throws Exception {
        Map<String, Object> expectedMap = HALMapper.create().readValue(expected, Map.class);
        Map<String, Object> actualMap = HALMapper.create().readValue(actual, Map.class);
        assertEquals(expectedMap, actualMap);
    }
}

