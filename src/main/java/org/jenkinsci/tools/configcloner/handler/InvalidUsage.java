package org.jenkinsci.tools.configcloner.handler;

import org.jenkinsci.tools.configcloner.CommandResponse;

public class InvalidUsage extends Handler {
    
    private final Handler usage;
    private final String message;

    public InvalidUsage(final Handler usage, final String message) {
        
        super(null);
        
        assert (usage instanceof Usage);
        
        this.usage = usage;
        this.message = message;
    }

    @Override
    public CommandResponse run(final CommandResponse response) {
        
        response.err().println(message);
        response.returnCode(-1);
        return this.usage.run(response);
    }
}
