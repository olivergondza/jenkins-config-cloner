package org.jenkinsci.tools.configcloner;

import hudson.cli.CLI;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class ConfigTransfer {

    private final CommandResponse response;
    private final CLIPool cliPool;

    public ConfigTransfer(final CommandResponse response, final CLIPool cliPool) {

        this.response = response;
        this.cliPool = cliPool;
    }

    public CommandResponse.Accumulator execute(
            final ConfigDestination destination,
            final String stdin,
            final String... command
    ) {

        try {

            final CLI service = cliPool.connection(destination.jenkins());
            final CommandResponse.Accumulator response = this.response.accumulate();
            final ByteArrayInputStream in = new ByteArrayInputStream(
                    stdin.getBytes("UTF-8")
            );

            final int ret = service.execute(Arrays.asList(command), in, response.out(), response.err());
            return response.returnCode(ret);

        } catch (final UnsupportedEncodingException ex) {

            throw new AssertionError(ex);
        }
    }
}
