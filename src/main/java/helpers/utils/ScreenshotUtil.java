package helpers.utils;

import configuration.driver.ContainerizedDriver;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class ScreenshotUtil {

    private static final Logger LOGGER = LogManager.getLogger(ScreenshotUtil.class);
    private final static String HOST_SCREENSHOT_RELATIVE_PATH = ProjectConfiguration.getProperty(PropertyNameSpace.HOST_SCREENSHOTS_PATH);

    public static File takeAndSaveScreenshot(ContainerizedDriver containerizedDriver) {
        if (containerizedDriver.getDriver() instanceof TakesScreenshot takesScreenshot) {
            byte[] screenshotBytes = takesScreenshot.getScreenshotAs(OutputType.BYTES);
            String screenshotName = StringUtil.generateUniqueName("failed-test") + ".png";
            File hostDir = new File(HOST_SCREENSHOT_RELATIVE_PATH);
            File hostFile = new File(hostDir, screenshotName);

            if (!hostDir.exists()) {
                hostDir.mkdirs();
            }

            try (FileOutputStream fos = new FileOutputStream(hostFile)) {
                fos.write(screenshotBytes);
                LOGGER.info("Screenshot saved to host: {}", hostFile.getAbsolutePath());
                return hostFile;
            } catch (IOException e) {
                LOGGER.error("Error while saving screenshot to host: {}", e.getMessage());
                LOGGER.error(PrintUtil.prettyPrintObjectCollection.apply(Arrays.stream(e.getStackTrace()).toList()));
                return null;
            }
        } else {
            LOGGER.warn("Driver cannot make screenshot at the moment.");
            return null;
        }
    }
}
