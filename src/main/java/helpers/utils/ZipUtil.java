package helpers.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

    public static String readFileFromZip(File zipFile, String entryName) {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String normalizedEntryName = normalizePath(entry.getName());
                if (normalizedEntryName.equals(entryName) || normalizedEntryName.endsWith("/" + entryName)) {
                    StringBuilder content = new StringBuilder();
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        content.append(new String(buffer, 0, len, StandardCharsets.UTF_8));
                    }
                    LOGGER.info("Read file '{}' from archive {} ({} chars)", entryName, zipFile.getName(), content.length());
                    return content.toString();
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read file '{}' from ZIP: {}", entryName, zipFile.getName(), e);
            throw new RuntimeException("Failed to read file from ZIP archive: " + e.getMessage(), e);
        }
        throw new RuntimeException("File '" + entryName + "' not found in ZIP archive: " + zipFile.getName());
    }

    private static String normalizePath(String path) {
        return path.replace('\\', '/');
    }
}
