package org.jenkinsci.tools.configcloner;

import java.util.HashMap;
import java.util.Map;

import org.jenkinsci.tools.configcloner.handler.CloneGlobal;
import org.jenkinsci.tools.configcloner.handler.CloneJob;
import org.jenkinsci.tools.configcloner.handler.CloneNode;
import org.jenkinsci.tools.configcloner.handler.Handler;
import org.jenkinsci.tools.configcloner.handler.InvalidUsage;
import org.jenkinsci.tools.configcloner.handler.Usage;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class Main {

    private final CommandResponse response;
    private final JCommander commander = new JCommander();
    private final Handler usage = new Usage(commander);
    private static final Map<String, Handler> commandMapping = new HashMap<String, Handler>();
    {
        final ConfigTransfer config = new ConfigTransfer(new CommandResponse(System.out, System.err));

        addCommand(  "help", usage);
        addCommand("global", new CloneGlobal(config));
        addCommand(   "job", new CloneJob(config));
        addCommand(  "node", new CloneNode(config));
    }

    private void addCommand(final String name, final Handler handler) {

        commandMapping.put(name, handler);
        commander.addCommand(name, handler);
    }

    public static void main(final String[] args) {

        final CommandResponse resp = new CommandResponse(System.out, System.err);
        final CommandResponse response = new Main(resp).run(args);

        System.exit(response.returnCode());
    }

    public Main(final CommandResponse response) {

        commander.setProgramName("remote-cloner");

        this.response = response;
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

    public Handler getHandler(final String... args) {

        final Handler handler = commandMapping.get(commander.getParsedCommand());

        return handler != null ? handler : usage;
    }
}
