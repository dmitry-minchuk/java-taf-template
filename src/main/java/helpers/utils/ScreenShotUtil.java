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
            if (!destinationDir.exists()) {
                destinationDir.mkdirs();
            }
            File destinationFile = new File(destinationDir, screenshotName);
            try {
                FileUtils.copyFile(screenshotFile, destinationFile);
                LOGGER.info("Screenshot saved into: {}", destinationFile.getAbsolutePath());
                return destinationFile;
            } catch (IOException e) {
                LOGGER.error("Error while saving screenshot: {}", e.getMessage());
                LOGGER.error(e.getStackTrace());
                return null;
            }
        } else {
            LOGGER.warn("Driver cannot make screenshot at the moment.");
            return null;
        }
    }

    public static String generateScreenshotName(String testMethodName) {
        return "failed_" + testMethodName + "_" + System.currentTimeMillis() + ".png";
    }
}
