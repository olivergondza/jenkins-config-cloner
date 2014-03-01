package org.jenkinsci.tools.configcloner.handler;

import static org.jenkinsci.tools.configcloner.handler.Helper.dest;
import static org.jenkinsci.tools.configcloner.handler.Helper.map;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.jenkinsci.tools.configcloner.CommandInvoker;
import org.jenkinsci.tools.configcloner.ConfigDestination;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.beust.jcommander.ParameterException;

@RunWith(JUnitParamsRunner.class)
public class CloneNodeTest {

    @Test(expected = ParameterException.class) @Parameters(method = "invalidArgs")
    public void failWithIncorectArguments(final String[] args) {

        final TransferHandler handler = handler(args);
        handler.source();
        handler.destinations();
    }

    public Object[][] invalidArgs() {

        return new String[][][] {
                // invalid count
                {{}},
                {{"http://jenki.ns"}},

                // same url
                {{"http://jenki.ns", "http://jenki.ns"}},

                // not an url
                {{"not-an-url", "http://jenki.ns"}},
                {{"http://jenki.ns", "not-an-url"}},
                {{"not-an-url", "not-an-url"}},
        };
    }

    @Test @Parameters(method = "validArgs")
    public void parseValidDestinations(final String[] args, final ConfigDestination[] dests) {

        final TransferHandler handler = handler(args);

        assertEquals(dests[0], handler.source());
        assertEquals(
                Arrays.asList(dests).subList(1, dests.length),
                handler.destinations()
        );
    }

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

    private TransferHandler handler(final String[] args) {

        final CommandInvoker invoker = new CommandInvoker("node").args(args);
        return (TransferHandler) invoker.main().getHandler(invoker.commandArgs());
    }
}
