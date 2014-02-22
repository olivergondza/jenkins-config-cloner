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

import hudson.Util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jenkinsci.tools.configcloner.CommandResponse;
import org.jenkinsci.tools.configcloner.ConfigDestination;
import org.jenkinsci.tools.configcloner.ConfigTransfer;
import org.unix4j.Unix4j;
import org.unix4j.builder.Unix4jCommandBuilder;

import com.beust.jcommander.Parameter;

import difflib.DiffUtils;
import difflib.Patch;

public abstract class TransferHandler extends Handler {

    @Parameter(names = {"--force", "-f"}, description = "Overwrite target configuration if exists")
    protected boolean force = false;

    @Parameter(names = {"--expression", "-e"}, description = "Transform configuration")
    protected List<String> expressions = Collections.emptyList();

    @Parameter(names = {"--dry-run", "-n"}, description = "Do not perform any modifications to any instance")
    protected boolean dryRun = false;

    protected TransferHandler(ConfigTransfer config) {
        super(config);
    }

    /**
     * Template method to fetch and sent to many.
     *
     * Expects superclass will override *CommandName methods. This can be overriden in case the template is not suitable.
     */
    @Override
    public CommandResponse run(final CommandResponse response) {

        response.out().println("Fetching " + this.source());
        final CommandResponse.Accumulator xml = config.execute(
                this.source(), "", this.getCommandName(), this.source().entity()
        );

        if (!xml.succeeded()) return response.merge(xml);

        for (final ConfigDestination dest: this.destinations()) {

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

    protected abstract ConfigDestination source();
    protected abstract List<ConfigDestination> destinations();
    protected abstract String getCommandName();
    protected abstract String updateCommandName();
    protected abstract String createCommandName();
    protected abstract String deleteCommandName();
}
