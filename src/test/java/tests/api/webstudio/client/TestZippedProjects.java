package tests.api.webstudio.client;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.testng.annotations.Factory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Walks the local client_projects tree, groups ZIP archives the same way the
 * legacy UI test did (all zips in a folder whose name ends with "deployment"
 * belong to one group; every other zip is its own group), and creates one
 * test-class instance per group via {@code @Factory}.
 *
 * In ReportPortal each factory-created instance becomes its own &lt;test&gt;
 * with one @Test row per uploaded project inside the group.
 */
public class TestZippedProjects extends AbstractZippedProjectsApi {

    private static final String ROOT_DIR = System.getProperty("zip.projects.root",
            "/Users/dmitryminchuk/Projects/eis/client_projects/customers_projects_test_automation_6.x");
    private static final String EXT = ".zip";
    private static final String DEPLOYMENT_SUFFIX = "deployment";

    public TestZippedProjects(List<File> zipsInGroup, String groupLabel) {
        super(zipsInGroup, groupLabel);
    }

    @Factory
    public static Object[] factory() {
        List<Group> groups = discoverGroups(new File(ROOT_DIR));
        return groups.stream()
                .map(g -> (Object) new TestZippedProjects(g.zips, g.label))
                .toArray();
    }

    private static List<Group> discoverGroups(File rootDir) {
        Collection<File> allZips = FileUtils.listFiles(rootDir, TrueFileFilter.TRUE, TrueFileFilter.TRUE);
        List<File> sortedZips = allZips.stream()
                .filter(f -> f.getAbsolutePath().endsWith(EXT))
                .sorted(Comparator.comparing(File::getAbsolutePath))
                .toList();

        Map<File, Group> deploymentGroups = new LinkedHashMap<>();
        List<Group> singletonGroups = new ArrayList<>();
        Set<File> seenDeploymentFolders = new HashSet<>();

        for (File zip : sortedZips) {
            File parent = zip.getParentFile();
            if (parent.getName().endsWith(DEPLOYMENT_SUFFIX)) {
                if (!seenDeploymentFolders.add(parent)) {
                    continue;
                }
                List<File> zipsInFolder = sortedZips.stream()
                        .filter(z -> z.getParentFile().equals(parent))
                        .toList();
                deploymentGroups.put(parent, new Group(zipsInFolder, label(rootDir, parent, true)));
            } else {
                singletonGroups.add(new Group(List.of(zip), label(rootDir, zip, false)));
            }
        }

        List<Group> result = new ArrayList<>(deploymentGroups.values());
        result.addAll(singletonGroups);
        return result;
    }

    private static String label(File root, File zipOrFolder, boolean isDeploymentFolder) {
        String rel = root.toURI().relativize(zipOrFolder.toURI()).getPath();
        if (rel.endsWith("/")) {
            rel = rel.substring(0, rel.length() - 1);
        }
        if (isDeploymentFolder) {
            return rel + " (deployment group)";
        }
        if (rel.endsWith(EXT)) {
            rel = rel.substring(0, rel.length() - EXT.length());
        }
        return rel;
    }

    private static final class Group {
        final List<File> zips;
        final String label;

        Group(List<File> zips, String label) {
            this.zips = zips;
            this.label = label;
        }
    }
}
