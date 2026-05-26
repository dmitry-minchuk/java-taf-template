package tests.api.webstudio.integration;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.api.AuthenticationSettingsMethod;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Section A of EPBDS-15960 — global `allowBypassProtectedBranches` setting toggle
 * via PATCH /web/admin/settings/authentication.
 *
 * Covered: A.1 (default OFF), A.2 (admin enables), A.3 (admin disables), A.6 (OpenAPI schema).
 * Not covered here (will be added later):
 *   A.4 — non-admin → 403 (needs non-admin user provisioning, shared with Section B).
 *   A.5 — persistence across container restart (the TAF container is not restartable mid-test).
 */
public class TestProtectedBranchBypassSettingApi extends BaseTest {

    @Test
    @TestCaseId("EPBDS-15960")
    @Description("Section A: PATCH /web/admin/settings/authentication toggles allowBypassProtectedBranches; "
            + "default is OFF after fresh install; OpenAPI schema exposes the field")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testAllowBypassProtectedBranchesSettingToggle() {
        AuthenticationSettingsMethod settings = new AuthenticationSettingsMethod();

        // A.1 — fresh container, default value is OFF
        Response initialGet = settings.getSettings();
        assertThat(initialGet.getStatusCode())
                .as("GET authentication settings should return 200 for admin")
                .isEqualTo(200);
        assertThat(initialGet.jsonPath().getBoolean("allowBypassProtectedBranches"))
                .as("A.1 — allowBypassProtectedBranches must default to false on a fresh container")
                .isFalse();

        // A.2 — admin can enable the setting (PATCH 204 + GET returns true)
        Response patchOn = settings.setAllowBypassProtectedBranches(true);
        assertThat(patchOn.getStatusCode())
                .as("A.2 — PATCH ON should return 204 No Content")
                .isEqualTo(204);
        assertThat(settings.getSettings().jsonPath().getBoolean("allowBypassProtectedBranches"))
                .as("A.2 — GET after PATCH ON should reflect allowBypassProtectedBranches=true")
                .isTrue();

        // A.3 — admin can disable the setting (PATCH 204 + GET returns false)
        Response patchOff = settings.setAllowBypassProtectedBranches(false);
        assertThat(patchOff.getStatusCode())
                .as("A.3 — PATCH OFF should return 204 No Content")
                .isEqualTo(204);
        assertThat(settings.getSettings().jsonPath().getBoolean("allowBypassProtectedBranches"))
                .as("A.3 — GET after PATCH OFF should reflect allowBypassProtectedBranches=false")
                .isFalse();

        // A.6 — OpenAPI document exposes the new field on the AuthenticationSettings schema.
        String openApiResponse = RestAssured.given()
                .header("Accept", "application/json")
                .auth().preemptive().basic("admin", "admin")
                .get(settings.getOpenApiUrl())
                .asString();
        assertThat(openApiResponse)
                .as("A.6 — OpenAPI schema should expose `allowBypassProtectedBranches` on AuthenticationSettings")
                .contains("allowBypassProtectedBranches");
    }
}
