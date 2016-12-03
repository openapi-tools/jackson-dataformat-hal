package dk.nykredit.jackson.dataformat.hal.deser;


import com.fasterxml.jackson.databind.ObjectMapper;
import dk.nykredit.jackson.dataformat.hal.HALLink;
import dk.nykredit.jackson.dataformat.hal.HALMapper;
import dk.nykredit.jackson.dataformat.hal.annotation.EmbeddedResource;
import dk.nykredit.jackson.dataformat.hal.annotation.Link;
import dk.nykredit.jackson.dataformat.hal.annotation.Resource;
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
                "    \"self\": {" +
                "      \"href\": \"http://self.url\"," +
                "      \"templated\": false" +
                "    }," +
                "    \"link1\": {" +
                "      \"href\": \"http://other.link.url\"," +
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

        @EmbeddedResource("extras")
        public void setExtras(String extras) {
            fields.put("extras", extras);
        }
    }
}
