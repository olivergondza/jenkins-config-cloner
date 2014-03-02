package org.jenkinsci.tools.configcloner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.junit.Test;

public class UrlParserTest {

    @Test(expected = IllegalArgumentException.class)
    public void sameInstanceShouldBeDisallowed() {

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

        assertThat(new URL("http://localhost:4242"), equalTo(to.jenkins()));
        assertThat("my-job", equalTo(to.entity()));
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
