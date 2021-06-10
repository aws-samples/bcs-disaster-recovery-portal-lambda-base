// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package aws.proserve.bcs.dr.lambda.cmd;

import java.time.Duration;

/**
 * A reified command executor to run commands via {@code ssh} tunneling. The default time out for a single execution is
 * one hour. A typical such command looks like below:
 * <pre>
 *     /usr/bin/ssh
 *     -tt
 *     -oUserKnownHostsFile=/dev/null
 *     -oStrictHostKeyChecking=no
 *     -oConnectTimeout=3600
 *     -i /tmp/xxxxxxxxx.key
 *     ec2-user@ec2-xx-xx-xx-xx.us-west-xx.compute.amazonaws.com
 *     /usr/bin/ls /
 * </pre>
 * This way, the authentication is circumvented and the execution goes through to the destination host indirectly.
 *
 * @see Command
 * @see CommandExecutor
 */
public class SshExecutor extends DefaultCommandExecutor {
    private static final Duration TIMEOUT_ONE_HOUR = Duration.ofHours(1);

    private final String user;
    private final String host;
    private final String privateKey;

    /**
     * Creates an {@code ssh} command executor.
     *
     * @param name       used in logging as the name of the processing thread.
     * @param user       login username.
     * @param host       login host address.
     * @param privateKey the private key to authenticate login.
     */
    public SshExecutor(final String name,
                       final String user,
                       final String host,
                       final String privateKey) {
        super(name);
        this.user = user;
        this.host = host;
        this.privateKey = privateKey;
    }

    @Override
    public ExecutionResult execute(final Command command, final String... args) {
        return execute(TIMEOUT_ONE_HOUR, command, args);
    }

    public ExecutionResult execute(final Duration timeoutSec, final Command command, final String... args) {
        return super.execute(ssh(timeoutSec).command(command), args);
    }

    @Override
    public ExecutionResult executeAsRoot(final Command command, final String... args) {
        return super.execute(ssh(TIMEOUT_ONE_HOUR).command(SysCommands.sudo().command(command)), args);
    }

    private SysCommands.Ssh ssh(final Duration timeoutSec) {
        return SysCommands.ssh()
                .tty()
                .nullHostFile()
                .noKeyChecking()
                .timeout(timeoutSec.getSeconds())
                .privateKey(privateKey)
                .host(user, host);
    }
}
