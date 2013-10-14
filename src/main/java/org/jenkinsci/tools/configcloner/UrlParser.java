/*
 * The MIT License
 *
 * Copyright (c) 2013 Red Hat, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
