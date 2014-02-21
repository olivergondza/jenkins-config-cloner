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
package org.jenkinsci.tools.configcloner.handler;

import java.util.List;

import org.jenkinsci.tools.configcloner.CommandResponse;
import org.jenkinsci.tools.configcloner.ConfigDestination;
import org.jenkinsci.tools.configcloner.ConfigTransfer;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "Clone global configuration. NOT IMPLEMENTED.")
public class CloneGlobal extends TransferHandler {

    @Parameter(description = "[<SRC>] [<DST>...]")
    private List<String> jobs;
    private ConfigDestination source;
    private ConfigDestination destination;

    public CloneGlobal(final ConfigTransfer config) {

        super(config);
    }

    @Override
    public CommandResponse run(final CommandResponse response) {

        throw new UnsupportedOperationException();
    }

    @Override
    protected ConfigDestination source() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected List<ConfigDestination> destinations() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected String getCommandName() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected String updateCommandName() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected String createCommandName() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected String deleteCommandName() {
        throw new UnsupportedOperationException();
    }
}
