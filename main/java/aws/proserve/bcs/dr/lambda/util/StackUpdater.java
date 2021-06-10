// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package aws.proserve.bcs.dr.lambda.util;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.ListStacksRequest;
import com.amazonaws.services.cloudformation.model.ListStacksResult;
import com.amazonaws.services.cloudformation.model.StackSummary;
import com.amazonaws.services.cloudformation.model.UpdateStackRequest;
import com.amazonaws.waiters.WaiterParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class StackUpdater {
    private static final String CREATE_COMPLETE = "CREATE_COMPLETE";
    private static final String DELETE_COMPLETE = "DELETE_COMPLETE";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final AmazonCloudFormation cfn;
    private final String stackName;

    private Optional<String> status;

    public StackUpdater(AmazonCloudFormation cfn, String stackName) {
        this.cfn = cfn;
        this.stackName = stackName;
    }

    public boolean isValid() {
        final var listRequest = new ListStacksRequest();
        ListStacksResult result;
        do {
            result = cfn.listStacks(listRequest);
            listRequest.setNextToken(result.getNextToken());
            status = result.getStackSummaries().stream()
                    .filter(s -> s.getStackName().equals(stackName))
                    .findFirst()
                    .map(StackSummary::getStackStatus);
        } while (status.isEmpty() && result.getNextToken() != null);

        return status.isPresent() && status.get().equals(CREATE_COMPLETE);
    }

    public void update(String template) {
        if (status.isEmpty() || status.get().equals(DELETE_COMPLETE)) {
            cfn.createStack(new CreateStackRequest()
                    .withStackName(stackName)
                    .withTemplateBody(template));
            cfn.waiters().stackCreateComplete().run(new WaiterParameters<>(new DescribeStacksRequest()
                    .withStackName(stackName)));
        } else {
            cfn.updateStack(new UpdateStackRequest()
                    .withStackName(stackName)
                    .withTemplateBody(template));
            cfn.waiters().stackUpdateComplete().run(new WaiterParameters<>(new DescribeStacksRequest()
                    .withStackName(stackName)));
        }
        log.info("Updated stack [{}].", stackName);
    }
}
