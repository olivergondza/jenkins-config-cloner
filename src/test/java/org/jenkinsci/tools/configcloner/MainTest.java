package org.jenkinsci.tools.configcloner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.text.IsEmptyString.isEmptyString;
import static org.jenkinsci.tools.configcloner.handler.Helper.stdoutContains;
import static org.jenkinsci.tools.configcloner.handler.Helper.succeeded;

import org.jenkinsci.tools.configcloner.CommandResponse.Accumulator;
import org.jenkinsci.tools.configcloner.handler.Usage;
import org.junit.Test;

public class MainTest {

    final Accumulator rsp = CommandResponse.accumulate();

    @Test
    public void getUsageWhenNoArgsProvided() {

        assertThat(run().getHandler(), instanceOf(Usage.class));
    }

    @Test
    public void getUsageWhenInvalidArgsProvided() {

        assertThat(run("no-such-command").getHandler(), instanceOf(Usage.class));
    }

    @Test
    public void failedValidationShouldInvokeUsage() {

        run("job", "invalid-arg");

        assertThat(rsp, not(succeeded()));
        assertThat(rsp.stderr(), not(isEmptyString()));
        assertThat(rsp, stdoutContains("Usage:"));
    }

    private Main run(String... args) {

        final CLIPool cliPool = new CLIPool(CLIFactory.provided());
        final Main main = new Main(rsp, cliPool);
        main.run(args);
        cliPool.close();

        return main;
    }
}
