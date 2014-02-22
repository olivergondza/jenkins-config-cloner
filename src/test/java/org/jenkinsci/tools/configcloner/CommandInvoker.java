package org.jenkinsci.tools.configcloner;

import hudson.model.User;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;

import org.jenkinsci.main.modules.cli.auth.ssh.UserPropertyImpl;

public class CommandInvoker {

    private final URL url;
    private final String kind;
    private String[] options = new String[] {};

    public CommandInvoker(URL url, String kind) {
        this.url = url;
        this.kind = kind;
    }

    public CommandInvoker opts(String... opts) {
        this.options = opts;
        return this;
    }

    public CommandResponse.Accumulator invoke(String src, String... dst) {
        RandKeyCLIFactory factory = new RandKeyCLIFactory();

        try {

            User.current().addProperty(new UserPropertyImpl(
                   factory.publicKeyString()
            ));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        return (CommandResponse.Accumulator) new Main(
                CommandResponse.accumulate(),
                new CLIPool(factory)).run(buildArgs(src, dst)
        );
    }

    private String[] buildArgs(String src, String... dst) {
        String[] mainArgs = new String[options.length + 1 + dst.length + 1];
        int i = 0;
        mainArgs[i++] = kind;

        System.arraycopy(options, 0, mainArgs, i, options.length);
        i += options.length;

        mainArgs[i++] = url + src;

        for(String arg: dst) {

            mainArgs[i++] = url + arg;
        }

        return mainArgs;
    }

    private static class RandKeyCLIFactory extends org.jenkinsci.tools.configcloner.CLIFactory {

        private final KeyPair pair;

        public RandKeyCLIFactory() {
            this.pair = generateKeyPair();
        }

        @Override
        protected List<KeyPair> userKeys() {

            return Arrays.asList(pair);
        }

        private KeyPair generateKeyPair() {
            try {
                KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
                keyGen.initialize(1024);
                return keyGen.generateKeyPair();
            } catch (NoSuchAlgorithmException ex) {
                throw new RuntimeException(ex);
            }
        }

        public String publicKeyString() {

            try {
                // TODO expose this
                Method m = UserPropertyImpl.class.getDeclaredMethod(
                        "getPublicKeySignature", PublicKey.class
                );
                m.setAccessible(true);

                return String.format(
                        "ssh-rsa %s tester@jenkins-ci.org",
                        (String) m.invoke(null, pair.getPublic())
                );
            } catch (NoSuchMethodException ex) {
                throw new RuntimeException(ex);
            } catch (SecurityException ex) {
                throw new RuntimeException(ex);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException(ex);
            } catch (InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}