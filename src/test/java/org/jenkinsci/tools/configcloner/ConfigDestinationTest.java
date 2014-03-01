package org.jenkinsci.tools.configcloner;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ConfigDestinationTest {

    @Test
    public void newDestinationShouldShareTheHost() {

        final ConfigDestination old = new ConfigDestination(
                "http://localhost:8080", "my-slave"
        );

        final ConfigDestination derived = old.newEntity("master");

        assertEquals(new ConfigDestination("http://localhost:8080", "master"), derived);
    }
}
