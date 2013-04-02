package org.jenkinsci.tools.configcloner;

public class HandlerException extends RuntimeException {

    public HandlerException(final Exception cause) {
        super(cause);
    }
}
