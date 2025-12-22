# Java Test Automation Framework (Playwright + TestContainers)

A Java test automation framework built with **Playwright**, **TestNG**, **TestContainers**, and modern testing technologies. This framework supports both local and Docker-based execution modes with unified driver management.

## 🏗️ Architecture Overview

```
Components → DriverPool (Unified Interface)
                    ↓
            [Automatic Mode Detection]
                    ↓
    LOCAL Mode → Direct Playwright → Container App
    DOCKER Mode → Container Playwright → Container App
                    ↓
            [File Operations Support]
                    ↓
    Upload: Volume Mapping + TestDataUtil
    Download: DownloadUtil (mode-aware)
```

## ✨ Key Features

- **Dual Execution Modes**: `PLAYWRIGHT_LOCAL` (default) and `PLAYWRIGHT_DOCKER` with automatic detection
- **Unified Driver Pool**: Single interface for both execution modes
- **TestContainers Integration**: Application containers with network isolation
- **Parallel Test Execution**: TestNG parallel execution with configurable thread counts
- **Report Portal Integration**: Enhanced reporting with screenshots, videos, and logs
- **Comprehensive Configuration**: Property-based configuration with environment override support
- **Cross-Browser Support**: Chromium, Firefox, and WebKit browsers
- **File Upload/Download Support**: Mode-aware file operations with volume mapping

## 🚀 Quick Start

### Prerequisites

- **Java 21** or higher
- **Maven 3.8+**
- **Docker** (for Docker execution mode and application containers)

### Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd java-taf-template
   ```

2. **Install Playwright browsers** (for local execution)
   ```bash
   mvn clean test -Dtest=TestAdminEmail -Dexecution.mode=PLAYWRIGHT_LOCAL
   ```

3. **Verify Docker setup** (for Docker execution mode)
   ```bash
   docker pull ghcr.io/openl-tablets/webstudio:6.0.0-d0f5599b68ba
   ```

## 📋 Test Execution Guide

### Execution Modes

The framework supports two execution modes controlled by the `execution.mode` system property:

- **`PLAYWRIGHT_LOCAL`** (default): Playwright runs on host machine, faster startup
- **`PLAYWRIGHT_DOCKER`**: Playwright runs in Docker containers, better isolation

### Running Test Suites

#### Available Test Suites
Located in `src/test/resources/testng_suites/`:
- `studio_smoke.xml` - Smoke tests with parallel execution (3 threads)
- `studio_issues.xml` - Issue regression tests  
- `rules_editor.xml` - Rules editor functionality tests

#### Suite Execution Examples
```bash
# Run smoke tests in LOCAL mode (default)
mvn clean test -Dsuite=studio_smoke

# Run smoke tests with explicit mode
mvn clean test -Dsuite=studio_smoke -Dexecution.mode=PLAYWRIGHT_LOCAL
mvn clean test -Dsuite=studio_smoke -Dexecution.mode=PLAYWRIGHT_DOCKER

# Run other suites
mvn clean test -Dsuite=studio_issues -Dexecution.mode=PLAYWRIGHT_LOCAL
mvn clean test -Dsuite=rules_editor -Dexecution.mode=PLAYWRIGHT_DOCKER
```

### Running Individual Tests

#### Single Test Class
```bash
# Local mode
mvn clean test -Dtest=TestAdminEmail -Dexecution.mode=PLAYWRIGHT_LOCAL

# Docker mode  
mvn clean test -Dtest=TestAdminEmail -Dexecution.mode=PLAYWRIGHT_DOCKER
```

#### Single Test Method
```bash
mvn clean test -Dtest=TestAdminEmail#testPlaywrightAdminEmail -Dexecution.mode=PLAYWRIGHT_LOCAL
```

#### Multiple Test Classes
```bash
mvn clean test -Dtest=TestAdminEmail,TestUserSettingsAndDetails -Dexecution.mode=PLAYWRIGHT_LOCAL
```

### Parallel Execution Verification

Tests support parallel execution with thread-safe driver management:
- **LOCAL Mode**: Multiple Playwright instances on host machine
- **DOCKER Mode**: Multiple Docker containers with isolated Playwright instances

Look for log entries indicating parallel execution:
```
[TestNG-test-1] [INFO] Initializing test with Playwright: testPlaywrightAdminEmail
[TestNG-test-2] [INFO] Initializing test with Playwright: testPlaywrightUserSettings
```

## ⚙️ Configuration

### Core Configuration (`src/test/resources/config.properties`)

#### Browser & Execution Settings
```properties
# Browser configuration
browser=chrome                          # chrome, firefox, webkit
browser_version=latest
playwright_default_timeout=5000        # Default timeout in ms
test_retry_count=1                      # Test retry attempts
```

#### Container & Application Settings  
```properties
# Application container
default_app_port=8080
docker_image_name=ghcr.io/openl-tablets/webstudio:6.0.0-d0f5599b68ba
deployed_app_path=                      # App context path (empty for root)

