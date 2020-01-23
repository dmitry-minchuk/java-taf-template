package utils;

import domain.PropertyName;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyHandler {
    public static String getProperty(PropertyName pn) {
        try (InputStream input = new FileInputStream("src/main/resources/config.properties")) {
            Properties properties = new Properties();
            properties.load(input);
            return properties.getProperty(pn.getValue());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
