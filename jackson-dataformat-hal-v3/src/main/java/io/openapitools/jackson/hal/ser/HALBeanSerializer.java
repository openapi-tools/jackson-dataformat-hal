package io.openapitools.jackson.hal.ser;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonToken;
import tools.jackson.core.type.WritableTypeId;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.jsontype.TypeSerializer;
import tools.jackson.databind.ser.BeanPropertyWriter;
import tools.jackson.databind.ser.bean.BeanSerializerBase;
import tools.jackson.databind.ser.impl.ObjectIdWriter;
import io.openapitools.jackson.dataformat.hal.HALLink;
import io.openapitools.jackson.dataformat.hal.annotation.Curie;
import io.openapitools.jackson.dataformat.hal.annotation.Curies;
import io.openapitools.jackson.dataformat.hal.annotation.EmbeddedResource;
import io.openapitools.jackson.dataformat.hal.annotation.Link;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.util.NameTransformer;

/**
 * Serializer to handle {@link io.openapitools.jackson.dataformat.hal.annotation.Resource} beans ensuring they are serialized according to the HAL
 * specification. This implies placing links inside the <code>_links</code> property and embedded objects inside the <code>_embedded</code>
 * property.
 */
public class HALBeanSerializer extends BeanSerializerBase {

    private static final Logger LOG = LoggerFactory.getLogger(HALBeanSerializer.class);
    private final BeanDescription.Supplier beanDescription;

    public HALBeanSerializer(BeanSerializerBase src, BeanDescription.Supplier beanDescription) {
        super(src);
        this.beanDescription = beanDescription;
    }

    // Secondary mutant constructor required for Jackson 3 abstract base implementations
    protected HALBeanSerializer(HALBeanSerializer src, ObjectIdWriter objectIdWriter) {
        super(src, objectIdWriter);
        this.beanDescription = src.beanDescription;
    }

    protected HALBeanSerializer(HALBeanSerializer src, ObjectIdWriter objectIdWriter, Object filterId) {
        super(src, objectIdWriter, filterId);
        this.beanDescription = src.beanDescription;
    }

    protected HALBeanSerializer(HALBeanSerializer src, Set<String> toIgnore, Set<String> toInclude) {
        super(src, toIgnore, toInclude);
        this.beanDescription = src.beanDescription;
    }

    protected HALBeanSerializer(HALBeanSerializer src, BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties) {
        super(src, properties, filteredProperties);
        this.beanDescription = src.beanDescription;
    }

    @Override
    public BeanSerializerBase withObjectIdWriter(ObjectIdWriter objectIdWriter) {
        return new HALBeanSerializer(this, objectIdWriter);
    }

    @Override
    public BeanSerializerBase withFilterId(Object o) {
        return new HALBeanSerializer(this, _objectIdWriter, o);
    }

    @Override
    protected BeanSerializerBase withProperties(BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties) {
        return new HALBeanSerializer(this, properties, filteredProperties);
    }

    @Override
    protected BeanSerializerBase asArraySerializer() {
        return this;
    }

    @Override
    protected BeanSerializerBase withByNameInclusion(Set<String> toIgnore, Set<String> toInclude) {
        return new HALBeanSerializer(this, toIgnore, toInclude);
    }

    @Override
    public ValueSerializer<Object> unwrappingSerializer(NameTransformer unwrapper) {
        // Return this instance directly, or wrap with tools.jackson.databind.ser.impl.UnwrappingBeanSerializer if needed
        return this;
    }

    @Override
    public void serialize(Object bean, JsonGenerator jgen, SerializationContext provider) throws JacksonException {
        FilteredProperties filtered = new FilteredProperties(bean, provider, beanDescription);

        // this serializer acts autonomous and creates the complete object
        jgen.writeStartObject(bean);
        filtered.serialize(bean, jgen, provider);
        jgen.writeEndObject();
    }

    @Override
    public void serializeWithType(Object bean, JsonGenerator jgen, SerializationContext provider,
                                  TypeSerializer typeSer) throws JacksonException {
        FilteredProperties filtered = new FilteredProperties(bean, provider, beanDescription);

        // this serializer lets the TypeSerializer create the outer frame and inserts the rest of the object
        WritableTypeId typeIdDef = _typeIdDef(typeSer, bean, JsonToken.START_OBJECT);
        WritableTypeId handledTypeId = typeSer.writeTypePrefix(jgen, provider, typeIdDef);
        filtered.serialize(bean, jgen, provider);
        typeSer.writeTypeSuffix(jgen, provider, handledTypeId);
    }

