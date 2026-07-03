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
 * studio_preconfig_projects_regression suite. Injects a dynamic markdown
 * launch description that names what is actually being validated.
 */
public class PreconfigProjectsReportPortalListener extends BaseTestNGListener {

    public PreconfigProjectsReportPortalListener() {
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
        sb.append("### EIS Preconfig Projects — Compile & Deploy Validation").append("\n\n");
        sb.append("Syncs the local Mercurial clones of the EIS preconfig product repositories ")
                .append("(`hg pull -u` from vno-hg.exigengroup.com), discovers every OpenL project ")
                .append("(`*/src/main/openl/rules.xml`), zips it and validates the full studio lifecycle: ")
                .append("upload, compile, deploy to a production repository, service availability in ruleservice. ")
                .append("Driven entirely via WebStudio REST (no UI).")
                .append("\n\n");
        sb.append("### Run metadata").append("\n\n");
        sb.append("- **Started:** `").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("`\n");
        sb.append("- **Studio image:** `").append(safeProp(PropertyNameSpace.DOCKER_IMAGE_NAME)).append("`\n");
        sb.append("- **Ruleservice image:** `").append(safeProp(PropertyNameSpace.WS_DOCKER_IMAGE_NAME)).append("`\n");
        sb.append("- **Sources:** local hg clones under `Projects/eis/preconfigs` ")
                .append("(benefits-policy, commercial-policy, personal-claims, personalpolicy)\n\n");
        sb.append("### Per project flow").append("\n\n");
        sb.append("- one `@Factory` instance per discovered project → fresh WebStudio + PostgreSQL (production repo) + ruleservice trio\n");
        sb.append("- `PUT /rest/repos/design/projects/{name}` (multipart) — upload the zipped `src/main/openl`\n");
        sb.append("- `PATCH /rest/projects/{id}` → status=OPENED, then `POST …/tests/run` as the compile trigger\n");
        sb.append("- `GET /rest/projects/{id}/status` → fail on compile errors\n");
        sb.append("- `POST /rest/deployments` → deploy to the `production` repository\n");
        sb.append("- polled `GET ws:/admin/services` → the service from `rules-deploy.xml` must be served (HTTP 200)\n\n");
        sb.append("### Scope note").append("\n\n");
        sb.append("Jar-dependent projects get an automatic Maven stage (openl-maven-plugin build + repack with ")
                .append("direct provided JARs from Nexus); dependency-free ones are zipped straight from hg sources. ")
                .append("Disable the Maven half with `-Dpreconfig.include.jar.dependent=false`.\n");
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
