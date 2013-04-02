package org.jenkinsci.tools.configcloner;

import hudson.cli.CLI;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigTransfer {

    private static final String[] KEY_FILES = {".ssh/id_rsa", ".ssh/id_dsa", ".ssh/identity"};
    private static final List<String> COMMAND = Arrays.asList("groovy", "=");

    private final CommandResponse response;

    public ConfigTransfer(final CommandResponse response) {

        this.response = response;
    }

    public CommandResponse.Accumulator fetch(final ConfigDestination source) {

        return execute(source, "println new URL(\"%s\").getContent();");
    }

    public CommandResponse.Accumulator send(final ConfigDestination destination, final String content) {

        return execute(destination, "println new URL(\"%s\").getContent();");
    }

    private CommandResponse.Accumulator execute(final ConfigDestination destination, final String stdin) {

        try {

            CLI service = null;
            try {

                final ByteArrayInputStream in = new ByteArrayInputStream(
                        String.format(stdin, destination.url()).getBytes("UTF-8")
                );
                final CommandResponse.Accumulator response = this.response.accumulate();

                service = instantiateCli(destination);
                final int ret = service.execute(COMMAND, in, response.out(), response.err());

                return response.returnCode(ret);
            } finally {

                if (service != null) {

                    service.close();
                }
            }
        } catch (final UnsupportedEncodingException ex) {

            throw new AssertionError(ex);
        } catch (final IOException ex) {

            throw new HandlerException(ex);
        } catch (final GeneralSecurityException ex) {

            throw new HandlerException(ex);
        } catch (final InterruptedException ex) {

            throw new HandlerException(ex);
        }
    }

    private CLI instantiateCli(
            final ConfigDestination destination
    ) throws IOException, GeneralSecurityException, InterruptedException {

        final CLI service = new CLI(destination.jenkins());

        final List<KeyPair> userKeys = userKeys();

        if (!userKeys.isEmpty()) {

            response.out().println("Authenticated access to " + destination.url());
        } else {

            response.out().println("Anonimous access to" + destination.url());
        }

        service.authenticate(userKeys);

        return service;
    }

    private List<KeyPair> userKeys() {

        final List<KeyPair> pairs = new ArrayList<KeyPair>(3);

        final File home = new File(System.getProperty("user.home"));
        for (final String path : KEY_FILES) {

            final File key = new File(home, path);
            if (!key.exists()) continue;

            try {

                pairs.add(CLI.loadKey(key));
            } catch (final IOException ex) {

                System.err.println("Failed to load " + key);
            } catch (final GeneralSecurityException ex) {

                System.err.println("Failed to load " + key);
            }
        }

        return pairs;
    }
}
