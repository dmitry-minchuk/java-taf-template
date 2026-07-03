package tests.api.webstudio.client.central;

import tests.api.webstudio.client.base.AbstractStudioCentralProjectsApi;

import configuration.appcontainer.AppContainerStartParameters;

public class TestStudioCentralGroupRatingClaim extends AbstractStudioCentralProjectsApi {
    @Override
    protected AppContainerStartParameters params() {
        return AppContainerStartParameters.STUDIO_CENTRAL_GROUP_1_PARAMS;
    }

    @Override
    protected String groupLabel() {
        return "rating + claim";
    }
}
