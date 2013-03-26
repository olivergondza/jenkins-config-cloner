package org.jenkinsci.tools.remotecloner;

public class HandlerException extends RuntimeException {

    public HandlerException(final Exception cause) {
        super(cause);
    }
}
