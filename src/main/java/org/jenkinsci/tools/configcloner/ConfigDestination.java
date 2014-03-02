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

        return jenkins().toString() + "::" + entity();
    }

    public static ConfigDestination fromString(String locator) throws MalformedURLException {

        final String[] chunks = locator.split("::");

        if (chunks.length != 2) throw new IllegalArgumentException("Invalid locator syntax");

        return new ConfigDestination(new URL(chunks[0]), chunks[1]);
    }
}
