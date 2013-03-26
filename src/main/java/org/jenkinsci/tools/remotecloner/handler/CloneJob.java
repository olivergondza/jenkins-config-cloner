package org.jenkinsci.tools.remotecloner.handler;

import java.util.List;

import org.jenkinsci.tools.remotecloner.CommandResponse;
import org.jenkinsci.tools.remotecloner.ConfigDestination;
import org.jenkinsci.tools.remotecloner.ConfigTransfer;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "Clone job configuration from <SRC> to <DST>. Destination job will be either created or overwritten.")
public class CloneJob extends Handler {

    @Parameter(description = "[<SRC>] [<DST>...]")
    private List<String> jobs;
    private ConfigDestination source;
    private ConfigDestination destination;
    
    public CloneJob(final ConfigTransfer config) {
        
        super(config);
    }
    
    @Override
    public void validate() {
        
        if (jobs != null && jobs.size() != 2) throw new ParameterException(
                "Expecting 2 positional arguments"
        );
        
        source = ConfigDestination.parse(jobs.get(0));
        destination = source.pair(jobs.get(1));
    }

    @Override
    public CommandResponse run(final CommandResponse response) {
    
        final CommandResponse.Accumulator xml = config.fetch(source);
        
        if (!destination.exists()) {
            
            destination = destination.newPath("/createItem?name=");
        }        
        
        return config.send(destination, xml.stdout());
    }
}
