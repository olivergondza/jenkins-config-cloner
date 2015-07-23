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

import hudson.cli.CLI;
import hudson.cli.PrivateKeyProvider;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.List;

import javax.annotation.CheckForNull;


public class CLIFactory {

    private @CheckForNull String[] keyFiles;

    /**
     * Use standard location for ssh keys in the system.
     */
    public static CLIFactory system() {
        return new CLIFactory(null);
    }

    /**
     * Use custom key location.
     */
    public static CLIFactory provided(String... keyFiles) {
        return new CLIFactory(keyFiles);
    }

    public CLIFactory(@CheckForNull String... keyFiles) {
        this.keyFiles = keyFiles;
    }

    public CLI create(final URL destination) throws IOException, InterruptedException {

        final CLI service = new CLI(destination);

        final List<KeyPair> userKeys = userKeys();
        try {

            service.authenticate(userKeys);
        } catch (GeneralSecurityException ex) {

            System.out.printf("Anonymous access to %s: %s%n", destination, ex.getMessage());
        }

        return service;
    }

    protected List<KeyPair> userKeys() {

        final PrivateKeyProvider provider = new PrivateKeyProvider();
        if (keyFiles == null) {
            provider.readFromDefaultLocations();
            return provider.getKeys();
        }

        for (final String path: keyFiles) {

            try {

                provider.readFrom(new File(path));
            } catch (IOException ex) { // if the PEM file is encrypted

                System.err.println("Failed to load key from " + path);
                ex.printStackTrace();
            } catch (GeneralSecurityException ex) {

                System.err.println("Failed to load key from " + path);
                ex.printStackTrace();
            }
        }

        return provider.getKeys();
    }
}
