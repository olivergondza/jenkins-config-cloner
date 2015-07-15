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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jenkinsci.tools.configcloner.handler.CloneJob;
import org.jenkinsci.tools.configcloner.handler.CloneNode;
import org.jenkinsci.tools.configcloner.handler.CloneView;
import org.jenkinsci.tools.configcloner.handler.Handler;
import org.jenkinsci.tools.configcloner.handler.InvalidUsage;
import org.jenkinsci.tools.configcloner.handler.Recipe;
import org.jenkinsci.tools.configcloner.handler.Usage;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public class Main {

    private final CommandResponse response;
    private final Handler usage = new Usage(this);
    private final Map<String, Handler> commandMapping = new LinkedHashMap<String, Handler>();

    public static void main(final String[] args) {

        final CommandResponse resp = CommandResponse.system();
        final CLIPool cliPool = new CLIPool();
        final CommandResponse response = new Main(resp, cliPool).run(args);

        cliPool.close();
        System.exit(response.returnCode());
    }

    public Main(CommandResponse response, CLIPool cliPool) {

        this.response = response;
        setupMapping(cliPool);
    }

    private void setupMapping(CLIPool cliPool) {

        final ConfigTransfer config = new ConfigTransfer(cliPool);

        addCommand(usage);
        addCommand(new CloneJob(config));
        addCommand(new CloneView(config));
        addCommand(new CloneNode(config));
        addCommand(new Recipe(config, cliPool));
    }

    private void addCommand(final Handler handler) {

        commandMapping.put(handler.name(), handler);
    }

    public CommandResponse run(final String... args) {

        try {

            getHandler(args).run(response);
        } catch (final Exception ex) {

            ex.printStackTrace(response.err());
            usage.run(response);
            response.returnCode(-1);
        }

        return response;
    }

    public Map<String, Handler> commandMapping() {

        return Collections.unmodifiableMap(commandMapping);
    }

    public Handler getHandler(final String... args) {

        if (args.length == 0) return usage;

        final Handler handler = commandMapping.get(args[0]);

        if (handler == null) return usage;

        final CmdLineParser parser = new CmdLineParser(handler);

        try {

            parser.parseArgument(args.length == 0 ? new String[] {} : Arrays.copyOfRange(args, 1, args.length));
        } catch (CmdLineException ex) {

            return new InvalidUsage(parser, ex);
        }

        return handler;
    }
}
