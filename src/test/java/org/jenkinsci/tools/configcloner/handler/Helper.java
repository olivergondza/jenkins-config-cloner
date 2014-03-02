package org.jenkinsci.tools.configcloner.handler;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.jenkinsci.tools.configcloner.CommandResponse;
import org.jenkinsci.tools.configcloner.CommandResponse.Accumulator;
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

    public static abstract class CommandResponseMatcher extends TypeSafeDiagnosingMatcher<CommandResponse.Accumulator> {
        private final String description;

        public CommandResponseMatcher(String description) {
            this.description = description;
        }

        public void describeTo(Description description) {
            description.appendText(this.description);
        }

        @Override protected boolean matchesSafely(Accumulator item, Description desc) {
            if (match(item, desc)) {
                dump(item, desc);
                return true;
            }

            return false;
        }

        abstract protected boolean match(Accumulator item, Description desc);
    }

    public static CommandResponseMatcher succeeded() {
        return new CommandResponseMatcher("Command suceeded") {
            @Override protected boolean match(Accumulator item, Description desc) {
                return item.succeeded();
            }
        };
    }

    public static CommandResponseMatcher stdoutContains(final String expected) {
        return new CommandResponseMatcher("Standard output should contain string " + expected) {
            @Override protected boolean match(Accumulator item, Description desc) {
                return item.stdout().contains(expected);
            }
        };
    }

    public static CommandResponseMatcher stderrContains(final String expected) {
        return new CommandResponseMatcher("Error output should contain string " + expected) {
            @Override protected boolean match(Accumulator item, Description desc) {
                return item.stderr().contains(expected);
            }
        };
    }

    private static void dump(Accumulator item, Description mismatchDescription) {

        mismatchDescription.appendText("return code: " + item.returnCode());
        mismatchDescription.appendText(item.stdout("out > %s"));
        mismatchDescription.appendText(item.stderr("err > %s"));
    }
}