# Java Test Automation Framework (TAF) Template

A modern, containerized test automation framework built on **TestContainers**, **REST Assured**, and **Playwright** for testing web applications with a component-based Page Object Model architecture.

## 🚀 Framework Overview

This framework provides a robust foundation for UI and API test automation with:

- **🐳 Containerized Testing**: Docker-based isolation using TestContainers
- **🔧 Component Architecture**: Reusable UI components with scoped element handling
- **📡 API Integration**: Clean REST Assured patterns with optimized logging
- **⚡ Performance Optimized**: Native Playwright waits, retry logic, and efficient element interactions
- **🎯 Test Management**: Comprehensive test lifecycle management and reporting
- **📁 File Operations**: Complete file upload/download support for both execution modes
- **🔄 Dual Execution Modes**: LOCAL and DOCKER modes with automatic detection

### Core Technologies
- **Java 21** with Maven build system
- **TestContainers** for Docker-based test environments
- **Playwright** for modern UI automation with native waiting
- **Selenium WebDriver** for legacy test support (dual-mode capability)
- **REST Assured** for API testing
- **TestNG** for test framework and parallel execution
- **Docker** for application and browser containerization

## 🏗️ Architecture Components

### 🧩 Page Object Model Rules

#### **Modern Playwright Architecture:**

#### **Pages (`PlaywrightBasePage` descendants):**
- Handle URL navigation, page-level operations, and component composition
- Use `PlaywrightDriverPool.getPage()` for unified page access across execution modes
- Support both LOCAL and DOCKER execution modes transparently

#### **Components (`PlaywrightBasePageComponent` descendants):**
- Support component scoping with root locator boundaries
- Initialize child elements using `createScopedElement()` for proper DOM scoping
- Support nested component hierarchy: Page → Component → SubComponent → Element
- Use `createScopedComponent()` for dynamic component creation with type safety

#### **Elements (`PlaywrightWebElement`):**
- Enhanced Playwright locator wrapper with built-in waiting strategies
- Support parent/child locator relationships for component scoping
- Native Playwright auto-wait eliminates custom wait logic
- Readable element names for enhanced debugging experience

#### **Legacy Selenium Support:**
- **Selenium Pages**: `BasePage` descendants for legacy tests
- **Selenium Components**: `BasePageComponent` with `@FindBy` annotation support  
- **Selenium Elements**: `SmartWebElement` with Selenium WebDriver integration
- Full backward compatibility maintained during migration period

### 🎯 Element Access Patterns

#### **Accessing Elements and Components:**
- Access should ideally be through action-oriented methods (e.g., `clickLoginButton()`, `getHeaderText()`)
- If direct access is needed, use well-defined methods (e.g., `getLogoElement()`). Avoid direct public field access
- Use fluent interfaces for component navigation and interaction

#### **Smart Element Factory:**
- Responsible for initializing elements and components based on annotations
- Handles lazy initialization and dependency injection for Page Object components

#### **No Automatic Getters:**
- Getters are not automatically generated for all private fields. Create explicit methods as needed
- Focus on behavior-driven methods rather than simple property access

## 🐳 Container Architecture

### Dual Execution Modes

#### **LOCAL Mode** (Default)
- **Playwright Host Execution**: Playwright runs on host machine for direct debugging
- **Application Container**: Isolated application instances using TestContainers
- **Port Mapping**: Dynamic port allocation with `getMappedPort()` for host access
- **File Operations**: Direct filesystem access for uploads/downloads

#### **DOCKER Mode** (CI/CD Optimized)
- **Playwright Containers**: Isolated Playwright instances using Microsoft official images
- **Application Containers**: Isolated application instances in same Docker network
- **Network Communication**: Container-to-container communication via Docker networks
- **File Operations**: Volume mapping for uploads, container extraction for downloads

### TestContainers Integration
- **Application Containers**: Isolated instances of the application under test
- **Browser Containers**: Containerized Playwright instances for complete isolation
- **Network Isolation**: Docker networks for secure container communication
- **Automatic Mode Detection**: Framework selects appropriate execution mode based on configuration

### Container Lifecycle
1. **Test Setup**: Creates Docker network, starts application and browser containers
2. **Test Execution**: Runs tests with native Playwright waits against containerized application
3. **Test Teardown**: Collects logs, takes screenshots on failure, cleans up containers

## 📡 API Testing Architecture

### REST Assured Integration
- **ApiBaseMethod**: Base class for all API testing with standard patterns
- **Clean Logging**: RestAssuredFilter provides structured request/response logging
- **JSON Handling**: JsonPath extraction with support for dotted field names
- **Error Handling**: Comprehensive error handling with meaningful failure messages

### Application Monitoring
- **GetApplicationInfoMethod**: Automated application version logging
- **Health Checks**: API-based application health monitoring
- **Container Communication**: Proper HTTP communication using mapped ports

## 🎪 Admin UI Component Library

Complete implementation of Admin UI components for OpenL Tablets WebStudio:

