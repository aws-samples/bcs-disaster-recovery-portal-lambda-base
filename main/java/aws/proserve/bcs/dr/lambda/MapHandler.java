// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package aws.proserve.bcs.dr.lambda;

import java.util.Map;

@FunctionalInterface
public interface MapHandler<T> extends Handler<T, Map<String, Object>> {
}
