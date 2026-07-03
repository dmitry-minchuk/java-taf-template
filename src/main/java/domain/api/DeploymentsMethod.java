package domain.api;

import io.restassured.http.Method;
import io.restassured.response.Response;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Deployments REST API: deploy an opened design project into a production repository
 * (the same operation as the UI DeployModal). The service side is picked up by any
 * ruleservice instance watching that production repository.
 */
public class DeploymentsMethod extends AuthorizedApiMethod {

    public DeploymentsMethod() {
        super("/rest/deployments");
    }

    /**
     * @param productionRepositoryId production repository config id (e.g. "production")
     * @param deploymentName         name of the deployment unit to create
     * @param projectId              base64 project id as returned by GET /rest/projects
     */
    public Response deploy(String productionRepositoryId, String deploymentName, String projectId, String comment) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("productionRepositoryId", productionRepositoryId);
        body.put("deploymentName", deploymentName);
        body.put("projectId", projectId);
        body.put("comment", comment);
        return callApi(Method.POST, authorizedJsonRequest(body), fullApiUrl, true);
    }
}
