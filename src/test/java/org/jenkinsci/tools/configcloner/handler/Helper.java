package org.jenkinsci.tools.configcloner.handler;

import org.jenkinsci.tools.configcloner.ConfigDestination;

public class Helper {

    public static ConfigDestination dest(final String jenkins, final String path) {

        return new ConfigDestination(jenkins, path);
    }

    public static Mapping map(final String... args) {

        return new Mapping(args);
    }

    public static class Mapping {

        private final String[] from;

        public Mapping(final String... args) {

            from = args;
        }

        public Object[][] to(final ConfigDestination... to) {

            assert from.length == to.length;

            return new Object[][] {
                    from, to
            };
        }
    }
}