package io.openapitoools.jackson.dataformat.hal.ser;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.openapitools.jackson.dataformat.hal.HALLink;
import io.openapitools.jackson.dataformat.hal.HALMapper;
import io.openapitools.jackson.dataformat.hal.annotation.EmbeddedResource;
import io.openapitools.jackson.dataformat.hal.annotation.Link;
import io.openapitools.jackson.dataformat.hal.annotation.Resource;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class HALBeanSerializerJsonViewIT {

    ObjectMapper om = new HALMapper();

    @Test
    public void testSerializationOfView() throws Exception {
        TopResource res1 = new TopResource();

        om.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
        String json = om.writerWithView(ResourceView1.class).writeValueAsString(res1);
        assertEquals("{" +
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
                "}",
                json);
    }

    @Test
    public void testSerializationOfViewWithInclusion() throws Exception {
        TopResource res1 = new TopResource();

        om.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);
        String json = om.writerWithView(ResourceView1.class).writeValueAsString(res1);
        assertEquals("{\"_links\":{" +
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
                "}",
            json);
    }

    @Test
    public void testSerializationOfNoView() throws Exception {
        TopResource res1 = new TopResource();

        String json = om.writeValueAsString(res1);
        assertEquals("{\"_links\":{" +
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
                "}",
            json);
    }

    @Resource
    public static class TopResource {

        // No View
        public String stateWithoutView = "stateWithoutView";

        @Link
        public HALLink linkWithoutView = new HALLink.Builder("/link/without/view").build();

        @EmbeddedResource("childWithoutView")
        public ChildResource childWithoutView = new ChildResource("childWithoutView");

        // Resource View 1
        @JsonView(HALBeanSerializerJsonViewIT.ResourceView1.class)
        public String stateWithView1 = "stateWithView1";

        @Link("linkWithView1")
        @JsonView(HALBeanSerializerJsonViewIT.ResourceView1.class)
        public HALLink linkWithView1 = new HALLink.Builder("/link/with/view/1").build();

        @EmbeddedResource("childWithView1")
        @JsonView(HALBeanSerializerJsonViewIT.ResourceView1.class)
        public ChildResource childWithView1 = new ChildResource("childWithView1");

        // Resource View 2
        @JsonView(HALBeanSerializerJsonViewIT.ResourceView2.class)
        public String stateWithView2 = "stateWithView2";

        @Link("linkWithView2")
        @JsonView(HALBeanSerializerJsonViewIT.ResourceView2.class)
        public HALLink linkWithView2 = new HALLink.Builder("/link/with/view/2").build();

        @EmbeddedResource("childWithView2")
        @JsonView(HALBeanSerializerJsonViewIT.ResourceView2.class)
        public ChildResource childWithView2 = new ChildResource("childWithView2");
    }

    @Resource
    @JsonView({
        HALBeanSerializerJsonViewIT.ResourceView1.class,
        HALBeanSerializerJsonViewIT.ResourceView2.class
    })
    public static class ChildResource {

        public String id;

        @Link
        public HALLink self;

        public ChildResource(String id) {
            this.id = id;
            self = new HALLink.Builder(URI.create("/top/1/child/" + id)).build();
        }

    }

    public interface ResourceView1 {

    }

    public interface ResourceView2 {

    }
}
