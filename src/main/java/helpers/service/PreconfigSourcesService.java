package helpers.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Source provider for the preconfig-projects regression: syncs the local Mercurial clones of the
 * EIS preconfig product repositories ({@code hg pull -u}), discovers every OpenL project inside
 * them ({@code <module>/src/main/openl/rules.xml}) and packs each one into a flat ZIP that
 * WebStudio's {@code PUT /rest/repos/{repo}/projects/{name}} accepts.
 * <p>
 * Unlike the client zip regression (static ZIP snapshots) and the central-studio regression
 * (git design repos cloned by Studio itself), preconfigs live in Mercurial inside Maven modules —
 * Studio cannot mount hg as a design repository, so the sources are zipped and uploaded instead.
 * <p>
 * Projects whose rules.xml declares a {@code <classpath>} need domain JARs that only exist after
 * a Maven build ({@code mvn package} of the {@code openl}-packaging module); they are skipped
 * unless {@code -Dpreconfig.include.jar.dependent=true} is set.
 */
public class PreconfigSourcesService {

    private static final Logger LOGGER = LogManager.getLogger(PreconfigSourcesService.class);

    public static final String REPOS_ROOT = System.getProperty("preconfig.repos.root",
            "/Users/dmitryminchuk/Projects/eis/preconfigs");
    /** Product repos that actually contain OpenL projects (commercial-claim is a pure Java product). */
    public static final List<String> HG_REPOS = List.of(
            "eis-preconfig-benefits-policy",
            "eis-preconfig-commercial-policy",
            "eis-preconfig-personal-claims",
            "eis-preconfig-personalpolicy");

    private static final boolean SYNC_ENABLED = Boolean.parseBoolean(
            System.getProperty("preconfig.hg.sync", "true"));
    private static final boolean INCLUDE_JAR_DEPENDENT = Boolean.parseBoolean(
            System.getProperty("preconfig.include.jar.dependent", "true"));
    private static final int MAVEN_BUILD_TIMEOUT_MINUTES = 30;
    /** Optional substring filter over project labels (repo/module) — handy for debugging one case. */
    private static final String MODULE_FILTER = System.getProperty("preconfig.module.filter", "");
    private static final int HG_PULL_TIMEOUT_MINUTES = 15;
    private static final int HG_CLONE_TIMEOUT_MINUTES = 60;
    private static final String HG_BASE_URL = System.getProperty("preconfig.hg.base.url",
            "http://vno-hg.exigengroup.com/hg/");
    private static final Path ZIP_OUTPUT_DIR = Path.of("target", "preconfig-zips");
    private static final Pattern PROJECT_NAME = Pattern.compile("<name>([^<]+)</name>");
    private static final Pattern SERVICE_NAME = Pattern.compile("<serviceName>([^<]+)</serviceName>");

    /** One discovered OpenL preconfig project, already packed as an uploadable flat ZIP. */
    public record PreconfigProject(String repoName, String moduleName, String projectName,
                                   String serviceName, String label, File zip) {
    }

    /**
     * Brings every known repo up to date: clones it if missing (first run on a fresh machine),
     * otherwise {@code hg pull -u}. A failed pull logs a warning and the local copy is used;
     * a failed clone leaves the repo out of this run (discovery logs the absence).
     */
    public void syncRepositories() {
        if (!SYNC_ENABLED) {
            LOGGER.info("Preconfig hg sync disabled (-Dpreconfig.hg.sync=false); using local working copies");
            return;
        }
        File root = new File(REPOS_ROOT);
        if (!root.isDirectory() && !root.mkdirs()) {
            LOGGER.warn("Cannot create {} — using whatever local copies exist", REPOS_ROOT);
            return;
        }
        for (String repo : HG_REPOS) {
            File repoDir = new File(root, repo);
            if (repoDir.isDirectory()) {
                runHg(repo, "pull -u", "hg", "-R", repoDir.getAbsolutePath(), "pull", "-u");
            } else {
                LOGGER.info("Preconfig repo [{}] not found under {} — cloning ({} min timeout)",
                        repo, REPOS_ROOT, HG_CLONE_TIMEOUT_MINUTES);
                runHg(repo, "clone", "hg", "clone", HG_BASE_URL + repo, repoDir.getAbsolutePath());
            }
        }
    }

