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

            jobs.set(1, destination().jenkins() + "/createItem?name=");
        }

        return config.send(destination(), xml.stdout());
    }

    /*package*/ ConfigDestination source() {

        return PARSER.destination(jobs.get(0));
    }

    /*package*/ ConfigDestination destination() {

        return PARSER.pair(source(), jobs.get(1));
    }

    private static final UrlParser PARSER = new UrlParser() {

        @Override
        protected ConfigDestination parseDestination(final URL url) {

            final Matcher urlMatcher = Pattern
                    .compile("^(.*/?)((?:\\bview/.*?)?job/.*)$")
                    .matcher(url.toString())
            ;

            if (!urlMatcher.matches()) return new ConfigDestination(url, "");

            final String jenkins = urlMatcher.group(1);
            String path = urlMatcher.group(2);
            if (!path.endsWith("/config.xml")) {

                path += "/config.xml";
            }

            return new ConfigDestination(jenkins, path);
        }
    };
}
