package org.jenkinsci.tools.configcloner.handler;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.jenkinsci.tools.configcloner.Abstract;
import org.jenkinsci.tools.configcloner.ConfigDestination;
import org.jenkinsci.tools.configcloner.handler.CloneJob;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.beust.jcommander.ParameterException;

public class CloneJobTest extends Abstract {

    private final CloneJob handler = Mockito.mock(CloneJob.class, Mockito.CALLS_REAL_METHODS);

    @Test(dataProvider = "invalidArgs", expectedExceptions = ParameterException.class)
    public void failWithIncorectArguments(final String[] args) {

        initialize(args);
    }

    @DataProvider
    public Object[][] invalidArgs() {

        return new String[][][] {
                // invalid count
                {null},
                {{}},
                {{"http://jenki.ns"}},
                {{"http://jenki.ns", "http://jenki.ns", "http://jenki.ns"}},

                // same url
                {{"http://jenki.ns", "http://jenki.ns"}},

                // not an url
                {{"not-an-url", "http://jenki.ns"}},
                {{"http://jenki.ns", "not-an-url"}},
                {{"not-an-url", "not-an-url"}},
        };
    }

    @Test(dataProvider = "validArgs")
    public void parseValidDestinations(final String[] args, final ConfigDestination[] dests) {

        assert args.length == 2 && dests.length == 2;

        initialize(args);

        assertEquals(dests[0], handler.source());
        assertEquals(dests[1], handler.destination());
    }

    @DataProvider
    public Object[][] validArgs() {

        return new Object[][][] {
                map("http://1.jnk.ns/job/a", "http://2.jnk.ns/")
                .to(dest("http://1.jnk.ns/", "job/a/config.xml"), dest("http://2.jnk.ns/", "job/a/config.xml")),
        };
    }

    private void initialize(final String[] args) {

        Whitebox.setInternalState(handler, "jobs", args == null ? null : Arrays.asList(args));
        handler.validate();
    }

    private ConfigDestination dest(final String jenkins, final String path) {

        return new ConfigDestination(jenkins, path);
    }

    private Mapping map(final String... args) {

        return new Mapping(args);
    }

    private static class Mapping {

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
