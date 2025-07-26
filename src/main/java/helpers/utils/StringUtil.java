package helpers.utils;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONObject;
import org.openqa.selenium.By;

import java.io.File;
import java.util.List;
import java.util.function.Function;

public class StringUtil {

    public static String generateUniqueName() {
        return generateUniqueName(15);
    }

    public static String generateUniqueName(String baseName) {
        return baseName + "-" + RandomStringUtils.randomNumeric(6);
    }

    public static String generateUniqueName(int length) {
        return generateUniqueName(RandomStringUtils.randomAlphanumeric(length));
    }

    public static String splitStringAndGetNthElement(String string, String splitter, int nthElementFromEnd) {
        String[] elements = string.split(splitter);
        if(nthElementFromEnd >= elements.length)
            throw new RuntimeException(String.format("Required element does not exist! Total elements: %s, wanted element from the end: %s", elements.length, nthElementFromEnd));
        int n = elements.length - nthElementFromEnd;
        return elements[n];
    }

    public static String getPathWithoutFileName(String path) {
        String fileName = splitStringAndGetNthElement(path, File.separator, 1);
        return path.replaceAll(File.separator + fileName, "");
    }

    public static String getLocator(By by) {
        String prefix = by.toString().split(" ")[0] + " ";
        return by.toString().replaceAll(prefix, "");
    }

    public static String buildPath(boolean startWithFs, String... values) {
        StringBuilder sb = new StringBuilder();
        if(startWithFs) {
            sb.append(File.separator);
        }
        for (String value : values) {
            sb.append(value).append(File.separator);
        }
        return sb.deleteCharAt(sb.length()-1).toString();
    }

    public static String makeUniversalPath(String path) {
        return path.replace("\\", File.separator).replace("/", File.separator);
    }

    public static String makeUnixPath(String path) {
        return path.replace("\\", "/");
    }

    public static String deleteNewLines(String string) {
        return string.replace("\n", "");
    }

    public static String deleteNewLinesAndSpaces(String string) {
        return string.replace("\n", "").replace(" ", "");
    }

    public static Function<List<?>, String> prettyPrintJsonObjectList = j -> {
        if(j == null) return null;
        StringBuffer sb = new StringBuffer();
        j.forEach(o -> {
            sb.append(new JSONObject(o).toString(4));
            sb.append("\n");
        });
        return sb.toString();
    };

    public static Function<List<?>, String> prettyPrintObjectList = j -> {
        if(j == null) return null;
        StringBuffer sb = new StringBuffer();
        j.forEach(o -> {
            sb.append(o.toString());
            sb.append("\n");
        });
        return sb.toString();
    };

    public static String formatJsonResponse(String responseBody) {
        try {
            JSONObject jsonObject = new JSONObject(responseBody);
            return jsonObject.toString(4);
        } catch (Exception e) {
            return responseBody;
        }
    }
    
    public static String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "unnamed";
        }
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
