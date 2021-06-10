// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package aws.proserve.bcs.dr.lambda;

import com.amazonaws.services.lambda.runtime.RequestHandler;

@FunctionalInterface
public interface Handler<T, R> extends RequestHandler<T, R> {
}
