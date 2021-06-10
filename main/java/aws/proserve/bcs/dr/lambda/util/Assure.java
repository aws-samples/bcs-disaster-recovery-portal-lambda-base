// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package aws.proserve.bcs.dr.lambda.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public final class Assure {
    private static final Logger log = LoggerFactory.getLogger(Assure.class);

    /**
     * Retries 18 times with an interval of 10 seconds, in total 3 minute.
     *
     * @throws IllegalStateException if the runnable cannot return successfully in time.
     */
    public static void assure(Runnable runnable) throws IllegalStateException {
        assure(runnable, 18, 10);
    }

    /**
     * @throws IllegalStateException if the runnable cannot return successfully in time.
     */
    public static void assure(Runnable runnable, int retry, int intervalSeconds) throws IllegalStateException {
        do {
            try {
                runnable.run();
                return;
            } catch (Exception e) {
                log.debug(String.format("Assuring, wait for %d seconds, %d retries left.", intervalSeconds, retry), e);
                try {
                    TimeUnit.SECONDS.sleep(intervalSeconds);
                } catch (InterruptedException interrupted) {
                    log.info("Interrupted", interrupted);
                    break;
                }
            }
        } while (--retry > 0);

        throw new IllegalStateException("Assuring failed.");
    }
}
