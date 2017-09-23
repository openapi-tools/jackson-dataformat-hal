# Overview

This project contains a [Jackson](http://wiki.fasterxml.com/JacksonHome) extension component to support the
[HAL JSON](http://tools.ietf.org/html/draft-kelly-json-hal) format both with respect to
serializing and deserializing.

The goal is to handle HAL links and embedded objects as POJO properties with a data-binding similar to
the normal Jackson JSON handling.

# Status
Module is considered production ready.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.openapitools.jackson.dataformat/jackson-dataformat-hal/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.openapitools.jackson.dataformat/jackson-dataformat-hal/)
[![Javadoc](https://javadoc-emblem.rhcloud.com/doc/io.openapitools.jackson.dataformat/jackson-dataformat-hal/badge.svg)](https://www.javadoc.io/doc/io.openapitools.jackson.dataformat/jackson-dataformat-hal)

# Usage

The extension comes with a few annotations to mark the HAL links and embedded objects as well as a class
to handle the HAL link specification. A POJO used for HAL formatted JSON could look as follows.

```java
@Resource
class Model {
    String modelProperty;

    @Link
    HALLink self;

    @Link("rel:associated")
    HALLink relation;

    @Embedded
    AssociatedModel associated;
}

@Resource
class AssociatedModel {
    String associatedProperty;

    @Link
    HALLink self;
}
```

The `@Resource` annotation marks the POJO as a HAL based resource. The `@Link` and `@Embedded` annotations
marks links to go into the `_links` object and embedded resources to go into the `_embedded` object respectively.
HAL links must be defined using the `HALLink` class as this models the properties defined in the HAL specification.
Both `@Link` and `@Embedded` fields may be collections or arrays.

The JSON resulting from the above small POJO model would look like the following.

```json
{
    "_links": {
        "self": { "href": "https://..."},
        "rel:associated": { "href": "https://..."}
    },
    "_embedded": {
        "associated": {
            "_links": {
                "self": { "href": "https://..." }
            },
            "associatedProperty": "..."
        }
    },
    "modelProperty": "..."
}
```

All fields which are not annotated will be handled by the normal Jackson JSON data-binding.

## Serializing POJOs as HAL JSON

Serialization is similar to the normal JSON serialization using the `HALMapper` instead of the
`ObjectMapper`.

```java
ObjectMapper halMapper = new HALMapper();
String json = halMapper.writeValueAsString(new POJO());
```

## Deserializing POJOs from XML

Deserialization is also similar to the normal JSON handling using the `HALMapper`.

```java
ObjectMapper halMapper = new HALMapper();
POJO value = halMapper.readValue("{..json..}", POJO.class);
```

## Using HAL Extension in JAX-RS Application

To use the HAL extension in a JAX-RS application you can add the [jackson-jaxrs-providers](https://github.com/FasterXML/jackson-jaxrs-providers) module
to your application and use this to ensure the `HALMapper` is being used.

```java
@ApplicationPath("/")
public class MyApplication extends Application {
    private final Set<Object> singletons = new HashSet<>();

    public MyApplication() {
        singletons.add(new JacksonJsonProvider(new HALMapper()));
    }

    @Override
    public Set<Object> getSingletons() {
        return Collections.unmodifiableSet(singletons);
    }
}
```

## Using HAL Extension in JAX-RS Client

Using the HAL extension in a JAX-RS client works very similar to using it in an application. The
jackson-jaxrs-providers can be used to register the `HALMapper`.

```java
ClientBuilder cb = ClientBuilder.newBuilder();
Client client = cb.register(new JacksonJsonProvider(new HALMapper())).build();
```
## HALLink extended with temporal aspect
The HALLink is extended with a `seen` property. This is beyond the current specification of HAL.
An addendum or change to HAL will be proposed to incoorporate the seen property in the `_Link` specification.
The purpose of the `seen` property is to give consumers of a service the ability to judge for themselves whether
the last time a HAL related object was seen either as a `link` or as an `embedded` object.


## Knows Limitations

The Wildfly application server comes with both a Jackson and Jackson 2 module which takes precedence
over the ones registered with the JAX-RS client. Thus to make sure the HAL extension is actually
used when creating a JAX-RS client running inside Wildfly you need to exclude the Jackson modules in
the `jboss-deployment-structure.xml` file.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jboss-deployment-structure>
    <deployment>
        <exclusions>
            <module name="org.jboss.resteasy.resteasy-jackson-provider"/>
            <module name="org.jboss.resteasy.resteasy-jackson2-provider"/>
        </exclusions>
    </deployment>
</jboss-deployment-structure>
```