### Core Admin Components
- **SystemSettingsPageComponent**: System configuration and database setup
- **UsersPageComponent**: User management with creation, editing, and deletion
- **SecurityPageComponent**: Authentication modes (Single, Multi, AD, SAML, OAuth2)
- **EmailPageComponent**: SMTP configuration and email verification
- **MyProfilePageComponent**: User profile management and password changes
- **MySettingsPageComponent**: Personal preferences and display settings
- **RepositoriesPageComponent**: Git repository configuration with Builder pattern
- **NotificationPageComponent**: Notification preferences and management
- **TagsPageComponent**: Tag creation, categorization, and management

### Navigation System
- **AdminNavigationComponent**: Enum-based navigation with consistent routing
- **AdminPage**: Central coordinator for all admin functionality
- **CurrentUserComponent**: User menu integration with admin navigation

### Architecture Features
- **Global Confirmation Handling**: Unified ConfirmationPopupComponent accessible from BasePageComponent
- **Builder Patterns**: Complex configuration objects for multi-parameter operations
- **HashMap Optimizations**: Performance-optimized selections replacing switch statements
- **Wait Strategy Optimization**: Eliminated redundant waits, leveraging SmartWebElement retry logic

## 🔧 Configuration & Setup

### Build Commands
```bash
# Compile the project
mvn clean compile

# Run all tests (Playwright LOCAL mode by default)
mvn clean test

# Run specific test suite
mvn clean test -Dsuite=<suite_name>

# Run single test with execution mode
mvn clean test -Dtest=<TestClassName>

# Playwright execution modes
mvn clean test -Dexecution.mode=PLAYWRIGHT_LOCAL   # Default: Host Playwright
mvn clean test -Dexecution.mode=PLAYWRIGHT_DOCKER  # Container Playwright
mvn clean test -Dexecution.mode=SELENIUM           # Legacy Selenium mode

# Example: Run admin email test in Docker mode
mvn clean test -Dtest=TestPlaywrightAdminEmail -Dexecution.mode=PLAYWRIGHT_DOCKER
```

### Key Configuration Files
- `src/test/resources/config.properties`: Main configuration with container, database, and test settings
- `src/test/resources/testng_suites/`: TestNG suite definitions for different test categories
- `src/test/resources/test_data/`: Test data organized by test class names

### Essential Properties
- `web_element_explicit_wait`: Default element wait timeout (10 seconds)
- `test_retry_count`: Number of retry attempts for failed tests
- `browser`: Browser type (chrome/firefox)
- `default_app_port`: Application container port  
- `deployed_app_path`: Application context path
- `host_resource_path`: Test data directory (src/test/resources)
- `container_resource_path`: Container volume mount path (/test_resources)
- `execution.mode`: Execution mode (PLAYWRIGHT_LOCAL/PLAYWRIGHT_DOCKER/SELENIUM)

### Playwright-Specific Properties
- `enable_screenshot_on_failure`: Automatic screenshot capture (true)
- `enable_video_recording`: Video recording for test sessions (false)
- `playwright_downloads_path`: Download directory (target/downloads)
- `playwright_videos_path`: Video storage directory (target/videos)

## 📊 Test Execution & Reporting

### Test Lifecycle
1. **Mode Detection**: Automatic execution mode detection based on configuration
2. **Container Setup**: Docker container orchestration for app and browser (if needed)
3. **Driver Initialization**: Playwright or Selenium driver setup with unified interface
4. **Test Execution**: Native Playwright waits with component-scoped element interactions
5. **File Operations**: Seamless file upload/download support across execution modes
6. **Result Collection**: Enhanced screenshot capture, log collection, and ReportPortal integration  
7. **Container Cleanup**: Automatic resource cleanup and container termination

### Reporting Integration
- **ReportPortal**: Advanced test reporting with screenshots and logs
- **Jenkins Pipeline**: Parallel execution support with artifact publishing
- **HTML Reports**: Detailed test execution reports with failure analysis

## 🚀 Getting Started

1. **Prerequisites**: Java 21, Maven, Docker
2. **Clone Repository**: `git clone <repository-url>`
3. **Configure Properties**: Update `src/test/resources/config.properties`
4. **Choose Execution Mode**:
   - **LOCAL** (default): `mvn clean test -Dsuite=studio_smoke`
   - **DOCKER**: `mvn clean test -Dsuite=studio_smoke -Dexecution.mode=PLAYWRIGHT_DOCKER`
5. **View Reports**: Check `target/` directory for test reports, screenshots, and videos

## 🎯 Best Practices

### Component Development
- Follow existing component patterns and naming conventions
- Use descriptive method names (get/set/enable/disable/click)
- Implement proper error handling and wait conditions
- Maintain consistent coding style with existing components
- Use dynamic locators with `.format()` for parameterized elements

### Test Development
- Extend `BaseTest` for container management
- Use Page Object Model patterns consistently
- Leverage component composition for complex UI interactions
- Implement proper test data management and cleanup
- Add meaningful assertions and error messages

### Performance Optimization
- Leverage SmartWebElement's built-in waiting strategies
- Use HashMap mappings for performance-critical selections
- Implement Builder patterns for complex operations
- Avoid redundant waits and unnecessary element interactions

---

**This framework provides a production-ready foundation for scalable test automation with modern containerized architecture, clean API integration, and comprehensive UI component libraries.** 🎉