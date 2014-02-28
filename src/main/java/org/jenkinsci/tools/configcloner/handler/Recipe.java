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

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.groovy.control.CompilationFailedException;
import org.jenkinsci.tools.configcloner.CLIPool;
import org.jenkinsci.tools.configcloner.CommandResponse;
import org.jenkinsci.tools.configcloner.ConfigTransfer;
import org.jenkinsci.tools.configcloner.Main;

import com.beust.jcommander.Parameter;

public class Recipe extends Handler {

    private final CLIPool cliPool;

    @Parameter(names = {"--dry-run", "-n"}, description = "Do not perform any modifications to any instance")
    private boolean dryRun = false;

    @Parameter(description="Recipe file to be executed", required=true, arity=1)
    private List<String> recipeFile;

    private int recipeResult = 0;

    public Recipe(ConfigTransfer config, CLIPool cliPool) {
        super(config);
        this.cliPool = cliPool;
    }

    @Override
    public String name() {
        return "recipe";
    }

    @Override
    public CommandResponse run(CommandResponse response) {

        final GroovyShell shell = new GroovyShell(initBinding(
                cliPool, response
        ));

        try {

            response.out().println("Evaluating recepie " + recipeFile.get(0));
            shell.evaluate(new File(recipeFile.get(0)));
        } catch (CompilationFailedException ex) {

            response.err().println(ex.toString());
            return response.returnCode(-1);
        } catch (IOException ex) {

            response.err().println(ex.toString());
            return response.returnCode(-1);
        }

        return response.returnCode(recipeResult);
    }

    private Binding initBinding(final CLIPool cliPool, final CommandResponse response) {

        final Binding binding = new Binding();
        binding.setProperty("clone", new Dsl(cliPool, response));
        binding.setProperty("out", response.out());
        binding.setProperty("err", response.err());

        return binding;
    }

    // TODO: Generate this from existing TransferHandlers
    private class Dsl {

        private final CLIPool cliPool;
        private final CommandResponse response;

        public Dsl(CLIPool cliPool, CommandResponse response) {
            this.cliPool = cliPool;
            this.response = response;
        }

        @SuppressWarnings("unused")
        public void job(String... args) {
            run("job", args);
        }

        @SuppressWarnings("unused")
        public void view(String... args) {
            run("view", args);
        }

        @SuppressWarnings("unused")
        public void node(String... args) {
            run("node", args);
        }

        private void run(String name, String... args) {

            final ArrayList<String> effectiveArgs = new ArrayList<String>(args.length + 2);
            effectiveArgs.add("");
            effectiveArgs.add(name);

            final List<String> commandArgs = Arrays.asList(args);
            if (Recipe.this.dryRun && !commandArgs.contains("-n") && !commandArgs.contains("--dry-run")) {

                effectiveArgs.add("--dry-run");
            }

            effectiveArgs.addAll(commandArgs);

            final String[] arrayArgs = effectiveArgs.toArray(new String[effectiveArgs.size()]);
            CommandResponse result = new Main(response, cliPool).run(arrayArgs);

            if (recipeResult == 0 && result.returnCode() != 0) {
System.out.println("Setting return code: " + result.returnCode());
                recipeResult = result.returnCode();
            }
        }
    }
}
