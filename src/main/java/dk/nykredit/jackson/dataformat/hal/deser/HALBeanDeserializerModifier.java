/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.nykredit.jackson.dataformat.hal.deser;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import dk.nykredit.jackson.dataformat.hal.annotation.Resource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Modify the deserialization of classes annotated with {@link Resource}.
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
            List<BeanPropertyDefinition> modified = new ArrayList<>();
            Iterator<BeanPropertyDefinition> defIt = propDefs.iterator();
            while (defIt.hasNext()) {
                BeanPropertyDefinition pbd = defIt.next();                
                for (ReservedProperty rp : ReservedProperty.values()) {
                    String alternateName = rp.alternateName(pbd.getField(), pbd.getName());
                    if (!pbd.getName().equals(alternateName)) {
                        modified.add(pbd.withName(new PropertyName(alternateName)));
                        defIt.remove();                                                                    
                    }
                }
            }
            propDefs.addAll(modified);
        }
        return propDefs;
    }
      
}
