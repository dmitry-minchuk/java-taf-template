package helpers.utils;

import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import configuration.driver.DockerDriverPool;
import configuration.driver.LocalDriverPool;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;

public class DownloadUtil {
    
    protected static final Logger LOGGER = LogManager.getLogger(DownloadUtil.class);

    private static final int DEFAULT_TIMEOUT_MS = Integer.parseInt(
        ProjectConfiguration.getProperty(PropertyNameSpace.PLAYWRIGHT_DEFAULT_TIMEOUT)
    );
    private static final int RETRY_TIMEOUT_MS = 30000;
    private static final int RETRY_INTERVAL_MS = 1000;
    
    public static File downloadFile(Locator trigger) {
        return downloadFile(trigger, getDefaultTimeout());
    }
    
    public static File downloadFile(Locator trigger, int timeoutMs) {
        if (isDockerMode()) {
            LOGGER.info("Downloading file in DOCKER mode using createReadStream()");
            return downloadFileFromContainer(trigger, timeoutMs);
        } else {
            LOGGER.info("Downloading file in LOCAL mode using createReadStream()");
            return downloadFileLocal(trigger, timeoutMs);
        }
    }
    
    private static File downloadFileLocal(Locator trigger, int timeoutMs) {
        Page page = LocalDriverPool.getPage();

        return WaitUtil.retryOnException(() -> {
            Download download = page.waitForDownload(new Page.WaitForDownloadOptions().setTimeout(timeoutMs), trigger::click);

            LOGGER.info("Download started: {}", download.suggestedFilename());

            try (InputStream inputStream = download.createReadStream()) {
                String fileName = download.suggestedFilename();
                if (fileName == null || fileName.isEmpty()) {
                    fileName = "download_" + UUID.randomUUID();
                }

                File tempFile = File.createTempFile("playwright_download_", "_" + sanitizeFileName(fileName));

                try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }

                LOGGER.info("Download completed in LOCAL mode: {} (size: {} bytes)", tempFile.getName(), tempFile.length());
                return tempFile;

            } catch (Exception e) {
                LOGGER.error("Failed to download file in LOCAL mode: {}", e.getMessage());
                throw new RuntimeException("LOCAL download failed: " + e.getMessage(), e);
            }
        }, RETRY_TIMEOUT_MS, RETRY_INTERVAL_MS, "Download file in LOCAL mode");
    }
    
    private static File downloadFileFromContainer(Locator trigger, int timeoutMs) {
        Page page = DockerDriverPool.getPage();

        return WaitUtil.retryOnException(() -> {
            Download download = page.waitForDownload(new Page.WaitForDownloadOptions().setTimeout(timeoutMs), trigger::click);

            LOGGER.info("Download started in container: {}", download.suggestedFilename());

            try (InputStream inputStream = download.createReadStream()) {
                String fileName = download.suggestedFilename();
                if (fileName == null || fileName.isEmpty()) {
                    fileName = "download_" + UUID.randomUUID();
                }

                File tempFile = File.createTempFile("playwright_container_download_", "_" + sanitizeFileName(fileName));

                try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }

                LOGGER.info("Download completed in DOCKER mode: {} (size: {} bytes)", tempFile.getName(), tempFile.length());
                return tempFile;

            } catch (Exception e) {
                LOGGER.error("Failed to download file in DOCKER mode: {}", e.getMessage());
                throw new RuntimeException("DOCKER download failed: " + e.getMessage(), e);
            }

        }, RETRY_TIMEOUT_MS, RETRY_INTERVAL_MS, "Download file in DOCKER mode");
    }
    
    private static boolean isDockerMode() {
        try {
            return DockerDriverPool.isInitialized();
        } catch (Exception e) {
            return false;
        }
    }

    private static String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "unnamed";
        }
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
    
    private static int getDefaultTimeout() {
        return DEFAULT_TIMEOUT_MS;
    }
    
    public static boolean cleanupDownloadFile(File downloadedFile) {
        if (downloadedFile != null && downloadedFile.exists()) {
            boolean deleted = downloadedFile.delete();
            if (deleted) {
                LOGGER.debug("Cleaned up download file: {}", downloadedFile.getName());
            } else {
                LOGGER.warn("Failed to cleanup download file: {}", downloadedFile.getName());
            }
            return deleted;
        }
        return false;
    }
}