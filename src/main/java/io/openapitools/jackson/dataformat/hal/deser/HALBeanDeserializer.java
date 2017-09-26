package io.openapitools.jackson.dataformat.hal.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.std.DelegatingDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * Deserializer to handle incomming application/hal+json.
 */
public class HALBeanDeserializer extends DelegatingDeserializer {

    public HALBeanDeserializer(BeanDeserializerBase delegate) {
        super(delegate);
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        TreeNode tn = p.getCodec().readTree(p);  
        if (tn.isObject()) {
            ObjectNode root = (ObjectNode) tn;
            for (ReservedProperty rp : ReservedProperty.values()) {
                ObjectNode on = (ObjectNode) tn.get(rp.getPropertyName());
                if (on != null) {
                    Iterator<Map.Entry<String,JsonNode>> it = on.fields();
                    while (it.hasNext()) {
                        Map.Entry<String,JsonNode> jn = it.next();
                        root.set(rp.alternateName(jn.getKey()), jn.getValue());
                    }
                    root.remove(rp.getPropertyName());                
                }
                
            }
        }

        final JsonParser modifiedParser = tn.traverse(p.getCodec());
        modifiedParser.nextToken();
        return _delegatee.deserialize(modifiedParser, ctxt);
    }

    @Override
    protected JsonDeserializer<?> newDelegatingInstance(JsonDeserializer<?> newDelegatee) {
        return new HALBeanDeserializer((BeanDeserializerBase) newDelegatee);
    }
    
}
