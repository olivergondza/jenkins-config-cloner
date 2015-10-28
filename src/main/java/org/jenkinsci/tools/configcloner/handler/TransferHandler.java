/*
 * The MIT License
 *
 * Copyright (c) 2014 Red Hat, Inc.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jenkinsci.tools.configcloner.CommandResponse;
import org.jenkinsci.tools.configcloner.ConfigDestination;
import org.jenkinsci.tools.configcloner.ConfigTransfer;
import org.jenkinsci.tools.configcloner.UrlParser;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.unix4j.Unix4j;
import org.unix4j.builder.Unix4jCommandBuilder;

import difflib.DiffUtils;
import difflib.Patch;
import hudson.Util;

public abstract class TransferHandler implements Handler {

    @Argument(multiValued = true, usage = "[<SRC>] [<DST>...]", metaVar = "URLS")
    private List<String> entities = new ArrayList<String>();

    @Option(name = "-f", aliases = { "--force" }, usage = "Overwrite target configuration if exists")
    protected boolean force = false;

    @Option(name = "-e", aliases = { "--expression" }, usage = "Transform configuration")
    protected List<String> expressions = new ArrayList<String>();

    @Option(name = "-n", aliases = { "--dry-run" }, usage = "Do not perform any modifications to any instance")
    protected boolean dryRun = false;

    protected final ConfigTransfer config;

    protected TransferHandler(final ConfigTransfer config) {
        this.config = config;
    }

    /**
     * Template method to fetch and sent to many.
     *
     * Expects subclasses override *CommandName methods. This can be overriden in case the template is not suitable.
     */
    public CommandResponse run(final CommandResponse response) {

        // Get both of these before doing any work to fail validation early.
        final ConfigDestination source = this.source();
        final List<ConfigDestination> destinations = this.destinations();

        response.out().println("Fetching " + source);
        final CommandResponse.Accumulator xml = config.execute(
                source, "", this.getCommandName(), source.entity()
        );

        if (!xml.succeeded()) return response.merge(xml);

        for (final ConfigDestination dest: destinations) {

            response.out().println("Sending " + dest);
            send(dest, response, xml);
        }

        return response;
    }

    private CommandResponse send(
            final ConfigDestination destination,
            final CommandResponse response,
            final CommandResponse.Accumulator xml
    ) {

        final String destJob = destination.entity();

        final String xmlString = getXml(fixupConfig(xml.stdout(), destination), response);

        if (dryRun) return response.returnCode(0);

        if (force) {

            final CommandResponse.Accumulator rsp = config.execute(
                    destination, xmlString, this.updateCommandName(), destJob
            );

            if (rsp.succeeded()) return response.returnCode(0);
        }

        return response.merge(
                config.execute(destination, xmlString, this.createCommandName(), destJob)
        );
    }

    private String getXml(String rawXml, CommandResponse response) {

        if (expressions.isEmpty()) return rawXml;

        Unix4jCommandBuilder builder = Unix4j.fromString(rawXml);
        for (String expr: expressions) {
            builder = builder.sed(expr);
        }

        final String newXml = builder.toStringResult();

        if (dryRun) response.out().print(describeTransformation(rawXml, newXml));

        return newXml;
    }

    private String describeTransformation(String rawXml, String newXml) {

        final List<String> rawLines = Arrays.asList(rawXml.split("\\r?\\n|\\r"));
        Patch patch = DiffUtils.diff(rawLines, Arrays.asList(newXml.split("\\r?\\n|\\r")));

        List<String> diff = DiffUtils.generateUnifiedDiff("Original", "Transformed", rawLines, patch, 3);
        return Util.join(diff, System.lineSeparator());
    }

    /**
     * Transform configuration before sending it to destination
     */
    protected String fixupConfig(String config, ConfigDestination destination) {
        return config;
    }

    protected ConfigDestination source() {
        if (entities == null || entities.size() < 2) throw new IllegalArgumentException(
                "Expecting 2 or more positional arguments"
        );
        return urlParser().destination(entities.get(0));
    }

    protected List<ConfigDestination> destinations() {
        if (entities == null || entities.size() < 2) throw new IllegalArgumentException(
                "Expecting 2 or more positional arguments"
        );
        return urlParser().pair(source(), entities.subList(1, entities.size()));
    }

    protected abstract UrlParser urlParser();

    protected abstract String getCommandName();
    protected abstract String updateCommandName();
    protected abstract String createCommandName();
    protected abstract String deleteCommandName();
}
