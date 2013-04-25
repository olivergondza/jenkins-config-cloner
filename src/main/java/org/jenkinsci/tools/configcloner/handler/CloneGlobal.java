package org.jenkinsci.tools.configcloner.handler;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.jenkinsci.tools.configcloner.CommandResponse;
import org.jenkinsci.tools.configcloner.ConfigDestination;
import org.jenkinsci.tools.configcloner.ConfigTransfer;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "Clone global configuration. NOT IMPLEMENTED.")
public class CloneGlobal extends Handler {

    @Parameter(description = "[<SRC>] [<DST>...]")
    private List<String> jobs;
    private ConfigDestination source;
    private ConfigDestination destination;

    public CloneGlobal(final ConfigTransfer config) {

        super(config);
    }

    @Override
    public CommandResponse run(final CommandResponse response) {

        throw new NotImplementedException();
    }
}
