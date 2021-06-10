// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package aws.proserve.bcs.dr.lambda.cmd;

import java.util.List;
import java.util.Map;

/**
 * A command interface with read-only methods to obtain its content.
 */
public interface Command {
    /**
     * @return an <em>unmodifiable</em> list of all the tokens.
     */
    List<String> asList();

    /**
     * @return a string representation of the command.
     */
    String asString();

    /**
     * @return an <em>unmodifiable</em> map of all environment properties.
     */
    Map<String, String> getExports();
}
