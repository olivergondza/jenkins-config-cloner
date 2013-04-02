package org.jenkinsci.tools.configcloner.handler;

import org.jenkinsci.tools.configcloner.CommandResponse;
import org.jenkinsci.tools.configcloner.ConfigTransfer;

public abstract class Handler {
    
    protected final ConfigTransfer config;

    protected Handler(final ConfigTransfer config) {
        
        this.config = config;
    }
    
    public void validate() {}

    public abstract CommandResponse run(final CommandResponse response);
}
