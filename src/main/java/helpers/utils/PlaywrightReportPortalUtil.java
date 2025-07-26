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
    
    public static void startVideoRecording() {
        try {
            BrowserContext context = PlaywrightDriverPool.getBrowserContext();
            
            // Configure video recording
            String videoPath = generateVideoPath();
            Path videoDir = Paths.get(videoPath).getParent();
            Files.createDirectories(videoDir);
            
            LOGGER.info("Video recording started: {}", videoPath);
            
        } catch (Exception e) {
            LOGGER.error("Failed to start video recording: {}", e.getMessage());
        }
    }
    
    public static File stopVideoRecording(String testName) {
        try {
            Page page = PlaywrightDriverPool.getPage();
            page.close();
            
            // Video files are automatically saved when page is closed
            String videoPath = generateVideoPath(testName);
            File videoFile = new File(videoPath);
            
            if (videoFile.exists()) {
                LOGGER.info("Video recording saved: {}", videoFile.getName());
                return videoFile;
            } else {
                LOGGER.warn("Video file not found: {}", videoPath);
                return null;
            }
            
        } catch (Exception e) {
            LOGGER.error("Failed to stop video recording: {}", e.getMessage());
            return null;
        }
    }
    
    public static void attachVideoOnFailure(String testName) {
        File videoFile = stopVideoRecording(testName);
        if (videoFile != null) {
            ReportPortal.emitLog("Test Failure Video", "ERROR", new Date(), videoFile);
            LOGGER.info("Video attached to ReportPortal: {}", videoFile.getName());
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