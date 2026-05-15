package configuration.listeners;

import com.epam.reportportal.listeners.ListenerParameters;
import com.epam.reportportal.service.ReportPortal;
import com.epam.reportportal.testng.BaseTestNGListener;
import com.epam.reportportal.testng.TestNGService;
import com.epam.reportportal.utils.properties.PropertiesLoader;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Drop-in replacement for ReportPortalTestNGListener used by the
 * studio_zip_projects_regression suite. Injects a dynamic markdown
 * launch description that names what is actually being validated.
 */
public class ZippedProjectsReportPortalListener extends BaseTestNGListener {

    public ZippedProjectsReportPortalListener() {
        super(new TestNGService(buildReportPortal()));
    }

    private static ReportPortal buildReportPortal() {
        ListenerParameters params = new ListenerParameters(PropertiesLoader.load());
        String existing = params.getDescription();
        if (existing == null || existing.isEmpty()) {
            params.setDescription(buildDescription());
        }
        return ReportPortal.builder().withParameters(params).build();
    }

    private static String buildDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("### Local Zipped Projects — API Validation").append("\n\n");
        sb.append("Walks the local client-projects tree, uploads every ZIP archive into a ")
                .append("fresh WebStudio container, then validates each project (open, list modules, ")
                .append("check compilation, run tests). Driven entirely via WebStudio REST (no UI).")
                .append("\n\n");
        sb.append("### Run metadata").append("\n\n");
        sb.append("- **Started:** `").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("`\n");
        sb.append("- **Container image:** `").append(safeProp(PropertyNameSpace.DOCKER_IMAGE_NAME)).append("`\n");
        sb.append("- **Source tree:** `client_projects/customers_projects_test_automation_6.x`\n\n");
        sb.append("### Grouping").append("\n\n");
        sb.append("- ZIPs that live in a folder whose name ends with `deployment` are uploaded **together** ")
                .append("into one container, so cross-project dependencies inside the deployment unit resolve.\n");
        sb.append("- Every other ZIP is uploaded standalone into its own container.\n");
        sb.append("- Each group spawns its own TestNG instance via `@Factory`, so containers are never reused ")
                .append("between groups.\n\n");
        sb.append("### Per project flow").append("\n\n");
        sb.append("- `PUT /rest/repos/design/projects/{name}` (multipart) — upload one ZIP\n");
        sb.append("- `PATCH /rest/projects/{id}` → status=OPENED (per project, to make it current in session)\n");
        sb.append("- `GET /rest/projects/{id}/modules`\n");
        sb.append("- `GET /rest/compile/progress/-1/-1` → fail on ERROR-severity messages\n");
        sb.append("- `POST /rest/projects/{id}/tests/run` + polled `GET …/tests/summary` (JSESSIONID kept across calls)\n");
        return sb.toString();
    }

    private static String safeProp(PropertyNameSpace prop) {
        try {
            String v = ProjectConfiguration.getProperty(prop);
            return v == null ? "(unset)" : v;
        } catch (RuntimeException e) {
            return "(unset)";
        }
    }
}
