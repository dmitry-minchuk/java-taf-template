package helpers.utils;

import com.microsoft.playwright.Page;
import configuration.driver.PlaywrightDriverPool;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class PlaywrightScreenshotUtil {

    private static final Logger LOGGER = LogManager.getLogger(PlaywrightScreenshotUtil.class);
    private final static String HOST_SCREENSHOT_RELATIVE_PATH = ProjectConfiguration.getProperty(PropertyNameSpace.HOST_SCREENSHOTS_PATH);

    public static File takeAndSaveScreenshot() {
        return takeAndSaveScreenshot(PlaywrightDriverPool.getPage());
    }

    public static File takeAndSaveScreenshot(Page page) {
        if (page != null) {
            byte[] screenshotBytes = page.screenshot(new Page.ScreenshotOptions()
                    .setFullPage(true)
                    .setType(com.microsoft.playwright.options.ScreenshotType.PNG));
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
                LOGGER.debug("Error while saving screenshot to host: {}", e.getMessage());
                LOGGER.debug(PrintUtil.prettyPrintObjectCollection.apply(Arrays.stream(e.getStackTrace()).toList()));
                return null;
            }
        } else {
            LOGGER.warn("Page cannot make screenshot at the moment.");
            return null;
        }
    }

    public static File takeAndSaveElementScreenshot(Page page, String selector) {
        if (page != null) {
            var locator = page.locator(selector);
            byte[] screenshotBytes = locator.screenshot(new com.microsoft.playwright.Locator.ScreenshotOptions()
                    .setType(com.microsoft.playwright.options.ScreenshotType.PNG));
            String screenshotName = StringUtil.generateUniqueName("element-screenshot") + ".png";
            File hostDir = new File(HOST_SCREENSHOT_RELATIVE_PATH);
            File hostFile = new File(hostDir, screenshotName);

            if (!hostDir.exists()) {
                hostDir.mkdirs();
            }

            try (FileOutputStream fos = new FileOutputStream(hostFile)) {
                fos.write(screenshotBytes);
                LOGGER.info("Element screenshot saved to host: {}", hostFile.getAbsolutePath());
                return hostFile;
            } catch (IOException e) {
                LOGGER.debug("Error while saving element screenshot to host: {}", e.getMessage());
                LOGGER.debug(PrintUtil.prettyPrintObjectCollection.apply(Arrays.stream(e.getStackTrace()).toList()));
                return null;
            }
        } else {
            LOGGER.warn("Page cannot make element screenshot at the moment.");
            return null;
        }
    }
}