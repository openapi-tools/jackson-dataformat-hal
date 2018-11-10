package io.openapitools.jackson.dataformat.hal.deser;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import io.openapitools.jackson.dataformat.hal.annotation.Curie;
import io.openapitools.jackson.dataformat.hal.annotation.Curies;
import io.openapitools.jackson.dataformat.hal.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Modify the deserialization of classes annotated with {@link Resource}. Deserialization will handle the reserved
 * properties <code>_links</code> and <code>_embedded</code> by assigning a unique property name to each of the
 * properties that are part of these sections.
 */
public class HALBeanDeserializerModifier extends BeanDeserializerModifier {

    @Override
    public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
        Resource ann = beanDesc.getClassAnnotations().get(Resource.class);
        if (ann != null) {
            return new HALBeanDeserializer((BeanDeserializer) deserializer);
        }
        return deserializer;
    }

    @Override
    public List<BeanPropertyDefinition> updateProperties(DeserializationConfig config, BeanDescription beanDesc, List<BeanPropertyDefinition> propDefs) {
        Resource ann = beanDesc.getClassAnnotations().get(Resource.class);
        if (ann != null) {
            CurieMap map = createCurieMap(beanDesc);
            List<BeanPropertyDefinition> modified = new ArrayList<>();
            Iterator<BeanPropertyDefinition> properties = propDefs.iterator();
            while (properties.hasNext()) {
                BeanPropertyDefinition property = properties.next();
                for (ReservedProperty rp : ReservedProperty.values()) {
                    String alternateName = rp.alternateName(property, map);
                    if (!property.getName().equals(alternateName)) {
                        modified.add(property.withName(new PropertyName(alternateName)));
                        properties.remove();
                    }
                }
            }
            propDefs.addAll(modified);
        }
        return propDefs;
    }

    private CurieMap createCurieMap(BeanDescription beanDesc) {
        ArrayList<CurieMap.Mapping> mappings = new ArrayList<>();
        Curie sc = beanDesc.getClassAnnotations().get(Curie.class);
        if (sc != null) {
            mappings.add(new CurieMap.Mapping(sc));
        }
        Curies cs = beanDesc.getClassAnnotations().get(Curies.class);
        if (cs != null) {
            Arrays.stream(cs.value()).forEach(c -> mappings.add(new CurieMap.Mapping(c)));
        }
        return new CurieMap(mappings.toArray(new CurieMap.Mapping[0]));
    }

}
