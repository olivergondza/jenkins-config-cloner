package org.jenkinsci.tools.remotecloner.handler;

import java.util.List;

import org.jenkinsci.tools.remotecloner.CommandResponse;
import org.jenkinsci.tools.remotecloner.ConfigTransfer;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "Clone node configuration from <SRC> to <DST>")
public class CloneNode extends Handler {
    
    @Parameter(description = "[<SRC>] [<DST>...]")
    private List<String> nodes;

    public CloneNode(final ConfigTransfer config) {
        
        super(config);
    }

    @Override
    public CommandResponse run(final CommandResponse response) {
        
        return null;
    }
}
