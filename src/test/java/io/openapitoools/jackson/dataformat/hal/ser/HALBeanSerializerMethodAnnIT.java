package io.openapitoools.jackson.dataformat.hal.ser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.openapitools.jackson.dataformat.hal.HALLink;
import io.openapitools.jackson.dataformat.hal.HALMapper;
import io.openapitools.jackson.dataformat.hal.annotation.Curie;
import io.openapitools.jackson.dataformat.hal.annotation.Curies;
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
public class HALBeanSerializerMethodAnnIT {
    ObjectMapper om = new HALMapper();

    @Test
    public void test() throws IOException {
        TestResource resource = new TestResource();
        resource.setSelf(new HALLink.Builder("http://self.url").build());
        resource.setRelatedLink1(new HALLink.Builder("http://other.link.url").build());
        resource.setRelatedLink2(new HALLink.Builder("http://other.link.url2").build());
        resource.setName("POJO name");
        resource.setAuxData("added information");

        final String json = om.writeValueAsString(resource);

        // Verify generated json
        JsonNode jsonNode = new ObjectMapper().readTree(json);

        JsonNode links = jsonNode.path("_links");
        assertFalse(links.isMissingNode());
        assertEquals(4, links.size());

        System.out.println(links);

        JsonNode relative1UsingCurie = links.get("cur1:relative2");
        assertFalse(relative1UsingCurie.isMissingNode());

        JsonNode curies = links.get("curies");
        assertFalse(curies.isMissingNode());
        assertEquals(1, curies.size());

        JsonNode embedded = jsonNode.path("_embedded");
        assertFalse(embedded.isMissingNode());
        assertEquals(1, embedded.size());

        assertEquals("POJO name", jsonNode.get("name").asText());
    }

    @Test
    public void singleCurieTest() throws Exception {

        // test support for @Curie as opposed to @Curies

        SingleCurieResource resource = new SingleCurieResource();
        resource.setRelatedLink1(new HALLink.Builder("http://other.link.url").build());

        final String json = om.writeValueAsString(resource);

        // Verify generated json
        JsonNode jsonNode = new ObjectMapper().readTree(json);

        JsonNode links = jsonNode.path("_links");
        assertFalse(links.isMissingNode());
        assertEquals(2, links.size());

        JsonNode relative1UsingCurie = links.get("cur1:relative1");
        assertFalse(relative1UsingCurie.isMissingNode());

        JsonNode curies = links.get("curies");
        assertFalse(curies.isMissingNode());
        assertEquals(1, curies.size());
    }

    @Resource
    @Curies({@Curie(href = "http://docs.my.site/{rel}", curie = "cur1"),
             @Curie(href = "http://docs.my.site/{rel}", curie = "cur2-not-used")})
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

        public HALLink getRelatedLink2() {
            return (HALLink) fields.get("friend2");
        }

        @Link("relative1")
        public void setRelatedLink1(HALLink link) {
            fields.put("friend1", link);
        }

        @Link(value = "relative2", curie = "cur1")
        public void setRelatedLink2(HALLink link) {
            fields.put("friend2", link);
        }

        @EmbeddedResource
        public void setAuxData(String auxData) {
            fields.put("auxdata", auxData);
        }

        public String getAuxData() {
            return (String) fields.get("auxdata");
        }
    }

    // test support for @Curie as opposed to @Curies
    @Resource
    @Curie(href = "http://docs.my.site/{rel}", curie = "cur1")
    public static class SingleCurieResource {

        private final Map<String, Object> fields = new HashMap<>();

        public HALLink getRelatedLink1() {
            return (HALLink) fields.get("friend1");
        }

        @Link(value = "relative1", curie = "cur1")
        public void setRelatedLink1(HALLink link) {
            fields.put("friend1", link);
        }

    }
}

