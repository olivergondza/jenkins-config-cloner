package org.jenkinsci.tools.configcloner;

import java.net.MalformedURLException;
import java.net.URL;

import org.jenkinsci.tools.configcloner.ConfigDestination;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

public class ConfigDestinationTest {

  @Test
  public void newDestinationShouldShareTheHost() throws MalformedURLException {

      final ConfigDestination old = new ConfigDestination(
              "http://localhost:8080", "my-slave"
      );

      final ConfigDestination derived = old.newEntity("master");

      assertEquals(new ConfigDestination("http://localhost:8080", "master"), derived);
  }
}
