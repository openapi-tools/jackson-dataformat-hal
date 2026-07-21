package io.openapitools.jackson.dataformat.hal.ser;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.databind.util.NameTransformer;
import io.openapitools.jackson.dataformat.hal.HALLink;
import io.openapitools.jackson.dataformat.hal.annotation.Curie;
import io.openapitools.jackson.dataformat.hal.annotation.Curies;
import io.openapitools.jackson.dataformat.hal.annotation.EmbeddedResource;
import io.openapitools.jackson.dataformat.hal.annotation.Link;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serializer to handle {@link io.openapitools.jackson.dataformat.hal.annotation.Resource} beans ensuring they are serialized according to the HAL
 * specification. This implies placing links inside the <code>_links</code> property and embedded objects inside the <code>_embedded</code>
 * property.
 */
public class HALBeanSerializer extends BeanSerializerBase {

    private static final Logger LOG = LoggerFactory.getLogger(HALBeanSerializer.class);
    private final BeanDescription beanDescription;

    public HALBeanSerializer(BeanSerializerBase src, BeanDescription beanDescription) {
        super(src);
        this.beanDescription = beanDescription;
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
    protected BeanSerializerBase withProperties(BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties) {
        return this;
    }

    @Override
    protected BeanSerializerBase asArraySerializer() {
        return this;
    }

    @Override
    protected BeanSerializerBase withByNameInclusion(Set<String> toIgnore, Set<String> toInclude) {
        return this;
    }

    @Override
    public void serialize(Object bean, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        FilteredProperties filtered = new FilteredProperties(bean, provider, beanDescription);

        // this serializer acts autonomous and creates the complete object
        jgen.writeStartObject();
        filtered.serialize(bean, jgen, provider);
        jgen.writeEndObject();
    }

    @Override
    public void serializeWithType(Object bean, JsonGenerator jgen, SerializerProvider provider,
        TypeSerializer typeSer) throws IOException {
        FilteredProperties filtered = new FilteredProperties(bean, provider, beanDescription);

        // this serializer lets the TypeSerializer create the outer frame and inserts the rest of the object
        WritableTypeId typeIdDef = _typeIdDef(typeSer, bean, JsonToken.START_OBJECT);
        typeSer.writeTypePrefix(jgen, typeIdDef);
        filtered.serialize(bean, jgen, provider);
        typeSer.writeTypeSuffix(jgen, typeIdDef);
    }

    /**
     * Modelling the properties of the bean segmented into HAL categories: links, embedded resources, and state
     */
    private class FilteredProperties {

        private List<BeanPropertyWriter> state = new ArrayList<>();
        private Map<String, LinkProperty> links = new TreeMap<>();
        private Map<String, BeanPropertyWriter> embedded = new TreeMap<>();

        // All of the possible curies that COULD be used (provided via Curie/Curies annotations)
        private Map<String, String> curieMap = new TreeMap<>();
        // All of the curies that actually ARE being used (provided via Link annotations)
        private Set<String> curiesInUse = new TreeSet<>();

        public FilteredProperties(Object bean, SerializerProvider provider,
                                  BeanDescription beanDescription) throws IOException {

            populateCurieMap(beanDescription);

            BeanPropertyWriter[] props;


            if (_filteredProps != null && provider.getActiveView() != null) {
                props = Arrays.stream(_filteredProps).filter(bpw -> {

                    if (bpw == null || bpw.getViews() == null || bpw.getViews().length == 0) {
                        return provider.getConfig().isEnabled(MapperFeature.DEFAULT_VIEW_INCLUSION);
                    }

                    return Arrays.stream(bpw.getViews()).anyMatch(clazz -> clazz.isAssignableFrom(provider.getActiveView()));
                }).toArray(BeanPropertyWriter[] ::new);
            } else {
                props = _props;
            }

            for (BeanPropertyWriter prop : props) {
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
                        String curie = "".equals(l.curie()) ? null : l.curie();
                        if (!"".equals(l.curie())) {
                            curiesInUse.add(l.curie());
                        }
                        Object value = prop.get(bean);
                        if (value instanceof Collection) {
                            addLinks(relation, (Collection<HALLink>) prop.get(bean), curie);
                        } else if (value instanceof HALLink) {
                            addLink(relation, (HALLink) prop.get(bean), curie);
                        }

                    } else {
                        state.add(prop);
                    }
                } catch (Exception e) {
                    wrapAndThrow(provider, e, bean, prop.getName());
                }
            }

            if (!curiesInUse.isEmpty()) {
                addCurieLinks();
            }
        }

        private void addCurieLinks() {
            Collection<HALLink> curieLinks = new ArrayList<>();
            for (String curie: curiesInUse) {
                if (curieMap.containsKey(curie)) {
                    curieLinks.add(new HALLink.Builder(curieMap.get(curie))
                            .name(curie)
                            .build());
                } else {
                    LOG.warn("No Curie/Curies annotation provided for [{}]", curie);
                }
            }
            addLinks("curies", curieLinks, null);
        }

        private void populateCurieMap(BeanDescription beanDescription) {

            // Curies should only be shown if they are being used by some other link.
            // Populate CurieMap now so that it can be referred to later during link
            // serialisation.  Note - either a single Curie annotation can be used by
            // itself or a collection can be wrapped using Curies.

            List<Curie> curieAnnotations = new ArrayList<>();
            if (null != beanDescription.getClassAnnotations().get(Curie.class)) {
                curieAnnotations.add(beanDescription.getClassAnnotations().get(Curie.class));
            }
            if (null != beanDescription.getClassAnnotations().get(Curies.class)) {
                curieAnnotations.addAll(Arrays.asList(beanDescription.getClassAnnotations().get(Curies.class).value()));
            }

            for (Curie curie : curieAnnotations) {
                if (curieMap.containsKey(curie.prefix())) {
                    LOG.warn("Curie annotation already exists [{}]", curie.prefix());
                }
                curieMap.put(curie.prefix(), curie.href());
            }
        }

        public void serialize(Object bean, JsonGenerator jgen, SerializerProvider provider) throws IOException {
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
                        // use the relation as new name for the original property
                        BeanPropertyWriter prop = renameBeanProperty(embedded.get(rel), rel);

                        // serialize the field as normal field
                        prop.serializeAsField(bean, jgen, provider);
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
        }

        private void addEmbeddedProperty(String rel, BeanPropertyWriter property) {
            if (embedded.put(rel, property) != null) {
                LOG.warn("Embedded resource already existed with rel [{}] in class [{}]", rel, _handledType);
            }
        }

        private void addLink(String rel, HALLink link, String curie) {
            if (links.put(applyCurieToRel(rel, curie), new LinkProperty(link)) != null) {
                LOG.warn("Link resource already existed with rel [{}] in class [{}]", rel, _handledType);
            }
        }

        private void addLinks(String rel, Collection<HALLink> links, String curie) {
            if (this.links.put(applyCurieToRel(rel, curie), new LinkProperty(links)) != null) {
                LOG.warn("Link resource already existed with rel [{}] in class [{}]", rel, _handledType);
            }
        }

        private String applyCurieToRel(String rel, String curie) {
            return (null == curie) ? rel : curie + ":" + rel;
        }

        private BeanPropertyWriter renameBeanProperty(BeanPropertyWriter prop, String newName) {
            return prop.rename(new NameTransformer() {
                @Override
                public String transform(String name) {
                    return newName;
                }

                @Override
                public String reverse(String transformed) {
                    return null;
                }
            });
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
