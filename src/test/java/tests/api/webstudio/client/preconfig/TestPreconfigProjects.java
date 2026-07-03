package tests.api.webstudio.client.preconfig;

import tests.api.webstudio.client.base.AbstractPreconfigProjectsApi;

import helpers.service.PreconfigSourcesService;
import helpers.service.PreconfigSourcesService.PreconfigProject;
import org.testng.annotations.Factory;

import java.util.List;

/**
 * Preconfig-projects regression: syncs the local Mercurial clones of the EIS preconfig product
 * repositories (hg pull -u), discovers every OpenL project inside them and creates one
 * test-class instance per project via {@code @Factory}. Each instance gets its own
 * WebStudio + PostgreSQL(production repo) + ruleservice trio and validates
 * upload → compile → deploy → service availability.
 *
 * In ReportPortal each factory-created instance becomes its own test named
 * {@code testPreconfigProject[<repo>/<module>]}.
 */
public class TestPreconfigProjects extends AbstractPreconfigProjectsApi {

    public TestPreconfigProjects(PreconfigProject project) {
        super(project);
    }

    @Factory
    public static Object[] factory() {
        PreconfigSourcesService sources = new PreconfigSourcesService();
        sources.syncRepositories();
        List<PreconfigProject> projects = sources.discoverProjects();
        return projects.stream()
                .map(p -> (Object) new TestPreconfigProjects(p))
                .toArray();
    }
}
