package io.openapitools.jackson.dataformat.hal;

/**
 * Simple CURIE provider checks for rels of the form "a:b" and
 * generates a CURIE pointing to some base URI + "/" + a + "-" + b.
 *
 * rels starting with "http" will be ignored as they are considered to
 * be fully qualified hence not eligible for CURIE creation.
 *
 * e.g. given relsBaseURI = "https://example.com/rels a CURIE with
 * href https://example.com/rels/a-{rel} will be provided.
 */
public class SimpleCURIEProvider implements CURIEProvider {

    private final String relsBaseURI;

    public SimpleCURIEProvider(String relsBaseURI) {
        this.relsBaseURI = relsBaseURI;
    }

    @Override
    public boolean shouldProvideCURIE(String rel, HALLink halLink) {
        // rel does not start with http and contains colon return true else false.
        // The http check is used to filter out fully qualified rels.
        return ((!rel.startsWith("http")) && (rel.contains(":"))) ? true : false;
    }
    
    @Override
    public HALLink provideCURIE(String rel, HALLink halLink) {
        String[] relParts = rel.split(":");
        return new HALLink.Builder(relsBaseURI + "/" + relParts[0] + "-{rel}")
                .name(relParts[0])
                .templated(true)
                .build();
    }
}
