package helpers.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Extracts the OpenL project name from a ZIP archive the same way WebStudio
 * does in its "Create Project from ZIP" form. Resolution order:
 *
 *  1. rules.xml at zip root, &lt;name&gt;…&lt;/name&gt; verbatim.
 *  2. rules.xml found anywhere inside the archive.
 *  3. Archive comment of the form "Project 'NAME' version N".
 *  4. Zip filename without the .zip extension.
 *
 *  This matches what WebStudio auto-fills in the UI for archives that don't
 *  carry a rules.xml (e.g. legacy exports).
 */
public final class ZipProjectNameReader {

    private static final Logger LOGGER = LogManager.getLogger(ZipProjectNameReader.class);
    private static final Pattern NAME_PATTERN = Pattern.compile("<name>([^<]*)</name>");
    private static final Pattern COMMENT_NAME_PATTERN = Pattern.compile("Project\\s+'([^']+)'");

    private ZipProjectNameReader() {
    }

    public static String readProjectName(File zip) {
        try (ZipFile archive = new ZipFile(zip)) {
            String nameFromRoot = readNameFrom(archive, "rules.xml");
            if (nameFromRoot != null) return logAndReturn(zip, "rules.xml (root)", nameFromRoot);

            String nameFromNested = findNestedRulesXmlName(archive);
            if (nameFromNested != null) return logAndReturn(zip, "rules.xml (nested)", nameFromNested);

            String comment = archive.getComment();
            if (comment != null) {
                Matcher m = COMMENT_NAME_PATTERN.matcher(comment);
                if (m.find()) return logAndReturn(zip, "zip comment", m.group(1));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read project name from " + zip.getAbsolutePath(), e);
        }
        String fallback = stripExtension(zip.getName());
        LOGGER.warn("No project name found in {}; falling back to filename [{}]", zip.getAbsolutePath(), fallback);
        return fallback;
    }

    private static String readNameFrom(ZipFile archive, String entryName) throws IOException {
        ZipEntry entry = archive.getEntry(entryName);
        if (entry == null) return null;
        try (InputStream in = archive.getInputStream(entry)) {
            String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            Matcher m = NAME_PATTERN.matcher(content);
            return m.find() ? m.group(1) : null;
        }
    }

    private static String findNestedRulesXmlName(ZipFile archive) throws IOException {
        Enumeration<? extends ZipEntry> entries = archive.entries();
        while (entries.hasMoreElements()) {
            ZipEntry e = entries.nextElement();
            if (e.isDirectory()) continue;
            String n = e.getName();
            if (n.equals("rules.xml") || n.endsWith("/rules.xml")) {
                String name = readNameFrom(archive, n);
                if (name != null) return name;
            }
        }
        return null;
    }

    private static String stripExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }

    private static String logAndReturn(File zip, String source, String name) {
        LOGGER.debug("Project name from {} ({}): [{}]", zip.getName(), source, name);
        return name;
    }
}
