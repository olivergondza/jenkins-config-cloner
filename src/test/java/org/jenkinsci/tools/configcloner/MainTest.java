package org.jenkinsci.tools.configcloner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;


import org.jenkinsci.tools.configcloner.Main;
import org.jenkinsci.tools.configcloner.handler.Handler;
import org.jenkinsci.tools.configcloner.handler.Usage;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.annotations.Test;

import com.beust.jcommander.ParameterException;

@PrepareForTest(Main.class)
public class MainTest extends Abstract {

    @Test
    public void getUsageWhenNoArgsProvided() {

        assertThat(dispatch(), instanceOf(Usage.class));
    }

    @Test
    public void getUsageWhenInvalidArgsProvided() {

        assertThat(dispatch("no-such-command"), instanceOf(Usage.class));
    }

    @Test
    public void failedValidationShouldInvokeUsage() {

        final Handler handler = PowerMockito.mock(Handler.class);
        Mockito.doThrow(new ParameterException("Fake Exception")).when(handler).validate();

        final Main main = PowerMockito.spy(this.main);
        PowerMockito.when(main.getHandler("global")).thenReturn(handler);

        final int code = main.run("global").returnCode();

        assertThat(code, not(equalTo(0)));

        Mockito.verify(handler).validate();
        Mockito.verify(handler, Mockito.never()).run(inResponse);

        assertThat(stderr(), containsString("Fake Exception"));
        assertThat(stdout(), containsString("Usage: remote-cloner [options] [command] [command options]"));
    }
}
