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

public class TestDataUtil {

    private final static String HOST_RESOURCE_PATH = ProjectConfiguration.getProperty(PropertyNameSpace.HOST_RESOURCE_PATH);
    private final static String CONTAINER_RESOURCE_PATH = ProjectConfiguration.getProperty(PropertyNameSpace.CONTAINER_RESOURCE_PATH);

    public static String getFilePathFromResources(String fileName) {
        File resourcesDir = new File(HOST_RESOURCE_PATH);
        String absoluteHostResourcesPath = resourcesDir.getAbsolutePath();

        Collection<File> foundFiles = FileUtils.listFiles(resourcesDir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        List<File> matchingFiles = foundFiles.stream()
                .filter(file -> file.getName().contains(fileName))
                .toList();

        if (matchingFiles.size() == 1) {
            String relativePath = getRelativePath(matchingFiles, absoluteHostResourcesPath);
            return CONTAINER_RESOURCE_PATH + "/" + relativePath.replace(File.separator, "/");
        } else {
            throw new RuntimeException(String.format("File '%s' not found here %s or found more than one: %s",
                    fileName,
                    HOST_RESOURCE_PATH,
                    PrintUtil.prettyPrintObjectCollection.apply(matchingFiles)));
        }
    }

    private static String getRelativePath(List<File> matchingFiles, String absoluteHostResourcesPath) {
        String localAbsolutePath = matchingFiles.getFirst().getAbsolutePath();
        String relativePath = "";
        if (localAbsolutePath.startsWith(absoluteHostResourcesPath)) {
            relativePath = localAbsolutePath.substring(absoluteHostResourcesPath.length());
            if (!relativePath.isEmpty() && relativePath.startsWith(File.separator)) {
                relativePath = relativePath.substring(File.separator.length());
            }
        }
        return relativePath;
    }

    public static Properties loadEmailProperties(String fileName) {
        Properties properties = new Properties();
        try {
            File resourcesDir = new File(HOST_RESOURCE_PATH);
            Collection<File> foundFiles = FileUtils.listFiles(resourcesDir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
            List<File> matchingFiles = foundFiles.stream()
                    .filter(file -> file.getName().contains(fileName))
                    .toList();

            if (matchingFiles.size() == 1) {
                File propertiesFile = matchingFiles.getFirst();
                try (InputStream input = FileUtils.openInputStream(propertiesFile)) {
                    properties.load(input);
                }
            } else {
                throw new RuntimeException(String.format("File '%s' not found here %s or found more than one: %s",
                        fileName,
                        HOST_RESOURCE_PATH,
                        PrintUtil.prettyPrintObjectCollection.apply(matchingFiles)));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load properties from file '" + fileName + "': " + e.getMessage(), e);
        }
        return properties;
    }
}

