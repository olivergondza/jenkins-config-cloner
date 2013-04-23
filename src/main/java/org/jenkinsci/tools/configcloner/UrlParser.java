package org.jenkinsci.tools.configcloner;

import java.net.MalformedURLException;
import java.net.URL;

import com.beust.jcommander.ParameterException;

/**
 * Get Jenkins url and entity name from url
 * 
 * @author ogondza
 */
public abstract class UrlParser {

    protected abstract ConfigDestination parseDestination(final URL url);

    public final ConfigDestination destination(final String stringUrl) {

        return parseDestination(validateUrl(stringUrl));
    }

    /**
     * Infer entity using base and url
     */
    public ConfigDestination pair(
            final ConfigDestination base, final String urlCandidate
    ) {

        final ConfigDestination dest = _parseDest(base, urlCandidate);

        if (dest.equals(base)) throw new ParameterException(
                "Source and destination is the same url"
        );

        return dest;
    }

    private ConfigDestination _parseDest(
            final ConfigDestination base, final String urlCandidate
    ) {

        final ConfigDestination dest = destination(urlCandidate);

        // We have absolute iditifier
        if (!dest.entity().isEmpty()) return dest;

        return dest.newEntity(base.entity());
    }

    public static URL validateUrl(final String url) {

        try {

            return new URL(url);
        } catch (final MalformedURLException ex) {

            throw new ParameterException(ex);
        }
    }
}
