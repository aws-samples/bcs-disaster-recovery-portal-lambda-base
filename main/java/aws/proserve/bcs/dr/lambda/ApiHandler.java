// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package aws.proserve.bcs.dr.lambda;

import aws.proserve.bcs.dr.lambda.dto.Response;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public abstract class ApiHandler implements MapHandler<APIGatewayProxyRequestEvent> {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final ObjectMapper mapper = new ObjectMapper();

    protected Map<String, Object> output(Response response) {
        try {
            if (response.isSuccessful()) {
                return Map.of(
                        "statusCode", 200,
                        "body", mapper.writeValueAsString(response.getResult()));
            } else {
                return Map.of(
                        "statusCode", 400,
                        "body", mapper.writeValueAsString(response.getCause()));
            }
        } catch (JsonProcessingException e) {
            log.warn("Unable to marshal output", e);
            return Map.of(
                    "statusCode", 500,
                    "body", e.getLocalizedMessage());
        }
    }
}
