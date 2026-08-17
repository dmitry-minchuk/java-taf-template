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

public class PreconfigSourcesService {

    private static final Logger LOGGER = LogManager.getLogger(PreconfigSourcesService.class);

    public static final String REPOS_ROOT = System.getProperty("preconfig.repos.root",
            "/Users/dmitryminchuk/Projects/eis/preconfigs");
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
    private static final int MAVEN_VERSION_TIMEOUT_MINUTES = 1;
    private static final String MODULE_FILTER = System.getProperty("preconfig.module.filter", "");
    private static final int HG_PULL_TIMEOUT_MINUTES = 15;
    private static final int HG_CLONE_TIMEOUT_MINUTES = 60;
    private static final String HG_BASE_URL = System.getProperty("preconfig.hg.base.url",
            "http://vno-hg.exigengroup.com/hg/");
    private static final Path ZIP_OUTPUT_DIR = Path.of("target", "preconfig-zips");
    private static final Pattern PROJECT_NAME = Pattern.compile("<name>([^<]+)</name>");
    private static final Pattern SERVICE_NAME = Pattern.compile("<serviceName>([^<]+)</serviceName>");

    public record PreconfigProject(String repoName, String moduleName, String projectName,
                                   String serviceName, String label, File zip, String buildFailure) {

        public PreconfigProject {
            if ((zip == null) == (buildFailure == null)) {
                throw new IllegalArgumentException("A preconfig project carries either an uploadable ZIP or the "
                        + "reason it has none, never both and never neither: " + label);
            }
        }

        public static PreconfigProject packed(String repoName, String moduleName, String projectName,
                                              String serviceName, String label, File zip) {
            return new PreconfigProject(repoName, moduleName, projectName, serviceName, label, zip, null);
        }

        public static PreconfigProject unbuildable(String repoName, String moduleName, String projectName,
                                                   String serviceName, String label, String buildFailure) {
            return new PreconfigProject(repoName, moduleName, projectName, serviceName, label, null, buildFailure);
        }
    }

    private record BuildOutcome(File zip, String failure) {

        static BuildOutcome built(File zip) {
            return new BuildOutcome(zip, null);
        }

        static BuildOutcome failed(String failure) {
            return new BuildOutcome(null, failure);
        }
    }

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

