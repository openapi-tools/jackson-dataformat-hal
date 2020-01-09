package io.openapitoools.jackson.dataformat.hal.ser;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
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

public class HALBeanSerializerPolymorphicIT {

  ObjectMapper om = new HALMapper();

  @Test
  public void testSerializationForResourceWithEmbeddableList() throws Exception {
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
            + "{\"_links\":{\"self\":{\"href\":\"/top/1/child/1\"}},\"type\":\"ChildResource\",\"id\":\"1\"},"
            + "{\"_links\":{\"self\":{\"href\":\"/top/1/child/2\"}},\"type\":\"OtherChildResource\",\"id\":\"2\",\"name\":\"Max\"}]"
            + "},"
            + "\"id\":\"1\"}",
        json);
  }

  @Test
  public void testSerializationForResourceWithList() throws Exception {
    TopResourceWithoutEmbedded resource = new TopResourceWithoutEmbedded();
    String json = om.writeValueAsString(resource);
    assertEquals(
        "{"
            + "\"_links\":{"
            + "\"self\":{\"href\":\"/top/1\"}"
            + "},"
            + "\"id\":\"1\","
            + "\"children\":["
            + "{\"_links\":{\"self\":{\"href\":\"/top/1/child/1\"}},\"type\":\"ChildResource\",\"id\":\"1\"},"
            + "{\"_links\":{\"self\":{\"href\":\"/top/1/child/2\"}},\"type\":\"OtherChildResource\",\"id\":\"2\",\"name\":\"Max\"}]"
            + "}",
        json);
  }

  @Test
  public void testSerializationForAnnotatedPolymorphicObject() throws Exception {
    ChildResource resource = new ChildResource("1");
    String json = om.writeValueAsString(resource);
    assertEquals(
        "{"
            + "\"_links\":{"
            + "\"self\":{\"href\":\"/top/1/child/1\"}"
            + "},"
            + "\"type\":\"ChildResource\","
            + "\"id\":\"1\"}",
        json);
  }

  @Test
  public void testSerializationForNotAnnotatedPolymorphicObject() throws Exception {
    ChildResource resource = new OtherChildResource("1", "Max");
    String json = om.writeValueAsString(resource);
    assertEquals(
        "{"
            + "\"_links\":{"
            + "\"self\":{\"href\":\"/top/1/child/1\"}"
            + "},"
            + "\"type\":\"ChildResource\","
            + "\"id\":\"1\","
            + "\"name\";\"Max\"}",
        json);
  }

  @Resource
  public static class TopResource {

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

  @Resource
  public static class TopResourceWithoutEmbedded {
    public String id = "1";

    @Link public HALLink self = new HALLink.Builder(URI.create("/top/1")).build();

    public Collection<ChildResource> children =
        Arrays.asList(new ChildResource("1"), new OtherChildResource("2", "Max"));
  }

  @JsonTypeInfo(
      use = Id.NAME,
      include = As.EXISTING_PROPERTY,
      property = "type",
      defaultImpl = ChildResource.class)
  @JsonSubTypes({@JsonSubTypes.Type(value = OtherChildResource.class, name = "OtherChildResource")})
  @Resource
  public static class ChildResource {

    public String type = "ChildResource";

    public String id;

    @Link public HALLink self;

    public ChildResource(String id) {
      this.id = id;
      self = new HALLink.Builder(URI.create("/top/1/child/" + id)).build();
    }
  }

  @Resource
  public static class OtherChildResource extends ChildResource {

    public String type = "OtherChildResource";

    public String name;

    public OtherChildResource(String id, String name) {
      super(id);
      this.name = name;
    }
  }
}
