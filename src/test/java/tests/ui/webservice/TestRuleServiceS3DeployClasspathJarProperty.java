package tests.ui.webservice;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerPool;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import configuration.network.NetworkPool;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import domain.ui.webservice.pages.ServicePage;
import helpers.service.DeployInfrastructureService;
import helpers.utils.StringUtil;
import helpers.utils.WaitUtil;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class TestRuleServiceS3DeployClasspathJarProperty extends BaseTest {

    private static final String PROJECT_NAME = "Example 3 - Auto Policy Calculation";
    private static final String DATASOURCE_JAR_CONTAINER_PATH = "/opt/openl/app/webapps/ROOT/WEB-INF/lib/Example3-datasource.jar";
    private static final Path TEST_DATA_DIR = Paths.get("src/test/resources/test_data/TestRuleServiceS3DeployClasspathJarProperty");
    private static final Path DATASOURCE_JAR_V1 = TEST_DATA_DIR.resolve("Example3-datasource1.jar");
    private static final Path DATASOURCE_JAR_V2 = TEST_DATA_DIR.resolve("Example3-datasource2.jar");
    private static final String VERSION_1 = "1";
    private static final String VERSION_2 = "2";

    private static final Map<String, String> additionalContainerConfig = new HashMap<>();
    private static final Map<String, String> additionalContainerFiles = new HashMap<>();

    private DeployInfrastructureService deployInfra;

    @Override
    @BeforeMethod
    public void beforeMethod(ITestResult result) {
        deployInfra = DeployInfrastructureService.builder()
                .withS3Mock()
                .build();
        deployInfra.start();
        deployInfra.createBucket();

        configureRuleService("NEVER", DATASOURCE_JAR_V1);
        super.beforeMethod(result);
    }

    @Override
    @AfterMethod
    public void afterMethod(ITestResult result) {
        super.afterMethod(result);
        deployInfra.cleanup();
    }

    @Test
    @TestCaseId("IPBQA-32605")
    @Description("Verify S3 repository deployment behavior for ruleservice.datasource.deploy.classpath.jars modes NEVER, IF_ABSENT, ALWAYS")
    @AppContainerConfig(
            startParams = AppContainerStartParameters.EMPTY,
            dockerImageProperty = PropertyNameSpace.WS_DOCKER_IMAGE_NAME
    )
    public void testRuleServiceS3DeployClasspathJarProperty() {
        ServicePage servicePage = openServicePage();
        waitForServicesPageLoaded();

        assertThat(servicePage.getProjectElement(getServiceDisplayName(VERSION_1)).isVisible(1000))
                .as("Project should not appear in Rule Services when classpath jar deploy mode is NEVER")
                .isFalse();
        assertThat(waitForBucketToRemainEmpty(5000))
                .as("S3 bucket should remain empty while classpath jar deploy mode is NEVER")
                .isTrue();

        restartRuleService("IF_ABSENT", DATASOURCE_JAR_V1);
        waitForProjectVersion(getServiceDisplayName(VERSION_1), VERSION_1);
        Map<String, DeployInfrastructureService.ObjectSnapshot> initialSnapshot = getDeployObjects(waitForNonEmptyBucketSnapshot());

        assertThat(deployInfra.bucketExists())
                .as("S3 bucket should exist after the first deployment")
                .isTrue();
        assertThat(deployInfra.isBucketVersioningEnabled())
                .as("OpenL S3 repository should enable bucket versioning")
                .isTrue();

        restartRuleService("IF_ABSENT", DATASOURCE_JAR_V2);
        waitForProjectVersion(getServiceDisplayName(VERSION_1), VERSION_1);
        Map<String, DeployInfrastructureService.ObjectSnapshot> ifAbsentSnapshot = getDeployObjects(waitForNonEmptyBucketSnapshot());

        assertThat(ifAbsentSnapshot.get(getDeploymentObjectKey(VERSION_1)))
                .as("The existing V1 deployment object should stay unchanged when classpath jar deploy mode is IF_ABSENT")
                .isEqualTo(initialSnapshot.get(getDeploymentObjectKey(VERSION_1)));

        restartRuleService("ALWAYS", DATASOURCE_JAR_V2);
        waitForProjectVersion(getServiceDisplayName(VERSION_2), VERSION_2);
        Map<String, DeployInfrastructureService.ObjectSnapshot> alwaysSnapshot = getDeployObjects(waitForNonEmptyBucketSnapshot());

        assertThat(alwaysSnapshot)
                .as("Forced redeploy should produce the V2 deployment object")
                .containsKey(getDeploymentObjectKey(VERSION_2));
        assertThat(hasRedeployedObject(
                ifAbsentSnapshot.get(getDeploymentObjectKey(VERSION_2)),
                alwaysSnapshot.get(getDeploymentObjectKey(VERSION_2))))
                .as("The V2 deployment object should be redeployed when classpath jar deploy mode is ALWAYS")
                .isTrue();
    }

    private void configureRuleService(String classpathJarMode, Path datasourceJar) {
        additionalContainerConfig.clear();
        additionalContainerConfig.putAll(deployInfra.getS3RuleServiceConfig(classpathJarMode));

        additionalContainerFiles.clear();
        additionalContainerFiles.put(datasourceJar.toAbsolutePath().toString(), DATASOURCE_JAR_CONTAINER_PATH);
    }

    private void restartRuleService(String classpathJarMode, Path datasourceJar) {
        configureRuleService(classpathJarMode, datasourceJar);
        AppContainerPool.closeAppContainer();
        AppContainerPool.setAppContainer(
                StringUtil.generateUniqueName("wscontainer"),
                NetworkPool.getNetwork(),
                new HashMap<>(additionalContainerConfig),
                new HashMap<>(additionalContainerFiles),
                ProjectConfiguration.getProperty(PropertyNameSpace.WS_DOCKER_IMAGE_NAME));
    }

    private ServicePage openServicePage() {
        LocalDriverPool.getPage().navigate(LocalDriverPool.getAppUrl());
        return new ServicePage(LocalDriverPool.getPage());
    }

    private void waitForServicesPageLoaded() {
        assertThat(WaitUtil.waitForCondition(
                () -> {
                    LocalDriverPool.getPage().navigate(LocalDriverPool.getAppUrl());
                    ServicePage currentPage = new ServicePage(LocalDriverPool.getPage());
                    return currentPage.getShowAllDeploymentsCheckBox().isVisible(1000);
                },
                30000,
                1000,
                "Waiting for Rule Services page to load"))
                .isTrue();
    }

    private void waitForProjectVersion(String expectedDisplayName, String expectedVersion) {
        assertThat(WaitUtil.waitForCondition(
                () -> {
                    LocalDriverPool.getPage().navigate(LocalDriverPool.getAppUrl());
                    ServicePage currentPage = new ServicePage(LocalDriverPool.getPage());
                    if (!currentPage.getShowAllDeploymentsCheckBox().isVisible(1000)) {
                        return false;
                    }
                    if (!currentPage.getProjectElement(expectedDisplayName).isVisible(1000)) {
                        return false;
                    }
                    String href = currentPage.getProjectTitleLink(expectedDisplayName).getAttribute("href");
                    return href != null && decodeHref(href).contains("version=" + expectedVersion);
                },
                60000,
                2000,
                "Waiting for Rule Services deployment '" + expectedDisplayName + "' to show version " + expectedVersion))
                .isTrue();

        String href = new ServicePage(LocalDriverPool.getPage()).getProjectTitleLink(expectedDisplayName).getAttribute("href");
        assertThat(decodeHref(href))
                .as("Swagger URL should point to version " + expectedVersion)
                .contains("version=" + expectedVersion);
    }

    private String decodeHref(String href) {
        return URLDecoder.decode(href, StandardCharsets.UTF_8);
    }

    private String getServiceDisplayName(String version) {
        return PROJECT_NAME + "_V" + version + "/" + PROJECT_NAME;
    }

    private String getDeploymentObjectKey(String version) {
        return "deploy/" + getServiceDisplayName(version);
    }

    private Map<String, DeployInfrastructureService.ObjectSnapshot> waitForNonEmptyBucketSnapshot() {
        AtomicReference<Map<String, DeployInfrastructureService.ObjectSnapshot>> snapshotRef = new AtomicReference<>(Map.of());

        assertThat(WaitUtil.waitForCondition(
                () -> {
                    Map<String, DeployInfrastructureService.ObjectSnapshot> snapshot = deployInfra.snapshotObjects();
                    if (snapshot.isEmpty()) {
                        return false;
                    }
                    snapshotRef.set(snapshot);
                    return true;
                },
                30000,
                1000,
                "Waiting for S3 deployment objects to appear"))
                .isTrue();

        return snapshotRef.get();
    }

    private boolean waitForBucketToRemainEmpty(long durationMs) {
        long deadline = System.currentTimeMillis() + durationMs;
        while (System.currentTimeMillis() < deadline) {
            if (!deployInfra.isBucketEmpty()) {
                return false;
            }
            WaitUtil.sleep(500, "Verifying that S3 bucket stays empty in NEVER mode");
        }
        return true;
    }

    private Map<String, DeployInfrastructureService.ObjectSnapshot> getDeployObjects(
            Map<String, DeployInfrastructureService.ObjectSnapshot> snapshot) {
        return snapshot.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("deploy/"))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (left, right) -> right,
                        TreeMap::new));
    }

    private boolean hasRedeployedObject(
            DeployInfrastructureService.ObjectSnapshot before,
            DeployInfrastructureService.ObjectSnapshot after) {
        return before == null
                || after != null
                && (after.lastModified().isAfter(before.lastModified())
                || !Objects.equals(after.versionId(), before.versionId())
                || !Objects.equals(after.etag(), before.etag()));
    }
}
