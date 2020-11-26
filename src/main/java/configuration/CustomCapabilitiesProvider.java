package configuration;

import org.openqa.selenium.remote.DesiredCapabilities;

public class CustomCapabilitiesProvider {
    private static DesiredCapabilities capabilities = new DesiredCapabilities();

    // TODO: build this through config.properties
    public static DesiredCapabilities getCapabilities() {
        capabilities.setCapability("enableVNC", true);
        return capabilities;
    }
}
