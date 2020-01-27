package utils;

import domain.PropertyNameSpace;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class ProjectConfiguration {
    protected static final Logger LOGGER = LogManager.getLogger(ProjectConfiguration.class);
    private final static String CONFIG_PATH = "src/main/resources/config.properties";

    public static String getProperty(PropertyNameSpace pn) {
        return getProperty(pn.getValue());
    }

    public static String getPropertyByEnv(PropertyNameSpace pn) {
        return getPropertyByEnv(pn.getValue());
    }

    public static String getProperty(String pn) {
        try (InputStream input = new FileInputStream(CONFIG_PATH)) {
            Properties properties = new Properties();
            properties.load(input);
            return properties.getProperty(pn);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String getPropertyByEnv(String pn) {
        String env = getProperty(PropertyNameSpace.ENV);
        LOGGER.info("Environment: " + env);

        if( env.length() == 0) {
            return getProperty(pn);
        } else {
            List<String> propertyNameList = ProjectConfiguration.getProperties().stream().filter(p -> p.contains(".")
                    && p.substring(0, p.indexOf(".")).equals(env)
                    && p.substring(p.indexOf(".") + 1).equals(pn)).collect(Collectors.toList());

            if(propertyNameList.isEmpty()) {
                throw new RuntimeException("Property was not found for given env parameter. Env parameter: " + env);
            } else if(propertyNameList.size() > 1) {
                StringBuilder sb = new StringBuilder();
                propertyNameList.forEach(p -> {sb.append(p); sb.append(" ");});
                throw new RuntimeException("Extra property was found for given env parameter. Env parameter: " + env + ". Found extra parameters: " + sb);
            }

            LOGGER.info(pn + ": " + getProperty(propertyNameList.get(0)));
            return getProperty(propertyNameList.get(0));
        }
    }

    public static Set<String> getProperties() {
        try (InputStream input = new FileInputStream(CONFIG_PATH)) {
            Properties properties = new Properties();
            properties.load(input);
            return properties.stringPropertyNames();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
