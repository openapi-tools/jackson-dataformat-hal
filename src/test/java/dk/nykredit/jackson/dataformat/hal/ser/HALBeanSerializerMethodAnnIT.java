package dk.nykredit.jackson.dataformat.hal.ser;

import com.fasterxml.jackson.databind.JsonNode;
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
public class HALBeanSerializerMethodAnnIT {
    ObjectMapper om = new HALMapper();

    @Test
    public void test() throws IOException {
        TestResource resource = new TestResource();
        resource.setSelf(new HALLink.Builder("http://self.url").build());
        resource.setRelatedLink1(new HALLink.Builder("http://other.link.url").build());
        resource.setName("POJO name");
        resource.setAuxData("added information");

        final String json = om.writeValueAsString(resource);

        // Verify generated json
        JsonNode jsonNode = new ObjectMapper().readTree(json);

        JsonNode links = jsonNode.path("_links");
        assertFalse(links.isMissingNode());
        assertEquals(2, links.size());

        JsonNode embedded = jsonNode.path("_embedded");
        assertFalse(embedded.isMissingNode());
        assertEquals(1, embedded.size());

        assertEquals("POJO name", jsonNode.get("name").asText());
    }

    @Resource
    public static class TestResource {
        // Guard against Jackson unintentionally using class fields for serialization.
        private final Map<String, Object> fields = new HashMap<>();

        public void setName(String name) {
            fields.put("name", name);
        }

        public String getName() {
            return (String) fields.get("name");
        }

        @Link
        public HALLink getSelf() {
            return (HALLink) fields.get("self");
        }

        public void setSelf(HALLink link) {
            fields.put("self", link);
        }

        public HALLink getRelatedLink1() {
            return (HALLink) fields.get("friend1");
        }

        @Link("relative1")
        public void setRelatedLink1(HALLink link) {
            fields.put("friend1", link);
        }

        @EmbeddedResource
        public void setAuxData(String auxData) {
            fields.put("auxdata", auxData);
        }

        public String getAuxData() {
            return (String) fields.get("auxdata");
        }
    }
}

