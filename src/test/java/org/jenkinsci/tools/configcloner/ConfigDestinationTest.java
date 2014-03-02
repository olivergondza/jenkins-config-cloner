package org.jenkinsci.tools.configcloner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

public class ConfigDestinationTest {

    @Test
    public void newDestinationShouldShareTheHost() {

        final ConfigDestination old = new ConfigDestination(
                "http://localhost:8080", "my-slave"
        );

        final ConfigDestination derived = old.newEntity("master");

        assertThat(new ConfigDestination("http://localhost:8080", "master"), equalTo(derived));
    }
}
