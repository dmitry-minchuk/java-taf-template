package tests.api.webstudio.client.preconfig;

import configuration.annotations.LocalOnly;
import tests.api.webstudio.client.base.AbstractPreconfigProjectsApi;

import helpers.service.PreconfigSourcesService;
import helpers.service.PreconfigSourcesService.PreconfigProject;
import org.testng.annotations.Factory;

import java.util.List;

@LocalOnly(reason = "Needs JDK 25 and the preconfigured project set, see studio_preconfig_projects_regression.xml")
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
