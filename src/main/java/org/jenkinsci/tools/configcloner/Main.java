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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jenkinsci.tools.configcloner.handler.CloneGlobal;
import org.jenkinsci.tools.configcloner.handler.CloneJob;
import org.jenkinsci.tools.configcloner.handler.CloneNode;
import org.jenkinsci.tools.configcloner.handler.CloneView;
import org.jenkinsci.tools.configcloner.handler.Handler;
import org.jenkinsci.tools.configcloner.handler.InvalidUsage;
import org.jenkinsci.tools.configcloner.handler.Recipe;
import org.jenkinsci.tools.configcloner.handler.Usage;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class Main {

    private final CommandResponse response;
    private final JCommander commander = new JCommander();
    private final Handler usage = new Usage(commander);
    private final Map<String, Handler> commandMapping = new HashMap<String, Handler>();

    public static void main(final String[] args) {

        final CommandResponse resp = new CommandResponse(System.out, System.err);
        final CLIPool cliPool = new CLIPool();
        final CommandResponse response = new Main(resp, cliPool).run(args);

        cliPool.close();
        System.exit(response.returnCode());
    }

    public Main(CommandResponse response, CLIPool cliPool) {
        commander.setProgramName("remote-cloner");

        this.response = response;
        setupMapping(cliPool);
    }

    private void setupMapping(CLIPool cliPool) {

        final ConfigTransfer config = new ConfigTransfer(cliPool);

        addCommand(usage);
        addCommand(new CloneJob(config));
        addCommand(new CloneView(config));
        addCommand(new CloneNode(config));
        addCommand(new CloneGlobal(config));
        addCommand(new Recipe(config, cliPool));
    }

    private void addCommand(final Handler handler) {

        commandMapping.put(handler.name(), handler);
        commander.addCommand(handler.name(), handler);
    }

    public CommandResponse run(final String... args) {

        Handler handler;
        try {

            commander.parse(args);

            handler = getHandler(args);
            handler.validate();
        } catch (final ParameterException ex) {

            handler = new InvalidUsage(usage, ex.getMessage());
        }

        CommandResponse response;
        try {

            response = handler.run(this.response);
        } catch (final ParameterException ex) {

            response = this.response;
            response.err().println(ex.getMessage());
            response.returnCode(-1);
        }

        if (response == null) throw new AssertionError(
                "null response from " + handler.toString()
        );

        return response;
    }

    public Map<String, Handler> commandMapping() {

        return Collections.unmodifiableMap(commandMapping);
    }

    public Handler getHandler(final String... args) {

        final Handler handler = commandMapping.get(commander.getParsedCommand());

        return handler != null ? handler : usage;
    }
}
