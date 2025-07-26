package helpers.utils;

import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

// This class is for searching and getting test_data files by name
public class TestDataUtil {
    private final static String HOST_RESOURCE_PATH = ProjectConfiguration.getProperty(PropertyNameSpace.HOST_RESOURCE_PATH);
    private final static String CONTAINER_RESOURCE_PATH = ProjectConfiguration.getProperty(PropertyNameSpace.CONTAINER_RESOURCE_PATH);

    public static String getFilePathFromResources(String fileName) {
        File resourcesDir = new File(HOST_RESOURCE_PATH);
        String absoluteHostResourcesPath = resourcesDir.getAbsolutePath();
        String relativePath = getRelativePath(getFile(fileName), absoluteHostResourcesPath);
        return CONTAINER_RESOURCE_PATH + "/" + relativePath.replace(File.separator, "/");
    }

    private static String getRelativePath(File matchingFile, String absoluteHostResourcesPath) {
        String localAbsolutePath = matchingFile.getAbsolutePath();
        String relativePath = "";
        if (localAbsolutePath.startsWith(absoluteHostResourcesPath)) {
            relativePath = localAbsolutePath.substring(absoluteHostResourcesPath.length());
            if (!relativePath.isEmpty() && relativePath.startsWith(File.separator)) {
                relativePath = relativePath.substring(File.separator.length());
            }
        }
        return relativePath;
    }

    public static File getFile(String fileName) {
        File resourcesDir = new File(HOST_RESOURCE_PATH);
        Collection<File> foundFiles = FileUtils.listFiles(resourcesDir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        List<File> matchingFiles = foundFiles.stream()
                .filter(file -> file.getName().contains(fileName))
                .toList();

        if (matchingFiles.size() == 1) {
            return matchingFiles.getFirst();
        } else {
            throw new RuntimeException(String.format("File '%s' not found here %s or found more than one: %s",
                    fileName,
                    HOST_RESOURCE_PATH,
                    PrintUtil.prettyPrintObjectCollection.apply(matchingFiles)));
        }
    }

    public static InputStream getInputStream(String fileName) {
        try {
            File file = getFile(fileName);
            return FileUtils.openInputStream(file);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get input_stream from file '" + fileName + "': " + e.getMessage(), e);
        }
    }

    public static Properties makePropertiesFromFile(String fileName) {
        Properties properties = new Properties();
        try (InputStream input = getInputStream(fileName)) {
            properties.load(input);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create properties from file '" + fileName + "': " + e.getMessage(), e);
        }
        return properties;
    }
}