    public List<PreconfigProject> discoverProjects() {
        List<PreconfigProject> result = new ArrayList<>();
        int skippedJarDependent = 0;
        int failedPreparations = 0;
        int filteredOut = 0;
        if (INCLUDE_JAR_DEPENDENT) {
            LOGGER.info("The Maven stage will run with [{}] (JAVA_HOME={}); the EIS preconfig modules need JDK 25",
                    mavenJavaVersion(), System.getenv("JAVA_HOME"));
        }
        for (String repo : HG_REPOS) {
            File repoDir = new File(REPOS_ROOT, repo);
            if (!repoDir.isDirectory()) {
                LOGGER.warn("Preconfig repo [{}] not found under {} — skipping discovery", repo, REPOS_ROOT);
                continue;
            }
            List<Path> rulesFiles;
            try {
                rulesFiles = findRulesXml(repoDir.toPath());
            } catch (RuntimeException e) {
                String scanLabel = repo + "/(repository scan)";
                if (!MODULE_FILTER.isEmpty() && !scanLabel.contains(MODULE_FILTER)) {
                    filteredOut++;
                    continue;
                }
                failedPreparations++;
                String failure = String.format("Scanning %s for OpenL projects failed, so none of its projects "
                        + "could be validated: %s", repoDir, e);
                LOGGER.warn("[{}] — it will be reported as a failed test. {}", scanLabel, failure);
                result.add(PreconfigProject.unbuildable(repo, "(repository scan)", repo, null, scanLabel, failure));
                continue;
            }
            for (Path rulesXml : rulesFiles) {
                String label = repo + "/" + repoDir.toPath().relativize(rulesXml);
                try {
                    Path openlDir = rulesXml.getParent();
                    Path moduleDir = openlDir.getParent().getParent().getParent();
                    String moduleName = moduleDir.getFileName().toString();
                    label = repo + "/" + moduleName;
                    if (!MODULE_FILTER.isEmpty() && !label.contains(MODULE_FILTER)) {
                        filteredOut++;
                        continue;
                    }
                    String rulesContent = readQuietly(rulesXml);
                    String projectName = extractProjectName(rulesContent, moduleName);
                    String serviceName = extractServiceName(openlDir.resolve("rules-deploy.xml"));
                    if (isJarDependent(rulesContent, moduleDir)) {
                        if (!INCLUDE_JAR_DEPENDENT) {
                            skippedJarDependent++;
                            LOGGER.info("Skipping [{}] — needs domain JARs from a Maven build "
                                    + "(-Dpreconfig.include.jar.dependent=false)", label);
                            continue;
                        }
                        BuildOutcome outcome = mavenBuildZip(moduleDir, label);
                        if (outcome.failure() != null) {
                            failedPreparations++;
                            result.add(PreconfigProject.unbuildable(repo, moduleName, projectName, serviceName,
                                    label, outcome.failure()));
                            continue;
                        }
                        result.add(PreconfigProject.packed(repo, moduleName, projectName, serviceName,
                                label, outcome.zip()));
                    } else {
                        result.add(PreconfigProject.packed(repo, moduleName, projectName, serviceName,
                                label, zipOpenlDir(openlDir, moduleName)));
                    }
                } catch (RuntimeException e) {
                    failedPreparations++;
                    String moduleName = label.substring(label.indexOf('/') + 1);
                    String failure = String.format("Preparing the sources at %s failed: %s", rulesXml, e);
                    LOGGER.warn("[{}] — it will be reported as a failed test. {}", label, failure);
                    result.add(PreconfigProject.unbuildable(repo, moduleName, moduleName, null, label, failure));
                }
            }
        }
        LOGGER.info("Discovered {} preconfig OpenL project(s): {} ready to validate, {} that could not be prepared "
                        + "(reported as failed tests), {} skipped by -Dpreconfig.include.jar.dependent=false, "
                        + "{} left out by -Dpreconfig.module.filter={}",
                result.size(), result.size() - failedPreparations, failedPreparations, skippedJarDependent,
                filteredOut, MODULE_FILTER.isEmpty() ? "(unset)" : MODULE_FILTER);
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

    private static boolean isJarDependent(String rulesXml, Path moduleDir) {
        if (rulesXml.contains("<classpath>")) {
            return true;
        }
        Path pom = moduleDir.resolve("pom.xml");
        return Files.isRegularFile(pom) && readQuietly(pom).contains("<dependency>");
    }

    private static BuildOutcome mavenBuildZip(Path moduleDir, String label) {
        Path reactorRoot = findReactorRoot(moduleDir);
        String modulePath = reactorRoot.relativize(moduleDir).toString();
        LOGGER.info("Building [{}] with Maven (reactor {}, module {})", label, reactorRoot, modulePath);
        String installFailure = runMaven(reactorRoot, label,
                "install", "-pl", modulePath, "-am", "-DskipTests");
        if (installFailure != null) {
            return BuildOutcome.failed(installFailure);
        }
        String copyFailure = runMaven(moduleDir, label, "dependency:copy-dependencies",
                "-DincludeScope=provided", "-DexcludeTransitive=true",
                "-DoutputDirectory=target/provided-lib");
        if (copyFailure != null) {
            return BuildOutcome.failed(copyFailure);
        }
        File zip = moduleDir.resolve("target").resolve(moduleDir.getFileName() + ".zip").toFile();
        if (!zip.isFile()) {
            String failure = "The Maven build succeeded but produced no deployable ZIP at " + zip;
            LOGGER.warn("[{}] — it will be reported as a failed test. {}", label, failure);
            return BuildOutcome.failed(failure);
        }
        return repackWithProvidedJars(zip, moduleDir.resolve("target").resolve("provided-lib"), label);
    }

    private static String runMaven(Path workDir, String label, String... goals) {
        List<String> cmd = new ArrayList<>(List.of("mvn", "-B"));
        cmd.addAll(List.of(goals));
        Path outputFile = null;
        Process p = null;
        try {
            outputFile = Files.createTempFile("preconfig-maven-", ".log");
            p = new ProcessBuilder(cmd)
                    .directory(workDir.toFile())
                    .redirectErrorStream(true)
                    .redirectOutput(outputFile.toFile())
                    .start();
            boolean finished = p.waitFor(MAVEN_BUILD_TIMEOUT_MINUTES, TimeUnit.MINUTES);
            if (!finished) {
                p.destroyForcibly().waitFor();
            }
            if (!finished || p.exitValue() != 0) {
                String reason = finished
                        ? "exit " + p.exitValue()
                        : "no result within " + MAVEN_BUILD_TIMEOUT_MINUTES + " min, so it was killed";
                String failure = String.format("Maven %s failed in %s (%s). Last output:%n%s",
                        goals[0], workDir, reason, tail(readIfPossible(outputFile), 30));
                LOGGER.warn("Maven {} failed for [{}] — it will be reported as a failed test. {}",
                        goals[0], label, failure);
                return failure;
            }
            return null;
        } catch (IOException | InterruptedException e) {
            if (p != null && p.isAlive()) {
                p.destroyForcibly();
            }
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            String failure = String.format("Maven %s could not be run in %s: %s", goals[0], workDir, e);
            LOGGER.warn("Maven {} could not be run for [{}] — it will be reported as a failed test. {}",
                    goals[0], label, failure);
            return failure;
        } finally {
            deleteIfPossible(outputFile);
        }
    }

    private static String mavenJavaVersion() {
        Path outputFile = null;
        Process p = null;
        try {
            outputFile = Files.createTempFile("preconfig-maven-version-", ".log");
            p = new ProcessBuilder("mvn", "-v")
                    .redirectErrorStream(true)
                    .redirectOutput(outputFile.toFile())
                    .start();
            if (!p.waitFor(MAVEN_VERSION_TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
                p.destroyForcibly().waitFor();
                return "mvn -v gave no answer within " + MAVEN_VERSION_TIMEOUT_MINUTES + " min";
            }
            return readIfPossible(outputFile).lines()
                    .filter(line -> line.startsWith("Java version:"))
                    .findFirst()
                    .orElse("mvn -v printed no Java version");
        } catch (IOException | InterruptedException e) {
            if (p != null && p.isAlive()) {
                p.destroyForcibly();
            }
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return "mvn -v could not be run: " + e;
        } finally {
            deleteIfPossible(outputFile);
        }
    }

    private static String readIfPossible(Path file) {
        try {
            return Files.readString(file);
        } catch (IOException e) {
            return "(the Maven output could not be read back from " + file + ": " + e.getMessage() + ")";
        }
    }

    private static void deleteIfPossible(Path file) {
        if (file == null) {
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            LOGGER.warn("Could not delete the temporary Maven log {}: {}", file, e.getMessage());
        }
    }

    private static BuildOutcome repackWithProvidedJars(File builtZip, Path providedLibDir, String label) {
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
            return BuildOutcome.built(result);
        } catch (IOException e) {
            String failure = String.format("Failed to repack %s with the provided JARs from %s: %s",
                    builtZip, providedLibDir, e.getMessage());
            LOGGER.warn("[{}] — it will be reported as a failed test. {}", label, failure);
            return BuildOutcome.failed(failure);
        }
    }

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
