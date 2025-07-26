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

/**
 * Enhanced BaseTest to support multiple execution modes
 * Phase 3: Selenium, Playwright Local, and Playwright Docker execution
 */
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
    
    /**
     * Determine execution mode from system properties
     * Phase 3: Support environment-based mode selection
     */
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
    
    /**
     * PLAYWRIGHT PHASE 1: Local Playwright test initialization (no Docker)
     */
    private void initializePlaywrightLocalTest(ITestResult result) {
        LOGGER.info("Initializing test with Playwright: {}", 
                   result.getMethod().getMethodName());
        
        // Set up app container first
        setupAppContainer(result, null);
        
        // Initialize Playwright through unified interface (no network needed for Phase 1)
        PlaywrightDriverPool.initializePlaywright(null);
    }
    
    /**
     * PLAYWRIGHT PHASE 3: Docker-aware Playwright test initialization
     */
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
    
    /**
     * Shared app container setup for Playwright modes
     * Phase 3: Centralized container configuration logic
     */
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
    
    /**
     * PLAYWRIGHT PHASE 1: Local Playwright test cleanup (no Docker)
     */
    private void cleanupPlaywrightLocalTest(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            // Take screenshot with Playwright
            byte[] screenshot = PlaywrightDriverPool.takeScreenshot();
            if (screenshot != null) {
                // Create temporary file for ReportPortal
                File tempScreenshot = createTempScreenshotFile(screenshot);
                if (tempScreenshot != null) {
                    ReportPortal.emitLog("Test Failure Screenshot", "INFO", new Date(), tempScreenshot);
                }
            }
            
            // Log application logs (same as Selenium mode)
            ReportPortal.emitLog("Application LOG", "INFO", new Date(), LogsUtil.saveAppLogs(AppContainerPool.get()));
        }
        
        // Close Playwright
        PlaywrightDriverPool.closePlaywright();
        
        // Close app container
        AppContainerPool.closeAppContainer();
    }
    
    /**
     * PLAYWRIGHT PHASE 3: Docker-aware Playwright test cleanup
     */
    private void cleanupPlaywrightDockerTest(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            // Take screenshot with Playwright Docker
            byte[] screenshot = PlaywrightDockerDriverPool.takeScreenshot();
            if (screenshot != null) {
                // Create temporary file for ReportPortal
                File tempScreenshot = createTempScreenshotFile(screenshot);
                if (tempScreenshot != null) {
                    ReportPortal.emitLog("Test Failure Screenshot", "INFO", new Date(), tempScreenshot);
                }
            }
            
            // Log application logs (same as Selenium mode)
            ReportPortal.emitLog("Application LOG", "INFO", new Date(), LogsUtil.saveAppLogs(AppContainerPool.get()));
        }
        
        // Close Playwright Docker
        PlaywrightDockerDriverPool.closePlaywrightDocker();
        
        // Close app container and network
        AppContainerPool.closeAppContainer();
        NetworkPool.closeNetwork();
    }
    
    /**
     * SELENIUM MIGRATION: Original Selenium-based test initialization (kept for rollback)
     */
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
    
    /**
     * SELENIUM MIGRATION: Original Selenium-based test cleanup (kept for rollback)
     */
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
    
    /**
     * Create temporary file from Playwright screenshot bytes
     */
    private File createTempScreenshotFile(byte[] screenshotBytes) {
        try {
            File tempFile = File.createTempFile("playwright-screenshot-", ".png");
            tempFile.deleteOnExit();
            java.nio.file.Files.write(tempFile.toPath(), screenshotBytes);
            return tempFile;
        } catch (Exception e) {
            LOGGER.error("Failed to create temporary screenshot file: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Check if test is running in Playwright mode (local or Docker)
     */
    protected boolean isPlaywrightMode() {
        return EXECUTION_MODE == ExecutionMode.PLAYWRIGHT_LOCAL || EXECUTION_MODE == ExecutionMode.PLAYWRIGHT_DOCKER;
    }
    
    /**
     * Check if test is running in Selenium mode
     */
    protected boolean isSeleniumMode() {
        return EXECUTION_MODE == ExecutionMode.SELENIUM;
    }
    
    /**
     * Check if test is running in Playwright Docker mode
     */
    protected boolean isPlaywrightDockerMode() {
        return EXECUTION_MODE == ExecutionMode.PLAYWRIGHT_DOCKER;
    }
}
