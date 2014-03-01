package org.jenkinsci.tools.configcloner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.text.IsEmptyString.isEmptyString;

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

        assertThat(rsp.returnCode(), not(equalTo(0)));
        assertThat(rsp.stderr(), not(isEmptyString()));
        assertThat(rsp.stdout(), containsString("Usage: "));
    }

    private Main run(String... args) {

        final CLIPool cliPool = new CLIPool();
        final Main main = new Main(rsp, cliPool);
        main.run(args);
        cliPool.close();

        return main;
    }
}
