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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Verify that HAL annotations can be used on methods as well as fields.
 */
public class HALBeanSerializerMethodAnnIT {
    ObjectMapper om = new HALMapper();

    @Test
    public void testCuries() throws IOException {
        TestResource resource = new TestResource();
        resource.setSelf(new HALLink.Builder("http://self.url").build());
        resource.setRelatedLink1(new HALLink.Builder("http://other.link.url").build());
        resource.setRelatedLink2(new HALLink.Builder("http://other.link.url2").build());
        resource.setRelatedLink3(new HALLink.Builder("http://other.link.url3{?id}").build());
        resource.setRelatedLink4(new HALLink.Builder("/other.link.url4{?id}").build());
        resource.setName("POJO name");
        resource.setAuxData("added information");

        final String json = om.writeValueAsString(resource);

        // Verify generated json
        JsonNode jsonNode = new ObjectMapper().readTree(json);

        JsonNode links = jsonNode.path("_links");
        assertFalse(links.isMissingNode());
        assertEquals(6, links.size());

        JsonNode curies = links.get("curies");
        assertFalse(curies.isMissingNode());
        assertEquals(3, curies.size());

        JsonNode relative2UsingCurie = links.get("cur1:relative2");
        assertFalse(relative2UsingCurie.isMissingNode());
        assertEquals("http://other.link.url2", relative2UsingCurie.get("href").asText());
        assertEquals(false, relative2UsingCurie.get("templated").asBoolean());

        JsonNode relative3UsingCurie = links.get("cur2:relative3");
        assertFalse(relative3UsingCurie.isMissingNode());
        assertEquals("http://other.link.url3{?id}", relative3UsingCurie.get("href").asText());
        assertEquals(true, relative3UsingCurie.get("templated").asBoolean());
        
       
        JsonNode relative4UsingCurie = links.get("cur3:relative4");
        assertFalse(relative4UsingCurie.isMissingNode());
        assertEquals("/other.link.url4{?id}", relative4UsingCurie.get("href").asText());
        assertEquals(true, relative4UsingCurie.get("templated").asBoolean());

        JsonNode curie4 = curies.get("cur4-not-used");
        assertNull(curie4);

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

        JsonNode curies = links.get("curies");
        assertFalse(curies.isMissingNode());
        // prune empty which means only 1 exixts
        assertEquals(1, curies.size());
        
        JsonNode curie1 = curies.get(0);
        assertNotNull(curie1);
        assertEquals("cur1", curie1.get("name").asText());
        assertEquals("http://docs.my.site/{rel}", curie1.get("href").asText());
        
        JsonNode relative1UsingCurie = links.get("cur1:relative1");
        assertFalse(relative1UsingCurie.isMissingNode());
    }

    @Resource
    @Curies({@Curie(href = "http://docs.my.site/{rel}", curie = "cur1"),
             @Curie(href = "http://docs.myother.site/{rel}", curie = "cur2"),
             @Curie(href = "http://docs.another.site/{rel}", curie = "cur3"), 
             @Curie(href = "http://docs.my.site/{rel}", curie = "cur4-not-used")})
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

        public HALLink getRelatedLink3() {
            return (HALLink) fields.get("friend3");
        }
    
        public HALLink getRelatedLink4() {
            return (HALLink) fields.get("friend4");
        }

        @Link("relative1")
        public void setRelatedLink1(HALLink link) {
            fields.put("friend1", link);
        }

        @Link(value = "relative2", curie = "cur1")
        public void setRelatedLink2(HALLink link) {
            fields.put("friend2", link);
        }
 
        @Link(value = "relative3", curie = "cur2")
        public void setRelatedLink3(HALLink link) {
            fields.put("friend3", link);
        }
 
        @Link(value = "relative4", curie = "cur3")
        public void setRelatedLink4(HALLink link) {
            fields.put("friend4", link);
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

