package org.jenkinsci.tools.configcloner;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

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

    public CommandResponse returnCode(final int ret) {

        returnCode = ret;
        return this;
    }

    public int returnCode() {

        return returnCode;
    }
    
    public boolean succeeded() {
        
        return returnCode() == 0;
    }

    public PrintStream out() {

        return outputStream;
    }

    public PrintStream err() {

        return errorStream;
    }

    public static Accumulator accumulate() {

        return new Accumulator(new ByteArrayOutputStream(), new ByteArrayOutputStream());
    }
    
    public CommandResponse merge(final Accumulator response) {
        
        returnCode = response.returnCode();
        outputStream.append(response.stdout());
        errorStream.append(response.stderr());
        
        return this;
    }

    public static class Accumulator extends CommandResponse{

        final ByteArrayOutputStream out;
        final ByteArrayOutputStream err;

        public Accumulator(final ByteArrayOutputStream out, final ByteArrayOutputStream err) {

            super(new PrintStream(out), new PrintStream(err));

            this.out = out;
            this.err = err;
        }

        public String stdout() {

            return asString(out);
        }

        public String stdout(final String pattern) {

            return decorate(out, pattern);
        }

        public String stderr() {

            return asString(err);
        }

        public String stderr(final String pattern) {

            return decorate(err, pattern);
        }

        public void dump(final String operation) {

            System.out.println(operation + ": " + returnCode());
            System.out.print(stdout("out > %s"));
            System.err.print(stderr("err > %s"));
        }

        private String decorate(final ByteArrayOutputStream stream, String pattern) {

            if (!pattern.endsWith("\n")) {

                pattern += "\n";
            }

            final String in = asString(stream);
            final StringBuilder builder = new StringBuilder(in.length());
            for (final String line: asString(stream).split("\n")) {

                builder.append(String.format(pattern, line));
            }

            return builder.toString();
        }

        private String asString(final ByteArrayOutputStream stream) {

            try {

                return stream.toString("UTF-8");
            } catch (final UnsupportedEncodingException ex) {

                throw new AssertionError(ex);
            }
        }

        @Override
        public Accumulator returnCode(final int ret) {

            return (Accumulator) super.returnCode(ret);
        }
    }
}
