package io.openapitools.jackson.dataformat.hal;

/**
 * The CURIEProvider interface adds support for generation
 * of CURIEs from links provided.  The idea is to automatically
 * add CURIEs without the need for users to manually add them in
 * every resource/representation.
 */
public interface CURIEProvider {

    /**
     * Determine if the HALLink should be processed for CURIE provision.
     * For example, an implementation might choose to process all links containing
     * a colon e.g. "a:b".
     */
    public boolean shouldProvideCURIE(String rel, HALLink halLink);

    /**
     * Provide a CURIE link generated from the HALLink currently being processed.  For example
     * an implementation might choose to convert a link "a:b" into CURIE link:
     *
     * <pre>
     *     {
     *       "name": "a",
     *       "href": "http://example.com/docs/rels/a-{rel}",
     *       "templated": true
     *     }
     * </pre>
     *
     */
    public HALLink provideCURIE(String rel, HALLink halLink);

}
