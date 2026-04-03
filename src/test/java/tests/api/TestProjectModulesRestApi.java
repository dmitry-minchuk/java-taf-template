package tests.api;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.api.ProjectModulesMethod;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import helpers.utils.ZipUtil;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TestProjectModulesRestApi extends AbstractProjectApiTest {

    @Test
    @TestCaseId("EPBDS-15035")
    @Description("Verify project modules REST API supports happy-path operations, wildcard modules and key validation errors for a local design-repository project")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testProjectModulesRestApi() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        String projectId = resolveProjectIdByName(projectName);

        ProjectModulesMethod modulesMethod = new ProjectModulesMethod(projectId);

        assertThat(extractModuleNames(modulesMethod.listModules()))
                .as("Newly created Sample Project should contain the default Main module")
                .contains("Main");

        Response addModuleResponse = modulesMethod.addModule(Map.of(
                "name", "MainTests",
                "path", "MainTests.xlsx",
                "createFile", true
        ));
        assertThat(addModuleResponse.getStatusCode())
                .as("POST /rest/projects/{projectId}/modules should return HTTP 201")
                .isEqualTo(201);
        assertThat(addModuleResponse.jsonPath().getString("name")).isEqualTo("MainTests");
        assertThat(addModuleResponse.jsonPath().getString("path")).isEqualTo("MainTests.xlsx");

        Response editModuleResponse = modulesMethod.editModule("Main", Map.of(
                "name", "Main-CW",
                "path", "Main.xlsx"
        ));
        assertThat(editModuleResponse.getStatusCode())
                .as("PUT /rest/projects/{projectId}/modules/{moduleName} should return HTTP 200")
                .isEqualTo(200);
        assertThat(editModuleResponse.jsonPath().getString("name")).isEqualTo("Main-CW");
        assertThat(editModuleResponse.jsonPath().getString("path")).isEqualTo("Main.xlsx");

        Response copyModuleResponse = modulesMethod.copyModule("Main-CW", Map.of(
                "newModuleName", "Main-NY",
                "newPath", "Main-NY.xlsx"
        ));
        assertThat(copyModuleResponse.getStatusCode())
                .as("POST /rest/projects/{projectId}/modules/{moduleName}/copy should return HTTP 201")
                .isEqualTo(201);
        assertThat(copyModuleResponse.jsonPath().getString("moduleName")).isEqualTo("Main-NY");
        assertThat(copyModuleResponse.jsonPath().getString("path")).isEqualTo("Main-NY.xlsx");
        assertThat(copyModuleResponse.jsonPath().getBoolean("wildcardCovered")).isFalse();

        Response copyWithoutPathResponse = modulesMethod.copyModule("Main-CW", Map.of(
                "newModuleName", "Main-TX"
        ));
        assertThat(copyWithoutPathResponse.getStatusCode())
                .as("Copy without explicit path should derive the target file name automatically")
                .isEqualTo(201);
        assertThat(copyWithoutPathResponse.jsonPath().getString("moduleName")).isEqualTo("Main-TX");
        assertThat(copyWithoutPathResponse.jsonPath().getString("path")).isEqualTo("Main-TX.xlsx");
        assertThat(copyWithoutPathResponse.jsonPath().getBoolean("wildcardCovered")).isFalse();

        assertThat(extractModuleNames(modulesMethod.listModules()))
                .as("List modules should reflect add, edit and copy operations")
                .contains("Main-CW", "MainTests", "Main-NY", "Main-TX")
                .doesNotContain("Main");

        Response duplicateAddResponse = modulesMethod.addModule(Map.of(
                "name", "Main-CW",
                "path", "Duplicate.xlsx"
        ));
        assertThat(duplicateAddResponse.getStatusCode())
                .as("Duplicate module names should be rejected with HTTP 409")
                .isEqualTo(409);
        assertThat(duplicateAddResponse.jsonPath().getString("code"))
                .isEqualTo("openl.error.409.module.name.exists.message");

        Response missingModuleEditResponse = modulesMethod.editModule("MissingModule", Map.of(
                "name", "StillMissing",
                "path", "StillMissing.xlsx"
        ));
        assertThat(missingModuleEditResponse.getStatusCode())
                .as("Editing a non-existing module should return HTTP 404")
                .isEqualTo(404);
        assertThat(missingModuleEditResponse.jsonPath().getString("code"))
                .isEqualTo("openl.error.404.module.file.not.found.message");

        Response missingModuleDeleteResponse = modulesMethod.removeModule("MissingModule", false);
        assertThat(missingModuleDeleteResponse.getStatusCode())
                .as("Deleting a non-existing module should return HTTP 404")
                .isEqualTo(404);
        assertThat(missingModuleDeleteResponse.jsonPath().getString("code"))
                .isEqualTo("openl.error.404.module.not.found.message");

        Response addWildcardModuleResponse = modulesMethod.addModule(Map.of(
                "name", "WildcardRules",
                "path", "rules/*.xlsx"
        ));
        assertThat(addWildcardModuleResponse.getStatusCode())
                .as("Adding a wildcard module should return HTTP 201")
                .isEqualTo(201);
        assertThat(addWildcardModuleResponse.jsonPath().getString("name")).isEqualTo("WildcardRules");
        assertThat(addWildcardModuleResponse.jsonPath().getString("path")).isEqualTo("rules/*.xlsx");

        Response modulesWithWildcardResponse = modulesMethod.listModules();
        assertThat(extractModuleNames(modulesWithWildcardResponse))
                .contains("WildcardRules");
        assertThat(findModule(modulesWithWildcardResponse, "WildcardRules").get("matchedModules"))
                .as("Fresh wildcard module should not match any files yet")
                .isEqualTo(List.of());

        Response copyToWildcardTargetResponse = modulesMethod.copyModule("Main-CW", Map.of(
                "newModuleName", "Bad",
                "newPath", "rules/*.xlsx"
        ));
        assertThat(copyToWildcardTargetResponse.getStatusCode())
                .as("Copy target cannot be a wildcard path itself")
                .isEqualTo(400);
        assertThat(copyToWildcardTargetResponse.jsonPath().getString("code"))
                .isEqualTo("openl.error.400.module.path.wildcard.message");

        Response wildcardCoveredCopyResponse = modulesMethod.copyModule("Main-CW", Map.of(
                "newPath", "rules/Main-Copy.xlsx"
        ), true);
        assertThat(wildcardCoveredCopyResponse.getStatusCode())
                .as("Copying into a wildcard-covered path with force=true should return HTTP 201")
                .isEqualTo(201);
        assertThat(wildcardCoveredCopyResponse.jsonPath().getString("moduleName")).isEqualTo("Main-Copy");
        assertThat(wildcardCoveredCopyResponse.jsonPath().getString("path")).isEqualTo("rules/Main-Copy.xlsx");
        assertThat(wildcardCoveredCopyResponse.jsonPath().getBoolean("wildcardCovered")).isTrue();

        Response modulesAfterWildcardCopyResponse = modulesMethod.listModules();
        Map<String, Object> wildcardModule = findModule(modulesAfterWildcardCopyResponse, "WildcardRules");
        List<Map<String, Object>> matchedModules = (List<Map<String, Object>>) wildcardModule.get("matchedModules");
        assertThat(matchedModules)
                .as("Wildcard module should expose covered concrete modules after copy")
                .hasSize(1);
        assertThat(matchedModules.getFirst().get("name")).isEqualTo("Main-Copy");
        assertThat(matchedModules.getFirst().get("path")).isEqualTo("rules/Main-Copy.xlsx");

        Response removeModuleResponse = modulesMethod.removeModule("Main-NY", false);
        assertThat(removeModuleResponse.getStatusCode())
                .as("DELETE /rest/projects/{projectId}/modules/{moduleName} should return HTTP 204")
                .isEqualTo(204);

        Response removeKeepFileResponse = modulesMethod.removeModule("Main-TX", true);
        assertThat(removeKeepFileResponse.getStatusCode())
                .as("Deleting a module with keepFile=true should still return HTTP 204")
                .isEqualTo(204);

        Response removeWildcardModuleResponse = modulesMethod.removeModule("WildcardRules", false);
        assertThat(removeWildcardModuleResponse.getStatusCode())
                .as("Deleting a wildcard module should return HTTP 204")
                .isEqualTo(204);

        assertThat(extractModuleNames(modulesMethod.listModules()))
                .as("Deleted module should disappear from the descriptor")
                .contains("Main-CW", "MainTests")
                .doesNotContain("Main-NY", "Main-TX", "WildcardRules", "Main");

        LocalDriverPool.getPage().reload();
        EditorPage editorPage = new EditorPage();
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);
        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getEditorToolbarPanelComponent().clickExport();

        File exportedZip = editorPage.getExportProjectDialogComponent().clickExportAndDownload();
        String rulesXml = ZipUtil.readFileFromZip(exportedZip, "rules.xml");
        List<String> archivedFiles = ZipUtil.listFiles(exportedZip);

        assertThat(rulesXml)
                .as("rules.xml should persist module API changes")
                .contains("<name>Main-CW</name>")
                .contains("<rules-root path=\"Main.xlsx\"/>")
                .contains("<name>MainTests</name>")
                .contains("<rules-root path=\"MainTests.xlsx\"/>")
                .doesNotContain("<name>Main-NY</name>")
                .doesNotContain("<name>Main-TX</name>")
                .doesNotContain("<name>WildcardRules</name>");
        assertThat(archivedFiles)
                .as("keepFile=true should preserve the copied file in the exported archive")
                .anyMatch(this::isMainTxFile)
                .noneMatch(this::isMainNyFile);
    }

    private List<String> extractModuleNames(Response response) {
        assertThat(response.getStatusCode())
                .as("GET /rest/projects/{projectId}/modules should return HTTP 200")
                .isEqualTo(200);
        return response.jsonPath().getList("name");
    }

    private Map<String, Object> findModule(Response response, String moduleName) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> modules = (List<Map<String, Object>>) (List<?>) response.jsonPath().getList("");
        return modules.stream()
                .filter(module -> moduleName.equals(module.get("name")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Module was not found: " + moduleName));
    }

    private boolean isMainTxFile(String archivedPath) {
        return archivedPath.equals("Main-TX.xlsx") || archivedPath.endsWith("/Main-TX.xlsx");
    }

    private boolean isMainNyFile(String archivedPath) {
        return archivedPath.equals("Main-NY.xlsx") || archivedPath.endsWith("/Main-NY.xlsx");
    }
}
