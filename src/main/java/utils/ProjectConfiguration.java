package utils;

import domain.PropertyNameSpace;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

public class ProjectConfiguration {

    private final static String CONFIG_PATH = "src/main/resources/config.properties";

    public static String getProperty(PropertyNameSpace pn) {
        return getProperty(pn.getValue());
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
