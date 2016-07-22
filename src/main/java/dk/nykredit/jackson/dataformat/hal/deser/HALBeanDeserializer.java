package dk.nykredit.jackson.dataformat.hal.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.NameTransformer;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Deserializer to handle incomming application/hal+json.
 */
public class HALBeanDeserializer extends BeanDeserializerBase {

    public HALBeanDeserializer(BeanDeserializerBase src) {
        super(src);
        _delegateDeserializer = src;
    }

    @Override
    public JsonDeserializer<Object> unwrappingDeserializer(NameTransformer unwrapper) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public BeanDeserializerBase withObjectIdReader(ObjectIdReader oir) {
        return this;
    }

    @Override
    public BeanDeserializerBase withIgnorableProperties(Set<String> ignorableProps) {
        return this;
    }

    @Override
    protected BeanDeserializerBase asArrayDeserializer() {
        return this;
    }

    @Override
    public Object deserializeFromObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        return deserialize(p, ctxt);
    }

    @Override
    protected Object _deserializeUsingPropertyBased(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        return _delegateDeserializer.deserialize(modifiedParser, ctxt);
    }
    
}
