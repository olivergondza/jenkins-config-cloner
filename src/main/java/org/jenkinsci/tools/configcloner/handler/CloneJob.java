package org.jenkinsci.tools.configcloner.handler;

import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jenkinsci.tools.configcloner.CommandResponse;
import org.jenkinsci.tools.configcloner.ConfigDestination;
import org.jenkinsci.tools.configcloner.ConfigTransfer;
import org.jenkinsci.tools.configcloner.UrlParser;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "Clone job configuration from <SRC> to <DST>")
public class CloneJob extends Handler {

    @Parameter(description = "[<SRC>] [<DST>...]")
    private List<String> jobs;

    @Parameter(names = {"--force", "-f"}, description = "Overwrite target job if exists")
    private boolean force = false;

    public CloneJob(final ConfigTransfer config) {

        super(config);
    }

    @Override
    public void validate() {

        if (jobs == null || jobs.size() < 2) throw new ParameterException(
                "Expecting 2 or more positional arguments"
        );

        // Instantiates both destinations
        destinations();
    }

    /*package*/ ConfigDestination source() {

        return PARSER.destination(jobs.get(0));
    }

    /*package*/ List<ConfigDestination> destinations() {

        return PARSER.pair(source(), jobs.subList(1, jobs.size()));
    }

    @Override
    public CommandResponse run(final CommandResponse response) {

        response.out().println("Fetching " + source());
        final CommandResponse.Accumulator xml = config.execute(
                source(), "", "get-job", source().entity()
        );

        if (!xml.succeeded()) return response.merge(xml);

        for (final ConfigDestination dest: destinations()) {

            response.out().println("Sending " + dest);
            send(dest, response, xml);
        }

        return response;
    }

    private CommandResponse send(
            final ConfigDestination destination,
            final CommandResponse response,
            final CommandResponse.Accumulator xml
    ) {

        final String destJob = destination.entity();

        if (force) {

            final CommandResponse.Accumulator rsp = config.execute(
                    destination, xml.stdout(), "update-job", destJob
            );

            if (rsp.succeeded()) return response.returnCode(0);
        }

        return response.merge(
                config.execute(destination, xml.stdout(), "create-job", destJob)
        );
    }

    private static final UrlParser PARSER = new UrlParser() {

        @Override
        protected ConfigDestination parseDestination(final URL url) {

            final Matcher urlMatcher = Pattern
                    .compile("^(.*?/)(?:view/[^/]+/)*job/([^/]+).*")
                    .matcher(url.toString())
            ;

            if (!urlMatcher.matches()) return new ConfigDestination(url, "");

            final String jenkins = urlMatcher.group(1);
            String entity = urlMatcher.group(2);

            return new ConfigDestination(jenkins, entity);
        }
    };
}
