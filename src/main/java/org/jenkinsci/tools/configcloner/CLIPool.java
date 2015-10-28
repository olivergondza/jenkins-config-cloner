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

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import hudson.cli.CLI;

/**
 * Cache CLI connections so they can be reused for multiple requests.
 *
 * @author ogondza
 */
public class CLIPool {

    private final CLIFactory factory;

    private final Map<URL, CLI> pool = new HashMap<URL, CLI>(2);

    public CLIPool(CLIFactory factory) {
        this.factory = factory;
    }

    public void close() {

        for(final CLI connection: pool.values()) {

            try {

                connection.close();
            } catch (Exception ex) {

                ex.printStackTrace();
            }
        }
    }

    public CLI connection(final URL instance) {

        try {

            final CLI connection = pool.containsKey(instance)
                    ? pool.get(instance)
                    : factory.create(instance)
            ;

            pool.put(instance, connection);

            return connection;
        } catch (IOException ex) {

            throw new HandlerException(ex);
        } catch (InterruptedException ex) {

            throw new HandlerException(ex);
        }
    }
}
