package dk.nykredit.jackson.dataformat.hal;

import java.net.URI;
import java.net.URL;

import javax.xml.bind.annotation.XmlAttribute;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Representation of link as defined in HAL - see
 * <a href="http://tools.ietf.org/html/draft-kelly-json-hal-06">JSON Hypertext Application Language</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HALLink {

    private String href;
    private Boolean templated;
    private String type;
    private URL deprecation;
    private String name;
    private URI profile;
    private String title;
    private String hreflang;

    public HALLink() {
        // Used by JAXB
    }

    protected HALLink(Builder builder) {
        this.href = builder.href;
        this.templated = builder.templated;
        this.type = builder.type;
        this.deprecation = builder.deprecation;
        this.name = builder.name;
        this.profile = builder.profile;
        this.title = builder.title;
        this.hreflang = builder.hreflang;
    }

    @XmlAttribute
    public String getHref() {
        return href;
    }

    @XmlAttribute
    public Boolean getTemplated() {
        return templated;
    }

    @XmlAttribute
    public String getType() {
        return type;
    }

    @XmlAttribute
    public URL getDeprecation() {
        return deprecation;
    }

    @XmlAttribute
    public String getName() {
        return name;
    }

    @XmlAttribute
    public URI getProfile() {
        return profile;
    }

    @XmlAttribute
    public String getTitle() {
        return title;
    }

    @XmlAttribute
    public String getHreflang() {
        return hreflang;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HALLink link = (HALLink) o;

        if (href != null ? !href.equals(link.href) : link.href != null) {
            return false;
        }
        if (templated != null ? !templated.equals(link.templated) : link.templated != null) {
            return false;
        }
        if (type != null ? !type.equals(link.type) : link.type != null) {
            return false;
        }
        if (deprecation != null ? !deprecation.equals(link.deprecation) : link.deprecation != null) {
            return false;
        }
        if (name != null ? !name.equals(link.name) : link.name != null) {
            return false;
        }
        if (profile != null ? !profile.equals(link.profile) : link.profile != null) {
            return false;
        }
        if (title != null ? !title.equals(link.title) : link.title != null) {
            return false;
        }
        return hreflang != null ? hreflang.equals(link.hreflang) : link.hreflang == null;
    }

    @Override
    public int hashCode() {
        int result = href != null ? href.hashCode() : 0;
        result = 31 * result + (templated != null ? templated.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (deprecation != null ? deprecation.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (profile != null ? profile.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (hreflang != null ? hreflang.hashCode() : 0);
        return result;
    }

    /**
     * Builder to help build {@link HALLink} instances.
     */
    public static class Builder {

        private final String href;
        private Boolean templated;
        private String type;
        private URL deprecation;
        private String name;
        private URI profile;
        private String title;
        private String hreflang;

        /**
         * Should be used when constructing a templated link.
         * @param href Template URI to construct link from.
         */
        public Builder(String href) {
            this.href = href;
            templated = true;
        }

        /**
         * Should be used when constructing a link which is not templated.
         * @param href URI to construct link from.
         */
        public Builder(URI href) {
            this.href = href.toString();
        }

        public Builder templated(boolean templated) {
            this.templated = templated;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder deprecation(URL deprecation) {
            this.deprecation = deprecation;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder profile(URI profile) {
            this.profile = profile;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder hreflang(String hreflang) {
            this.hreflang = hreflang;
            return this;
        }

        public HALLink build() {
            return new HALLink(this);
        }
    }
}
