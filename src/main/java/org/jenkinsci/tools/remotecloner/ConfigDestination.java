package org.jenkinsci.tools.remotecloner;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.beust.jcommander.ParameterException;

public class ConfigDestination {

    private final String host;
    private final String path;

    public static ConfigDestination parse(final String urlCandidate) {

        final URL url = getUrl(urlCandidate);

        final String stringUrl = url.toString();

        final int domainPos = stringUrl.indexOf(url.getHost());
        final int endOfTheHost = stringUrl.indexOf('/', domainPos);
        final String host = endOfTheHost == -1
                ? stringUrl
                : stringUrl.substring(0, endOfTheHost)
        ;
        return new ConfigDestination(host, url.getPath());
    }

    public ConfigDestination(final String host, final String path) {

        if (host == null || host.isEmpty()) throw new IllegalArgumentException(
                "Empty host provided"
        );

        if (path == null) throw new IllegalArgumentException("Empty path provided");

        this.host = host;
        this.path = path;
    }

    public ConfigDestination pair(final String urlCandidate) {

        final ConfigDestination dest = _parseDest(urlCandidate);

        if (dest.url().equals(url())) throw new ParameterException(
                "Source and destination is the same url"
        );

        return dest;
    }

    private ConfigDestination _parseDest(final String urlCandidate) {

        final URL url = getUrl(urlCandidate);
        final ConfigDestination dest = ConfigDestination.parse(urlCandidate);

        // We have absolute path given
        if (!url.getPath().isEmpty()) return dest;

        return new ConfigDestination(dest.host().toString(), path());
    }

    public ConfigDestination newPath(final String path) {

        return ConfigDestination.parse(host + path);
    }

    private static URL getUrl(final String url) {

        try {

            return new URL(url);
        } catch (final MalformedURLException ex) {

            ex.printStackTrace();
            throw new AssertionError("Invalid URL " + url);
        }
    }

    public URL host() {

        return getUrl(host);
    }

    public String path() {

        return path;
    }

    public URL url() {

        return getUrl(host + path);
    }

    public boolean exists() {

        try {

            url().openStream();
            return true;
        } catch (final IOException ex) {

            return false;
        }
    }
}
