package io.openapitools.jackson.dataformat.hal.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.std.DelegatingDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * Deserializer to handle incoming application/hal+json.
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
                    ArrayList<CurieMapping.Mapping> mappings = new ArrayList<>();
                    if (ReservedProperty.LINKS.equals(rp) && on.has("curies")) {
                        ArrayNode curies = (ArrayNode) on.get("curies");
                        curies.elements().forEachRemaining(node -> mappings.add(createMapping((ObjectNode) node)));
                        on.remove("curies");
                    }
                    CurieMapping curieMapping = new CurieMapping(mappings.toArray(new CurieMapping.Mapping[0]));

                    Iterator<Map.Entry<String,JsonNode>> it = on.fields();
                    while (it.hasNext()) {
                        Map.Entry<String,JsonNode> jn = it.next();
                        String propertyName = curieMapping.resolve(jn.getKey()).map(URI::toString).orElse(jn.getKey());
                        root.set(rp.alternateName(propertyName), jn.getValue());
                    }
                    root.remove(rp.getPropertyName());                
                }
                
            }
        }

        final JsonParser modifiedParser = tn.traverse(p.getCodec());
        modifiedParser.nextToken();
        return _delegatee.deserialize(modifiedParser, ctxt);
    }

    private CurieMapping.Mapping createMapping(ObjectNode node) {
        return new CurieMapping.Mapping(node.get("name").textValue(), node.get("href").textValue());
    }

    private void removeCuries(ReservedProperty rp, ObjectNode on) {
        // Check for curies in the _links object.  If they exist, remove them
        // as we have nothing in the bean to deserialize into.  Curies only exist
        // as annotations on the bean!
        if (rp == ReservedProperty.LINKS) {
            if (on.has("curies")) {
                on.remove("curies");
            }
        }
    }

    @Override
    protected JsonDeserializer<?> newDelegatingInstance(JsonDeserializer<?> newDelegatee) {
        return new HALBeanDeserializer((BeanDeserializerBase) newDelegatee);
    }
    
}
