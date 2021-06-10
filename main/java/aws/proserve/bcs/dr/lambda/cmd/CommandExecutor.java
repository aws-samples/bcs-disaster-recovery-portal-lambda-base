// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package aws.proserve.bcs.dr.lambda.cmd;

/**
 * A command executor interface.
 */
public interface CommandExecutor extends AutoCloseable {

    /**
     * @apiNote Mask off its throwing Exception requirement.
     */
    @Override
    void close();

    /**
     * Wrap it inside a try-catch block:
     * <pre>
     * try (final var executor = new CommandExecutor.create()) {
     *     executor.execute(...);
     * } catch (Exception e) {
     *     ...
     * }
     * </pre>
     */
    static CommandExecutor create(String name) {
        return new DefaultCommandExecutor(name);
    }

    /**
     * Executes a command with a variable-array of arguments.
     *
     * @param command a command object.
     * @param args    a variable array of arguments.
     * @return the execution result.
     */
    ExecutionResult execute(final Command command, final String... args);

    /**
     * Executes a command as {@code root} with a variable-array of arguments.
     *
     * @param command a command object.
     * @param args    a variable array of arguments.
     * @return the execution result.
     */
    default ExecutionResult executeAsRoot(final Command command, final String... args) {
        return execute(SysCommands.sudo().command(command), args);
    }
}
