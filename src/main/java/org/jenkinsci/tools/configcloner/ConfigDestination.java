package org.jenkinsci.tools.configcloner;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class ConfigDestination {

    private final String jenkins;
    private final String path;

    public ConfigDestination(final URL jenkins, final String path) {

        this(jenkins.toString(), path);
    }

    public ConfigDestination(final String jenkins, final String path) {

        if (jenkins == null || jenkins.isEmpty()) throw new IllegalArgumentException(
                "Empty host provided"
        );

        if (path == null) throw new IllegalArgumentException("Empty path provided");

        this.jenkins = jenkins;
        this.path = path;
    }

    public ConfigDestination newPath(final String path) {

        return new ConfigDestination(jenkins, path);
    }

    public String path() {

        return path;
    }

    public URL url() {

        return getUrl(jenkins + path);
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

    public boolean exists() {

        try {

            url().openStream();
            return true;
        } catch (final IOException ex) {

            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + jenkins.hashCode();
        result = 31 * result + path.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {

        if (this == obj) return true;

        if (obj == null) return false;

        if (getClass() != obj.getClass()) return false;

        final ConfigDestination other = (ConfigDestination) obj;

        return this.path.equals(other.path) && this.jenkins.equals(other.jenkins);
    }

    @Override
    public String toString() {

        return jenkins().toString() + ":" + path();
    }
}
