package domain.api;

import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * Group administration: {@code POST /rest/admin/management/groups} (form-urlencoded).
 * A Studio group must exist before a project ACL can be granted to it as a non-principal SID
 * (external/group authority) — see {@link AclProjectsMethod#grantRole}.
 */
public class GroupsMethod extends AuthorizedApiMethod {

    public GroupsMethod() {
        super("/rest/admin/management/groups");
    }

    public Response createGroup(String name) {
        RequestSpecification spec = authorizedRequest()
                .contentType(ContentType.URLENC)
                .formParam("name", name);
        return callApi(Method.POST, spec, fullApiUrl, true);
    }
}
