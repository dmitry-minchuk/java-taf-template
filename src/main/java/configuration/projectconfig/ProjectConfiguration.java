package configuration.projectconfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ProjectConfiguration {
    protected static final Logger LOGGER = LogManager.getLogger(ProjectConfiguration.class);
    private final static String CONFIG_PATH = "src/test/resources/config.properties";
    private final static String DOTENV_PATH = ".env";

    private static final Properties DOTENV = loadDotEnv();

    public static String getProperty(PropertyNameSpace pn) {
        return getProperty(pn.getValue());
    }

    public static String getProperty(String pn) {
        String systemProperty = System.getProperty(pn);
        if (systemProperty != null && !systemProperty.isEmpty()) {
            return systemProperty;
        }
        String dotEnv = DOTENV.getProperty(pn);
        if (dotEnv != null && !dotEnv.isEmpty()) {
            return dotEnv;
        }
        try (InputStream input = new FileInputStream(CONFIG_PATH)) {
            Properties properties = new Properties();
            properties.load(input);
            return properties.getProperty(pn);
        } catch (IOException ex) {
            throw new RuntimeException(ex.toString());
        }
    }

    private static Properties loadDotEnv() {
        Properties props = new Properties();
        Path path = Path.of(DOTENV_PATH);
        if (!Files.exists(path)) {
            return props;
        }
        try (FileReader reader = new FileReader(path.toFile())) {
            props.load(reader);
        } catch (IOException ex) {
            LOGGER.warn("Failed to load .env file: {}", ex.toString());
        }
        return props;
    }
}
