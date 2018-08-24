package io.openapitools.jackson.dataformat.hal.ser;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import io.openapitools.jackson.dataformat.hal.CURIEProvider;
import io.openapitools.jackson.dataformat.hal.HALLink;
import io.openapitools.jackson.dataformat.hal.annotation.EmbeddedResource;
import io.openapitools.jackson.dataformat.hal.annotation.Link;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serializer to handle {@link io.openapitools.jackson.dataformat.hal.annotation.Resource} beans ensuring they are serialized according to the HAL
 * specification. This implies placing links inside the <code>_links</code> property and embedded objects inside the <code>_embedded</code>
 * property.
 */
public class HALBeanSerializer extends BeanSerializerBase {

    private static final Logger LOG = LoggerFactory.getLogger(HALBeanSerializer.class);
    private final CURIEProvider curieProvider;

    public HALBeanSerializer(BeanSerializerBase src, CURIEProvider curieProvider) {
        super(src);
        this.curieProvider = curieProvider;
    }

    @Override
    public BeanSerializerBase withObjectIdWriter(ObjectIdWriter objectIdWriter) {
        return this;
    }

    @Override
    public BeanSerializerBase withFilterId(Object o) {
        return this;
    }

    @Override
    protected BeanSerializerBase asArraySerializer() {
        return this;
    }

    @Override
    protected BeanSerializerBase withIgnorals(Set<String> set) {
        return this;
    }

    @Override
    public void serialize(Object bean, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        FilteredProperties filtered = new FilteredProperties(bean, provider);
        filtered.serialize(bean, jgen, provider);
    }

    /**
     * Modelling the properties of the bean segmented into HAL categories: links, embedded resources, and state
     */
    private class FilteredProperties {

        private List<BeanPropertyWriter> state = new ArrayList<>();
        private Map<String, LinkProperty> links = new TreeMap<>();
        private Map<String, BeanPropertyWriter> embedded = new TreeMap<>();
        private Map<String, HALLink> curies = new HashMap<>();

        public FilteredProperties(Object bean, SerializerProvider provider) throws IOException {
            for (BeanPropertyWriter prop : _props) {
                try {
                    if (prop.getAnnotation(EmbeddedResource.class) != null) {
                        Object object = prop.get(bean);
                        if (object != null) {
                            EmbeddedResource er = prop.getAnnotation(EmbeddedResource.class);
                            String val = "".equals(er.value()) ? prop.getName() : er.value();
                            addEmbeddedProperty(val, prop);
                        }

                    } else if (prop.getAnnotation(Link.class) != null) {
                        Link l = prop.getAnnotation(Link.class);
                        String relation = "".equals(l.value()) ? prop.getName() : l.value();
                        Object value = prop.get(bean);
                        if (value instanceof Collection) {
                            addLinks(relation, (Collection<HALLink>) prop.get(bean));
                        } else if (value instanceof HALLink) {
                            addLink(relation, (HALLink) prop.get(bean));
                        }

                    } else {
                        state.add(prop);
                    }
                } catch (Exception e) {
                    wrapAndThrow(provider, e, bean, prop.getName());
                }
            }
            if ((null != curieProvider) && (curies.size() > 0)) {
                addCuries(curies);
            }

        }

        public void serialize(Object bean, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeStartObject();

            if (!links.isEmpty()) {
                jgen.writeFieldName("_links");
                jgen.writeStartObject();
                for (String rel : links.keySet()) {
                    jgen.writeFieldName(rel);
                    links.get(rel).serialize(jgen);
                }
                jgen.writeEndObject();
            }

            if (!embedded.isEmpty()) {
                jgen.writeFieldName("_embedded");
                jgen.writeStartObject();
                for (String rel : embedded.keySet()) {
                    try {
                        jgen.writeFieldName(rel);
                        jgen.writeObject(embedded.get(rel).get(bean));
                    } catch (Exception e) {
                        wrapAndThrow(provider, e, bean, rel);
                    }
                }
                jgen.writeEndObject();
            }

            for (BeanPropertyWriter prop : state) {
                try {
                    prop.serializeAsField(bean, jgen, provider);
                } catch (Exception e) {
                    wrapAndThrow(provider, e, bean, prop.getName());
                }
            }

            jgen.writeEndObject();
        }

        private void addEmbeddedProperty(String rel, BeanPropertyWriter property) {
            if (embedded.put(rel, property) != null) {
                LOG.warn("Embedded resource already existed with rel [{}] in class [{}]", rel, _handledType);
            }
        }

        private void addLink(String rel, HALLink link) {
            if (links.put(rel, new LinkProperty(link)) != null) {
                LOG.warn("Link resource already existed with rel [{}] in class [{}]", rel, _handledType);
            }
            if (null != curieProvider) {
                addCURIE(rel, link);
            }
        }

        private void addLinks(String rel, Collection<HALLink> links) {
            if (this.links.put(rel, new LinkProperty(links)) != null) {
                LOG.warn("Link resource already existed with rel [{}] in class [{}]", rel, _handledType);
            }
            if (null != curieProvider) {
                for (HALLink halLink: links) {
                    addCURIE(rel, halLink);
                }
            }
        }

        private void addCURIE(String rel, HALLink link) {
            if (curieProvider.shouldProvideCURIE(rel, link)) {
                HALLink curie = curieProvider.provideCURIE(rel, link);
                checkForDivergentCURIEs(curie);
                curies.put(curie.getName(), curie);
            }
        }

        private void checkForDivergentCURIEs(HALLink curie) {
            // Check if CURIE already exists with a different HREF.  That's a
            // mistake if it does.
            if (curies.containsKey(curie.getName())) {
                HALLink existingCURIE = curies.get(curie.getName());
                if (!existingCURIE.getHref().equals(curie.getHref())) {
                    LOG.warn("CURIE [{}] detected with different hrefs [{}], [{}]",
                            curie.getName(), curie.getHref(), existingCURIE.getHref());
                }
            }
        }

        private void addCuries(Map<String, HALLink> curies) {
            if (this.links.put("curies", new LinkProperty(curies.values())) != null) {
                LOG.warn("Link resource already existed with rel [{}] in class [{}]", "curies", _handledType);
            }
        }

    }

    /**
     * Representing either a single link (one-to-one relation) or a collection of links.
     */
    private static class LinkProperty {

        private HALLink link;
        private Collection<HALLink> links;

        public LinkProperty(HALLink link) {
            this.link = link;
        }

        public LinkProperty(Collection<HALLink> links) {
            this.links = links == null ? new HashSet<HALLink>() : links;
        }

        public void serialize(JsonGenerator jgen) throws IOException {
            if (link != null) {
                writeLinkObject(jgen, link);
            } else if (links != null) {
                jgen.writeStartArray();
                for (HALLink curLink : links) {
                    writeLinkObject(jgen, curLink);
                }
                jgen.writeEndArray();
            }
        }

        private void writeLinkObject(JsonGenerator jgen, HALLink link) throws IOException {
            jgen.writeObject(link);
        }

    }

}
