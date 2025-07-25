package tests;

import com.epam.reportportal.service.ReportPortal;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerPool;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DriverPool;
import configuration.driver.PlaywrightDriverPool;
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
 * Updated BaseTest to support both Selenium and Playwright during migration
 * Phase 1: Switch to Playwright for local execution
 */
public abstract class BaseTest {
    protected static final Logger LOGGER = LogManager.getLogger(BaseTest.class);
    
    // Migration flag - set to true to use Playwright, false for Selenium
    private static final boolean USE_PLAYWRIGHT = true; // Phase 1: Enable Playwright

    @BeforeMethod
    public void beforeMethod(ITestResult result) {
        if (USE_PLAYWRIGHT) {
            // PLAYWRIGHT MIGRATION: New Playwright-based setup
            initializePlaywrightTest(result);
        } else {
            // SELENIUM MIGRATION: Original Selenium-based setup (kept for rollback)
            initializeSeleniumTest(result);
        }
        
        // Log application info in one line (works for both modes)
        LOGGER.info(new GetApplicationInfoMethod().getApplicationInfoOneLiner());
    }

    @AfterMethod
    public void afterMethod(ITestResult result) {
        if (USE_PLAYWRIGHT) {
            // PLAYWRIGHT MIGRATION: New Playwright-based cleanup
            cleanupPlaywrightTest(result);
        } else {
            // SELENIUM MIGRATION: Original Selenium-based cleanup (kept for rollback)
            cleanupSeleniumTest(result);
        }
    }
    
    /**
     * PLAYWRIGHT MIGRATION: New Playwright-based test initialization
     */
    private void initializePlaywrightTest(ITestResult result) {
        LOGGER.info("Initializing test with Playwright: {}", 
                   result.getMethod().getMethodName());
        
        // Initialize Playwright (no network needed for Phase 1)
        PlaywrightDriverPool.setPlaywright();
        
        // Set up app container (same as Selenium mode)
        String appContainerName = StringUtil.generateUniqueName("appcontainer");
        Method testMethod = result.getMethod().getConstructorOrMethod().getMethod();
        AppContainerConfig configAnnotation = testMethod.getAnnotation(AppContainerConfig.class);
        Map<String, String> containerConfig;

        if (configAnnotation != null) {
            containerConfig = configAnnotation.startParams().getParameterMap();
            String copyFileFromPath = configAnnotation.copyFileFromPath().isEmpty() ? null : configAnnotation.copyFileFromPath();
            String copyFileToContainerPath = configAnnotation.copyFileToContainerPath().isEmpty() ? null : configAnnotation.copyFileToContainerPath();
            // Phase 1: App container without network dependency
            AppContainerPool.setAppContainer(appContainerName, null, containerConfig, copyFileFromPath, copyFileToContainerPath);
        } else {
            AppContainerPool.setAppContainer(appContainerName, null, AppContainerStartParameters.EMPTY.getParameterMap(), null, null);
        }
    }
    
    /**
     * PLAYWRIGHT MIGRATION: New Playwright-based test cleanup
     */
    private void cleanupPlaywrightTest(ITestResult result) {
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
     * Check if test is running in Playwright mode
     */
    protected boolean isPlaywrightMode() {
        return USE_PLAYWRIGHT;
    }
    
    /**
     * Check if test is running in Selenium mode
     */
    protected boolean isSeleniumMode() {
        return !USE_PLAYWRIGHT;
    }
}
