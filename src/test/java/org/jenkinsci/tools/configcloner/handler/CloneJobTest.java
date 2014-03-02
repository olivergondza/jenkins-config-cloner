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

@RunWith(JUnitParamsRunner.class)
public class CloneJobTest {

    private final CommandInvoker invoker = new CommandInvoker("job");

    @Test(expected = IllegalArgumentException.class) @Parameters(method = "invalidArgs")
    public void failWithIncorectArguments(final String[] args) {

        invoker.args(args);
        invoker.main().getHandler(invoker.commandArgs()).run(null);
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

        invoker.args(args);
        final TransferHandler handler = (TransferHandler) invoker.main().getHandler(invoker.commandArgs());

        assertEquals(dests[0], handler.source());
        assertEquals(
                Arrays.asList(dests).subList(1, dests.length),
                handler.destinations()
        );
    }

    public Object[][] validArgs() {

        return new Object[][][] {
                map("http://1.jnk.ns/job/a", "http://2.jnk.ns/").to(
                        dest("http://1.jnk.ns/", "a"),
                        dest("http://2.jnk.ns/", "a")
                ),
                map("http://1.jnk.ns/job/a/", "http://2.jnk.ns/job/b").to(
                        dest("http://1.jnk.ns/", "a"),
                        dest("http://2.jnk.ns/", "b")
                ),
                map("http://1.jnk.ns/job/a/config.xml", "http://2.jnk.ns/").to(
                        dest("http://1.jnk.ns/", "a"),
                        dest("http://2.jnk.ns/", "a")
                ),
                map("http://1.jnk.ns/view/someview/job/a/config.xml", "http://2.jnk.ns/").to(
                        dest("http://1.jnk.ns/", "a"),
                        dest("http://2.jnk.ns/", "a")
                ),
                map("http://1.jnk.ns/view/someview/job/a/config.xml", "http://2.jnk.ns/view/v/job/c/").to(
                        dest("http://1.jnk.ns/", "a"),
                        dest("http://2.jnk.ns/", "c")
                ),

                map("http://1.jnk.ns/jenkins/job/jobname", "http://2.jnk.ns/infra/hudson/view/v/job/name/config.xml").to(
                        dest("http://1.jnk.ns/jenkins/", "jobname"),
                        dest("http://2.jnk.ns/infra/hudson/", "name")
                ),

                map("http://1.jnk.ns/job/src", "http://2.jnk.ns/job/dst1", "http://3.jnk.ns/job/dst2").to(
                        dest("http://1.jnk.ns/", "src"),
                        dest("http://2.jnk.ns/", "dst1"),
                        dest("http://3.jnk.ns/", "dst2")
                ),
        };
    }
}
