package io.openapitools.jackson.dataformat.hal.deser;

import io.openapitools.jackson.dataformat.hal.annotation.Curie;
import java.lang.management.OperatingSystemMXBean;
import java.net.URI;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Defining mapping of a curie in the context of HAL. That is this will only substitute
 * a curie reference into <code>{rel}</code> of a templated URI.
 *
 * @see <a href="https://www.w3.org/TR/2010/NOTE-curie-20101216/">CURIE SYntax 1.0</a>
 */
public class CurieMapping {

    private final ConcurrentHashMap<String, String> mappings = new ConcurrentHashMap<>();


    public CurieMapping(Mapping... mappings) {
        Objects.requireNonNull(mappings, "Non-null array must be provided");
        Arrays.stream(mappings).forEach(m -> this.mappings.put(m.prefix, m.template));
    }

    /**
     * Resolve the given curie using this mapping. Return empty if no relevant mapping could be found.
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
