// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package aws.proserve.bcs.dr.lambda.cmd;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * An abstract command which helps build a reified command.
 * <p>
 * The recursive generics of this class is to assist returning itself for chained invocation.
 */
public class CommandBase<T extends CommandBase<T>> implements Command {
    protected final Logger log = LogManager.getLogger(getClass());

    private final List<String> tokens = new ArrayList<>();
    private final Map<String, String> exports = new HashMap<>();

    @Override
    public final List<String> asList() {
        return Collections.unmodifiableList(tokens);
    }

    @Override
    public final String asString() {
        final var export = exports.isEmpty() ? "" :
                exports.entrySet().stream()
                        .map(e -> String.format("export %s=%s; ", e.getKey(), e.getValue()))
                        .collect(Collectors.joining());
        return export + String.join(" ", asList());
    }

    @Override
    public Map<String, String> getExports() {
        return Collections.unmodifiableMap(exports);
    }

    /**
     * Exports a key-value pair. It would be exported by the Linux {@code export} keyword.
     *
     * @param key   the key value
     * @param value the value to be exported
     */
    public final T export(final String key, final String value) {
        exports.put(key, value);
        return (T) this;
    }

    /**
     * Adds a Boolean token.
     */
    protected final T add(final boolean token) {
        return add(String.valueOf(token));
    }

    /**
     * Adds a character token.
     */
    protected final T add(final char token) {
        return add(String.valueOf(token));
    }

    /**
     * Adds an integer token.
     */
    protected final T add(final int token) {
        return add(String.valueOf(token));
    }

    /**
     * Adds a long integer token.
     */
    protected final T add(final long token) {
        return add(String.valueOf(token));
    }

    /**
     * Adds a string token.
     */
    protected final T add(final String token) {
        tokens.add(token);
        return (T) this;
    }

    /**
     * Adds a key-value pair with a Boolean value, separated by a space.
     */
    protected final T add(final String key, final boolean value) {
        return add(key, String.valueOf(value));
    }

    /**
     * Adds a key-value pair with an integer value, separated by a space.
     */
    protected final T add(final String key, final int value) {
        return add(key, String.valueOf(value));
    }

    /**
     * Adds a key-value pair with a long integer value, separated by a space.
     */
    protected final T add(final String key, final long value) {
        return add(key, String.valueOf(value));
    }

    /**
     * Adds a key-value pair with a string value, separated by a space.
     */
    protected final T add(final String key, final String value) {
        return add(key).add(value);
    }

    /**
     * Adds a key-value pair with a Boolean value, separated by an equal sign.
     */
    protected final T addWithEqual(final String key, final boolean value) {
        return addWithEqual(key, String.valueOf(value));
    }

    /**
     * Adds a key-value pair with an integer value, separated by an equal sign.
     */
    protected final T addWithEqual(final String key, final int value) {
        return addWithEqual(key, String.valueOf(value));
    }

    /**
     * Adds a key-value pair with a long value, separated by an equal sign.
     */
    protected final T addWithEqual(final String key, final long value) {
        return addWithEqual(key, String.valueOf(value));
    }

    /**
     * Adds a key-value pair with a string value, separated by an equal sign.
     */
    protected final T addWithEqual(final String key, final String value) {
        tokens.add(key + '=' + value);
        return (T) this;
    }

    /**
     * Creates a piped command with another command.
     *
     * @param command a command to receive the output in a piped way.
     */
    public final T pipe(Command command) {
        return add('|').add(command.asString());
    }

    @Override
    public final String toString() {
        return asString();
    }
}