    private void runHg(String repo, String action, String... cmd) {
        int timeoutMinutes = action.equals("clone") ? HG_CLONE_TIMEOUT_MINUTES : HG_PULL_TIMEOUT_MINUTES;
        try {
            Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
            String output = new String(p.getInputStream().readAllBytes());
            boolean finished = p.waitFor(timeoutMinutes, TimeUnit.MINUTES);
            if (!finished) {
                p.destroyForcibly();
                LOGGER.warn("hg {} timed out for [{}]", action, repo);
            } else if (p.exitValue() != 0) {
                LOGGER.warn("hg {} failed for [{}] (exit {}): {}", action, repo, p.exitValue(), lastLine(output));
            } else {
                LOGGER.info("hg {} [{}]: {}", action, repo, lastLine(output));
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.warn("hg {} failed for [{}]: {}", action, repo, e.getMessage());
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /** Scans all repos for {@code src/main/openl/rules.xml} and zips each project for upload. */
    public List<PreconfigProject> discoverProjects() {
        List<PreconfigProject> result = new ArrayList<>();
        int skippedJarDependent = 0;
        for (String repo : HG_REPOS) {
            File repoDir = new File(REPOS_ROOT, repo);
            if (!repoDir.isDirectory()) {
                LOGGER.warn("Preconfig repo [{}] not found under {} — skipping discovery", repo, REPOS_ROOT);
                continue;
            }
            for (Path rulesXml : findRulesXml(repoDir.toPath())) {
                Path openlDir = rulesXml.getParent();
                Path moduleDir = openlDir.getParent().getParent().getParent();
                String moduleName = moduleDir.getFileName().toString();
                String rulesContent = readQuietly(rulesXml);
                String label = repo + "/" + moduleName;
                if (!MODULE_FILTER.isEmpty() && !label.contains(MODULE_FILTER)) {
                    continue;
                }
                File zip;
                if (isJarDependent(rulesContent, moduleDir)) {
                    if (!INCLUDE_JAR_DEPENDENT) {
                        skippedJarDependent++;
                        LOGGER.info("Skipping [{}] — needs domain JARs from a Maven build "
                                + "(-Dpreconfig.include.jar.dependent=false)", label);
                        continue;
                    }
                    zip = mavenBuildZip(moduleDir, label);
                    if (zip == null) {
                        skippedJarDependent++;
                        continue;
                    }
                } else {
                    zip = zipOpenlDir(openlDir, moduleName);
                }
                String projectName = extractProjectName(rulesContent, moduleName);
                String serviceName = extractServiceName(openlDir.resolve("rules-deploy.xml"));
                result.add(new PreconfigProject(repo, moduleName, projectName, serviceName, label, zip));
            }
        }
        LOGGER.info("Discovered {} preconfig OpenL project(s) ({} jar-dependent project(s) skipped)",
                result.size(), skippedJarDependent);
        return result;
    }

    private static List<Path> findRulesXml(Path repoRoot) {
        try (Stream<Path> walk = Files.walk(repoRoot)) {
            return walk.filter(p -> p.endsWith(Path.of("src", "main", "openl", "rules.xml")))
                    .filter(p -> !p.toString().contains("/target/"))
                    .sorted(Comparator.comparing(Path::toString))
                    .toList();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to scan " + repoRoot, e);
        }
    }

    /**
     * A project needs Maven-built domain JARs when rules.xml declares a {@code <classpath>} OR the
     * module pom declares {@code <dependency>} entries (provided JARs with Java types the rules use
     * — e.g. ubx-policy references ExtDimension from preconfig-ubx-policy-domain without a classpath).
     */
    private static boolean isJarDependent(String rulesXml, Path moduleDir) {
        if (rulesXml.contains("<classpath>")) {
            return true;
        }
        Path pom = moduleDir.resolve("pom.xml");
        return Files.isRegularFile(pom) && readQuietly(pom).contains("<dependency>");
    }

    /**
     * Builds the module with Maven and produces a studio-uploadable ZIP with all needed JARs.
     * <p>
     * The openl-maven-plugin ZIP contains only the module's own JAR: dependencies are
     * {@code provided} (in production these preconfigs run inside the EIS platform whose server
     * classpath has them). A bare WebStudio has no such classpath, so the DIRECT provided
     * dependencies (e.g. ipb-policy-*-openl with the domain datatypes; transitive tree would be
     * ~400 jars of the whole EIS and is neither needed nor wanted) are copied next to it and
     * repacked into the ZIP's lib/ — the exact shape of the historical preconfig snapshots.
     * <p>
     * Returns null (and logs) if any step fails — the project is skipped, not the whole run.
     */
    private static File mavenBuildZip(Path moduleDir, String label) {
        Path reactorRoot = findReactorRoot(moduleDir);
        String modulePath = reactorRoot.relativize(moduleDir).toString();
        LOGGER.info("Building [{}] with Maven (reactor {}, module {})", label, reactorRoot, modulePath);
        // install (not package): in-reactor dependencies land in the local m2, so the follow-up
        // copy-dependencies run from the module alone can resolve them.
        if (!runMaven(reactorRoot, label, "install", "-pl", modulePath, "-am", "-DskipTests")) {
            return null;
        }
        if (!runMaven(moduleDir, label, "dependency:copy-dependencies",
                "-DincludeScope=provided", "-DexcludeTransitive=true",
                "-DoutputDirectory=target/provided-lib")) {
            return null;
        }
        File zip = moduleDir.resolve("target").resolve(moduleDir.getFileName() + ".zip").toFile();
        if (!zip.isFile()) {
            LOGGER.warn("Maven build of [{}] succeeded but {} is missing — skipping the project", label, zip);
            return null;
        }
        return repackWithProvidedJars(zip, moduleDir.resolve("target").resolve("provided-lib"), label);
    }

    private static boolean runMaven(Path workDir, String label, String... goals) {
        List<String> cmd = new ArrayList<>(List.of("mvn", "-B"));
        cmd.addAll(List.of(goals));
        try {
            Process p = new ProcessBuilder(cmd)
                    .directory(workDir.toFile())
                    .redirectErrorStream(true)
                    .start();
            String output = new String(p.getInputStream().readAllBytes());
            boolean finished = p.waitFor(MAVEN_BUILD_TIMEOUT_MINUTES, TimeUnit.MINUTES);
            if (!finished || p.exitValue() != 0) {
                if (!finished) {
                    p.destroyForcibly();
                }
                LOGGER.warn("Maven {} failed for [{}] ({}); skipping the project. Last output:\n{}",
                        goals[0], label, finished ? "exit " + p.exitValue() : "timeout", tail(output, 30));
                return false;
            }
            return true;
        } catch (IOException | InterruptedException e) {
            LOGGER.warn("Maven {} failed for [{}]: {} — skipping the project", goals[0], label, e.getMessage());
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }

    /** Adds the direct provided JARs into the built ZIP's lib/ and writes the result under target/preconfig-zips. */
    private static File repackWithProvidedJars(File builtZip, Path providedLibDir, String label) {
        try {
            Files.createDirectories(ZIP_OUTPUT_DIR);
            File result = ZIP_OUTPUT_DIR.resolve(builtZip.getName()).toFile();
            try (java.util.zip.ZipInputStream in = new java.util.zip.ZipInputStream(new FileInputStream(builtZip));
                 ZipOutputStream out = new ZipOutputStream(new FileOutputStream(result))) {
                for (ZipEntry e = in.getNextEntry(); e != null; e = in.getNextEntry()) {
                    out.putNextEntry(new ZipEntry(e.getName()));
                    in.transferTo(out);
                    out.closeEntry();
                }
                if (Files.isDirectory(providedLibDir)) {
                    try (Stream<Path> jars = Files.list(providedLibDir)) {
                        for (Path jar : jars.filter(f -> f.toString().endsWith(".jar")).sorted().toList()) {
                            out.putNextEntry(new ZipEntry("lib/" + jar.getFileName()));
                            try (FileInputStream jin = new FileInputStream(jar.toFile())) {
                                jin.transferTo(out);
                            }
                            out.closeEntry();
                        }
                    }
                }
            }
            return result;
        } catch (IOException e) {
            LOGGER.warn("Failed to repack [{}] with provided JARs: {} — skipping the project", label, e.getMessage());
            return null;
        }
    }

    /**
     * Topmost ancestor of the module that contains a pom.xml (the multi-module reactor root).
     * Directories WITHOUT a pom on the way up are skipped rather than stopping the walk —
     * e.g. personalpolicy nests reactors as policy-components-preconfig/auto/ubx/<module>
     * where {@code auto/} is a plain folder but the aggregator above it owns the module.
     */
    private static Path findReactorRoot(Path moduleDir) {
        Path root = moduleDir;
        for (Path dir = moduleDir.getParent(); dir != null; dir = dir.getParent()) {
            if (Files.isRegularFile(dir.resolve("pom.xml"))) {
                root = dir;
            }
            if (Files.isDirectory(dir.resolve(".hg"))) {
                break;
            }
        }
        return root;
    }

    private static String tail(String output, int lines) {
        String[] all = output.split("\n");
        int from = Math.max(0, all.length - lines);
        return String.join("\n", java.util.Arrays.copyOfRange(all, from, all.length));
    }

    private static String extractProjectName(String rulesXml, String fallback) {
        Matcher m = PROJECT_NAME.matcher(rulesXml);
        return m.find() ? m.group(1).trim() : fallback;
    }

    private static String extractServiceName(Path rulesDeployXml) {
        if (!Files.isRegularFile(rulesDeployXml)) {
            return null;
        }
        Matcher m = SERVICE_NAME.matcher(readQuietly(rulesDeployXml));
        return m.find() ? m.group(1).trim() : null;
    }

    /** Packs the content of {@code src/main/openl} into a flat ZIP (rules.xml at zip root). */
    private static File zipOpenlDir(Path openlDir, String moduleName) {
        try {
            Files.createDirectories(ZIP_OUTPUT_DIR);
            File zipFile = ZIP_OUTPUT_DIR.resolve(moduleName + ".zip").toFile();
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
                 Stream<Path> walk = Files.walk(openlDir)) {
                for (Path file : walk.filter(Files::isRegularFile).sorted().toList()) {
                    zos.putNextEntry(new ZipEntry(openlDir.relativize(file).toString().replace(File.separatorChar, '/')));
                    try (FileInputStream in = new FileInputStream(file.toFile())) {
                        in.transferTo(zos);
                    }
                    zos.closeEntry();
                }
            }
            return zipFile;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to zip " + openlDir, e);
        }
    }

    private static String readQuietly(Path file) {
        try {
            return Files.readString(file);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read " + file, e);
        }
    }

    private static String lastLine(String output) {
        String[] lines = output.trim().split("\n");
        return lines.length == 0 ? "" : lines[lines.length - 1].trim();
    }
}
