package tests.api;

import domain.api.ProjectsMethod;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import tests.BaseTest;

import java.util.List;
import java.util.Map;

public abstract class AbstractProjectApiTest extends BaseTest {

    protected String resolveProjectIdByName(String projectName) {
        ProjectsMethod projectsMethod = new ProjectsMethod();
        Response response = projectsMethod.getProjects(projectName);

        Assertions.assertThat(response.getStatusCode())
                .as("GET /rest/projects should return HTTP 200 for project '%s'", projectName)
                .isEqualTo(200);

        List<Map<String, Object>> projects = response.jsonPath().getList("content");
        return projects.stream()
                .filter(project -> projectName.equals(project.get("name")))
                .map(project -> String.valueOf(project.get("id")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Project ID was not found for project: " + projectName));
    }

    protected String resolveTableIdByName(String projectId, String tableName) {
        ProjectsMethod projectsMethod = new ProjectsMethod();
        Response response = projectsMethod.getProjectTables(projectId, tableName);

        Assertions.assertThat(response.getStatusCode())
                .as("GET /rest/projects/{projectId}/tables should return HTTP 200 for table '%s'", tableName)
                .isEqualTo(200);

        List<Map<String, Object>> tables = response.jsonPath().getList("content");
        return tables.stream()
                .filter(table -> tableName.equals(table.get("name")))
                .map(table -> String.valueOf(table.get("id")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Table ID was not found for table: " + tableName));
    }
}
