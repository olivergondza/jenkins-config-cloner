package org.jenkinsci.tools.configcloner.handler;

import java.util.List;

import org.jenkinsci.tools.configcloner.CommandResponse;
import org.jenkinsci.tools.configcloner.ConfigDestination;
import org.jenkinsci.tools.configcloner.ConfigTransfer;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "Clone job configuration from <SRC> to <DST>. Destination job will be either created or overwritten.")
public class CloneJob extends Handler {

    @Parameter(description = "[<SRC>] [<DST>...]")
    private List<String> jobs;

    public CloneJob(final ConfigTransfer config) {

        super(config);
    }

    @Override
    public void validate() {

        if (jobs == null || jobs.size() != 2) throw new ParameterException(
                "Expecting 2 positional arguments"
        );

        // Instantiates both destinations
        destination();
    }

    @Override
    public CommandResponse run(final CommandResponse response) {

//        if (!source().exists()) throw new ParameterException(
//                "Source url does not exists"
//        );

        final CommandResponse.Accumulator xml = config.fetch(source());

        if (!destination().exists()) {

            jobs.set(1, destination().host() + "/createItem?name=");
        }

        return config.send(destination(), xml.stdout());
    }

    private ConfigDestination source() {

        return ConfigDestination.parse(jobs.get(0));
    }

    private ConfigDestination destination() {

        return source().pair(jobs.get(1));
    }
}
