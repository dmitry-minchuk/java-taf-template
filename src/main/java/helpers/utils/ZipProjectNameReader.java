package helpers.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Extracts the OpenL project name from a ZIP archive's rules.xml exactly
 * the way WebStudio does in its "Create Project from ZIP" form — by reading
 * the <name>...</name> element verbatim (no trimming) from rules.xml.
 */
public final class ZipProjectNameReader {

    private static final Logger LOGGER = LogManager.getLogger(ZipProjectNameReader.class);
    private static final Pattern NAME_PATTERN = Pattern.compile("<name>([^<]*)</name>");

    private ZipProjectNameReader() {
    }

    public static String readProjectName(File zip) {
        try (ZipFile archive = new ZipFile(zip)) {
            ZipEntry rulesXml = archive.getEntry("rules.xml");
            if (rulesXml == null) {
                throw new IllegalStateException("rules.xml not found at zip root: " + zip.getAbsolutePath());
            }
            try (InputStream in = archive.getInputStream(rulesXml)) {
                String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                Matcher m = NAME_PATTERN.matcher(content);
                if (!m.find()) {
                    throw new IllegalStateException("<name> element not found in rules.xml of " + zip.getAbsolutePath());
                }
                String name = m.group(1);
                LOGGER.debug("Project name from {}: [{}]", zip.getName(), name);
                return name;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read project name from " + zip.getAbsolutePath(), e);
        }
    }
}
