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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class ConfigTransfer {

    private final CLIPool cliPool;

    public ConfigTransfer(final CLIPool cliPool) {
        this.cliPool = cliPool;
    }

    public CommandResponse.Accumulator execute(
            final ConfigDestination destination,
            final String stdin,
            final String... command
    ) {

        final CLI service = cliPool.connection(destination.jenkins());

        final CommandResponse.Accumulator response = CommandResponse.accumulate();
        final int ret = service.execute(Arrays.asList(command), inputStream(stdin), response.out(), response.err());
        return response.returnCode(ret);
    }

    private InputStream inputStream(String in) {

        try {

            return new ByteArrayInputStream(in.getBytes("UTF-8"));
        } catch (final UnsupportedEncodingException ex) {

            throw new AssertionError(ex);
        }
    }
}
