# Java Test Automation Framework (TAF) Template

A modern, containerized test automation framework built on **TestContainers**, **REST Assured**, and **Selenium WebDriver** for testing web applications with a component-based Page Object Model architecture.

## 🚀 Framework Overview

This framework provides a robust foundation for UI and API test automation with:

- **🐳 Containerized Testing**: Docker-based isolation using TestContainers
- **🔧 Component Architecture**: Reusable UI components with smart element handling
- **📡 API Integration**: Clean REST Assured patterns with optimized logging
- **⚡ Performance Optimized**: Built-in waits, retry logic, and efficient element interactions
- **🎯 Test Management**: Comprehensive test lifecycle management and reporting

### Core Technologies
- **Java 21** with Maven build system
- **TestContainers** for Docker-based test environments
- **Selenium WebDriver** for UI automation
- **REST Assured** for API testing
- **TestNG** for test framework and parallel execution
- **Docker** for application and browser containerization

## 🏗️ Architecture Components

### 🧩 Page Object Model Rules

#### **Pages (`BasePage` descendants):**
- Must have a public constructor with `super()` reference to parent constructor in BasePage
- Elements and Components within Pages are initialized using `@FindBy` annotations within the Page class
- Handle URL navigation, page-level operations, and component composition

#### **Components (`BasePageComponent` descendants):**
- Must have a public no-argument constructor
- Instances are declared as fields and initialized **solely** using `@FindBy` annotations within a Page or another Component
- Direct instantiation of Components using `new` within Page or Component classes is **prohibited**
- The `init(WebDriver driver, By locator)` method (inherited from `BasePageComponent`) is called by `SmartPageFactory` for initialization
- Provide reusable UI functionality across multiple pages

#### **Elements (`SmartWebElement`):**
- Instances are initialized directly using `new SmartWebElement(driver, locator, root)` within Page or Component classes
- Declared as private fields annotated with `@FindBy`, `@FindBys`, or `@FindAll`
- Enhanced WebElement wrapper with built-in waiting strategies and retry logic

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

### TestContainers Integration
- **Application Containers**: Isolated instances of the application under test
- **Selenium Containers**: Containerized WebDriver instances for browser automation
- **Network Isolation**: Docker networks for secure container communication
- **Port Mapping**: Dynamic port allocation with `getMappedPort()` for external access

### Container Lifecycle
1. **Test Setup**: Creates Docker network, starts application and Selenium containers
2. **Test Execution**: Runs tests with containerized browser against containerized application
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

# Run all tests
mvn clean test

# Run specific test suite
mvn clean test -Dsuite=<suite_name>

# Run single test
mvn clean test -Dtest=<TestClassName>
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

## 📊 Test Execution & Reporting

### Test Lifecycle
1. **Container Setup**: Automatic Docker container orchestration
2. **Element Initialization**: Smart factory-based element and component initialization
3. **Test Execution**: Robust test execution with built-in retry mechanisms
4. **Result Collection**: Screenshot capture, log collection, and reporting integration
5. **Container Cleanup**: Automatic resource cleanup and container termination

### Reporting Integration
- **ReportPortal**: Advanced test reporting with screenshots and logs
- **Jenkins Pipeline**: Parallel execution support with artifact publishing
- **HTML Reports**: Detailed test execution reports with failure analysis

## 🚀 Getting Started

1. **Prerequisites**: Java 21, Maven, Docker
2. **Clone Repository**: `git clone <repository-url>`
3. **Configure Properties**: Update `src/test/resources/config.properties`
4. **Run Tests**: `mvn clean test -Dsuite=studio_smoke`
5. **View Reports**: Check `target/` directory for test reports and screenshots

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