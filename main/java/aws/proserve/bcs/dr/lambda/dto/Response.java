// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package aws.proserve.bcs.dr.lambda.dto;

import org.immutables.value.Value;

import javax.annotation.Nullable;

@Value.Immutable
public interface Response {

    boolean isSuccessful();

    @Value.Default
    default Object getResult() {
        return "ok";
    }

    @Nullable
    @Value.Default
    default String getCause() {
        return null;
    }
}
