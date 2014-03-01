package org.jenkinsci.tools.configcloner;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.junit.Test;

import com.beust.jcommander.ParameterException;

public class UrlParserTest {

    @Test(expected = ParameterException.class)
    public void sameInstanceShouldBeDisallowed() throws ParameterException {

        final ConfigDestination from = new ConfigDestination(
                "http://localhost:8080", "my-job"
        );

        constParser(from).pair(from, Arrays.asList("http://localhost:8080"));
    }

    @Test
    public void completePathFromSourceWhenMissingInDestination() throws MalformedURLException {

        final ConfigDestination from = new ConfigDestination(
                "http://localhost:8080", "my-job"
        );

        final ConfigDestination to = constParser("http://localhost:4242", "")
                .pair(from, Arrays.asList("http://ignored:4242"))
                .get(0)
        ;

        assertEquals(new URL("http://localhost:4242"), to.jenkins());
        assertEquals("my-job", to.entity());
    }

    private UrlParser constParser(final String jenkins, final String path) {

        return constParser(new ConfigDestination(jenkins, path));
    }

    private UrlParser constParser(final ConfigDestination dest) {

        return new UrlParser() {

            @Override
            protected ConfigDestination parseDestination(final URL url) {

                return dest;
            }
        };
    }
}
