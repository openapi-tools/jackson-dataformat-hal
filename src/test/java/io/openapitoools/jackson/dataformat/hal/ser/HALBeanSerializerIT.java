package io.openapitoools.jackson.dataformat.hal.ser;


import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.openapitools.jackson.dataformat.hal.HALLink;
import io.openapitools.jackson.dataformat.hal.HALMapper;
import io.openapitools.jackson.dataformat.hal.annotation.EmbeddedResource;
import io.openapitools.jackson.dataformat.hal.annotation.Link;
import io.openapitools.jackson.dataformat.hal.annotation.Resource;


public class HALBeanSerializerIT {

    ObjectMapper om = new HALMapper();

    @Test
    public void testSerialization() throws Exception {
        TopResource res1 = new TopResource();
        String json = om.writeValueAsString(res1);
        Assertions.assertEquals("{"
                + "\"_links\":{"
                + "\"child\":[{\"href\":\"/top/1/child/1\"},{\"href\":\"/top/1/child/2\"}],"
                + "\"empty:list\":[],"
                + "\"self\":{\"href\":\"/top/1\"},"
                + "\"templated\":{\"href\":\"/uri/{id}\",\"templated\":true}"
                + "},"
                + "\"_embedded\":{"
                + "\"child\":["
                + "{\"_links\":{\"self\":{\"href\":\"/top/1/child/1\"}},\"id\":\"1\"},"
                + "{\"_links\":{\"self\":{\"href\":\"/top/1/child/2\"}},\"id\":\"2\"}]"
                + "},"
                + "\"id\":\"1\"}",
                json);
    }

    @Resource
    public static class TopResource {

        public String id = "1";

        @Link
        public HALLink self = new HALLink.Builder(URI.create("/top/1")).build();

        @Link("child")
        public HALLink childLink = new HALLink.Builder(URI.create("/should/be/overridden")).build();

        @Link
        public HALLink templated = new HALLink.Builder("/uri/{id}").build();

        @Link
        public HALLink nullLink = null;

        @Link("child")
        public Collection<HALLink> childLinks = Arrays.asList(
                new HALLink.Builder(URI.create("/top/1/child/1")).build(),
                new HALLink.Builder(URI.create("/top/1/child/2")).build());

        @Link("empty:list")
        public Collection<HALLink> childLinksEmpty = new ArrayList<HALLink>();

        @Link("null:list")
        public Collection<HALLink> childLinksNull = null;

        @EmbeddedResource // This should be overridden...
        public ChildResource child = new ChildResource("42");

        @EmbeddedResource("child")
        public Collection<ChildResource> children = Arrays.asList(new ChildResource("1"), new ChildResource("2"));

        @EmbeddedResource
        public String nullString = null;
    }

    @Resource
    public static class ChildResource {

        public String id;

        @Link
        public HALLink self;

        public ChildResource(String id) {
            this.id = id;
            self = new HALLink.Builder(URI.create("/top/1/child/" + id)).build();
        }

    }

}
