package helpers.utils;

import com.epam.reportportal.service.ReportPortal;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.ScreenshotType;
import configuration.driver.PlaywrightDriverPool;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class PlaywrightReportPortalUtil {
    
    private static final Logger LOGGER = LogManager.getLogger(PlaywrightReportPortalUtil.class);
    
    private static final String SCREENSHOT_DIR = "target/screenshots";
    private static final String VIDEO_DIR = "target/videos"; 
    private static final String DOWNLOAD_DIR = "target/downloads";
    
    static {
        createDirectories();
    }
    
    private static void createDirectories() {
        try {
            Files.createDirectories(Paths.get(SCREENSHOT_DIR));
            Files.createDirectories(Paths.get(VIDEO_DIR));
            Files.createDirectories(Paths.get(DOWNLOAD_DIR));
        } catch (IOException e) {
            LOGGER.warn("Failed to create media directories: {}", e.getMessage());
        }
    }
    
    public static void attachScreenshotOnFailure(String testName) {
        attachScreenshotOnFailure(testName, "Test Failure Screenshot");
    }
    
    public static void attachScreenshotOnFailure(String testName, String description) {
        try {
            byte[] screenshotBytes = PlaywrightDriverPool.takeScreenshot();
            if (screenshotBytes != null) {
                File screenshotFile = saveScreenshotToFile(screenshotBytes, testName);
                if (screenshotFile != null) {
                    ReportPortal.emitLog(description, "ERROR", new Date(), screenshotFile);
                    LOGGER.info("Screenshot attached to ReportPortal: {}", screenshotFile.getName());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to attach screenshot to ReportPortal: {}", e.getMessage());
        }
    }
    
    public static File captureScreenshot(String testName) {
        return captureScreenshot(testName, false);
    }
    
    public static File captureScreenshot(String testName, boolean fullPage) {
        try {
            Page page = PlaywrightDriverPool.getPage();
            
            Page.ScreenshotOptions options = new Page.ScreenshotOptions()
                .setType(ScreenshotType.PNG)
                .setFullPage(fullPage);
            
            byte[] screenshotBytes = page.screenshot(options);
            return saveScreenshotToFile(screenshotBytes, testName);
            
        } catch (Exception e) {
            LOGGER.error("Failed to capture screenshot: {}", e.getMessage());
            return null;
        }
    }
    
    public static void attachCustomScreenshot(String testName, String description) {
        File screenshotFile = captureScreenshot(testName, true);
        if (screenshotFile != null) {
            ReportPortal.emitLog(description, "INFO", new Date(), screenshotFile);
            LOGGER.info("Custom screenshot attached to ReportPortal: {}", screenshotFile.getName());
        }
    }
    
    // Get video as byte array using Playwright Page.video() API - works in memory without files
    public static byte[] getVideoBytes(String testName) {
        try {
            if (!isVideoRecordingEnabled()) {
                LOGGER.debug("Video recording is not enabled");
                return null;
            }
            
            Page page = PlaywrightDriverPool.getPage();
            
            // Check if video is available (only works if BrowserContext was configured with recordVideoDir)
            if (page.video() == null) {
                LOGGER.warn("No video recording found for page - ensure BrowserContext was configured with recordVideoDir");
                return null;
            }
            
            LOGGER.info("Retrieving video bytes for test: {}", testName);
            
            // Store video reference BEFORE closing page - REQUIRED by Playwright
            com.microsoft.playwright.Video video = page.video();
            
            // Close page to finalize video recording - REQUIRED by Playwright before saveAs
            page.close();
            LOGGER.debug("Page closed to finalize video recording for test: {}", testName);
            
            // Create temporary file for video extraction, then read as bytes
            File tempVideoFile = null;
            try {
                tempVideoFile = File.createTempFile("playwright_video_" + testName, ".webm");
                tempVideoFile.deleteOnExit();
                
                // Save video to temporary file - now page is closed so this should work
                video.saveAs(Paths.get(tempVideoFile.getAbsolutePath()));
                
                // Read file as bytes
                byte[] videoBytes = Files.readAllBytes(tempVideoFile.toPath());
                LOGGER.info("Successfully retrieved {} bytes of video data for test: {}", videoBytes.length, testName);
                return videoBytes;
                
            } finally {
                // Clean up temporary file
                if (tempVideoFile != null && tempVideoFile.exists()) {
                    tempVideoFile.delete();
                }
            }
            
        } catch (Exception e) {
            LOGGER.error("Failed to get video bytes for test {}: {}", testName, e.getMessage(), e);
            return null;
        }
    }
    
    // Attach video to ReportPortal using byte array (no temporary files on Jenkins)
    public static void attachVideoOnFailure(String testName) {
        try {
            if (!isVideoRecordingEnabled()) {
                LOGGER.debug("Video recording is not enabled, skipping video attachment");
                return;
            }
            
            byte[] videoBytes = getVideoBytes(testName);
            if (videoBytes != null && videoBytes.length > 0) {
                // Create temporary file for ReportPortal attachment
                File tempFile = File.createTempFile("test_failure_video_" + testName, ".webm");
                tempFile.deleteOnExit();
                
                try {
                    Files.write(tempFile.toPath(), videoBytes);
                    ReportPortal.emitLog("Test Failure Video", "ERROR", new Date(), tempFile);
                    LOGGER.info("Video attached to ReportPortal for test: {} (size: {} bytes)", testName, videoBytes.length);
                } finally {
                    // Clean up temp file after ReportPortal processes it
                    tempFile.delete();
                }
            } else {
                LOGGER.warn("No video data available to attach for test: {}", testName);
            }
            
        } catch (Exception e) {
            LOGGER.error("Failed to attach video to ReportPortal for test {}: {}", testName, e.getMessage(), e);
        }
    }
    
    public static void attachPageContent(String description) {
        try {
            Page page = PlaywrightDriverPool.getPage();
            String content = page.content();
            
            File tempFile = createTempFile("page-content-", ".html", content);
            if (tempFile != null) {
                ReportPortal.emitLog(description, "INFO", new Date(), tempFile);
                LOGGER.info("Page content attached to ReportPortal: {}", tempFile.getName());
            }
            
        } catch (Exception e) {
            LOGGER.error("Failed to attach page content: {}", e.getMessage());
        }
    }
    
    public static void attachBrowserLogs(String description) {
        try {
            // Note: Playwright doesn't have direct console log access like Selenium
            // This is a placeholder for browser log collection implementation
            String logs = "Browser logs collection not yet implemented for Playwright";
            
            File tempFile = createTempFile("browser-logs-", ".txt", logs);
            if (tempFile != null) {
                ReportPortal.emitLog(description, "INFO", new Date(), tempFile);
                LOGGER.info("Browser logs attached to ReportPortal: {}", tempFile.getName());
            }
            
        } catch (Exception e) {
            LOGGER.error("Failed to attach browser logs: {}", e.getMessage());
        }
    }
    
    public static void attachDownloadedFile(File downloadedFile, String description) {
        if (downloadedFile != null && downloadedFile.exists()) {
            ReportPortal.emitLog(description, "INFO", new Date(), downloadedFile);
            LOGGER.info("Downloaded file attached to ReportPortal: {}", downloadedFile.getName());
        } else {
            LOGGER.warn("Cannot attach downloaded file - file does not exist: {}", 
                downloadedFile != null ? downloadedFile.getPath() : "null");
        }
    }
    
    public static void attachTestData(String fileName, String description) {
        try {
            String filePath = TestDataUtil.getFilePathFromResources(fileName);
            File testDataFile = new File(filePath);
            
            if (testDataFile.exists()) {
                ReportPortal.emitLog(description, "INFO", new Date(), testDataFile);
                LOGGER.info("Test data file attached to ReportPortal: {}", testDataFile.getName());
            } else {
                LOGGER.warn("Test data file not found: {}", filePath);
            }
            
        } catch (Exception e) {
            LOGGER.error("Failed to attach test data file: {}", e.getMessage());
        }
    }
    
    public static void attachExecutionInfo() {
        try {
            PlaywrightDriverPool.ExecutionMode mode = PlaywrightDriverPool.getCurrentExecutionMode();
            String debugInfo = PlaywrightDriverPool.getDebugInfo();
            
            String executionInfo = String.format(
                "Execution Mode: %s%n%n%s", 
                mode, 
                debugInfo
            );
            
            File tempFile = createTempFile("execution-info-", ".txt", executionInfo);
            if (tempFile != null) {
                ReportPortal.emitLog("Test Execution Information", "INFO", new Date(), tempFile);
                LOGGER.info("Execution info attached to ReportPortal: {}", tempFile.getName());
            }
            
        } catch (Exception e) {
            LOGGER.error("Failed to attach execution info: {}", e.getMessage());
        }
    }
    
    private static File saveScreenshotToFile(byte[] screenshotBytes, String testName) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("%s_%s.png", 
                StringUtil.sanitizeFileName(testName), timestamp);
            
            Path screenshotPath = Paths.get(SCREENSHOT_DIR, fileName);
            Files.write(screenshotPath, screenshotBytes);
            
            File screenshotFile = screenshotPath.toFile();
            LOGGER.debug("Screenshot saved: {}", screenshotFile.getAbsolutePath());
            return screenshotFile;
            
        } catch (IOException e) {
            LOGGER.error("Failed to save screenshot to file: {}", e.getMessage());
            return null;
        }
    }
    
    private static String generateVideoPath() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("%s/test_%s.mp4", VIDEO_DIR, timestamp);
    }
    
    private static String generateVideoPath(String testName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String sanitizedName = StringUtil.sanitizeFileName(testName);
        return String.format("%s/%s_%s.mp4", VIDEO_DIR, sanitizedName, timestamp);
    }
    
    private static File createTempFile(String prefix, String suffix, String content) {
        try {
            File tempFile = File.createTempFile(prefix, suffix);
            tempFile.deleteOnExit();
            Files.write(tempFile.toPath(), content.getBytes());
            return tempFile;
            
        } catch (IOException e) {
            LOGGER.error("Failed to create temporary file: {}", e.getMessage());
            return null;
        }
    }
    
    public static String getScreenshotDirectory() {
        return SCREENSHOT_DIR;
    }
    
    public static String getVideoDirectory() {
        return VIDEO_DIR;
    }
    
    public static String getDownloadDirectory() {
        return DOWNLOAD_DIR;
    }
    
    public static boolean isVideoRecordingEnabled() {
        String enabled = ProjectConfiguration.getProperty(PropertyNameSpace.ENABLE_VIDEO_RECORDING);
        return Boolean.parseBoolean(enabled);
    }
    
    public static boolean isScreenshotOnFailureEnabled() {
        String enabled = ProjectConfiguration.getProperty(PropertyNameSpace.ENABLE_SCREENSHOT_ON_FAILURE);
        return enabled == null || Boolean.parseBoolean(enabled); // Default to true
    }
}