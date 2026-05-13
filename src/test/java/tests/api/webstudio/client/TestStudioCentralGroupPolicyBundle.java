package tests.api.webstudio.client;

import configuration.appcontainer.AppContainerStartParameters;

public class TestStudioCentralGroupPolicyBundle extends AbstractStudioCentralProjectsApi {
    @Override
    protected AppContainerStartParameters params() {
        return AppContainerStartParameters.STUDIO_CENTRAL_GROUP_2_PARAMS;
    }

    @Override
    protected String groupLabel() {
        return "policy bundle";
    }
}
