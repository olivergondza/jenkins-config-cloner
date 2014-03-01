package org.jenkinsci.tools.configcloner.handler;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.jenkinsci.tools.configcloner.CommandResponse;
import org.jenkinsci.tools.configcloner.CommandResponse.Accumulator;
import org.jenkinsci.tools.configcloner.ConfigDestination;
import org.jenkinsci.tools.configcloner.ConfigTransfer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;

public class CloneJobRunTest {

    private final ConfigDestination source = new ConfigDestination("http://src.com", "src");
    private final ConfigDestination destination = new ConfigDestination("http://dst.com", "dst");

    private Accumulator responseFetch, responseCreate, responseUpdate;
    private ConfigTransfer config;
    private CloneJob handler;

    @Before
    public void setUp() {

        config = Mockito.mock(ConfigTransfer.class);
        handler = Mockito.spy(new CloneJob(config));

        responseCreate = CommandResponse.accumulate().returnCode(0);
        responseUpdate = CommandResponse.accumulate().returnCode(0);
        responseFetch = CommandResponse.accumulate().returnCode(0);
        responseFetch.out().append("job-configuration");

        Mockito.doReturn(source).when(handler).source();
        Mockito.doReturn(Arrays.asList(destination)).when(handler).destinations();

        Mockito.doReturn(responseFetch).when(config).execute(source, "", "get-job", source.entity());
        Mockito.doReturn(responseCreate).when(config).execute(destination, "job-configuration", "create-job", destination.entity());
        Mockito.doReturn(responseUpdate).when(config).execute(destination, "job-configuration", "update-job", destination.entity());
    }

    @Test
    public void cloneShouldOnlyCreate() {

        verifyExecuted(response(), "create-job");
    }

    @Test
    public void forcedCloneShouldUpdate() {

        force();

        verifyExecuted(response(), "update-job");
    }

    @Test
    public void forcedCloneShouldCreateWhenUpdateFails() {

        force();

        jobDoesNotExist();

        verifyExecuted(response(), "update-job", "create-job");
    }

    private void jobDoesNotExist() {

        responseUpdate.returnCode(-1);
    }

    private CommandResponse response() {

        return handler.run(CommandResponse.accumulate());
    }

    public void verifyExecuted(final CommandResponse response, final String... commands) {

        Mockito.verify(config).execute(source, "", "get-job", source.entity());

        for(final String cmd: commands) {

            Mockito.verify(config).execute(destination, "job-configuration", cmd, destination.entity());
        }

        Mockito.verifyNoMoreInteractions(config);

        assertTrue(response.succeeded());
    }

    private void force() {

        Whitebox.setInternalState(handler, "force", true);
    }
}
