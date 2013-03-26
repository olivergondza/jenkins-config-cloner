package org.jenkinsci.tools.remotecloner;

import java.net.MalformedURLException;
import java.net.URL;

import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

import com.beust.jcommander.ParameterException;

public class ConfigDestinationTest {
    
  @Test
  public void parseFullUrl() throws MalformedURLException {
      
      final ConfigDestination dest = ConfigDestination.parse(
              "http://localhost:8080/job/my-job/config.xml"
      );
      
      assertEquals(new URL("http://localhost:8080"), dest.host());
      assertEquals("/job/my-job/config.xml", dest.path());
      assertEquals(new URL("http://localhost:8080/job/my-job/config.xml"), dest.url());
  }
  
  @Test
  public void completePathFromSourceWhenMissingInDestination() throws MalformedURLException {
      
      final ConfigDestination from = ConfigDestination.parse(
              "http://localhost:8080/job/my-job"
      );
      
      final ConfigDestination to = from.pair("http://localhost:4242");
      
      assertEquals(new URL("http://localhost:4242"), to.host());
      assertEquals("/job/my-job", to.path());
      assertEquals(new URL("http://localhost:4242/job/my-job"), to.url());
  }
  
  @Test
  public void newPathShouldShareTheHost() throws MalformedURLException {
      
      final ConfigDestination old = ConfigDestination.parse(
              "http://localhost:8080/job/my-job"
      );
      
      final ConfigDestination derived = old.newPath("/node/computer/master");
      
      assertEquals(new URL("http://localhost:8080/node/computer/master"), derived.url());
  }
  
  @Test
  public void exists() {
      
      assertTrue(ConfigDestination.parse("http://www.google.com").exists());
      assertFalse(ConfigDestination.parse("http://there.is.no.such.domain/and/url").exists());
  }
  
  @Test(expectedExceptions = ParameterException.class)
  public void sameDestinationsShouldBeDisallowed() throws MalformedURLException, ParameterException {
      
      final ConfigDestination from = ConfigDestination.parse(
              "http://localhost:8080/job/my-job/"
      );
      
      from.pair("http://localhost:8080");
  }
}
