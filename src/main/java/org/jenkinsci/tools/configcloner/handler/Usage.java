package org.jenkinsci.tools.configcloner.handler;

import org.jenkinsci.tools.configcloner.CommandResponse;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "Display usage")
public class Usage extends Handler {

    private final JCommander commander;

    public Usage(final JCommander commander) {
        
        super(null);
        this.commander = commander;
    }

    @Override
    public CommandResponse run(final CommandResponse response) {
        
        final StringBuilder helpMessage = new StringBuilder();
        commander.usage(helpMessage);
        
        response.out().print(helpMessage);
        
        return response;
    }
}
