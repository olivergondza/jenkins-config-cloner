package org.jenkinsci.tools.configcloner.handler;

import static org.jenkinsci.tools.configcloner.handler.Helper.dest;
import static org.jenkinsci.tools.configcloner.handler.Helper.map;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.jenkinsci.tools.configcloner.Abstract;
import org.jenkinsci.tools.configcloner.ConfigDestination;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.beust.jcommander.ParameterException;

public class CloneNodeTest extends Abstract {

    private final CloneNode handler = Mockito.mock(CloneNode.class, Mockito.CALLS_REAL_METHODS);

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

        initialize(args);

        assertEquals(dests[0], handler.source());
        assertEquals(
                Arrays.asList(dests).subList(1, dests.length),
                handler.destinations()
        );
    }

    @DataProvider
    public Object[][] validArgs() {

        return new Object[][][] {
                map("http://1.jnk.ns/computer/a", "http://2.jnk.ns/").to(
                        dest("http://1.jnk.ns/", "a"),
                        dest("http://2.jnk.ns/", "a")
                ),
                map("http://1.jnk.ns/computer/a/", "http://2.jnk.ns/computer/b").to(
                        dest("http://1.jnk.ns/", "a"),
                        dest("http://2.jnk.ns/", "b")
                ),

                map("http://1.jnk.ns/computer/src", "http://2.jnk.ns/computer/dst1", "http://3.jnk.ns/computer/dst2").to(
                        dest("http://1.jnk.ns/", "src"),
                        dest("http://2.jnk.ns/", "dst1"),
                        dest("http://3.jnk.ns/", "dst2")
                ),
        };
    }

    private void initialize(final String[] args) {

        Whitebox.setInternalState(handler, "nodes", args == null ? null : Arrays.asList(args));
        handler.validate();
    }
}
