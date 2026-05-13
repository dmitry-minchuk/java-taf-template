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
        sb.append("## Studio Central Projects — API Validation").append("\n\n");
        sb.append("End-to-end check that every project across the selected design repositories ")
                .append("opens, lists modules and passes its test tables. Driven entirely via WebStudio REST (no UI).")
                .append("\n\n");
        sb.append("**Started:** ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        sb.append("**Container image:** `").append(safeProp(PropertyNameSpace.DOCKER_IMAGE_NAME)).append("`").append("\n");
        sb.append("**Branch:** `").append(safeProp(PropertyNameSpace.GITLAB_BRANCH)).append("`").append("\n");
        sb.append("**User:** `").append(safeProp(PropertyNameSpace.GITLAB_USER)).append("`").append("\n\n");
        sb.append("### Repository groups").append("\n\n");
        sb.append("**rating + claim**").append("\n");
        sb.append("- openl-rating").append("\n");
        sb.append("- openl-claim").append("\n\n");
        sb.append("**policy bundle**").append("\n");
        sb.append("- openl-policy").append("\n");
        sb.append("- openl-policy-life").append("\n");
        sb.append("- openl-financials").append("\n\n");
        sb.append("### Per project flow").append("\n");
        sb.append("- `PATCH /rest/projects/{id}` → status=OPENED").append("\n");
        sb.append("- `GET /rest/projects/{id}/modules`").append("\n");
        sb.append("- `POST /rest/projects/{id}/tests/run` + polled `GET …/tests/summary` (JSESSIONID kept across calls)").append("\n");
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
