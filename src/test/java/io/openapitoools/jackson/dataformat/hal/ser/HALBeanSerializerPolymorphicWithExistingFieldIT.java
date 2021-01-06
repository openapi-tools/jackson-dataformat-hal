package io.openapitoools.jackson.dataformat.hal.ser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

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

public class HALBeanSerializerPolymorphicWithExistingFieldIT {

  ObjectMapper om = new HALMapper();

  @Test
  public void testSerializationForResourceWithEmbeddableList() throws Exception {
      @Resource
      @SuppressWarnings("unused")
      class TopResource {
	  public String id = "1";
	  @Link
	  public HALLink self = new HALLink.Builder(URI.create("/top/1")).build();
	  @Link("child")
	  public List<HALLink> childResourcesLink = Arrays.asList(
		  new HALLink.Builder(URI.create("/top/1/child/1")).build(),
		  new HALLink.Builder(URI.create("/top/1/child/2")).build());
	  @EmbeddedResource("child")
	  public Collection<ChildResource> children = Arrays.asList(new ChildResource("1"), new OtherChildResource("2", "Max"));
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
            + "{\"_links\":{\"self\":{\"href\":\"/top/1/child/1\"}},\"type\":\"ChildResource\",\"id\":\"1\"},"
            + "{\"_links\":{\"self\":{\"href\":\"/top/1/child/2\"}},\"type\":\"OtherChildResource\",\"id\":\"2\",\"name\":\"Max\"}]"
            + "},"
            + "\"id\":\"1\"}",
        json);
  }

  @Test
  public void testSerializationForResourceWithList() throws Exception {
      @Resource
      @SuppressWarnings("unused")
      class TopResourceWithoutEmbedded {
	  public String id = "1";
	  @Link
	  public HALLink self = new HALLink.Builder(URI.create("/top/1")).build();
	  public Collection<ChildResource> children = Arrays.asList(new ChildResource("1"),
		  new OtherChildResource("2", "Max"));
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
            + "{\"_links\":{\"self\":{\"href\":\"/top/1/child/1\"}},\"type\":\"ChildResource\",\"id\":\"1\"},"
            + "{\"_links\":{\"self\":{\"href\":\"/top/1/child/2\"}},\"type\":\"OtherChildResource\",\"id\":\"2\",\"name\":\"Max\"}]"
            + "}",
        json);
  }

  @Test
  public void testSerializationForResourceEmbedded() throws Exception {
      @Resource
      @SuppressWarnings("unused")
      class SimpleTopResourceEmbedded {
	  public String id = "1";
	  @Link
	  public HALLink self = new HALLink.Builder(URI.create("/top/1")).build();
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
            + "\"child\":{\"_links\":{\"self\":{\"href\":\"/top/1/child/1\"}},\"type\":\"ChildResource\",\"id\":\"1\"}"
            + "},"
            + "\"id\":\"1\"}",
        json);
  }

  @Test
  public void testSerializationForResource() throws Exception {
    @Resource
    @SuppressWarnings("unused")
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
            + "\"_links\":{\"self\":{\"href\":\"/top/1/child/1\"}},\"type\":\"ChildResource\",\"id\":\"1\""
            + "}}",
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
            + "\"type\":\"OtherChildResource\","
            + "\"id\":\"1\","
            + "\"name\":\"Max\"}",
        json);
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
