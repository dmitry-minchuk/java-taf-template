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

    private final ThreadLocal<String> testName = new ThreadLocal<>();

    private static final ExecutionMode EXECUTION_MODE = ExecutionMode.current();

    @BeforeMethod
    public void beforeMethod(ITestResult result) {
        setUniqueTestName(result);
        ReportPortalArtifactUtil.startTest(result, getTestName());

        switch (EXECUTION_MODE) {
            case PLAYWRIGHT_LOCAL -> initializePlaywrightLocalTest(result);
            case PLAYWRIGHT_DOCKER -> initializePlaywrightDockerTest(result);
        }

        LOGGER.info(new GetApplicationInfoMethod().getApplicationInfoOneLiner());
    }

    @AfterMethod
    public void afterMethod(ITestResult result) {
        try {
            switch (EXECUTION_MODE) {
                case PLAYWRIGHT_LOCAL -> cleanupPlaywrightLocalTest(result);
                case PLAYWRIGHT_DOCKER -> cleanupPlaywrightDockerTest(result);
            }
        } finally {
            ReportPortalArtifactUtil.finishTest(result);
        }
    }

    private void initializePlaywrightLocalTest(ITestResult result) {
        LOGGER.info("Initializing test with Playwright: {}", result.getMethod().getMethodName());

        Network network = ensureNetworkRegistered();

        setupAppContainer(result, network);

        DriverPool.initializePlaywright(network);
    }

    private void initializePlaywrightDockerTest(ITestResult result) {
        LOGGER.info("Initializing test with Playwright Docker: {}", result.getMethod().getMethodName());

        Network network = ensureNetworkRegistered();

        setupAppContainer(result, network);

        waitForNetworkConnectivity();

        DriverPool.initializePlaywright(network);
    }

    private Network ensureNetworkRegistered() {
        Network network = NetworkPool.getNetwork();
        if (network == null) {
            network = Network.newNetwork();
            NetworkPool.setNetwork(network);
        }
        return network;
    }

    private void waitForNetworkConnectivity() {
        if (AppContainerPool.get() == null) {
            LOGGER.warn("No app container found, skipping network connectivity check");
            return;
        }
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

    protected Map<String, String> additionalContainerConfig() {
        return Map.of();
    }

    protected Map<String, String> additionalContainerFiles() {
        return Map.of();
    }

    private void cleanupPlaywrightLocalTest(ITestResult result) {
        String testName = getTestName();

        if (result.getStatus() == ITestResult.FAILURE) {
            ReportPortalUtil.attachScreenshotOnFailure(testName);
            ReportPortalUtil.attachPageContent("Page Content at Failure");
            ReportPortalUtil.attachExecutionInfo();

            File appLog = LogsUtil.saveAppLogs(AppContainerPool.get());
            ReportPortalArtifactUtil.recordAttachment("Application LOG", "INFO", appLog);
            ReportPortalArtifactUtil.emitLog("Application LOG", "INFO", appLog);
        }

        DriverPool.closePlaywright();

        AppContainerPool.closeAppContainer();

        if (NetworkPool.getNetwork() != null) {
            NetworkPool.closeNetwork();
        }
    }

    private void cleanupPlaywrightDockerTest(ITestResult result) {
        String testName = getTestName();

        if (result.getStatus() == ITestResult.FAILURE) {
            ReportPortalUtil.attachScreenshotOnFailure(testName);
            ReportPortalUtil.attachPageContent("Page Content at Failure");
            ReportPortalUtil.attachExecutionInfo();

            ReportPortalUtil.attachVideoOnFailure(testName);

            File appLog = LogsUtil.saveAppLogs(AppContainerPool.get());
            ReportPortalArtifactUtil.recordAttachment("Application LOG", "INFO", appLog);
            ReportPortalArtifactUtil.emitLog("Application LOG", "INFO", appLog);
        }

        DockerDriverPool.closePlaywrightDocker();

        AppContainerPool.closeAppContainer();
        NetworkPool.closeNetwork();

        WaitUtil.sleep(2000, "Waiting for Docker daemon to complete resource cleanup");
    }

    private void setUniqueTestName(ITestResult result) {
        Object[] parameters = result.getParameters();
        String methodName = result.getMethod().getMethodName();

        if (parameters != null && parameters.length > 0) {
            String uniqueName = generateTestNameWithParameters(methodName, parameters);
            testName.set(uniqueName);
            LOGGER.debug("Set unique test name for DataProvider: {}", uniqueName);
        } else {
            testName.set(methodName);
        }
    }

    @Override
    public String getTestName() {
        return testName.get() != null ? testName.get() : "unknown-test";
    }

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

    private String sanitizeParameter(String paramValue) {
        String sanitized = paramValue;

        if (sanitized.contains("/") || sanitized.contains("\\")) {
            String[] parts = sanitized.split("[/\\\\]");
            sanitized = parts[parts.length - 1];
        }

        if (sanitized.endsWith(".zip")) {
            sanitized = sanitized.substring(0, sanitized.length() - 4);
        }

        if (sanitized.length() > 50) {
            sanitized = sanitized.substring(0, 47) + "...";
        }

        return sanitized;
    }

}
