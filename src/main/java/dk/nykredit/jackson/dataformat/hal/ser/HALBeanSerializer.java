package dk.nykredit.jackson.dataformat.hal.ser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import dk.nykredit.jackson.dataformat.hal.HALLink;
import dk.nykredit.jackson.dataformat.hal.annotation.EmbeddedResource;
import dk.nykredit.jackson.dataformat.hal.annotation.Link;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serializer to handle {@link Resource} beans ensuring they are serialized according to the HAL specification. This implies
 * placing links inside the <code>_links</code> property and embedded objects inside the <code>_embedded</code> property.
 */
public class HALBeanSerializer extends BeanSerializerBase {
    private static final Logger LOG = LoggerFactory.getLogger(HALBeanSerializer.class);

    public HALBeanSerializer(BeanSerializerBase src) {
        super(src);
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
    protected BeanSerializerBase withIgnorals(String[] toIgnore) {
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
        private List<BeanPropertyWriter> state = new ArrayList<BeanPropertyWriter>();
        private Map<String, LinkProperty> links = new TreeMap<String, LinkProperty>();
        private Map<String, BeanPropertyWriter> embedded = new TreeMap<String, BeanPropertyWriter>();

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
                        String val = "".equals(l.value()) ? prop.getName() : l.value();
                        if (prop.getType().isCollectionLikeType()) {
                            addLinks(val, (Collection<HALLink>) prop.get(bean));
                        } else {
                            addLink(val, (HALLink) prop.get(bean));
                        }

                    } else {
                        state.add(prop);
                    }
                } catch (Exception e) {
                    wrapAndThrow(provider, e, bean, prop.getName());
                }
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
            if (link != null) {
                if (links.put(rel, new LinkProperty(link)) != null) {
                    LOG.warn("Link resource already existed with rel [{}] in class [{}]", rel, _handledType);
                }
            }
        }

        private void addLinks(String rel, Collection<HALLink> links) {
            if (this.links.put(rel, new LinkProperty(links)) != null) {
                LOG.warn("Link resource already existed with rel [{}] in class [{}]", rel, _handledType);
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
            this.links = links == null ? (Collection<HALLink>) Collections.EMPTY_SET : links;
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
