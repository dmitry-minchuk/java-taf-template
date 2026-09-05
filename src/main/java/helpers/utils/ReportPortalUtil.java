package helpers.utils;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.ScreenshotType;
import configuration.driver.DriverPool;
import configuration.driver.PlaywrightTracing;
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

public class ReportPortalUtil {
    
    private static final Logger LOGGER = LogManager.getLogger(ReportPortalUtil.class);
    
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
            byte[] screenshotBytes = DriverPool.takeScreenshot();
            if (screenshotBytes != null) {
                File screenshotFile = saveScreenshotToFile(screenshotBytes, testName);
                if (screenshotFile != null) {
                    ReportPortalArtifactUtil.recordAttachment(description, "ERROR", screenshotFile);
                    ReportPortalArtifactUtil.emitLog(description, "ERROR", screenshotFile);
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
            Page page = DriverPool.getPage();
            
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
            ReportPortalArtifactUtil.recordAttachment(description, "INFO", screenshotFile);
            ReportPortalArtifactUtil.emitLog(description, "INFO", screenshotFile);
            LOGGER.info("Custom screenshot attached to ReportPortal: {}", screenshotFile.getName());
        }
    }
    
    public static byte[] getVideoBytes(String testName) {
        try {
            if (!isVideoRecordingEnabled()) {
                LOGGER.debug("Video recording is not enabled");
                return null;
            }
            
            Page page = DriverPool.getPage();
            
            if (page.video() == null) {
                LOGGER.warn("No video recording found for page - ensure BrowserContext was configured with recordVideoDir");
                return null;
            }
            
            LOGGER.info("Retrieving video bytes for test: {}", testName);
            
            com.microsoft.playwright.Video video = page.video();
            
            page.close();
            LOGGER.debug("Page closed to finalize video recording for test: {}", testName);
            
            File tempVideoFile = null;
            try {
                tempVideoFile = File.createTempFile("playwright_video_" + testName, ".webm");
                tempVideoFile.deleteOnExit();
                
                video.saveAs(Paths.get(tempVideoFile.getAbsolutePath()));
                
                byte[] videoBytes = Files.readAllBytes(tempVideoFile.toPath());
                LOGGER.info("Successfully retrieved {} bytes of video data for test: {}", videoBytes.length, testName);
                return videoBytes;
                
            } finally {
                if (tempVideoFile != null && tempVideoFile.exists()) {
                    tempVideoFile.delete();
                }
            }
            
        } catch (Exception e) {
            LOGGER.error("Failed to get video bytes for test {}: {}", testName, e.getMessage(), e);
            return null;
        }
    }
    
    public static void attachVideoOnFailure(String testName) {
        try {
            if (!isVideoRecordingEnabled()) {
                LOGGER.debug("Video recording is not enabled, skipping video attachment");
                return;
            }
            
            byte[] videoBytes = getVideoBytes(testName);
            if (videoBytes != null && videoBytes.length > 0) {
                File tempFile = File.createTempFile("test_failure_video_" + testName, ".webm");
                tempFile.deleteOnExit();
                
                try {
                    Files.write(tempFile.toPath(), videoBytes);
                    ReportPortalArtifactUtil.recordAttachment("Test Failure Video", "ERROR", tempFile);
                    ReportPortalArtifactUtil.emitLog("Test Failure Video", "ERROR", tempFile);
                    LOGGER.info("Video attached to ReportPortal for test: {} (size: {} bytes)", testName, videoBytes.length);
                } finally {
                    tempFile.delete();
                }
            } else {
                LOGGER.warn("No video data available to attach for test: {}", testName);
            }
            
        } catch (Exception e) {
            LOGGER.error("Failed to attach video to ReportPortal for test {}: {}", testName, e.getMessage(), e);
        }
    }
    
    public static boolean isDebugArtifactsOnSuccessEnabled() {
        return Boolean.parseBoolean(ProjectConfiguration.getProperty(PropertyNameSpace.DEBUG_ARTIFACTS_ON_SUCCESS));
    }

    public static boolean attachTrace(String testName, boolean keep) {
        if (!PlaywrightTracing.isEnabled()) {
            return false;
        }
        File traceFile = null;
        try {
            traceFile = PlaywrightTracing.stop(DriverPool.getBrowserContext(), testName, keep);
            if (traceFile == null) {
                return false;
            }
            ReportPortalArtifactUtil.recordAttachment("Playwright Trace", "INFO", traceFile);
            ReportPortalArtifactUtil.emitLog("Playwright Trace (open with: npx playwright show-trace trace.zip)", "INFO", traceFile);
            LOGGER.info("Playwright trace attached to ReportPortal for test: {} (size: {} bytes)", testName, traceFile.length());
            return true;
        } catch (IllegalStateException e) {
            LOGGER.debug("No Playwright context for test {}, trace skipped: {}", testName, e.getMessage());
            return false;
        } catch (Exception e) {
            LOGGER.error("Failed to attach Playwright trace for test {}: {}", testName, e.getMessage());
            return false;
        } finally {
            if (traceFile != null) {
                traceFile.delete();
                traceFile.getParentFile().delete();
            }
        }
    }

    public static void attachPageContent(String description) {
        try {
            Page page = DriverPool.getPage();
            String content = page.content();
            
            File tempFile = createTempFile("page-content-", ".html", content);
            if (tempFile != null) {
                ReportPortalArtifactUtil.recordAttachment(description, "INFO", tempFile);
                ReportPortalArtifactUtil.emitLog(description, "INFO", tempFile);
                LOGGER.info("Page content attached to ReportPortal: {}", tempFile.getName());
            }
            
        } catch (Exception e) {
            LOGGER.error("Failed to attach page content: {}", e.getMessage());
        }
    }
    
    public static void attachBrowserLogs(String description) {
        try {
            String logs = "Browser logs collection not yet implemented for Playwright";
            
            File tempFile = createTempFile("browser-logs-", ".txt", logs);
            if (tempFile != null) {
                ReportPortalArtifactUtil.recordAttachment(description, "INFO", tempFile);
                ReportPortalArtifactUtil.emitLog(description, "INFO", tempFile);
                LOGGER.info("Browser logs attached to ReportPortal: {}", tempFile.getName());
            }
            
        } catch (Exception e) {
            LOGGER.error("Failed to attach browser logs: {}", e.getMessage());
        }
    }
    
    public static void attachDownloadedFile(File downloadedFile, String description) {
        if (downloadedFile != null && downloadedFile.exists()) {
            ReportPortalArtifactUtil.recordAttachment(description, "INFO", downloadedFile);
            ReportPortalArtifactUtil.emitLog(description, "INFO", downloadedFile);
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
                ReportPortalArtifactUtil.recordAttachment(description, "INFO", testDataFile);
                ReportPortalArtifactUtil.emitLog(description, "INFO", testDataFile);
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
            configuration.driver.ExecutionMode mode = DriverPool.getCurrentExecutionMode();
            String debugInfo = DriverPool.getDebugInfo();
            
            String executionInfo = String.format(
                "Execution Mode: %s%n%n%s", 
                mode, 
                debugInfo
            );
            
            File tempFile = createTempFile("execution-info-", ".txt", executionInfo);
            if (tempFile != null) {
                ReportPortalArtifactUtil.recordAttachment("Test Execution Information", "INFO", tempFile);
                ReportPortalArtifactUtil.emitLog("Test Execution Information", "INFO", tempFile);
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
        return enabled == null || Boolean.parseBoolean(enabled);
    }
}
