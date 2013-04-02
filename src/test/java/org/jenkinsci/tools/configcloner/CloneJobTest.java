package org.jenkinsci.tools.configcloner;

import java.util.Arrays;

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

        runWith(args);
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

    private void runWith(final String[] args) {

        Whitebox.setInternalState(handler, "jobs", args == null ? null : Arrays.asList(args));
        handler.validate();
    }
}
