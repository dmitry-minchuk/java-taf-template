package tests;

import com.epam.reportportal.service.ReportPortal;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerPool;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DockerDriverPool;
import configuration.driver.LocalDriverPool;
import configuration.network.NetworkPool;
import domain.api.GetApplicationInfoMethod;
import helpers.utils.LogsUtil;
import helpers.utils.ReportPortalUtil;
import helpers.utils.StringUtil;
import helpers.utils.WaitUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.containers.Network;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseTest {
    protected static final Logger LOGGER = LogManager.getLogger(BaseTest.class);
    // Support multiple Playwright execution modes
    private static final ExecutionMode EXECUTION_MODE = getExecutionMode();

    // Execution modes for Playwright
    public enum ExecutionMode {
        PLAYWRIGHT_LOCAL,   // Phase 1: Playwright local execution
        PLAYWRIGHT_DOCKER   // Phase 3: Playwright with Docker integration
    }

    private static ExecutionMode getExecutionMode() {
        String mode = System.getProperty("execution.mode", "PLAYWRIGHT_LOCAL");

        try {
            ExecutionMode execMode = ExecutionMode.valueOf(mode.toUpperCase());
            LOGGER.info("Using execution mode: {}", execMode);
            return execMode;
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Unknown execution mode '{}', defaulting to PLAYWRIGHT_LOCAL", mode);
            return ExecutionMode.PLAYWRIGHT_LOCAL;
        }
    }

    @BeforeMethod
    public void beforeMethod(ITestResult result) {
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
    }

    private void initializePlaywrightLocalTest(ITestResult result) {
        LOGGER.info("Initializing test with Playwright: {}", result.getMethod().getMethodName());

        // Set up app container first
        setupAppContainer(result, null);

        // Initialize Playwright through unified interface (no network needed for Phase 1)
        LocalDriverPool.initializePlaywright(null);
    }

    private void initializePlaywrightDockerTest(ITestResult result) {
        LOGGER.info("Initializing test with Playwright Docker: {}", result.getMethod().getMethodName());

        // Create Docker network for container communication
        Network network = Network.newNetwork();
        NetworkPool.setNetwork(network);

        // Set up app container with network
        setupAppContainer(result, network);

        // CRITICAL: Wait for Docker DNS to sync container network aliases across the network
        // This ensures that when Playwright container starts, it can resolve the app container hostname
        waitForNetworkConnectivity(result);

        // Initialize Playwright through unified interface with network
        LocalDriverPool.initializePlaywright(network);
    }

    private void waitForNetworkConnectivity(ITestResult result) {
        if (AppContainerPool.get() == null) {
            LOGGER.warn("No app container found, skipping network connectivity check");
            return;
        }
        WaitUtil.sleep(1000, "Initial wait for DNS propagation");
    }

    private void setupAppContainer(ITestResult result, Network network) {
        String appContainerName = StringUtil.generateUniqueName("appcontainer");
        Method testMethod = result.getMethod().getConstructorOrMethod().getMethod();
        AppContainerConfig configAnnotation = testMethod.getAnnotation(AppContainerConfig.class);
        Map<String, String> containerConfig;

        if (configAnnotation != null) {
            containerConfig = configAnnotation.startParams().getParameterMap();
            containerConfig.putAll(getAdditionalContainerConfig(testMethod));
            containerConfig.forEach((key, value) -> LOGGER.info(String.format("[%s] -> [%s]", key, value)));
            String copyFileFromPath = configAnnotation.copyFileFromPath().isEmpty() ? null : configAnnotation.copyFileFromPath();
            String copyFileToContainerPath = configAnnotation.copyFileToContainerPath().isEmpty() ? null : configAnnotation.copyFileToContainerPath();
            AppContainerPool.setAppContainer(appContainerName, network, containerConfig, copyFileFromPath, copyFileToContainerPath);
        } else {
            AppContainerPool.setAppContainer(appContainerName, network, AppContainerStartParameters.EMPTY.getParameterMap(), null, null);
        }
    }

    // This is needed for container extra configuration and not breaking annotation config logic
    private Map<String, String> getAdditionalContainerConfig(Method testMethod) {
        Field additionalConfig;
        try {
            additionalConfig = testMethod.getDeclaringClass().getDeclaredField("additionalContainerConfig");
            additionalConfig.setAccessible(true);  // Allow access to private field
        } catch (NoSuchFieldException e) {
            return new HashMap<>();  // Return empty map if field doesn't exist
        }

        Object raw;
        try {
            raw = additionalConfig.get(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return getStringStringMap(raw);
    }

    // This is needed for container extra configuration and not breaking annotation config logic
    private static @NotNull Map<String, String> getStringStringMap(Object raw) {
        if (!(raw instanceof Map<?, ?> map)) {
            throw new IllegalStateException("additionalContainerConfig must be a Map<String,String>, but was " + (raw == null ? "null" : raw.getClass()));
        }

        Map<String, String> result = new HashMap<>();
        for (Map.Entry<?, ?> e : map.entrySet()) {
            if (!(e.getKey() instanceof String key)) {
                throw new IllegalStateException("Key must be String, but was " + e.getKey());
            }
            if (!(e.getValue() instanceof String value)) {
                throw new IllegalStateException("Value must be String, but was " + e.getValue());
            }
            result.put(key, value);
        }
        return result;
    }

    private void cleanupPlaywrightLocalTest(ITestResult result) {
        String testName = result.getMethod().getMethodName();

        if (result.getStatus() == ITestResult.FAILURE) {
            // Enhanced ReportPortal logging with ReportPortalUtil
            ReportPortalUtil.attachScreenshotOnFailure(testName);
            ReportPortalUtil.attachPageContent("Page Content at Failure");
            ReportPortalUtil.attachExecutionInfo();

            // Log application logs (same as Selenium mode)
            ReportPortal.emitLog("Application LOG", "INFO", new Date(), LogsUtil.saveAppLogs(AppContainerPool.get()));
        }

        // Close Playwright
        LocalDriverPool.closePlaywright();

        // Close app container
        AppContainerPool.closeAppContainer();
    }

    private void cleanupPlaywrightDockerTest(ITestResult result) {
        String testName = result.getMethod().getMethodName();

        if (result.getStatus() == ITestResult.FAILURE) {
            // Enhanced ReportPortal logging with ReportPortalUtil
            ReportPortalUtil.attachScreenshotOnFailure(testName);
            ReportPortalUtil.attachPageContent("Page Content at Failure");
            ReportPortalUtil.attachExecutionInfo();
            
            // Attach video for failed tests BEFORE closing Playwright (this will close page internally)
            ReportPortalUtil.attachVideoOnFailure(testName);

            // Log application logs (same as Selenium mode)
            ReportPortal.emitLog("Application LOG", "INFO", new Date(), LogsUtil.saveAppLogs(AppContainerPool.get()));
        }

        // Close Playwright Docker (page already closed by video attachment if failure occurred)
        DockerDriverPool.closePlaywrightDocker();

        // Close app container and network
        AppContainerPool.closeAppContainer();
        NetworkPool.closeNetwork();
    }


}
