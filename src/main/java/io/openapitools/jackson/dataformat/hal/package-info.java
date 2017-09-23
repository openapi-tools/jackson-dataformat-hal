/**
 * Package containing classes to help generating HAL compliant JSON output using the Jackson framework. The module is activated
 * by adding a Jackson module to the Jackson {@link com.fasterxml.jackson.databind.ObjectMapper}:
 * <pre>
 * ObjectMapper om = new ObjectMapper();
 * om.registerModule(new JacksonHALModule());
 * </pre>
 *
 * To signal some bean is to be treated as HAL resource it should be annotated with the {@link io.openapitools.jackson.dataformat.hal.annotation.Resource}
 * annotation. To mark a property of a HAL resource for inclusion as embedded resource the property should be annotations with
 * {@link io.openapitools.jackson.dataformat.hal.annotation.EmbeddedResource} - this is true for both collections of resources and for one-to-one
 * relationships. Adding a link is done by creating a property of type {@link HALLink} annotated with
 * {@link io.openapitools.jackson.dataformat.hal.annotation.Link} - this is also true for collections of links
 */
package io.openapitools.jackson.dataformat.hal;
