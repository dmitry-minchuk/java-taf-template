package tests;

import com.epam.reportportal.service.ReportPortal;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerPool;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DriverPool;
import configuration.driver.PlaywrightDriverPool;
import configuration.driver.PlaywrightDockerDriverPool;
import configuration.network.NetworkPool;
import domain.api.GetApplicationInfoMethod;
import helpers.utils.LogsUtil;
import helpers.utils.PlaywrightReportPortalUtil;
import helpers.utils.ScreenshotUtil;
import helpers.utils.StringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.Network;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;

public abstract class BaseTest {
    protected static final Logger LOGGER = LogManager.getLogger(BaseTest.class);
    
    // Execution modes for migration phases
    public enum ExecutionMode {
        SELENIUM,           // Original Selenium with Docker containers
        PLAYWRIGHT_LOCAL,   // Phase 1: Playwright local execution
        PLAYWRIGHT_DOCKER   // Phase 3: Playwright with Docker integration
    }
    
    // Phase 3: Support multiple execution modes
    private static final ExecutionMode EXECUTION_MODE = getExecutionMode();

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
            case SELENIUM -> {
                // SELENIUM: Original Docker-based Selenium execution
                initializeSeleniumTest(result);
            }
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
            case SELENIUM -> {
                // SELENIUM: Original Docker-based Selenium cleanup
                cleanupSeleniumTest(result);
            }
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
        LOGGER.info("Initializing test with Playwright: {}", 
                   result.getMethod().getMethodName());
        
        // Set up app container first
        setupAppContainer(result, null);
        
        // Initialize Playwright through unified interface (no network needed for Phase 1)
        PlaywrightDriverPool.initializePlaywright(null);
    }

    private void initializePlaywrightDockerTest(ITestResult result) {
        LOGGER.info("Initializing test with Playwright Docker: {}", 
                   result.getMethod().getMethodName());
        
        // Create Docker network for container communication
        Network network = Network.newNetwork();
        NetworkPool.setNetwork(network);
        
        // Set up app container with network
        setupAppContainer(result, network);
        
        // Initialize Playwright through unified interface with network
        PlaywrightDriverPool.initializePlaywright(network);
    }

    private void setupAppContainer(ITestResult result, Network network) {
        String appContainerName = StringUtil.generateUniqueName("appcontainer");
        Method testMethod = result.getMethod().getConstructorOrMethod().getMethod();
        AppContainerConfig configAnnotation = testMethod.getAnnotation(AppContainerConfig.class);
        Map<String, String> containerConfig;

        if (configAnnotation != null) {
            containerConfig = configAnnotation.startParams().getParameterMap();
            String copyFileFromPath = configAnnotation.copyFileFromPath().isEmpty() ? null : configAnnotation.copyFileFromPath();
            String copyFileToContainerPath = configAnnotation.copyFileToContainerPath().isEmpty() ? null : configAnnotation.copyFileToContainerPath();
            AppContainerPool.setAppContainer(appContainerName, network, containerConfig, copyFileFromPath, copyFileToContainerPath);
        } else {
            AppContainerPool.setAppContainer(appContainerName, network, AppContainerStartParameters.EMPTY.getParameterMap(), null, null);
        }
    }

    private void cleanupPlaywrightLocalTest(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        
        if (result.getStatus() == ITestResult.FAILURE) {
            // Enhanced ReportPortal logging with PlaywrightReportPortalUtil
            PlaywrightReportPortalUtil.attachScreenshotOnFailure(testName);
            PlaywrightReportPortalUtil.attachPageContent("Page Content at Failure");
            PlaywrightReportPortalUtil.attachExecutionInfo();
            
            // Log application logs (same as Selenium mode)
            ReportPortal.emitLog("Application LOG", "INFO", new Date(), LogsUtil.saveAppLogs(AppContainerPool.get()));
        }
        
        // Close Playwright
        PlaywrightDriverPool.closePlaywright();
        
        // Close app container
        AppContainerPool.closeAppContainer();
    }

    private void cleanupPlaywrightDockerTest(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        
        if (result.getStatus() == ITestResult.FAILURE) {
            // Enhanced ReportPortal logging with PlaywrightReportPortalUtil
            PlaywrightReportPortalUtil.attachScreenshotOnFailure(testName);
            PlaywrightReportPortalUtil.attachPageContent("Page Content at Failure");
            PlaywrightReportPortalUtil.attachExecutionInfo();
            
            // Log application logs (same as Selenium mode)
            ReportPortal.emitLog("Application LOG", "INFO", new Date(), LogsUtil.saveAppLogs(AppContainerPool.get()));
        }
        
        // Close Playwright Docker
        PlaywrightDockerDriverPool.closePlaywrightDocker();
        
        // Close app container and network
        AppContainerPool.closeAppContainer();
        NetworkPool.closeNetwork();
    }

    private void initializeSeleniumTest(ITestResult result) {
        LOGGER.info("Initializing test with Selenium: {}", 
                   result.getMethod().getMethodName());
        
        Network network = Network.newNetwork();
        DriverPool.setDriver(network);
        NetworkPool.setNetwork(network);
        String appContainerName = StringUtil.generateUniqueName("appcontainer");
        Method testMethod = result.getMethod().getConstructorOrMethod().getMethod();
        AppContainerConfig configAnnotation = testMethod.getAnnotation(AppContainerConfig.class);
        Map<String, String> containerConfig;

        if (configAnnotation != null) {
            containerConfig = configAnnotation.startParams().getParameterMap();
            String copyFileFromPath = configAnnotation.copyFileFromPath().isEmpty() ? null : configAnnotation.copyFileFromPath();
            String copyFileToContainerPath = configAnnotation.copyFileToContainerPath().isEmpty() ? null : configAnnotation.copyFileToContainerPath();
            AppContainerPool.setAppContainer(appContainerName, network, containerConfig, copyFileFromPath, copyFileToContainerPath);
        } else {
            AppContainerPool.setAppContainer(appContainerName, network, AppContainerStartParameters.EMPTY.getParameterMap(), null, null);
        }
    }

    private void cleanupSeleniumTest(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            File screenShot = ScreenshotUtil.takeAndSaveScreenshot(DriverPool.getDriverContainer());
            ReportPortal.emitLog("Application LOG", "INFO", new Date(), LogsUtil.saveAppLogs(AppContainerPool.get()));
            if (screenShot != null)
                ReportPortal.emitLog("Test Failure Screenshot", "INFO", new Date(), screenShot);
        }
        DriverPool.closeDriver();
        AppContainerPool.closeAppContainer();
        NetworkPool.closeNetwork();
    }
}
