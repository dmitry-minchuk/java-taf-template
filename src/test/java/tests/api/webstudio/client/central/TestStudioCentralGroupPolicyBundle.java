package tests.api.webstudio.client.central;

import configuration.annotations.LocalOnly;
import tests.api.webstudio.client.base.AbstractStudioCentralProjectsApi;

import configuration.appcontainer.AppContainerStartParameters;

@LocalOnly(reason = "Needs access to the Genesis central repositories, see studio_central_projects_regression.xml")
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
