package org.jenkinsci.tools.configcloner;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
    public List<ConfigDestination> pair(
            final ConfigDestination base, final List<String> urlCandidates
    ) {

        final List<ConfigDestination> urls = new ArrayList<ConfigDestination>(urlCandidates.size());
        for (final String url: urlCandidates) {

            final ConfigDestination dest = _parseDest(base, url);

            if (dest.equals(base)) throw new ParameterException(
                    "Source and destination is the same url"
            );

            urls.add(dest);
        }

        return urls;
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
