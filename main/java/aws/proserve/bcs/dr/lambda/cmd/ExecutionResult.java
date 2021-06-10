// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package aws.proserve.bcs.dr.lambda.cmd;

/**
 * A simple immutable class for execution result, which contains the result code, the output, the error message and a
 * reference to the command.
 */
public final class ExecutionResult {
    private final int code;
    private final String output;
    private final String error;
    private final Command command;

    public ExecutionResult(final int code,
                           final String output,
                           final String error,
                           final Command command) {
        this.code = code;
        this.output = output;
        this.error = error;
        this.command = command;
    }

    /**
     * @return {@code true} if the return code is 0, otherwise {@code false}.
     */
    public boolean isSuccessful() {
        return code == 0;
    }

    /**
     * @return execution return code.
     */
    public int getCode() {
        return code;
    }

    /**
     * @return execution output.
     */
    public String getOutput() {
        return output;
    }

    /**
     * @return execution error message, if any.
     */
    public String getError() {
        return error;
    }

    @Override
    public String toString() {
        return String.format("Code: %d, Output:%n%s%nError:%n%s%n", code, output, error);
    }
}
