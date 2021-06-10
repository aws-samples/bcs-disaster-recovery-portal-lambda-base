// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package aws.proserve.bcs.dr.lambda;

@FunctionalInterface
public interface StringsHandler<T> extends Handler<T, String[]> {
}
