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
 * Drop-in replacement for ReportPortalTestNGListener that injects a dynamic markdown
 * launch description before the Launch is created. Use in studio_central_projects_regression.xml
 * instead of com.epam.reportportal.testng.ReportPortalTestNGListener.
 */
public class StudioCentralReportPortalListener extends BaseTestNGListener {

    public StudioCentralReportPortalListener() {
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
        sb.append("### Studio Central Projects — API Validation").append("\n\n");
        sb.append("End-to-end check that every project across the selected design repositories ")
                .append("opens, lists modules and passes its test tables. Driven entirely via WebStudio REST (no UI).")
                .append("\n\n");
        sb.append("### Run metadata").append("\n\n");
        sb.append("- **Started:** `").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("`\n");
        sb.append("- **Container image:** `").append(safeProp(PropertyNameSpace.DOCKER_IMAGE_NAME)).append("`\n");
        sb.append("- **Branch:** `").append(safeProp(PropertyNameSpace.GITLAB_BRANCH)).append("`\n");
        sb.append("- **User:** `").append(safeProp(PropertyNameSpace.GITLAB_USER)).append("`\n\n");
        sb.append("### Repository groups").append("\n\n");
        sb.append("- **rating + claim**\n");
        sb.append("  - openl-rating\n");
        sb.append("  - openl-claim\n");
        sb.append("- **policy bundle**\n");
        sb.append("  - openl-policy\n");
        sb.append("  - openl-policy-life\n");
        sb.append("  - openl-financials\n\n");
        sb.append("### Per project flow").append("\n\n");
        sb.append("- All projects are bulk-opened in `@BeforeClass` so cross-project dependencies can resolve\n");
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
