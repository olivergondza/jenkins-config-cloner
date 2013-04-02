package org.jenkinsci.tools.configcloner;

import java.net.MalformedURLException;
import java.net.URL;

import org.jenkinsci.tools.configcloner.ConfigDestination;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

public class ConfigDestinationTest {

  @Test
  public void newPathShouldShareTheHost() throws MalformedURLException {

      final ConfigDestination old = new ConfigDestination(
              "http://localhost:8080", "/job/my-job"
      );

      final ConfigDestination derived = old.newPath("/node/computer/master");

      assertEquals(new URL("http://localhost:8080/node/computer/master"), derived.url());
  }

  @Test
  public void exists() {

      assertTrue(new ConfigDestination("http://www.google.com", "/").exists());
      assertFalse(new ConfigDestination("http://there.is.no.such.domain", "/and/url").exists());
  }
}
