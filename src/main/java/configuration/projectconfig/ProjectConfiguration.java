package configuration.projectconfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ProjectConfiguration {
    protected static final Logger LOGGER = LogManager.getLogger(ProjectConfiguration.class);
    private final static String CONFIG_PATH = "src/test/resources/config.properties";

    public static String getProperty(PropertyNameSpace pn) {
        return getProperty(pn.getValue());
    }

    public static String getProperty(String pn) {
        String systemProperty = System.getProperty(pn);
        if (systemProperty != null && !"".equals(systemProperty)) {
            return systemProperty;
        } else try (InputStream input = new FileInputStream(CONFIG_PATH)) {
            Properties properties = new Properties();
            properties.load(input);
            return properties.getProperty(pn);
        } catch (IOException ex) {
            throw new RuntimeException(ex.toString());
        }
    }
}