# Volume mappings
host_resource_path=src/test/resources
container_resource_path=/test_resources
host_screenshot_path=target/screenshots
host_app_logs_path=target/logs
```

#### Media & Reporting
```properties
# Playwright media capture
enable_video_recording=true             # Enable video for failed tests (Docker mode)
enable_screenshot_on_failure=true      # Screenshot on test failure
playwright_downloads_path=target/downloads
playwright_videos_path=target/videos
```

#### User Test Data
```properties
user_pool=admin_user::openl_1_user::openl_2_user

admin_user.login=admin
admin_user.password=admin

openl_1_user.login=openl_1
openl_1_user.password=h1plaKvaska
```

### Environment Override

System properties override configuration file values:
```bash
mvn test -Dexecution.mode=PLAYWRIGHT_DOCKER -Dbrowser=firefox -Dplaywright_default_timeout=10000
```

## 🏢 Project Structure

```
├── src/
│   ├── main/java/
│   │   ├── configuration/
│   │   │   ├── annotations/           # Custom annotations (@AppContainerConfig)
│   │   │   ├── appcontainer/          # TestContainers app management  
│   │   │   ├── core/ui/               # Core UI components (WebElement, CoreComponent)
│   │   │   ├── driver/                # Driver pools (LocalDriverPool, DockerDriverPool)
│   │   │   ├── listeners/             # TestNG listeners and retry analyzers
│   │   │   ├── network/               # Docker network management
│   │   │   └── projectconfig/         # Configuration management
│   │   ├── domain/
│   │   │   ├── api/                   # API test methods
│   │   │   ├── serviceclasses/        # Service classes and constants
│   │   │   └── ui/webstudio/         # Page objects and components
│   │   │       ├── components/        # UI components by functionality
│   │   │       └── pages/             # Page objects
│   │   └── helpers/
│   │       ├── service/               # Business logic services
│   │       └── utils/                 # Utility classes
│   └── test/
│       ├── java/tests/
│       │   ├── BaseTest.java          # Base test class with setup/teardown
│       │   └── ui/webstudio/          # Test classes organized by functionality
│       └── resources/
│           ├── config.properties       # Main configuration
│           ├── testng_suites/         # TestNG suite definitions
│           └── test_data/             # Test data files
```

## 🧪 Writing Tests

### Basic Test Class

```java
@Test
@TestCaseId("TEST-001")
@Description("Test admin email configuration")
@AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
public void testAdminEmail() {
    // Login and navigate
    String projectName = WorkflowService.loginCreateProjectFromZip(User.ADMIN, "test-project.zip");
    
    // Use page objects
    AdminPage adminPage = new AdminPage();
    adminPage.navigateToEmailSettings();
    
    // Use components  
    EmailPageComponent emailComponent = adminPage.getEmailPageComponent();
    emailComponent.setEmailUrl("smtp.example.com");
    emailComponent.applySettings();
    
    // Assertions
    assertThat(emailComponent.isSettingsSaved()).isTrue();
}
```

### Container Configuration

```java
// use exact additionalContainerConfig named field to create additional parameters which will be added on runtime by reflection in BaseTest
private static final Map<String, String> additionalContainerConfig = new HashMap<>(Map.ofEntries(
        Map.entry("production-repository.base.path", "TestWebservicesDeployUI")
));

// Use annotation for container setup
@AppContainerConfig(
    startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS,
    copyFileFromPath = "test-data/project.zip", 
    copyFileToContainerPath = "/opt/project.zip"
)

// Or use different configurations
@AppContainerConfig(startParams = AppContainerStartParameters.SAML_STUDIO_PARAMS)
@AppContainerConfig(startParams = AppContainerStartParameters.OAUTH_STUDIO_PARAMS)
```

### File Operations

```java
// File upload (mode-aware)
String testDataPath = TestDataUtil.getTestDataPath("project-template.zip");
zipComponent.selectFile(testDataPath);

// File download (mode-aware)
File downloadedFile = LocalDriverPool.downloadFile(downloadButton);
assertThat(downloadedFile).exists();
```

### DataProvider Tests with Unique Names

The framework automatically generates **unique test names** for DataProvider iterations in ReportPortal reports. This ensures each data set appears as a separate test instead of having identical names.

#### Problem Solved

**Before:** All DataProvider iterations appeared with the same name in ReportPortal
```
ReportPortal shows:
- testLocalZippedProjects
- testLocalZippedProjects
- testLocalZippedProjects
```

**After:** Each iteration has a unique, descriptive name
```
ReportPortal shows:
- testLocalZippedProjects[project1, project2]
- testLocalZippedProjects[claims-home, auto-policy]
- testLocalZippedProjects[commercial-underwriting]
```

#### How It Works

1. **BaseTest** implements `ITest` interface for custom test names
2. Names are generated **before** test execution in `@BeforeMethod`
3. Parameters are sanitized (filenames extracted, extensions removed, long values truncated)
4. Thread-safe for parallel execution using `ThreadLocal`

#### Usage Example

Simply extend `BaseTest` - no additional code needed:

```java
public class TestWithDataProvider extends BaseTest {

