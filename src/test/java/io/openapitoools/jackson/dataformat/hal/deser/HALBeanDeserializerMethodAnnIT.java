package io.openapitoools.jackson.dataformat.hal.deser;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Verify that HAL annotations can be used on methods as well as fields.
 */
public class HALBeanDeserializerMethodAnnIT {
    ObjectMapper om = new HALMapper();

    @Test
    public void testSingleCurie() throws IOException {
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
                "      \"templated\": false" +
                "    }" +
                "  }," +
                "  \"_embedded\": {" +
                "    \"extras\": \"the more you know\"" +
                "  }" +
                "}";

        TestSingleCurieResource resource = om.readValue(json, TestSingleCurieResource.class);
        assertEquals("POJO name", resource.fields.get("name"));

        HALLink self = (HALLink) resource.fields.get("self");
        assertEquals("http://self.url", self.getHref());
        assertFalse(self.getTemplated());

        HALLink other = (HALLink) resource.fields.get("otherlink");
        assertEquals("http://other.link.url", other.getHref());

        assertEquals("the more you know", resource.fields.get("extras"));        
    }

    @Test
    public void testCuries() throws IOException {
        final String json = "{" +
                "  \"name\": \"POJO name\"," +
                "  \"_links\": {" +
                "    \"curies\": [{" +
                "      \"href\": \"http://my.example.com/doc/{rel}\"," +
                "      \"name\": \"cur1\"," +
                "      \"templated\": true" +
                "    },{" +
                "      \"href\": \"http://my.otherexample.com/exdoc\"," +
                "      \"name\": \"cur2\"," +
                "      \"templated\": false" +                
                "    }]," +
                "    \"self\": {" +
                "      \"href\": \"http://self.url\"," +
                "      \"templated\": false" +
                "    }," +
                "    \"link1\": {" +
                "      \"href\": \"http://other.link.url\"," +
                "      \"templated\": false" +
                "    }," +
                "    \"cur1:link2\": {" +
                "      \"href\": \"http://link2.url\"," +
                "      \"templated\": true" +
                "    }," +            
                "    \"cur2:link3\": {" +
                "      \"href\": \"http://link3.url{?id}\"," +
                "      \"templated\": true" +
                "    }" +
                "  }," +
                "  \"_embedded\": {" +
                "    \"extras\": \"the more you know\"" +
                "  }" +
                "}";


        TestCuriesResource resource = om.readValue(json, TestCuriesResource.class);
        assertEquals("POJO name", resource.fields.get("name"));

        HALLink self = (HALLink) resource.fields.get("self");
        assertEquals("http://self.url", self.getHref());
        assertFalse(self.getTemplated());

        HALLink other = (HALLink) resource.fields.get("otherlink");
        assertEquals("http://other.link.url", other.getHref());

        HALLink curie2 = (HALLink) resource.fields.get("link2");
        assertEquals("http://link2.url", curie2.getHref());
        assertEquals(true, curie2.getTemplated());
        
        HALLink curie3 = (HALLink) resource.fields.get("link3");
        assertEquals("http://link3.url{?id}", curie3.getHref());
        assertEquals(true, curie3.getTemplated());

        HALLink curie4 = (HALLink) resource.fields.get("link4");
        assertNull(curie4);
       
        assertEquals("the more you know", resource.fields.get("extras"));        
    }

    
     @Test
    public void testUndefinedCurie() throws IOException {
        final String json = "{" +
                "  \"name\": \"POJO name\"," +
                "  \"_links\": {" +
                "    \"curies\": [{" +
                "      \"href\": \"http://my.example.com/doc/{rel}\"," +
                "      \"name\": \"cur1\"," +
                "      \"templated\": true" +
                "    },{" +
                "      \"href\": \"http://my.otherexample.com/exdoc\"," +
                "      \"name\": \"cur2\"," +
                "      \"templated\": false" +                
                "    }]," +
                "    \"self\": {" +
                "      \"href\": \"http://self.url\"," +
                "      \"templated\": false" +
                "    }," +
                "    \"link1\": {" +
                "      \"href\": \"http://other.link.url\"," +
                "      \"templated\": false" +
                "    }," +
                "    \"cur1:link2\": {" +
                "      \"href\": \"http://link2.url\"," +
                "      \"templated\": true" +
                "    }," +            
                "    \"cur2:link3\": {" +
                "      \"href\": \"http://link3.url{?id}\"," +
                "      \"templated\": true" +
                "    }," +            
                "    \"cur3:extralink\": {" +
                "      \"href\": \"http://extralink.url\"," +
                "      \"templated\": false" +
                "    }" +
                "  }," +
                "  \"_embedded\": {" +
                "    \"extras\": \"the more you know\"" +
                "  }" +
                "}";


        try {
            TestCuriesResource resource = om.readValue(json, TestCuriesResource.class);
            fail("There should be a failure due to non-defined query");
        } catch (UnrecognizedPropertyException upe) {
            //ignore
        }
    }
    @Test
    public void testCurieLinkURI() throws IOException {
        final String json = "{" +
                "  \"name\": \"POJO name\"," +
                "  \"_links\": {" +
                "    \"curies\": [{" +
                "      \"href\": \"http://my.example.com/doc/{rel}\"," +
                "      \"name\": \"cur1\"," +
                "      \"templated\": true" +
                "    },{" +
                "      \"href\": \"http://my.otherexample.com/exdoc\"," +
                "      \"name\": \"cur2\"," +
                "      \"templated\": false" +                
                "    }]," +
                "    \"self\": {" +
                "      \"href\": \"http://self.url\"," +
                "      \"templated\": false" +
                "    }," +
                "    \"link1\": {" +
                "      \"href\": \"http://other.link.url\"," +
                "      \"templated\": false" +
                "    }," +
                "    \"cur1:link2\": {" +
                "      \"href\": \"/link2.url\"," +
                "      \"templated\": true" +
                "    }," +            
                "    \"cur2:link3\": {" +
                "      \"href\": \"http://link3.url{?id}\"," +
                "      \"templated\": true" +
                "    }" +
                "  }," +
                "  \"_embedded\": {" +
                "    \"extras\": \"the more you know\"" +
                "  }" +
                "}";


        TestCuriesResource resource = om.readValue(json, TestCuriesResource.class);
        assertEquals("POJO name", resource.fields.get("name"));

        HALLink self = (HALLink) resource.fields.get("self");
        assertEquals("http://self.url", self.getHref());
        assertFalse(self.getTemplated());

        HALLink other = (HALLink) resource.fields.get("otherlink");
        assertEquals("http://other.link.url", other.getHref());

        HALLink curie2 = (HALLink) resource.fields.get("link2");
        assertEquals("/link2.url", curie2.getHref());
        assertEquals(true, curie2.getTemplated());
        
        HALLink curie3 = (HALLink) resource.fields.get("link3");
        assertEquals("http://link3.url{?id}", curie3.getHref());
        assertEquals(true, curie3.getTemplated());

        HALLink curie4 = (HALLink) resource.fields.get("link4");
        assertNull(curie4);
       
        assertEquals("the more you know", resource.fields.get("extras"));        
    }


    @Curie(curie = "cur1", href = "http://my.example.com/doc/{rel}")
    @Resource
    public static class TestSingleCurieResource {
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
    
   @Curies({@Curie(href = "http://docs.my.site/{rel}", curie = "cur1"),
             @Curie(href = "http://docs.myother.site/cur2", curie = "cur2"),
             @Curie(href = "http://docs.another.site/{rel}", curie = "cur3"), 
             @Curie(href = "http://docs.my.unusedsite/{rel}", curie = "cur4-not-used")})
    @Resource
    public static class TestCuriesResource {
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
            fields.put("link2", link);
        }

        @Link(value = "link3", curie = "cur2")
        public void setLink3(HALLink link) {
            fields.put("link3", link);
        }

        @Link(value = "link4", curie = "cur3")
        public void setLink4(HALLink link) {
            fields.put("link4", link);
        }

        @Link(value = "link5", curie = "cur4")
        public void setLink5(HALLink link) {
            fields.put("link5", link);
        }

        @EmbeddedResource("extras")
        public void setExtras(String extras) {
            fields.put("extras", extras);
        }
    }
}
