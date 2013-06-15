package org.jenkinsci.tools.configcloner;

import hudson.cli.CLI;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CLIPool {

    private static final String[] KEY_FILES = {".ssh/identity", ".ssh/id_rsa", ".ssh/id_dsa"};

    private final Map<URL, CLI> pool = new HashMap<URL, CLI>(2);

    public CLIPool add(final URL instance, final CLI connection) {

        pool.put(instance, connection);

        return this;
    }

    public void close() {

        for(final CLI connection: pool.values()) {

            try {

                connection.close();
            } catch (Exception ex) {

                ex.printStackTrace();
            }
        }
    }

    public CLI connection(final URL instance) {

        try {

            final CLI connection = pool.containsKey(instance)
                    ? pool.get(instance)
                    : instantiateCli(instance)
            ;

            pool.put(instance, connection);

            return connection;
        } catch (IOException ex) {

            throw new HandlerException(ex);
        } catch (GeneralSecurityException ex) {

            throw new HandlerException(ex);
        } catch (InterruptedException ex) {

            throw new HandlerException(ex);
        }
    }

    private CLI instantiateCli(final URL destination) throws
            IOException, GeneralSecurityException, InterruptedException
    {

        final CLI service = new CLI(destination);

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
