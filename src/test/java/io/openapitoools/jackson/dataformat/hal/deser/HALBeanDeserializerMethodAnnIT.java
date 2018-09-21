package io.openapitoools.jackson.dataformat.hal.deser;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.openapitools.jackson.dataformat.hal.HALLink;
import io.openapitools.jackson.dataformat.hal.HALMapper;
import io.openapitools.jackson.dataformat.hal.annotation.Curie;
import io.openapitools.jackson.dataformat.hal.annotation.EmbeddedResource;
import io.openapitools.jackson.dataformat.hal.annotation.Link;
import io.openapitools.jackson.dataformat.hal.annotation.Resource;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Verify that HAL annotations can be used on methods as well as fields.
 */
public class HALBeanDeserializerMethodAnnIT {
    ObjectMapper om = new HALMapper();

    @Test
    public void test() throws IOException {
        final String json = "{" +
                "  \"name\": \"POJO name\"," +
                "  \"_links\": {" +
                "    \"curies\": [{" +
                "      \"href\": \"http://my.example.com/doc/{rel}\"," +
                "      \"name\": \"cur1\"," +
                "      \"templated\": true" +
                "    }]," +
                "    \"self\": {" +
                "      \"href\": \"http://self.url\"," +
                "      \"templated\": false" +
                "    }," +
                "    \"link1\": {" +
                "      \"href\": \"http://other.link.url\"," +
                "      \"templated\": true" +
                "    }," +
                "    \"cur1:link2\": {" +
                "      \"href\": \"http://link2.url\"," +
                "      \"templated\": true" +
                "    }" +
                "  }," +
                "  \"_embedded\": {" +
                "    \"extras\": \"the more you know\"" +
                "  }" +
                "}";


        TestResource resource = om.readValue(json, TestResource.class);
        assertEquals("POJO name", resource.fields.get("name"));

        HALLink self = (HALLink) resource.fields.get("self");
        assertEquals("http://self.url", self.getHref());
        assertFalse(self.getTemplated());

        HALLink other = (HALLink) resource.fields.get("otherlink");
        assertEquals("http://other.link.url", other.getHref());

        assertEquals("the more you know", resource.fields.get("extras"));
    }


    @Curie(curie = "cur1", href = "http://my.example.com/doc/{rel}")
    @Resource
    public static class TestResource {
        // Guard against Jackson unintentionally using class fields for deserialization.
        private Map<String, Object> fields = new HashMap<>();

        public void setName(String name) {
            fields.put("name", name);
        }

        @Link("self")
        public void setSelfLink(HALLink link) {
            fields.put("self", link);
        }

        @Link("link1")
        public void setOtherlink(HALLink link) {
            fields.put("otherlink", link);
        }

        @Link(value = "link2", curie = "cur1")
        public void setLink2(HALLink link) {
            fields.put("http://link2.url", link);
        }

        @EmbeddedResource("extras")
        public void setExtras(String extras) {
            fields.put("extras", extras);
        }
    }
}
