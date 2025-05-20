package helpers.utils;

import configuration.driver.DriverPool;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import org.openqa.selenium.Dimension;

import java.util.Objects;
import java.util.Set;

public class WindowSwitcher {

    public static void switchToWindow(String title) {
        Set<String> handles = DriverPool.getDriver().getWindowHandles();
        for (String newWindow : handles) {
            DriverPool.getDriver().switchTo().window(newWindow);
            if (Objects.equals(DriverPool.getDriver().getTitle(), title)) {
                break;
            }
        }
    }

    public static void switchToWindow(String[] title) {
        Set<String> handles = DriverPool.getDriver().getWindowHandles();
        for (String newWindow : handles) {
            DriverPool.getDriver().switchTo().window(newWindow);
            for (int i = 0; i <= title.length - 1; i++) {
                if (Objects.equals(DriverPool.getDriver().getTitle(), title[i])) {
                    break;
                }
            }
        }
    }

    public static void closeWindow(String closeTitle, String toTitle) {
        if (toTitle.isEmpty()) {
            Set<String> handles = DriverPool.getDriver().getWindowHandles();
            for (String newWindow : handles) {
                DriverPool.getDriver().switchTo().window(newWindow);
                if (Objects.equals(DriverPool.getDriver().getTitle(), closeTitle)) {
                    DriverPool.getDriver().close();
                }
            }
        } else {
            String parent = "";
            Set<String> handles = DriverPool.getDriver().getWindowHandles();
            for (String newWindow : handles) {
                DriverPool.getDriver().switchTo().window(newWindow);
                if (Objects.equals(DriverPool.getDriver().getTitle(), closeTitle)) {
                    DriverPool.getDriver().close();
                }
                try {
                    if (Objects.equals(DriverPool.getDriver().getTitle(), toTitle)) {
                        parent = newWindow;
                    }
                } catch (Exception ignored) {
                }
            }
            DriverPool.getDriver().switchTo().window(parent);
        }
    }

    public static void closeWindow(String[] closeTitle) {
        Set<String> handles = DriverPool.getDriver().getWindowHandles();
        for (String newWindow : handles) {
            DriverPool.getDriver().switchTo().window(newWindow);
            for (int i = 0; i <= closeTitle.length - 1; i++) {
                if (Objects.equals(DriverPool.getDriver().getTitle(), closeTitle[i])) {
                    DriverPool.getDriver().close();
                    break;
                }
            }
        }
    }

    public static void switchToFrame(String name) {
        DriverPool.getDriver().switchTo().frame(name);
    }

    public static void reSwitchToFrame() {
        DriverPool.getDriver().switchTo().defaultContent();
    }

    public static String getWindowUrl() {
        return DriverPool.getDriver().getCurrentUrl();
    }

    public static void maximizeWindow(String title) {
        Set<String> handles = DriverPool.getDriver().getWindowHandles();
        for (String newWindow : handles) {
            DriverPool.getDriver().switchTo().window(newWindow);
            if (Objects.equals(DriverPool.getDriver().getTitle(), title)) {
                if (ProjectConfiguration.getProperty(PropertyNameSpace.BROWSER).equals("chrome")) {
                    DriverPool.getDriver().manage().window().setSize(new Dimension(1920, 1080));
                } else {
                    DriverPool.getDriver().manage().window().maximize();
                }
                break;
            }
        }
    }
}
