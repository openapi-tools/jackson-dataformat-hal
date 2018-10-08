package io.openapitoools.jackson.dataformat.hal.deser;

import io.openapitools.jackson.dataformat.hal.deser.CurieMapping;
import java.net.URI;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CurieMappingTest {

    @Test
    public void testBasicSubstitution() {
        CurieMapping cm = new CurieMapping(new CurieMapping.Mapping("prefix", "https://www.example.com/doc/{rel}"));
        URI uri = cm.resolve("prefix:reference").get();
        assertEquals(URI.create("https://www.example.com/doc/reference"), uri);
    }

}
