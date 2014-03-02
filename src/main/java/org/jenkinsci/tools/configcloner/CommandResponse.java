/*
 * The MIT License
 *
 * Copyright (c) 2013 Red Hat, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.tools.configcloner;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class CommandResponse {

    private int returnCode = -42;
    private final PrintStream outputStream;
    private final PrintStream errorStream;

    public static CommandResponse system() {
        return new CommandResponse(System.out, System.err);
    }

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

        if (returnCode == 0) {
            returnCode = response.returnCode();
        }
        outputStream.append(response.stdout());
        errorStream.append(response.stderr());

        return this;
    }

    public static class Accumulator extends CommandResponse{

        final ByteArrayOutputStream out;
        final ByteArrayOutputStream err;

        private Accumulator(final ByteArrayOutputStream out, final ByteArrayOutputStream err) {

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
            System.err.print(stderr("err > %s"));
            System.out.print(stdout("out > %s"));
        }

        private String decorate(final ByteArrayOutputStream stream, String pattern) {

            if (!pattern.endsWith("\n")) {

                pattern += "\n";
            }

            final String in = asString(stream);
            if (in.isEmpty()) return "";

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
