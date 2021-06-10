// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package aws.proserve.bcs.dr.lambda.cmd;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Common Linux operating system administrative commands.
 */
public final class SysCommands {

    private SysCommands() {
    }

    public static Awk awk() {
        return new Awk();
    }

    public static Bash bash() {
        return new Bash();
    }

    public static Cat cat() {
        return new Cat();
    }

    public static Df df() {
        return new Df();
    }

    public static Echo echo() {
        return new Echo();
    }

    public static Grep grep() {
        return new Grep();
    }

    public static Hostname hostname() {
        return new Hostname();
    }

    public static Kill kill() {
        return new Kill();
    }

    public static Ls ls(String dir) {
        return new Ls(dir);
    }

    public static Mkdir mkdir(String dir) {
        return new Mkdir(dir);
    }

    public static Rm rm() {
        return new Rm();
    }

    public static Ps ps() {
        return new Ps();
    }

    public static Ssh ssh() {
        return new Ssh();
    }

    public static Sudo sudo() {
        return new Sudo();
    }

    public static Tar tar() {
        return new Tar();
    }

    public static Xargs xargs() {
        return new Xargs();
    }

    /**
     * A class for {@code awk} command.
     */
    public static final class Awk extends CommandBase<Awk> {
        private Awk() {
            add("/usr/bin/awk");
        }

        public Awk pattern(String pattern) {
            return add(pattern);
        }
    }

    /**
     * A class for {@code bash} command.
     */
    public static final class Bash extends CommandBase<Bash> {
        private Bash() {
            add("/usr/bin/bash");
        }

        public Bash command(String command) {
            return add("-c '" + command + "'");
        }
    }


    /**
     * A class for {@code hostname} command.
     */
    public static final class Hostname extends CommandBase<Hostname> {
        private Hostname() {
            add("/usr/bin/hostname");
        }

        public Hostname name(final String name) {
            return add(name);
        }
    }

    /**
     * A class for {@code kill} command
     */
    public static final class Kill extends CommandBase<Kill> {
        private static final int SIGKILL = 9;

        private Kill() {
            add("/usr/bin/kill");
        }

        public Kill signal(int signal) {
            return add("-" + signal);
        }

        public Kill kill() {
            return signal(SIGKILL);
        }
    }

    /**
     * A class for {@code ls} command
     */
    public static final class Ls extends CommandBase<Ls> {
        private Ls(String dir) {
            add("/usr/bin/ls", dir);
        }
    }

    /**
     * A class for {@code grep} command
     */
    public static final class Grep extends CommandBase<Grep> {
        private Grep() {
            add("/usr/bin/grep");
        }

        public Grep excludeSelf() {
            return invertMatch("grep");
        }

        public Grep ignoreCase() {
            return add("--ignore-case");
        }

        public Grep invertMatch(String match) {
            return add("--invert-match", match);
        }

        public Grep regex(String regex) {
            return add("--perl-regexp", regex);
        }
    }

    /**
     * A class for {@code mkdir} command
     */
    public static final class Mkdir extends CommandBase<Mkdir> {
        private Mkdir(String dir) {
            add("/usr/bin/mkdir", dir);
        }
    }

    /**
     * A class for {@code ps} command
     */
    public static final class Ps extends CommandBase<Ps> {
        private Ps() {
            add("/usr/bin/ps");
        }

        public Ps all() {
            return add("-A");
        }

        public Ps fullFormat() {
            return add("-f");
        }
    }

    /**
     * A class for {@code rm} command
     */
    public static final class Rm extends CommandBase<Rm> {
        private Rm() {
            add("/usr/bin/rm");
        }

        public Rm force() {
            return add("-f");
        }

        public Rm file(String file) {
            return add(file);
        }

        public Rm folder(String folder) {
            return add("-r", folder);
        }
    }

    /**
     * A class for {@code ssh} command.
     */
    public static final class Ssh extends CommandBase<Ssh> {
        private Ssh() {
            add("/usr/bin/ssh");
        }

        public Ssh host(final String host) {
            return add(host);
        }

        public Ssh host(final String user, final String host) {
            return add(user + '@' + host);
        }

        public Ssh identity(final String file) {
            return add("-i", file);
        }

        public Ssh privateKey(final String key) {
            final Path keyFile;

            try {
                keyFile = Files.createTempFile(null, ".key");
                Files.write(keyFile, key.getBytes(StandardCharsets.US_ASCII));
            } catch (IOException e) {
                log.error("Unable to prepare identity file for ssh login.");
                throw new UncheckedIOException(e);
            }

            return identity(keyFile.toString());
        }

        /**
         * Force pseudo-tty allocation.
         */
        public Ssh tty() {
            return add("-tt");
        }

        public Ssh command(final Command command) {
            return add(command.asString());
        }

        public Ssh noKeyChecking() {
            return addWithEqual("-oStrictHostKeyChecking", "no");
        }

        public Ssh nullHostFile() {
            return addWithEqual("-oUserKnownHostsFile", "/dev/null");
        }

        public Ssh timeout(long seconds) {
            return addWithEqual("-oConnectTimeout", seconds);
        }
    }

    /**
     * A class for {@code sudo} command.
     */
    public static final class Sudo extends CommandBase<Sudo> {
        private Sudo() {
            add("/usr/bin/sudo");
        }

        public Sudo command(final Command command) {
            return add(command.asString());
        }
    }

    /**
     * A class for {@code sudo} command.
     */
    public static final class Tar extends CommandBase<Tar> {
        private Tar() {
            add("/usr/bin/tar");
        }

        public Tar compressFile(String target, String file) {
            return add("cvzf", target).add(file);
        }

        public Tar extractFile(String file, String dir) {
            return add("xvzf", file).addWithEqual("--directory", dir);
        }
    }

    /**
     * A class for {@code xargs} command
     */
    public static final class Xargs extends CommandBase<Xargs> {
        private Xargs() {
            add("/usr/bin/xargs");
        }

        public Xargs command(Command command) {
            return add(command.asString());
        }
    }

    /**
     * A class for {@code cat} command
     */
    public static final class Cat extends CommandBase<Cat> {
        private Cat() {
            add("/usr/bin/cat");
        }

        public Cat pipeToFile(String file, String content) {
            return add("<<EOF > " + file + "\n" + content + "EOF");
        }
    }

    /**
     * A class for {@code df} command
     */
    public static final class Df extends CommandBase<Df> {
        private Df() {
            add("/bin/df");
        }

        public Df humanReadable( ) {
            return add("-h");
        }
    }

    /**
     * A class for {@code echo} command
     */
    public static final class Echo extends CommandBase<Echo> {
        private Echo() {
            add("/usr/bin/echo");
        }

        public Echo content(String content) {
            return add(content);
        }

        public Echo pipeToCommand(String command) {
            return add("| " + command);
        }
    }
}
