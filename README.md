# Overview

This project contains a Jackson data format extension to support the
[HAL JSON](http://tools.ietf.org/html/draft-kelly-json-hal) format both with respect to
serializing and deserializing.

# Usage

The extension works very similar to the default JSON `ObjectMapper` however a number of
annotations are necessary to support the HAL format to specify which properties are supposed to be
in the `_links` and `_embedded` sections of the JSON.

Consider the following POJO.

```java
@Resource
class Model {
  String property;
  
  @Link
  HALLink self;

  @Embedded
  AssociatedModel associated;
}

@Resource
class AssociatedModel {
    ...
}
```

This would be equivalent to the following JSON.

```json
{
  "_links": {
    "self": {
      "href": "http://example.com/resources/1"
    }
  },
  "_embedded": {
    "associated": {
      ...
    }
  },
  "property": "..."
}
```

The annotations support giving both links and embedded objects an alternate name not using the
POJO property name.

The `HALLink` class must be used to defined embedded links as it corresponds to the link defined by
the specification.
