package io.openapitoools.jackson.dataformat.hal.ser;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.openapitools.jackson.dataformat.hal.HALLink;
import io.openapitools.jackson.dataformat.hal.HALMapper;
import io.openapitools.jackson.dataformat.hal.annotation.EmbeddedResource;
import io.openapitools.jackson.dataformat.hal.annotation.Link;
import io.openapitools.jackson.dataformat.hal.annotation.Resource;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Test;

public class HALBeanSerializerPolymorphicWithPropertyIT {

  ObjectMapper om = new HALMapper();

  @Test
  public void testSerializationForResourceWithEmbeddableList() throws Exception {
    @Resource
    class TopResource {

      public String id = "1";

      @Link public HALLink self = new HALLink.Builder(URI.create("/top/1")).build();

      @Link("child")
      public List<HALLink> childResourcesLink =
          Arrays.asList(
              new HALLink.Builder(URI.create("/top/1/child/1")).build(),
              new HALLink.Builder(URI.create("/top/1/child/2")).build());

      @EmbeddedResource("child")
      public Collection<ChildResource> children =
          Arrays.asList(new ChildResource("1"), new OtherChildResource("2", "Max"));
    }

    TopResource resource = new TopResource();
    String json = om.writeValueAsString(resource);
    assertEquals(
        "{"
            + "\"_links\":{"
            + "\"child\":[{\"href\":\"/top/1/child/1\"},{\"href\":\"/top/1/child/2\"}],"
            + "\"self\":{\"href\":\"/top/1\"}"
            + "},"
            + "\"_embedded\":{"
            + "\"child\":["
            + "{\"@type\":\"ChildResource\",\"_links\":{\"self\":{\"href\":\"/top/1/child/1\"}},\"id\":\"1\"},"
            + "{\"@type\":\"OtherChildResource\",\"_links\":{\"self\":{\"href\":\"/top/1/child/2\"}},\"id\":\"2\",\"name\":\"Max\"}]"
            + "},"
            + "\"id\":\"1\"}",
        json);
  }

  @Test
  public void testSerializationForResourceWithList() throws Exception {
    @Resource
    class TopResourceWithoutEmbedded {
      public String id = "1";

      @Link public HALLink self = new HALLink.Builder(URI.create("/top/1")).build();

      public Collection<ChildResource> children =
          Arrays.asList(new ChildResource("1"), new OtherChildResource("2", "Max"));
    }

    TopResourceWithoutEmbedded resource = new TopResourceWithoutEmbedded();
    String json = om.writeValueAsString(resource);
    assertEquals(
        "{"
            + "\"_links\":{"
            + "\"self\":{\"href\":\"/top/1\"}"
            + "},"
            + "\"id\":\"1\","
            + "\"children\":["
            + "{\"@type\":\"ChildResource\",\"_links\":{\"self\":{\"href\":\"/top/1/child/1\"}},\"id\":\"1\"},"
            + "{\"@type\":\"OtherChildResource\",\"_links\":{\"self\":{\"href\":\"/top/1/child/2\"}},\"id\":\"2\",\"name\":\"Max\"}]"
            + "}",
        json);
  }

  @Test
  public void testSerializationForResourceEmbedded() throws Exception {
    @Resource
    class SimpleTopResourceEmbedded {
      public String id = "1";

      @Link public HALLink self = new HALLink.Builder(URI.create("/top/1")).build();

      @EmbeddedResource
      public ChildResource child = new ChildResource("1");
    }

    SimpleTopResourceEmbedded resource = new SimpleTopResourceEmbedded();
    String json = om.writeValueAsString(resource);
    assertEquals(
        "{"
            + "\"_links\":{"
            + "\"self\":{\"href\":\"/top/1\"}"
            + "},"
            + "\"_embedded\":{"
            + "\"child\":{\"@type\":\"ChildResource\",\"_links\":{\"self\":{\"href\":\"/top/1/child/1\"}},\"id\":\"1\"}"
            + "},"
            + "\"id\":\"1\"}",
        json);
  }

  @Test
  public void testSerializationForResource() throws Exception {
    @Resource
    class SimpleTopResource {
      public String id = "1";

      @Link public HALLink self = new HALLink.Builder(URI.create("/top/1")).build();

      public ChildResource child = new ChildResource("1");
    }

    SimpleTopResource resource = new SimpleTopResource();
    String json = om.writeValueAsString(resource);
    assertEquals(
        "{"
            + "\"_links\":{"
            + "\"self\":{\"href\":\"/top/1\"}"
            + "},"
            + "\"id\":\"1\","
            + "\"child\":{"
            + "\"@type\":\"ChildResource\",\"_links\":{\"self\":{\"href\":\"/top/1/child/1\"}},\"id\":\"1\""
            + "}}",
        json);
  }

  @Test
  public void testSerializationForAnnotatedPolymorphicObject() throws Exception {
    ChildResource resource = new ChildResource("1");
    String json = om.writeValueAsString(resource);
    assertEquals(
        "{"
            + "\"@type\":\"ChildResource\","
            + "\"_links\":{"
            + "\"self\":{\"href\":\"/top/1/child/1\"}"
            + "},"
            + "\"id\":\"1\"}",
        json);
  }

  @Test
  public void testSerializationForNotAnnotatedPolymorphicObject() throws Exception {
    ChildResource resource = new OtherChildResource("1", "Max");
    String json = om.writeValueAsString(resource);
    assertEquals(
        "{"
            + "\"@type\":\"OtherChildResource\","
            + "\"_links\":{"
            + "\"self\":{\"href\":\"/top/1/child/1\"}"
            + "},"
            + "\"id\":\"1\","
            + "\"name\":\"Max\"}",
        json);
  }

  @JsonTypeInfo(
      use = Id.NAME,
      include = As.PROPERTY,
      defaultImpl = ChildResource.class)
  @JsonSubTypes({@JsonSubTypes.Type(value = OtherChildResource.class, name = "OtherChildResource")})
  @JsonTypeName("ChildResource")
  @Resource
  public static class ChildResource {
    public String id;

    @Link public HALLink self;

    public ChildResource(String id) {
      this.id = id;
      self = new HALLink.Builder(URI.create("/top/1/child/" + id)).build();
    }
  }

  @Resource
  @JsonTypeName("OtherChildResource")
  public static class OtherChildResource extends ChildResource {

    public String name;

    public OtherChildResource(String id, String name) {
      super(id);
      this.name = name;
    }
  }
}
