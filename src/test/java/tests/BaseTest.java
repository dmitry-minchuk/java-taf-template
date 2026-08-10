package tests;

import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerPool;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import configuration.driver.DockerDriverPool;
import configuration.driver.ExecutionMode;
import configuration.driver.DriverPool;
import configuration.network.NetworkPool;
import domain.api.GetApplicationInfoMethod;
import helpers.utils.LogsUtil;
import helpers.utils.ReportPortalArtifactUtil;
import helpers.utils.ReportPortalUtil;
import helpers.utils.StringUtil;
import helpers.utils.WaitUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.Network;
import org.testng.ITest;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseTest implements ITest {
    protected static final Logger LOGGER = LogManager.getLogger(BaseTest.class);

    // Thread-safe test name storage for DataProvider support
    private final ThreadLocal<String> testName = new ThreadLocal<>();

    // Single source of truth for mode detection — see ExecutionMode.current()
    private static final ExecutionMode EXECUTION_MODE = ExecutionMode.current();

    @BeforeMethod
    public void beforeMethod(ITestResult result) {
        // Set unique test name for DataProvider iterations (for ReportPortal)
        setUniqueTestName(result);
        ReportPortalArtifactUtil.startTest(result, getTestName());

        switch (EXECUTION_MODE) {
            case PLAYWRIGHT_LOCAL -> {
                // PLAYWRIGHT PHASE 1: Local Playwright execution (no Docker)
                initializePlaywrightLocalTest(result);
            }
            case PLAYWRIGHT_DOCKER -> {
                // PLAYWRIGHT PHASE 3: Docker-aware Playwright execution
                initializePlaywrightDockerTest(result);
            }
        }

        // Log application info in one line (works for both modes)
        LOGGER.info(new GetApplicationInfoMethod().getApplicationInfoOneLiner());
    }

    @AfterMethod
    public void afterMethod(ITestResult result) {
        try {
            switch (EXECUTION_MODE) {
                case PLAYWRIGHT_LOCAL -> {
                    // PLAYWRIGHT PHASE 1: Local Playwright cleanup
                    cleanupPlaywrightLocalTest(result);
                }
                case PLAYWRIGHT_DOCKER -> {
                    // PLAYWRIGHT PHASE 3: Docker-aware Playwright cleanup
                    cleanupPlaywrightDockerTest(result);
                }
            }
        } finally {
            ReportPortalArtifactUtil.finishTest(result);
        }
    }

    private void initializePlaywrightLocalTest(ITestResult result) {
        LOGGER.info("Initializing test with Playwright: {}", result.getMethod().getMethodName());

        // If the test pre-registered a network (e.g. for multi-container scenarios like deploy tests),
        // reuse it so that app container joins the same Docker network as other containers.
        Network network = NetworkPool.getNetwork();

        // Set up app container (with or without network)
        setupAppContainer(result, network);

        // Initialize Playwright through unified interface
        DriverPool.initializePlaywright(network);
    }

    private void initializePlaywrightDockerTest(ITestResult result) {
        LOGGER.info("Initializing test with Playwright Docker: {}", result.getMethod().getMethodName());

        // Reuse network if the test already created one (e.g. for multi-container scenarios),
        // otherwise create a new one for app + Playwright communication.
        Network network = NetworkPool.getNetwork();
        if (network == null) {
            network = Network.newNetwork();
            NetworkPool.setNetwork(network);
        }

        // Set up app container with network
        setupAppContainer(result, network);

        // Wait for Docker DNS to sync container network aliases across the network,
        // so the Playwright container can resolve the app container hostname on start
        waitForNetworkConnectivity();

        // Initialize Playwright through unified interface with network
        DriverPool.initializePlaywright(network);
    }

    private void waitForNetworkConnectivity() {
        if (AppContainerPool.get() == null) {
            LOGGER.warn("No app container found, skipping network connectivity check");
            return;
        }
        // First make sure the container itself is up (fast when it already is), then give Docker DNS
        // a short settle window — alias propagation is not observable from the host, only from a peer
        // container that does not exist yet at this point.
        WaitUtil.waitForCondition(() -> AppContainerPool.get().getAppContainer().isRunning(),
                10_000, 250, "Waiting for the app container to be running before Playwright container starts");
        WaitUtil.sleep(1000, "Letting Docker DNS propagate the app container hostname across the network");
    }

    private void setupAppContainer(ITestResult result, Network network) {
        String appContainerName = StringUtil.generateUniqueName("appcontainer");
        Method testMethod = result.getMethod().getConstructorOrMethod().getMethod();
        AppContainerConfig configAnnotation = testMethod.getAnnotation(AppContainerConfig.class);
        Map<String, String> containerConfig;

        if (configAnnotation != null) {
            containerConfig = configAnnotation.startParams().getParameterMap();
            containerConfig.putAll(additionalContainerConfig());
            containerConfig.forEach((key, value) -> LOGGER.info(String.format("[%s] -> [%s]", key, value)));
            Map<String, String> filesToCopy = new HashMap<>(additionalContainerFiles());
            if (!configAnnotation.copyFileFromPath().isEmpty() && !configAnnotation.copyFileToContainerPath().isEmpty()) {
                filesToCopy.put(configAnnotation.copyFileFromPath(), configAnnotation.copyFileToContainerPath());
            }
            String dockerImageName = ProjectConfiguration.getProperty(configAnnotation.dockerImageProperty());
            AppContainerPool.setAppContainer(appContainerName, network, containerConfig, filesToCopy.isEmpty() ? null : filesToCopy, dockerImageName);
        } else {
            AppContainerPool.setAppContainer(appContainerName, network, AppContainerStartParameters.EMPTY.getParameterMap(), null, ProjectConfiguration.getProperty(PropertyNameSpace.DOCKER_IMAGE_NAME));
        }
    }

    /**
     * Extra app-container configuration merged over the {@code @AppContainerConfig} start parameters.
     * Override in tests that need per-test container settings.
     */
    protected Map<String, String> additionalContainerConfig() {
        return Map.of();
    }

    /**
     * Extra "host path -> container path" files to copy into the app container before start.
     * Override in tests that stage test data into the container.
     */
    protected Map<String, String> additionalContainerFiles() {
        return Map.of();
    }

    private void cleanupPlaywrightLocalTest(ITestResult result) {
        String testName = getTestName();

        if (result.getStatus() == ITestResult.FAILURE) {
            // Enhanced ReportPortal logging with ReportPortalUtil
            ReportPortalUtil.attachScreenshotOnFailure(testName);
            ReportPortalUtil.attachPageContent("Page Content at Failure");
            ReportPortalUtil.attachExecutionInfo();

            // Log application logs (same as Selenium mode)
            File appLog = LogsUtil.saveAppLogs(AppContainerPool.get());
            ReportPortalArtifactUtil.recordAttachment("Application LOG", "INFO", appLog);
            ReportPortalArtifactUtil.emitLog("Application LOG", "INFO", appLog);
        }

        // Close Playwright
        DriverPool.closePlaywright();

        // Close app container
        AppContainerPool.closeAppContainer();

        // Close network if one was pre-registered by the test (e.g. multi-container scenarios)
        if (NetworkPool.getNetwork() != null) {
            NetworkPool.closeNetwork();
        }
    }

    private void cleanupPlaywrightDockerTest(ITestResult result) {
        String testName = getTestName();

        if (result.getStatus() == ITestResult.FAILURE) {
            // Enhanced ReportPortal logging with ReportPortalUtil
            ReportPortalUtil.attachScreenshotOnFailure(testName);
            ReportPortalUtil.attachPageContent("Page Content at Failure");
            ReportPortalUtil.attachExecutionInfo();
            
            // Attach video for failed tests BEFORE closing Playwright (this will close page internally)
            ReportPortalUtil.attachVideoOnFailure(testName);

            // Log application logs (same as Selenium mode)
            File appLog = LogsUtil.saveAppLogs(AppContainerPool.get());
            ReportPortalArtifactUtil.recordAttachment("Application LOG", "INFO", appLog);
            ReportPortalArtifactUtil.emitLog("Application LOG", "INFO", appLog);
        }

        // Close Playwright Docker (page already closed by video attachment if failure occurred)
        DockerDriverPool.closePlaywrightDocker();

        // Close app container and network
        AppContainerPool.closeAppContainer();
        NetworkPool.closeNetwork();

        // Wait for Docker daemon to complete resource cleanup before next test starts
        WaitUtil.sleep(2000, "Waiting for Docker daemon to complete resource cleanup");
    }

    // ========== DataProvider Support for Unique Test Names in ReportPortal ==========

    /**
     * Sets unique test name for DataProvider iterations to ensure they appear with different names in ReportPortal.
     * For tests with DataProvider: testName[param1, param2, ...]
     * For tests without DataProvider: testName (unchanged)
     */
    private void setUniqueTestName(ITestResult result) {
        Object[] parameters = result.getParameters();
        String methodName = result.getMethod().getMethodName();

        if (parameters != null && parameters.length > 0) {
            // Test has DataProvider parameters - generate unique name
            String uniqueName = generateTestNameWithParameters(methodName, parameters);
            testName.set(uniqueName);
            LOGGER.debug("Set unique test name for DataProvider: {}", uniqueName);
        } else {
            // Test without parameters - use standard method name
            testName.set(methodName);
        }
    }

    @Override
    public String getTestName() {
        return testName.get() != null ? testName.get() : "unknown-test";
    }

    /**
     * Generates unique test name by appending sanitized parameters.
     * Format: methodName[param1, param2, param3]
     */
    private String generateTestNameWithParameters(String methodName, Object[] params) {
        StringBuilder name = new StringBuilder(methodName).append("[");

        for (int i = 0; i < params.length; i++) {
            if (i > 0) {
                name.append(", ");
            }

            if (params[i] != null) {
                name.append(sanitizeParameter(params[i].toString()));
            } else {
                name.append("null");
            }
        }

        return name.append("]").toString();
    }

    /**
     * Sanitizes parameter value for display in test name.
     * - Extracts filename from file paths
     * - Removes .zip extension
     * - Truncates long strings
     */
    private String sanitizeParameter(String paramValue) {
        String sanitized = paramValue;

        // Extract filename from file paths
        if (sanitized.contains("/") || sanitized.contains("\\")) {
            String[] parts = sanitized.split("[/\\\\]");
            sanitized = parts[parts.length - 1];
        }

        // Remove .zip extension for cleaner display
        if (sanitized.endsWith(".zip")) {
            sanitized = sanitized.substring(0, sanitized.length() - 4);
        }

        // Truncate long strings (max 50 characters)
        if (sanitized.length() > 50) {
            sanitized = sanitized.substring(0, 47) + "...";
        }

        return sanitized;
    }

}
