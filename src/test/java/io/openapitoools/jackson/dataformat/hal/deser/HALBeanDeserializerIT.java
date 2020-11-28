package io.openapitoools.jackson.dataformat.hal.deser;

import java.io.StringReader;
import java.util.Collection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.openapitools.jackson.dataformat.hal.HALLink;
import io.openapitools.jackson.dataformat.hal.HALMapper;
import io.openapitools.jackson.dataformat.hal.annotation.EmbeddedResource;
import io.openapitools.jackson.dataformat.hal.annotation.Link;
import io.openapitools.jackson.dataformat.hal.annotation.Resource;


public class HALBeanDeserializerIT {
    static final String HAL_DOC = "{"
            + "\"_links\":{"
            + "\"child\":[{\"href\":\"/top/1/child/1\"},{\"href\":\"/top/1/child/2\"}],"
            + "\"empty:list\":[],"
            + "\"null:list\":[],"
            + "\"self\":{\"href\":\"/top/1\"},"
            + "\"templated\":{\"href\":\"/uri/{id}\",\"templated\":true, \"title\":\"top-title\", \"seen\":\"2017-08-10Z22:00:00\"}"
            + "},"
            + "\"_embedded\":{"
            + "\"child\":["
            + "{\"_links\":{\"self\":{\"href\":\"/top/1/child/1\"}},\"id\":\"1\"},"
            + "{\"_links\":{\"self\":{\"href\":\"/top/1/child/2\"}},\"id\":\"2\"}]"
            + "},"
            + "\"id\":\"1\"}";

    ObjectMapper om = new HALMapper();

    @Test
    public void testDeserialization() throws Exception {
        TopResource tr = om.readValue(new StringReader(HAL_DOC), TopResource.class);
        Assertions.assertEquals("1", tr.id);
        Assertions.assertEquals("/top/1", tr.self.getHref());
        Assertions.assertEquals("/uri/{id}", tr.templated.getHref());
        Assertions.assertTrue(tr.templated.getTemplated());
        Assertions.assertEquals(2, tr.childLinks.size());
        Assertions.assertEquals(2, tr.children.size());
        System.out.println("tr: " + tr);
    }

    @Test
    public void testDetailedDeserialization() throws Exception {
        TopResource tr = om.readValue(new StringReader(HAL_DOC), TopResource.class);
        Assertions.assertEquals("1", tr.id);
        Assertions.assertEquals("/top/1", tr.self.getHref());
        Assertions.assertEquals("/uri/{id}", tr.templated.getHref());
        Assertions.assertTrue(tr.templated.getTemplated());
        Assertions.assertEquals("top-title", tr.templated.getTitle());
        Assertions.assertEquals("2017-08-10Z22:00:00", tr.templated.getSeen());
        Assertions.assertEquals(2, tr.childLinks.size());
        Assertions.assertEquals(2, tr.children.size());
        System.out.println("tr: " + tr);
    }

    @Resource
    public static class TopResource {

        public String id;

        @Link
        public HALLink self;

        @Link
        public HALLink templated;

        @Link
        public HALLink nullLink;

        @Link("child")
        public Collection<HALLink> childLinks;

        @Link("empty:list")
        public Collection<HALLink> childLinksEmpty;

        @Link("null:list")
        public Collection<HALLink> childLinksNull;

        @EmbeddedResource("child")
        public Collection<ChildResource> children;

        @EmbeddedResource
        public String nullString;
    }

    @Resource
    public static class ChildResource {

        public String id;

        @Link
        public HALLink self;
    }

}
