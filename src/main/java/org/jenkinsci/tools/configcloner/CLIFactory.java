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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;


public class CLIFactory {

    private String[] keyFiles;

    /**
     * Use standard location for ssh keys in the system.
     */
    public static CLIFactory system() {
        return new CLIFactory("~/.ssh/identity", "~/.ssh/id_rsa", "~/.ssh/id_dsa");
    }

    /**
     * Use custom key location.
     */
    public static CLIFactory provided(String... keyFiles) {
        return new CLIFactory(keyFiles);
    }

    public CLIFactory(String... keyFiles) {
        this.keyFiles = keyFiles;
    }

    public CLI create(final URL destination) throws IOException, InterruptedException {

        final CLI service = new CLI(destination);

        final List<KeyPair> userKeys = userKeys();
        try {

            service.authenticate(userKeys);
        } catch (GeneralSecurityException ex) {

            System.out.println("Anonymous access: " + ex.getMessage());
            ex.printStackTrace();
        }

        return service;
    }

    protected List<KeyPair> userKeys() {

        final List<KeyPair> pairs = new ArrayList<KeyPair>(3);

        final String home = System.getProperty("user.home");
        for (final String path: keyFiles) {


            final File key = new File(path.replaceAll("~", home));
            if (!key.exists()) continue;

            try {

                pairs.add(CLI.loadKey(key));
            } catch (IOException ex) { // if the PEM file is encrypted

                // Not run interactively - skip
                if (System.console() == null) continue;

                // Ask for password
                pairs.add(tryEncryptedFile(key));
            } catch (GeneralSecurityException ex) {

                System.err.println("Failed to load " + key);
                System.err.println(ex.getMessage());
                ex.printStackTrace();
            }
        }

        return pairs;
    }

    /**
     * Call private CLI.tryEncryptedFile
     *
     * TODO have this exposed from core
     */
    private KeyPair tryEncryptedFile(final File key) {

        try {

            Method m = CLI.class.getDeclaredMethod("tryEncryptedFile", File.class);
            m.setAccessible(true);
            return (KeyPair) m.invoke(key, key);
        } catch (IllegalArgumentException ex) {

            throw new AssertionError(ex);
        } catch (IllegalAccessException ex) {

            throw new AssertionError(ex);
        } catch (InvocationTargetException ex) {

            throw new AssertionError(ex);
        } catch (SecurityException ex) {

            throw new AssertionError(ex);
        } catch (NoSuchMethodException ex) {

            throw new AssertionError(ex);
        }
    }
}
