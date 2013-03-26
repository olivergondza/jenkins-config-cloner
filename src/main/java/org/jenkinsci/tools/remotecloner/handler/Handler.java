package org.jenkinsci.tools.remotecloner.handler;

import org.jenkinsci.tools.remotecloner.CommandResponse;
import org.jenkinsci.tools.remotecloner.ConfigTransfer;

public abstract class Handler {
    
    protected final ConfigTransfer config;

    protected Handler(final ConfigTransfer config) {
        
        this.config = config;
    }
    
    public void validate() {}

    public abstract CommandResponse run(final CommandResponse response);
}
