package tests.api;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.api.ProjectRunMethod;
import domain.api.ProjectsMethod;
import domain.serviceclasses.constants.User;
import helpers.service.WorkflowService;
import helpers.utils.WaitUtil;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class TestProjectRunRestApi extends AbstractProjectApiTest {

    private static final String PROJECT_ARCHIVE = "RulesEditor.TestSearchOnProjectLevel.Example1BankRating.zip";
    private static final String REGULAR_TABLE_NAME = "BalanceDynamicIndexCalculation";
    private static final String SPREADSHEET_TABLE_NAME = "BalanceQualityIndexCalculation";
    private static final String TEST_TABLE_NAME = "BankLimitIndexTest";
    private static final String RUNTIME_ERROR_TABLE_NAME = "doRuntimeError";
    private static final String RUN_REQUEST_JSON = """
            {
              "currentFinancialData": {
                "reportDate": "2010-01-01T00:00:00.000",
                "totalAssets": 754299.0,
                "claimsOnDemand": 74502.0,
                "claimsUpTo3Months": 179938.0,
                "claimsSecuredByPropertyCharges": 134321.0,
                "claimsOnBanks": 110956.0,
                "claimsOnCustomers": 336872.0,
                "loanLossProvisionsForClaimsOnCustomers": 9117.0,
                "otherAssets": 7349.0,
                "capital": 40857.0,
                "equity": 28658.0,
                "liabilities": 725641.0,
                "liabilitiesToBanks": 137626.0,
                "liabilitiesToCustomers": 262827.0,
                "liabilitiesOnDemand": 193986.0,
                "liabilitiesToCustomersOnDemand": 143807.0,
                "consolidatedProfit": 1489.0
              },
              "previousFinancialData": {
                "reportDate": "2009-01-01T00:00:00.000",
                "totalAssets": 0.0,
                "claimsOnDemand": 69940.0,
                "claimsUpTo3Months": 176674.0,
                "claimsSecuredByPropertyCharges": 0.0,
                "claimsOnBanks": 0.0,
                "claimsOnCustomers": 0.0001,
                "loanLossProvisionsForClaimsOnCustomers": 0.0,
                "otherAssets": 0.0,
                "capital": 41437.0,
                "equity": 0.0,
                "liabilities": 817527.0,
                "liabilitiesToBanks": 0.0,
                "liabilitiesToCustomers": 264618.0,
                "liabilitiesOnDemand": 179283.0,
                "liabilitiesToCustomersOnDemand": 0.0,
                "consolidatedProfit": -4633.0
              }
            }
            """;
    private static final String SPREADSHEET_RUN_REQUEST_JSON = """
            {
              "reportDate": "2010-01-01T00:00:00.000",
              "totalAssets": 754299.0,
              "claimsOnDemand": 74502.0,
              "claimsUpTo3Months": 179938.0,
              "claimsSecuredByPropertyCharges": 134321.0,
              "claimsOnBanks": 110956.0,
              "claimsOnCustomers": 336872.0,
              "loanLossProvisionsForClaimsOnCustomers": 9117.0,
              "otherAssets": 7349.0,
              "capital": 40857.0,
              "equity": 28658.0,
              "liabilities": 725641.0,
              "liabilitiesToBanks": 137626.0,
              "liabilitiesToCustomers": 262827.0,
              "liabilitiesOnDemand": 193986.0,
              "liabilitiesToCustomersOnDemand": 143807.0,
              "consolidatedProfit": 1489.0
            }
            """;
    private static final String CREATE_RUNTIME_ERROR_TABLE_REQUEST = """
            {
              "moduleName": "Bank Rating",
              "table": {
                "tableType": "SimpleSpreadsheet",
                "kind": "Spreadsheet",
                "name": "doRuntimeError",
                "returnType": "SpreadsheetResult",
                "steps": [
                  {
                    "name": "INT",
                    "type": "Integer",
                    "value": "= 10"
                  },
                  {
                    "name": "ERROR",
                    "value": "= error(\\"sdsd\\")"
                  }
                ]
              }
            }
            """;

    @Test
    @TestCaseId("EPBDS-15752")
    @Description("Verify project run REST API covers regular and spreadsheet execution, runtime errors, media negotiation and rejection cases")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testProjectRunRestApi() {
        String projectName = WorkflowService.loginCreateProjectFromZip(User.ADMIN, PROJECT_ARCHIVE);
        String projectId = resolveProjectIdByName(projectName);
        ProjectsMethod projectsMethod = new ProjectsMethod();
        ProjectRunMethod projectRunMethod = new ProjectRunMethod(projectId);

        assertNoRunResultYet(projectRunMethod);

        String regularTableId = resolveTableIdByName(projectId, REGULAR_TABLE_NAME);
        Response startRunResponse = projectRunMethod.startRun(regularTableId, RUN_REQUEST_JSON);

        assertThat(startRunResponse.getStatusCode())
                .as("POST /rest/projects/{projectId}/run should return HTTP 202 for a regular table")
                .isEqualTo(202);

        Response regularResultResponse = waitForRunResult(projectRunMethod);
        assertRegularRunResult(regularResultResponse, regularTableId);

        Response xlsxResultResponse = projectRunMethod.getXlsxResult();
        assertThat(xlsxResultResponse.getStatusCode())
                .as("GET /rest/projects/{projectId}/run/result should return XLSX payload when requested")
                .isEqualTo(200);
        assertThat(xlsxResultResponse.getHeader("Content-Type"))
                .contains(ProjectRunMethod.XLSX_CONTENT_TYPE);
        assertThat(xlsxResultResponse.getHeader("Content-Disposition"))
                .contains("run-result.xlsx");
        assertThat(xlsxResultResponse.asByteArray())
                .as("XLSX response body should not be empty")
                .isNotEmpty();

        Response unacceptableMediaTypeResponse = projectRunMethod.getResult("application/json, text/plain");
        assertThat(unacceptableMediaTypeResponse.getStatusCode())
                .as("Unsupported Accept combinations should return HTTP 406")
                .isEqualTo(406);

        assertThat(projectRunMethod.cancelRun().getStatusCode())
                .as("DELETE /rest/projects/{projectId}/run should return HTTP 204 after a completed run")
                .isEqualTo(204);
        assertNoRunResultYet(projectRunMethod);

        String spreadsheetTableId = resolveTableIdByName(projectId, SPREADSHEET_TABLE_NAME);
        Response spreadsheetStartResponse = projectRunMethod.startRun(spreadsheetTableId, SPREADSHEET_RUN_REQUEST_JSON);
        assertThat(spreadsheetStartResponse.getStatusCode())
                .as("POST /rest/projects/{projectId}/run should return HTTP 202 for a spreadsheet table")
                .isEqualTo(202);

        Response spreadsheetResultResponse = waitForRunResult(projectRunMethod);
        assertThat(spreadsheetResultResponse.getStatusCode()).isEqualTo(200);
        assertThat(spreadsheetResultResponse.jsonPath().getString("tableId")).isEqualTo(spreadsheetTableId);
        assertThat(spreadsheetResultResponse.jsonPath().getDouble("result.Value_BalanceQualityIndex"))
                .as("Spreadsheet result should include the calculated Balance Quality Index")
                .isCloseTo(0.49d, within(0.000001d));
        assertThat(spreadsheetResultResponse.jsonPath().getString("result.Description_BalanceQualityIndex"))
                .contains("Balance Quality Index");
        assertThat(spreadsheetResultResponse.jsonPath().getList("parameters.name", String.class))
                .containsExactly("currentFinancialData");

        assertThat(projectRunMethod.cancelRun().getStatusCode()).isEqualTo(204);

        Response createRuntimeErrorTableResponse = projectsMethod.createTable(projectId, CREATE_RUNTIME_ERROR_TABLE_REQUEST);
        assertThat(createRuntimeErrorTableResponse.getStatusCode())
                .as("POST /rest/projects/{projectId}/tables should create a helper spreadsheet table for runtime error verification")
                .isEqualTo(201);
        assertThat(createRuntimeErrorTableResponse.jsonPath().getString("name")).isEqualTo(RUNTIME_ERROR_TABLE_NAME);

        String runtimeErrorTableId = resolveTableIdByName(projectId, RUNTIME_ERROR_TABLE_NAME);
        Response runtimeErrorStartResponse = projectRunMethod.startRun(runtimeErrorTableId);
        assertThat(runtimeErrorStartResponse.getStatusCode())
                .as("POST /rest/projects/{projectId}/run should accept runtime-error execution")
                .isEqualTo(202);

        Response runtimeErrorResultResponse = waitForRunResult(projectRunMethod);
        assertThat(runtimeErrorResultResponse.getStatusCode()).isEqualTo(200);
        assertThat(runtimeErrorResultResponse.jsonPath().getList("errors"))
                .as("Runtime errors should be returned in the run result payload")
                .hasSize(1);
        assertThat(runtimeErrorResultResponse.jsonPath().getString("errors[0].summary")).isEqualTo("sdsd");
        assertThat(runtimeErrorResultResponse.jsonPath().getString("errors[0].severity")).isEqualTo("ERROR");

        assertThat(projectRunMethod.cancelRun().getStatusCode()).isEqualTo(204);

        String testTableId = resolveTableIdByName(projectId, TEST_TABLE_NAME);
        Response testTableRunResponse = projectRunMethod.startRun(testTableId);
        assertThat(testTableRunResponse.getStatusCode())
                .as("Regular run API should reject OpenL test tables")
                .isEqualTo(400);
        assertThat(testTableRunResponse.jsonPath().getString("code"))
                .isEqualTo("openl.error.400.run.test-table.not.supported.message");

        Response unknownTableResponse = projectRunMethod.startRun("unknown-table-id");
        assertThat(unknownTableResponse.getStatusCode())
                .as("Unknown table id should return HTTP 404")
                .isEqualTo(404);
        assertThat(unknownTableResponse.jsonPath().getString("code"))
                .isEqualTo("openl.error.404.table.message");

        Response missingTableIdResponse = projectRunMethod.startRunWithoutTableId("{}");
        assertThat(missingTableIdResponse.getStatusCode())
                .as("Missing tableId request parameter should return HTTP 400")
                .isEqualTo(400);
        assertThat(missingTableIdResponse.jsonPath().getString("message"))
                .isNotBlank();
    }

    private Response waitForRunResult(ProjectRunMethod projectRunMethod) {
        long deadline = System.currentTimeMillis() + 30_000;
        Response lastResponse = null;

        while (System.currentTimeMillis() < deadline) {
            lastResponse = projectRunMethod.getJsonResult();
            if (lastResponse.getStatusCode() == 200) {
                return lastResponse;
            }
            assertThat(lastResponse.getStatusCode())
                    .as("Run result should be either pending (409) or completed (200)")
                    .isEqualTo(409);
            WaitUtil.sleep(500, "Waiting for run execution to complete");
        }

        throw new AssertionError("Run result did not complete in time. Last status code: "
                + (lastResponse == null ? "none" : lastResponse.getStatusCode()));
    }

    private void assertNoRunResultYet(ProjectRunMethod projectRunMethod) {
        Response noResultResponse = projectRunMethod.getJsonResult();

        assertThat(noResultResponse.getStatusCode())
                .as("GET /rest/projects/{projectId}/run/result without an active run should return HTTP 404")
                .isEqualTo(404);
        assertThat(noResultResponse.jsonPath().getString("code"))
                .isEqualTo("openl.error.404.run.execution.task.message");
    }

    private void assertRegularRunResult(Response resultResponse, String tableId) {
        assertThat(resultResponse.getStatusCode())
                .as("GET /rest/projects/{projectId}/run/result should return HTTP 200 after the regular run completes")
                .isEqualTo(200);
        assertThat(resultResponse.jsonPath().getString("tableId")).isEqualTo(tableId);
        assertThat(resultResponse.jsonPath().getDouble("result"))
                .as("BalanceDynamicIndexCalculation should return the expected numeric result")
                .isCloseTo(0.94d, within(0.000001d));
        assertThat(resultResponse.jsonPath().getString("resultSchema.type"))
                .isEqualTo("number");
        assertThat(resultResponse.jsonPath().getList("parameters.name", String.class))
                .as("Run response should echo input parameter names")
                .containsExactly("currentFinancialData", "previousFinancialData");
    }
}
