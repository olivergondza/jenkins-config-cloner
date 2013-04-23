package org.jenkinsci.tools.configcloner;

import java.net.MalformedURLException;
import java.net.URL;

public class ConfigDestination {

    private final String jenkins;
    private final String entity;

    public ConfigDestination(final URL jenkins, final String entity) {

        this(jenkins.toString(), entity);
    }

    public ConfigDestination(final String jenkins, final String path) {

        if (jenkins == null || jenkins.isEmpty()) throw new IllegalArgumentException(
                "Empty host provided"
        );

        if (path == null) throw new IllegalArgumentException("Empty path provided");

        this.jenkins = jenkins;
        this.entity = path;
    }

    public ConfigDestination newEntity(final String entity) {

        return new ConfigDestination(jenkins, entity);
    }

    public String entity() {

        return entity;
    }
    
    public URL jenkins() {

        return getUrl(jenkins);
    }

    private static URL getUrl(final String url) {

        try {

            return new URL(url);
        } catch (final MalformedURLException ex) {

            throw new AssertionError(ex);
        }
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + jenkins.hashCode();
        result = 31 * result + entity.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {

        if (this == obj) return true;

        if (obj == null) return false;

        if (getClass() != obj.getClass()) return false;

        final ConfigDestination other = (ConfigDestination) obj;

        return this.entity.equals(other.entity) && this.jenkins.equals(other.jenkins);
    }

    @Override
    public String toString() {

        return jenkins().toString() + ":" + entity();
    }
}
