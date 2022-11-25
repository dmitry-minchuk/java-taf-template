package helpers.utils;

import org.apache.commons.lang3.RandomStringUtils;

public class StringUtil {

    public static String generateUniqueName() {
        return String.format("appcontainer-%s", RandomStringUtils.randomNumeric(6));
    }
}
