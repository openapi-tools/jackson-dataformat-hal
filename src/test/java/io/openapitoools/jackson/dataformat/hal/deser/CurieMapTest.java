package io.openapitoools.jackson.dataformat.hal.deser;

import io.openapitools.jackson.dataformat.hal.deser.CurieMap;
import java.net.URI;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CurieMapTest {

    @Test
    public void testBasicSubstitution() {
        CurieMap cm = new CurieMap(new CurieMap.Mapping("prefix", "https://www.example.com/doc/{rel}"));
        URI uri = cm.resolve("prefix:reference").get();
        assertEquals(URI.create("https://www.example.com/doc/reference"), uri);
    }

}
