package helpers.utils;

import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;

public class ScreenShotUtil {

    private static final Logger LOGGER = LogManager.getLogger(ScreenShotUtil.class);
    private final static String HOST_SCREENSHOT_RELATIVE_PATH = ProjectConfiguration.getProperty(PropertyNameSpace.HOST_SCREENSHOTS_PATH);

    public static File takeAndSaveScreenshot(WebDriver driver, String screenshotName) {
        if (driver instanceof TakesScreenshot) {
            TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
            File screenshotFile = takesScreenshot.getScreenshotAs(OutputType.FILE);
            File destinationDir = new File(HOST_SCREENSHOT_RELATIVE_PATH);
            ensureScreenshotDirectoryExistsAndHasPermissions(destinationDir.getAbsolutePath());
//            if (!destinationDir.exists()) {
//                destinationDir.mkdirs();
//            }
            File destinationFile = new File(destinationDir, screenshotName);
            try {
                FileUtils.copyFile(screenshotFile, destinationFile);
                LOGGER.info("Screenshot saved into: {}", destinationFile.getAbsolutePath());
                return destinationFile;
            } catch (IOException e) {
                LOGGER.error("Error while saving screenshot: {}", e.getMessage());
                return null;
            }
        } else {
            LOGGER.warn("Driver cannot make screenshot at the moment.");
            return null;
        }
    }

    private static void ensureScreenshotDirectoryExistsAndHasPermissions(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                LOGGER.info("Директория для скриншотов создана: {}", directory.getAbsolutePath());
                try {
                    ProcessBuilder pb = new ProcessBuilder("chmod", "-R", "775", directory.getAbsolutePath());
                    Process process = pb.start();
                    int exitCode = process.waitFor();
                    if (exitCode == 0) {
                        LOGGER.info("Permissions set.");
                    } else {
                        LOGGER.warn("Could not set permissions. (Exit code: {}).", exitCode);
                    }
                } catch (IOException | InterruptedException e) {
                    LOGGER.warn("Error while setting permissions for screenshot directory: {}", e.getMessage());
                }
            } else {
                LOGGER.error("Could not create screenshot directory: {}", directory.getAbsolutePath());
            }
        }
    }

    public static String generateScreenshotName(String testMethodName) {
        return "failed_" + testMethodName + "_" + System.currentTimeMillis() + ".png";
    }
}
