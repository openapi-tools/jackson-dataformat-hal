package io.openapitools.jackson.hal.deser;

import io.openapitools.jackson.dataformat.hal.deser.CurieMap;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.TreeNode;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.bean.BeanDeserializerBase;
import tools.jackson.databind.deser.std.DelegatingDeserializer;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import java.net.URI;
import java.util.Map;
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
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        TreeNode tn = ctxt.readTree(p);
        if (tn.isObject()) {
            ObjectNode root = (ObjectNode) tn;
            for (ReservedProperty rp : ReservedProperty.values()) {
                ObjectNode on = (ObjectNode) tn.get(rp.getPropertyName());
                if (on != null) {
                    CurieMap curieMap = createCurieMap(rp, on);
                    on.remove("curies");

                    for (Map.Entry<String, JsonNode> jn : on.properties()) {
                        String propertyName = curieMap.resolve(jn.getKey()).map(URI::toString).orElse(jn.getKey());
                        root.set(rp.alternateName(propertyName), jn.getValue());
                    }

                    root.remove(rp.getPropertyName());
                }

            }
        }

        final JsonParser modifiedParser = tn.traverse(p.objectReadContext());
        modifiedParser.nextToken();
        return _delegatee.deserialize(modifiedParser, ctxt);
    }

    private CurieMap createCurieMap(ReservedProperty rp, ObjectNode on) {
        if (ReservedProperty.LINKS.equals(rp) && on.has("curies")) {
            ArrayNode curies = (ArrayNode) on.get("curies");
            return new CurieMap(StreamSupport.stream(curies.spliterator(), false)
                .map(n -> createMapping((ObjectNode) n)).toArray(CurieMap.Mapping[]::new));
        } else {
            return new CurieMap();
        }
    }

    private CurieMap.Mapping createMapping(ObjectNode node) {
        return new CurieMap.Mapping(node.get("name").asString(), node.get("href").asString());
    }

    @Override
    protected ValueDeserializer<?> newDelegatingInstance(ValueDeserializer<?> newDelegatee) {
        return new HALBeanDeserializer((BeanDeserializerBase) newDelegatee);
    }
}
