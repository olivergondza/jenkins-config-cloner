package org.jenkinsci.tools.configcloner;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class CommandResponse {

    private int returnCode;
    private final PrintStream outputStream;
    private final PrintStream errorStream;

    public CommandResponse(final PrintStream out, final PrintStream err) {

        if (out == null) throw new IllegalArgumentException("out is null");
        if (err == null) throw new IllegalArgumentException("err is null");

        this.returnCode = 0;
        this.outputStream = out;
        this.errorStream = err;
    }

    private static PrintStream emptyStream() {

        return new PrintStream(new ByteArrayOutputStream());
    }

    public CommandResponse returnCode(final int ret) {

        returnCode = ret;
        return this;
    }

    public int returnCode() {

        return returnCode;
    }

    public PrintStream out() {

        return outputStream;
    }

    public PrintStream err() {

        return errorStream;
    }

    public Accumulator accumulate() {

        return new Accumulator(errorStream);
    }

    public static class Accumulator extends CommandResponse{

        public Accumulator(final PrintStream err) {

            super(emptyStream(), err);
        }

        public String stdout() {

            out().close();
            return out().toString();
        }

        @Override
        public Accumulator returnCode(final int ret) {

            return (Accumulator) super.returnCode(ret);
        }
    }
}
