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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Deserializer to handle incoming application/hal+json. The de-serializer is responsible for intercepting
 * the reserved properties (<code>_links</code> and <code>_embedded</code>) and mapping the properties of these
 * objects in the incoming json to the uniquely assigned properties of the POJO class.
 */
public class HALBeanDeserializer extends DelegatingDeserializer {

    public HALBeanDeserializer(BeanDeserializerBase delegate) {
        super(delegate);
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        TreeNode tn = p.getCodec().readTree(p);
        if (tn.isObject()) {
            ObjectNode root = (ObjectNode) tn;
            for (ReservedProperty rp : ReservedProperty.values()) {
                ObjectNode on = (ObjectNode) tn.get(rp.getPropertyName());
                if (on != null) {
                    CurieMap curieMap = createCurieMap(rp, on);
                    on.remove("curies");

                    Iterator<Map.Entry<String, JsonNode>> it = on.fields();
                    while (it.hasNext()) {
                        Map.Entry<String, JsonNode> jn = it.next();
                        String propertyName = curieMap.resolve(jn.getKey()).map(URI::toString).orElse(jn.getKey());
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

    private CurieMap createCurieMap(ReservedProperty rp, ObjectNode on) {
        if (ReservedProperty.LINKS.equals(rp) && on.has("curies")) {
            ArrayNode curies = (ArrayNode) on.get("curies");
            List<CurieMap.Mapping> mappings = StreamSupport.stream(curies.spliterator(), false)
                    .map(n -> createMapping((ObjectNode) n))
                    .collect(Collectors.toList());
            return new CurieMap(mappings.toArray(new CurieMap.Mapping[0]));
        } else {
            return new CurieMap();
        }
    }

    private CurieMap.Mapping createMapping(ObjectNode node) {
        return new CurieMap.Mapping(node.get("name").textValue(), node.get("href").textValue());
    }

    @Override
    protected JsonDeserializer<?> newDelegatingInstance(JsonDeserializer<?> newDelegatee) {
        return new HALBeanDeserializer((BeanDeserializerBase) newDelegatee);
    }

}
