package helpers.utils;

import configuration.driver.ContainerizedDriver;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class ScreenShotUtil {

    private static final Logger LOGGER = LogManager.getLogger(ScreenShotUtil.class);
    private final static String HOST_SCREENSHOT_RELATIVE_PATH = ProjectConfiguration.getProperty(PropertyNameSpace.HOST_SCREENSHOTS_PATH);
    private final static String CONTAINER_SCREENSHOT_PATH = ProjectConfiguration.getProperty(PropertyNameSpace.CONTAINER_SCREENSHOTS_PATH);

    public static File takeAndSaveScreenshot(ContainerizedDriver containerizedDriver) {
        if (containerizedDriver.getDriver() instanceof TakesScreenshot takesScreenshot) {
            File screenshotFile = takesScreenshot.getScreenshotAs(OutputType.FILE);
            File destinationDirInContainer = new File(CONTAINER_SCREENSHOT_PATH);
            String screenshotName = StringUtil.generateUniqueName("failed-test") + ".png";
            File destinationFileInContainer = new File(destinationDirInContainer, screenshotName);

            if (!destinationDirInContainer.exists()) {
                destinationDirInContainer.mkdirs();
            }

            try {
                FileUtils.copyFile(screenshotFile, destinationFileInContainer);
                LOGGER.info("Screenshot saved into container: {}", destinationFileInContainer.getAbsolutePath());

                File hostDir = new File(HOST_SCREENSHOT_RELATIVE_PATH);
                if (!hostDir.exists()) {
                    hostDir.mkdirs();
                }
                File hostFile = new File(hostDir, screenshotName);
                containerizedDriver.getDriverContainer().copyFileFromContainer(destinationFileInContainer.getAbsolutePath(), hostFile.getAbsolutePath());
                LOGGER.info("Screenshot copied to host: {}", hostFile.getAbsolutePath());

                return hostFile;

            } catch (IOException e) {
                LOGGER.error("Error while saving/copying screenshot: {}", e.getMessage());
                LOGGER.error(PrintUtil.prettyPrintObjectCollection.apply(Arrays.stream(e.getStackTrace()).toList()));
                return null;
            }
        } else {
            LOGGER.warn("Driver cannot make screenshot at the moment.");
            return null;
        }
    }
}
