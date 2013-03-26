package org.jenkinsci.tools.remotecloner.handler;

import java.util.List;

import org.jenkinsci.tools.remotecloner.CommandResponse;
import org.jenkinsci.tools.remotecloner.ConfigDestination;
import org.jenkinsci.tools.remotecloner.ConfigTransfer;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "Clone global configuration")
public class CloneGlobal extends Handler {

    @Parameter(description = "[<SRC>] [<DST>...]")
    private List<String> jobs;
    private ConfigDestination source;
    private ConfigDestination destination;
    
    public CloneGlobal(final ConfigTransfer config) {
        
        super(config);
    }
    
    @Override
    public void validate() {
        
        if (jobs == null || jobs.size() != 2) throw new ParameterException(
                "Expecting 2 positional arguments"
        );
        
        source = ConfigDestination.parse(jobs.get(0));
        destination = source.pair(jobs.get(1));        
    }

    @Override
    public CommandResponse run(final CommandResponse response) {
     
        return config.fetch(source);
    }
}
