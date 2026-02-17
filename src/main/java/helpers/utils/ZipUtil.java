package helpers.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtil {

    private static final Logger LOGGER = LogManager.getLogger(ZipUtil.class);

    public static List<String> listFiles(File zipFile) {
        List<String> fileNames = new ArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    String fileName = normalizePath(entry.getName());
                    fileNames.add(fileName);
                    LOGGER.debug("Found file in archive: {}", fileName);
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read ZIP file: {}", zipFile.getName(), e);
            throw new RuntimeException("Failed to read ZIP archive: " + e.getMessage(), e);
        }

        LOGGER.info("Archive {} contains {} files", zipFile.getName(), fileNames.size());
        return fileNames;
    }

    private static String normalizePath(String path) {
        return path.replace('\\', '/');
    }
}
