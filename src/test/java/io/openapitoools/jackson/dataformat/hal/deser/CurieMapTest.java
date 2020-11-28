package io.openapitoools.jackson.dataformat.hal.deser;

import java.net.URI;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.openapitools.jackson.dataformat.hal.deser.CurieMap;

public class CurieMapTest {

    @Test
    public void testBasicSubstitution() {
        CurieMap cm = new CurieMap(new CurieMap.Mapping("prefix", "https://www.example.com/doc/{rel}"));
        URI uri = cm.resolve("prefix:reference").get();
        Assertions.assertEquals(URI.create("https://www.example.com/doc/reference"), uri);
    }

}
