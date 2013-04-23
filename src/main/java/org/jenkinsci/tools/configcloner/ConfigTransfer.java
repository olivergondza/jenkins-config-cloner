package org.jenkinsci.tools.configcloner;

import hudson.cli.CLI;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigTransfer {

    private static final String[] KEY_FILES = {".ssh/identity", ".ssh/id_rsa", ".ssh/id_dsa"};

    private final CommandResponse response;

    public ConfigTransfer(final CommandResponse response) {

        this.response = response;
    }

    public CommandResponse.Accumulator execute(
            final ConfigDestination destination,
            final String stdin,
            final String... command
    ) {

        try {

            CLI service = null;
            try {

                final ByteArrayInputStream in = new ByteArrayInputStream(
                        stdin.getBytes("UTF-8")
                );
                final CommandResponse.Accumulator response = this.response.accumulate();

                service = instantiateCli(destination);
                final int ret = service.execute(Arrays.asList(command), in, response.out(), response.err());
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

        if (userKeys.isEmpty()) {

            System.out.println("Anonimous access");
        }

        service.authenticate(userKeys);

        return service;
    }

    private List<KeyPair> userKeys() {

        final List<KeyPair> pairs = new ArrayList<KeyPair>(3);

        final File home = new File(System.getProperty("user.home"));
        for (final String path: KEY_FILES) {


            final File key = new File(home, path);
            if (!key.exists()) continue;
            
            try {
                
                pairs.add(CLI.loadKey(key));
            } catch (IOException ex) {
            
                //if the PEM file is encrypted, IOException is thrown
                pairs.add(tryEncryptedFile(key));
            } catch (GeneralSecurityException ex) {
                
                System.err.println("Failed to load " + key);
                System.err.println(ex.getMessage());
                ex.printStackTrace();
            }
        }

        return pairs;
    }

    /**
     * Call private CLI.tryEncryptedFile
     */
    private KeyPair tryEncryptedFile(final File key) {
        
        try {

            Method m = CLI.class.getDeclaredMethod("tryEncryptedFile", File.class);
            m.setAccessible(true);
            return (KeyPair) m.invoke(key, key);
        } catch (IllegalArgumentException ex) {
            
            throw new AssertionError(ex);
        } catch (IllegalAccessException ex) {

            throw new AssertionError(ex);
        } catch (InvocationTargetException ex) {

            throw new AssertionError(ex);
        } catch (SecurityException ex) {

            throw new AssertionError(ex);
        } catch (NoSuchMethodException ex) {

            throw new AssertionError(ex);
        }
    }
}
