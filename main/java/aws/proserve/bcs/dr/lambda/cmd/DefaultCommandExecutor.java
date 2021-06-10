// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package aws.proserve.bcs.dr.lambda.cmd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * A triple-threaded command executor. This executor takes a {@code Command} with its parameters, if any, and returns
 * the result of execution as an {@code ExecuteResult} object. The command submitted would be executed directly on the
 * host where the JVM is running. If you are looking for a way to running some commands via a {@code ssh} tunneling,
 * please refer to {@code SshExecutor}.
 * <h3>Resource Management</h3>
 * The implementation inherits the {@code AutoCloseable} interface, therefore, you <em>must</em> close this executor to
 * reclaim resources, otherwise there might be dangling threads which might prevent the program from being terminated.
 * You may wrap it inside a try-catch block:
 * <pre>
 * try (final CommandExecutor executor = new CommandExecutor()) {
 *     executor.execute(...);
 * } catch (Exception e) {
 *     ...
 * }
 * </pre>
 * By default the termination process would wait for one minute for all threads to join, after when the executor would
 * be shutdown immediately.
 * <h3>Multi-threaded Processing</h3>
 * The default implementation contains three threads, namely:
 * <ol>
 * <li>The main thread, which builds the process and starts it.</li>
 * <li>The output stream reading thread, which is bound to the process's output stream.</li>
 * <li>The error stream reading thread, which is bound to the process's error stream.</li>
 * </ol>
 * Thanks to the multi-threaded handling of the execution, while a long running command is being executed, intermittent
 * output/error messages are logged before the execution is finished or interrupted, which may help you debugging if a
 * command hangs excessively. Note that the output and error messages of generated while executing the command are
 * logged at {@code DEBUG} level.
 *
 * @see Command
 * @see CommandExecutor
 * @see SshExecutor
 * @see ExecutionResult
 */
class DefaultCommandExecutor implements CommandExecutor, AutoCloseable {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final ExecutorService exec;
    private Function<Command, Process> commandProcessFunction;

    /**
     * Initializes a command executor, where you have to provide a name as the threads' name.
     *
     * @param name name of the threads used in the command executor.
     */
    DefaultCommandExecutor(final String name) {
        this.exec = Executors.newFixedThreadPool(2, r -> new Thread(r, name));
        this.commandProcessFunction = command -> {
            final ProcessBuilder pb = new ProcessBuilder(command.asList());
            log.debug("Execute: " + command.asString());

            final var exports = command.getExports();
            if (!exports.isEmpty()) {
                pb.environment().putAll(exports);
            }

            try {
                return pb.start();
            } catch (IOException e) {
                log.error("Unable to start process", e);
                throw new UncheckedIOException(e);
            }
        };
    }

    @Override
    public void close() {
        exec.shutdown();

        try {
            if (!exec.awaitTermination(1, TimeUnit.MINUTES)) {
                exec.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            exec.shutdownNow();
        }
    }

    /**
     * Executes a command with environment properties.
     *
     * @param command a command to be executed.
     * @return the command execution result.
     */
    @Override
    public ExecutionResult execute(final Command command, final String... args) {
        try {
            final var process = commandProcessFunction.apply(command);

            if (args != null && args.length > 0) {
                try (final var writer = new BufferedWriter(
                        new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8))) {
                    for (final String arg : args) {
                        writer.write(arg);
                        writer.write('\n');
                    }
                }
            }

            final var outputReader = exec.submit(new StreamReader("OUTPUT", process.getInputStream()));
            final var errorReader = exec.submit(new StreamReader("ERROR", process.getErrorStream()));
            return new ExecutionResult(process.waitFor(), outputReader.get(), errorReader.get(), command);
        } catch (InterruptedException interrupt) {
            Thread.currentThread().interrupt();
            log.error("Unable to execute command " + command, interrupt);
            return new ExecutionResult(1, "", "", command);
        } catch (ExecutionException | UncheckedIOException | IOException e) {
            log.error("Unable to execute command " + command, e);
            return new ExecutionResult(1, "", "", command);
        }
    }

    static final class StreamReader implements Callable<String> {
        private static final Logger LOG = LoggerFactory.getLogger(StreamReader.class);

        private final String name;
        private final InputStream stream;

        StreamReader(final String name, final InputStream stream) {
            this.name = name;
            this.stream = stream;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof StreamReader)) {
                return false;
            }

            final StreamReader that = (StreamReader) o;
            return Objects.equals(name, that.name) &&
                    Objects.equals(stream, that.stream);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, stream);
        }

        @Override
        public String call() throws Exception {
            final var builder = new StringBuilder();
            try (final var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;

                while ((line = reader.readLine()) != null) {
                    builder.append(line).append('\n');
                    LOG.debug(String.format("[%s] %s", name, line));
                }

                if (builder.length() > 0) {
                    builder.deleteCharAt(builder.length() - 1); // remove the last return
                }

                return builder.toString();
            }
        }
    }
}