    @Test(dataProvider = "ProjectData")
    public void testMultipleProjects(String path1, String path2, String path3) {
        // ReportPortal will show: testMultipleProjects[project1, project2, project3]
        // Test logic here
    }

    @DataProvider(name = "ProjectData")
    public Object[][] getData() {
        return new Object[][] {
            {"/path/to/project1.zip", "/path/to/project2.zip", null},
            {"project-A.zip", "project-B.zip", "project-C.zip"}
        };
    }
}
```

#### Parameter Sanitization

| Original Parameter | ReportPortal Display |
|-------------------|---------------------|
| `/Users/user/Projects/file.zip` | `file` |
| `very-long-parameter-name-exceeding-50-chars...` | `very-long-parameter-name-exceeding-50-cha...` |
| `null` | `null` |

#### Compatibility

- ✅ Works with **all tests** extending BaseTest
- ✅ Tests **without DataProvider** work unchanged (standard method names)
- ✅ Thread-safe for **parallel execution**
- ✅ Compatible with ReportPortal agent-java-testng **5.3.2+**

## 🔧 Driver Management

### Automatic Mode Detection

The `LocalDriverPool` class provides unified access to Playwright functionality with automatic mode detection:

```java
// These methods work in both LOCAL and DOCKER modes
Page page = LocalDriverPool.getPage();
Browser browser = LocalDriverPool.getBrowser(); 
BrowserContext context = LocalDriverPool.getBrowserContext();

// Navigation (mode-aware URL handling)
LocalDriverPool.navigateToApp();          // Uses correct URL for each mode
LocalDriverPool.navigateTo("https://example.com");

// Utilities
byte[] screenshot = LocalDriverPool.takeScreenshot();
Page newPage = LocalDriverPool.createNewPage();
```

### Execution Mode Differences

| Feature | LOCAL Mode | DOCKER Mode |
|---------|------------|-------------|
| **Playwright Location** | Host machine | Docker container |
| **Application URL** | `localhost:mappedPort` | `container-network-url` |
| **File Access** | Direct host filesystem | Volume-mapped paths |
| **Performance** | Faster startup | Better isolation |
| **Debugging** | Easier browser inspection | Containerized debugging |
| **Video Recording** | Not available | Available for failed tests |

## 📊 Reporting & Debugging

### Report Portal Integration

The framework integrates with Report Portal for enhanced test reporting:

- **Screenshots**: Automatically attached on test failures
- **Videos**: Recorded for failed tests (Docker mode only)  
- **Page Content**: HTML content captured on failures
- **Application Logs**: Container logs attached to test results
- **Execution Info**: Debug information about driver state

### Debug Information

```java
// Get current execution mode and debug info
LocalDriverPool.ExecutionMode mode = LocalDriverPool.getCurrentExecutionMode();
String debugInfo = LocalDriverPool.getDebugInfo();
logger.info("Current mode: {}, Debug: {}", mode, debugInfo);
```

### Log Analysis

Application logs are automatically collected from containers:
```bash
# Logs are saved to configured path
target/logs/app-container-<timestamp>.log
```

## 🐛 Troubleshooting

### Common Issues

1. **Docker connectivity issues**
   ```bash
   # Verify Docker is running
   docker ps
   
   # Check Docker image availability
   docker pull ghcr.io/openl-tablets/webstudio:6.0.0-d0f5599b68ba
   ```

2. **Port conflicts** 
   - Application containers use random ports to avoid conflicts
   - Check mapped ports in logs: `"App URL for LOGIN service (LOCAL): http://localhost:32769"`

3. **File upload/download issues**
   - Verify volume mappings in configuration
   - Check host resource path permissions
   - Ensure container resource path is writable

4. **Parallel execution failures**
   - Each thread gets isolated Playwright instances
   - Docker mode creates separate containers per thread
   - Check thread-count in TestNG suite configuration

### Debug Mode

Run with increased logging:
```bash
mvn test -Dexecution.mode=PLAYWRIGHT_LOCAL -Dorg.slf4j.simpleLogger.defaultLogLevel=DEBUG
```

## 🤝 Contributing

1. Follow existing code patterns and naming conventions
2. Add tests for new functionality  
3. Update configuration documentation
4. Ensure both execution modes work correctly
5. Add appropriate logging and error handling

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.