    /**
     * Modeling the properties of the bean segmented into HAL categories: links, embedded resources, and state
     */
    private class FilteredProperties {

        private List<BeanPropertyWriter> state = new ArrayList<>();
        private Map<String, LinkProperty> links = new TreeMap<>();
        private Map<String, BeanPropertyWriter> embedded = new TreeMap<>();

        // All of the possible curies that COULD be used (provided via Curie/Curies annotations)
        private Map<String, String> curieMap = new TreeMap<>();
        // All of the curies that actually ARE being used (provided via Link annotations)
        private Set<String> curiesInUse = new TreeSet<>();

        public FilteredProperties(Object bean, SerializationContext provider,
                                  BeanDescription.Supplier beanDescription) throws JacksonException {

            populateCurieMap(beanDescription);

            BeanPropertyWriter[] props;

            if (_filteredProps != null && provider.getActiveView() != null) {
                props = Arrays.stream(_filteredProps).filter(bpw -> {
                    if (bpw == null || bpw.getViews() == null || bpw.getViews().length == 0) {
                        return provider.getConfig().isEnabled(MapperFeature.DEFAULT_VIEW_INCLUSION);
                    }
                    return Arrays.stream(bpw.getViews()).anyMatch(clazz -> clazz.isAssignableFrom(provider.getActiveView()));
                }).toArray(BeanPropertyWriter[]::new);
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

        private void populateCurieMap(BeanDescription.Supplier beanDescription) {

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

        public void serialize(Object bean, JsonGenerator jgen, SerializationContext provider) throws JacksonException {
            if (!links.isEmpty()) {
                jgen.writeName("_links");
                jgen.writeStartObject();
                for (String rel : links.keySet()) {
                    jgen.writeName(rel);
                    links.get(rel).serialize(jgen);
                }
                jgen.writeEndObject();
            }

            if (!embedded.isEmpty()) {
                jgen.writeName("_embedded");
                jgen.writeStartObject();
                for (String rel : embedded.keySet()) {
                    try {
                        // use the relation as new name for the original property
                        BeanPropertyWriter prop = renameBeanProperty(embedded.get(rel), rel);

                        // serialize the field as normal field
                        prop.serializeAsProperty(bean, jgen, provider);
                    } catch (Exception e) {
                        wrapAndThrow(provider, e, bean, rel);
                    }
                }
                jgen.writeEndObject();
            }

            for (BeanPropertyWriter prop : state) {
                try {
                    prop.serializeAsProperty(bean, jgen, provider);
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
            this.links = links == null ? new HashSet<>() : links;
        }

        public void serialize(JsonGenerator jgen) {
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

        /**
         * Write HALLink fields directly without delegating to Jackson's POJO serializer.
         * Using writePOJO() would propagate the active @JsonView into HALLink's own fields
         * (href, templated, etc.), which have no @JsonView annotations and would be filtered
         * out when DEFAULT_VIEW_INCLUSION=false. The @JsonView on the resource only controls
         * whether a link is included at all — not its internal structure.
         */
        private void writeLinkObject(JsonGenerator jgen, HALLink link) {
            jgen.writeStartObject();
            if (link.getHref() != null) {
                jgen.writeStringProperty("href", link.getHref());
            }
            if (link.getTemplated() != null) {
                jgen.writeBooleanProperty("templated", link.getTemplated());
            }
            if (link.getType() != null) {
                jgen.writeStringProperty("type", link.getType());
            }
            if (link.getDeprecation() != null) {
                jgen.writeStringProperty("deprecation", link.getDeprecation().toString());
            }
            if (link.getName() != null) {
                jgen.writeStringProperty("name", link.getName());
            }
            if (link.getProfile() != null) {
                jgen.writeStringProperty("profile", link.getProfile().toString());
            }
            if (link.getTitle() != null) {
                jgen.writeStringProperty("title", link.getTitle());
            }
            if (link.getHreflang() != null) {
                jgen.writeStringProperty("hreflang", link.getHreflang());
            }
            if (link.getSeen() != null) {
                jgen.writeStringProperty("seen", link.getSeen());
            }
            jgen.writeEndObject();
        }

    }
}
