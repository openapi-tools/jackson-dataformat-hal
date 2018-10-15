package io.openapitools.jackson.dataformat.hal.deser;

import io.openapitools.jackson.dataformat.hal.annotation.Curie;
import java.net.URI;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Defining map of a curies in the context of HAL. That is, this will only substitute
 * a curie reference into the <code>{rel}</code> placeholder of a templated URI.
 *
 * @see <a href="https://www.w3.org/TR/2010/NOTE-curie-20101216/">CURIE Syntax 1.0</a>
 */
public class CurieMap {

    private final ConcurrentHashMap<String, String> mappings = new ConcurrentHashMap<>();


    public CurieMap(Mapping... mappings) {
        Objects.requireNonNull(mappings, "Non-null array must be provided");
        Arrays.stream(mappings).forEach(m -> this.mappings.put(m.prefix, m.template));
    }

    /**
     * Resolve the given curie using this mapping. Return empty if no relevant mapping could be found.
     *
     * @param curie Curie to resolve using this map in the standard form <code>prefix:rel</code>
     * @return Resolved URI if map contained mapping for the given prefix - empty otherwise
     */
    public Optional<URI> resolve(String curie) {
        StringTokenizer st = new StringTokenizer(curie, ":");
        if (st.countTokens() != 2) {
            return Optional.empty();
        }

        String template = mappings.get(st.nextToken());
        if (template == null) {
            return Optional.empty();
        } else {
            URI resolvedURI = URI.create(template.replace("{rel}", st.nextToken()));
            return Optional.of(resolvedURI);
        }
    }

    /**
     * A single mapping definition in the map.
     */
    public static class Mapping {
        private final String prefix;
        private final String template;

        public Mapping(String prefix, String template) {
            this.prefix = prefix;
            this.template = template;
        }

        public Mapping(Curie curie) {
            this.prefix = curie.prefix();
            this.template = curie.href();
        }
    }
}
