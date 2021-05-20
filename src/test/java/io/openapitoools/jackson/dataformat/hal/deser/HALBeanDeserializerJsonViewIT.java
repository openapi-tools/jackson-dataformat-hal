package io.openapitoools.jackson.dataformat.hal.deser;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.MapperFeature;

import io.openapitools.jackson.dataformat.hal.HALLink;
import io.openapitools.jackson.dataformat.hal.HALMapper;
import io.openapitools.jackson.dataformat.hal.annotation.EmbeddedResource;
import io.openapitools.jackson.dataformat.hal.annotation.Link;
import io.openapitools.jackson.dataformat.hal.annotation.Resource;


public class HALBeanDeserializerJsonViewIT {
    static final String HAL_DOC_VIEW = "{" +
        "\"_links\":{" +
        "\"linkWithView1\":{" +
        "\"href\":\"/link/with/view/1\"," +
        "\"templated\":false" +
        "}}," +
        "\"_embedded\":{" +
        "\"childWithView1\":{" +
        "\"_links\":" +
        "{\"self\":" +
        "{\"href\":\"/top/1/child/childWithView1\"" +
        "}}," +
        "\"id\":\"childWithView1\"" +
        "}}," +
        "\"stateWithView1\":\"stateWithView1\"" +
        "}";

    static final String HAL_DOC_INCLUSIVE_VIEW = "{\"_links\":{" +
        "\"linkWithView1\":{" +
        "\"href\":\"/link/with/view/1\"," +
        "\"templated\":false" +
        "}," +
        "\"linkWithoutView\":{" +
        "\"href\":\"/link/without/view\"," +
        "\"templated\":false" +
        "}}," +
        "\"_embedded\":{" +
        "\"childWithView1\":{" +
        "\"_links\":{" +
        "\"self\":{" +
        "\"href\":\"/top/1/child/childWithView1\"" +
        "}}," +
        "\"id\":\"childWithView1\"" +
        "}," +
        "\"childWithoutView\":{" +
        "\"_links\":{" +
        "\"self\":{" +
        "\"href\":\"/top/1/child/childWithoutView\"" +
        "}}," +
        "\"id\":\"childWithoutView\"" +
        "}}," +
        "\"stateWithoutView\":\"stateWithoutView\"," +
        "\"stateWithView1\":\"stateWithView1\"" +
        "}";

    static final String HAL_DOC_NO_VIEW = "{\"_links\":{" +
        "\"linkWithView1\":{" +
        "\"href\":\"/link/with/view/1\"," +
        "\"templated\":false" +
        "}," +
        "\"linkWithView2\":{" +
        "\"href\":\"/link/with/view/2\",\"templated\":false" +
        "}," +
        "\"linkWithoutView\":{" +
        "\"href\":\"/link/without/view\"," +
        "\"templated\":false" +
        "}}," +
        "\"_embedded\":{" +
        "\"childWithView1\":{" +
        "\"_links\":{" +
        "\"self\":{" +
        "\"href\":\"/top/1/child/childWithView1\"" +
        "}}," +
        "\"id\":\"childWithView1\"" +
        "}," +
        "\"childWithView2\":{" +
        "\"_links\":{" +
        "\"self\":{" +
        "\"href\":\"/top/1/child/childWithView2\"" +
        "}}," +
        "\"id\":\"childWithView2\"" +
        "}," +
        "\"childWithoutView\":{" +
        "\"_links\":{" +
        "\"self\":{" +
        "\"href\":\"/top/1/child/childWithoutView\"" +
        "}}," +
        "\"id\":\"childWithoutView\"" +
        "}}," +
        "\"stateWithoutView\":\"stateWithoutView\"," +
        "\"stateWithView1\":\"stateWithView1\"" +
        ",\"stateWithView2\":\"stateWithView2\"" +
        "}";

    @Test
    public void testDeserializationOfView() throws IOException {
        TopResource tr = new HALMapper()
                .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false)
                .readerWithView(ResourceView1.class)
                .readValue(new StringReader(HAL_DOC_VIEW), TopResource.class);

        assertNull(tr.childWithoutView);
        assertNull(tr.linkWithoutView);
        assertNull(tr.stateWithoutView);

        assertNotNull(tr.childWithView1);
        assertNotNull(tr.linkWithView1);
        assertNotNull(tr.stateWithView1);

        assertNull(tr.childWithView2);
        assertNull(tr.linkWithView2);
        assertNull(tr.stateWithView2);

    }

    @Test
    public void testDeserializationOfViewWithInclusion() throws IOException {
        TopResource tr = new HALMapper()
                .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true)
                .readerWithView(ResourceView1.class)
                .readValue(new StringReader(HAL_DOC_INCLUSIVE_VIEW), TopResource.class);

        assertNotNull(tr.childWithoutView);
        assertNotNull(tr.linkWithoutView);
        assertNotNull(tr.stateWithoutView);

        assertNotNull(tr.childWithView1);
        assertNotNull(tr.linkWithView1);
        assertNotNull(tr.stateWithView1);

        assertNull(tr.childWithView2);
        assertNull(tr.linkWithView2);
        assertNull(tr.stateWithView2);
    }

    @Test
    public void testDeserializationOfNoView() throws IOException {
        TopResource tr = new HALMapper().readValue(new StringReader(HAL_DOC_NO_VIEW), TopResource.class);

        assertNotNull(tr.childWithoutView);
        assertNotNull(tr.linkWithoutView);
        assertNotNull(tr.stateWithoutView);

        assertNotNull(tr.childWithView1);
        assertNotNull(tr.linkWithView1);
        assertNotNull(tr.stateWithView1);

        assertNotNull(tr.childWithView2);
        assertNotNull(tr.linkWithView2);
        assertNotNull(tr.stateWithView2);
    }

    @Resource
    public static class TopResource {

        // No View
        public String stateWithoutView;

        @Link
        public HALLink linkWithoutView;

        @EmbeddedResource("childWithoutView")
        public ChildResource childWithoutView;

        // Resource View 1
        @JsonView(HALBeanDeserializerJsonViewIT.ResourceView1.class)
        public String stateWithView1;

        @Link("linkWithView1")
        @JsonView(HALBeanDeserializerJsonViewIT.ResourceView1.class)
        public HALLink linkWithView1;

        @EmbeddedResource("childWithView1")
        @JsonView(HALBeanDeserializerJsonViewIT.ResourceView1.class)
        public ChildResource childWithView1;

        // Resource View 2
        @JsonView(HALBeanDeserializerJsonViewIT.ResourceView2.class)
        public String stateWithView2;

        @Link("linkWithView2")
        @JsonView(HALBeanDeserializerJsonViewIT.ResourceView2.class)
        public HALLink linkWithView2;

        @EmbeddedResource("childWithView2")
        @JsonView(HALBeanDeserializerJsonViewIT.ResourceView2.class)
        public ChildResource childWithView2;
    }

    @Resource
    @JsonView({
        HALBeanDeserializerJsonViewIT.ResourceView1.class,
        HALBeanDeserializerJsonViewIT.ResourceView2.class
    })
    public static class ChildResource {

        public String id;

        @Link
        public HALLink self;
    }

    public interface ResourceView1 {

    }

    public interface ResourceView2 {

    }
}
