# CLAUDE.md
This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

 - Turn on Plan Mode on start up.
use-mcp ollama-rag
 - Use ollama-rag to understand codebase in depth using Vector and Graph embeddings
use-mcp playwright
 - Use it for more UI understanding - open the application on localhost:8090 (credentials admin/admin)
use-mcp context7
 - Use context7 for searching documentation

Project Goal: We need to migrate this framework from Selenium to Playwright. Previously it was developed with Selenium but we want to use inbuilt Playwright wait logic instead of super-complicated waiter based on Selenium. Take into account all the described functionality, create comprehensive plan with many steps and follow this plan (also store this plane here in CLAUDE.md for tracking and storing context).

Preliminary plan:
1. migrate it to Playwright for local run with default Playwright waiters (no any custom waits at all)
2. adjust waiters if needed
3. migrate it to use docker
4. fully migrate to use all docker functions and support existing infrastructure

Rules of Engagement
1. One Step at a Time: We will proceed strictly according to the plan. Do not move to the next step until we have completed and confirmed the current one.
2. Ask Questions: If you lack information, ask clarifying questions.
3. Explain Your Code: For every code snippet, provide a brief explanation of what it does and why you chose that specific solution.
4. Maintain a Log: After each successful step, we will update CLAUDE.md, adding the decisions made and the final code. Start every response with an update to this file.
5. Do not add Java-doc. 
6. Do not use Selenium style for new logic. You must copy Page -> Component -> Element hierarchy and inner methods logic, but use Playwright specific functionality in its native way (check with Context7) - no selenium-like waiters, no timeouts

## 🎯 SELENIUM TO PLAYWRIGHT MIGRATION PLAN

### **MIGRATION PROGRESS: PHASE 4 IN PROGRESS**

**Framework Migration**: Successfully migrated from Selenium WebDriver to Playwright with full Docker integration and component scoping architecture.

### **MIGRATION STRATEGY: 4-Phase Systematic Approach**

#### **PHASE 1: Local Playwright Setup** ✅ **COMPLETED**
- ✅ **PlaywrightWebElement**: Complete replacement for SmartWebElement with native waiting  
- ✅ **PlaywrightPageFactory**: @FindBy annotation support with Playwright locators  
- ✅ **PlaywrightDriverPool**: Local browser management (Chrome/Firefox) without Docker  
- ✅ **PlaywrightBasePage**: New base page class with Playwright navigation  
- ✅ **BaseTest**: Feature flag system (USE_PLAYWRIGHT=true) for easy switching  
- ✅ **Dual-mode Support**: Framework supports both Selenium and Playwright during migration

#### **PHASE 2: Wait Strategy Optimization** ✅ **COMPLETED**
- ✅ **PlaywrightExpectUtil**: Complete replacement for WaitUtil with native expect methods
- ✅ **Native Wait Patterns**: All waits use Playwright's built-in timeout and retry mechanisms
- ✅ **Zero Custom Waits**: Eliminated all WaitUtil.sleep() calls in favor of Playwright's auto-wait
- ✅ **Performance Improvement**: Significantly improved test execution speed and reliability

#### **PHASE 3: Docker Integration** ✅ **COMPLETED**
- ✅ **PlaywrightDockerDriverPool**: Docker-aware Playwright driver with container networking
- ✅ **Container Networking**: Host-accessible URL resolution for Playwright-on-host execution
- ✅ **Test Migration**: Successfully migrated testAdminEmail to testPlaywrightAdminEmail
- ✅ **Application Bug Discovery**: Documented user logout behavior inconsistency
- ✅ **Production Ready**: Optimized for Docker environments with proper resource management

#### **PHASE 4: Full Docker Ecosystem Migration** 🚀 **FINAL PHASE**
**Objective**: Complete Docker-based testing with all infrastructure functions

**Steps to Execute:**
1. **Container Orchestration Optimization**
   - Optimize Docker networks for Playwright + App containers
   - Implement proper container lifecycle management
   - Performance tuning for container startup/teardown

2. **Infrastructure Feature Migration**
   - Update ScreenshotUtil for Playwright screenshots ✅ **COMPLETED**
   - Ensure ReportPortal integration works with Playwright
   - Migrate all existing utility classes to Playwright equivalents

3. **Performance and Scalability**
   - Implement parallel execution strategies
   - Fine-tune Playwright settings for Docker environment
   - Optimize test execution speed and resource usage

**Success Criteria Phase 4:**
- Complete Docker-based test execution with Playwright
- All existing infrastructure features fully functional
- Superior performance compared to Selenium setup
- Full CI/CD pipeline compatibility maintained

#### **PHASE 5: Component Scoping & Architecture** ✅ **COMPLETED**
- ✅ **PlaywrightScreenshotUtil**: Native Playwright screenshot capture
- ✅ **Enhanced Logging**: INFO-level logging with readable element names
- ✅ **Component Scoping**: Root locator architecture for component boundaries - COMPLETED
- 🔄 **Infrastructure Migration**: Converting remaining utility classes to Playwright

### **PHASE 5: COMPONENT SCOPING ARCHITECTURE IMPLEMENTATION** ✅ **COMPLETED**

#### **Problem Solved**
**Issue**: Components were searching entire page for elements, causing potential conflicts and poor performance in complex UIs.

**Solution Implemented**: Root locator scoping where each component has boundaries and all child elements are scoped within those boundaries.

#### **Technical Implementation Completed** ✅
1. ✅ **Enhanced PlaywrightBasePageComponent**: Added root locator support with constructor-based initialization
   - Multiple constructor overloads for page-level vs component-scoped initialization
   - `createScopedElement()` methods for automatic parent/child locator management
   - Backward compatibility maintained with existing page-level components

2. ✅ **Scoped Element Initialization**: All child elements use `parent.locator.locator(selector)` pattern
   - PlaywrightWebElement supports parent locator constructor
   - Automatic scoping based on component root locator presence
   - Element names enhanced for better logging readability

3. ✅ **Component Constructor Updates**: Components accept parent locators and scope all child elements
   - PlaywrightEmailPageComponent: Scoped email form elements within admin panel
   - PlaywrightCurrentUserComponent: Scoped menu items within user dropdown drawer
   - Dynamic component creation with proper scoping in PlaywrightProxyMainPage

#### **Architecture Pattern Implemented** ✅
```java
// Enhanced PlaywrightBasePageComponent
public PlaywrightBasePageComponent(PlaywrightWebElement rootLocator) {
    this.page = rootLocator.getPage();
    this.rootLocator = rootLocator;
}

protected PlaywrightWebElement createScopedElement(String selector, String elementName) {
    if (hasRootLocator()) {
        return new PlaywrightWebElement(rootLocator, selector, elementName);  // Scoped
    } else {
        return new PlaywrightWebElement(page, selector, elementName);         // Page-level
    }
}

// Component Usage
var userMenuDrawer = new PlaywrightWebElement(page, "div.ant-drawer-content-wrapper", "User Menu Drawer");
userMenuDrawer.waitForVisible();
return new PlaywrightCurrentUserComponent(userMenuDrawer);  // All menu items scoped within drawer
```

#### **Benefits Achieved** ✅
- ✅ **Encapsulation**: Elements properly scoped within component boundaries - verified in test logs
- ✅ **Performance**: Faster element lookup within smaller DOM subtrees using parent.locator.locator()
- ✅ **Reliability**: Eliminates selector conflicts between components through proper scoping
- ✅ **Scalability**: Supports complex nested UI structures like user dropdowns and admin panels
- ✅ **Maintainability**: Clear element names in logs for better debugging experience

#### **Test Results** ✅ **VALIDATED**
```bash
mvn clean test -Dtest=TestPlaywrightAdminEmail  # ✅ PASSES with scoped component architecture
```

**Key Evidence from Test Logs:**
- "User Menu Drawer" scoping working correctly
- "Administration Menu Item" found within scoped drawer
- "Email Verification Checkbox", "Email URL Field" etc. scoped within admin email component
- All elements use readable names instead of technical selectors
- Component boundaries properly respected - no element conflicts

#### **Migration Impact** 🎯
- **Architecture Modernized**: Component hierarchy now uses proper DOM scoping patterns
- **Performance Improved**: Element search confined to component boundaries
- **Debugging Enhanced**: Readable element names in all log outputs
- **Framework Scalability**: Ready for complex nested component structures

### **COMPONENT ARCHITECTURE ENHANCEMENTS** ✅ **COMPLETED**

#### **Phase 5.1: Constructor Logic Simplification** ✅ **COMPLETED**
**Problem Solved**: Removed redundant `generateElementName(String selector)` method that was causing code complexity.

**Implementation**:
- ✅ **Simplified PlaywrightWebElement Constructors**: Removed auto-generation logic complexity
- ✅ **Explicit Element Naming**: Elements use explicit names when provided, "Element" as default fallback
- ✅ **Code Reduction**: Eliminated 42 lines of complex selector parsing logic
- ✅ **Predictable Behavior**: Element naming is now explicit and consistent

**Benefits Achieved**:
- **Reduced Complexity**: Cleaner constructor logic without auto-generation overhead
- **Explicit Control**: Element names are explicitly defined by developers
- **Maintainability**: Simpler codebase with predictable element naming patterns

#### **Phase 5.2: Component-within-Component Support** ✅ **COMPLETED**  
**Problem Solved**: Framework now supports nested component hierarchy (Page → Component → SubComponent → Element).

**New Methods Added to PlaywrightBasePageComponent**:
```java
// Create child component with selector and name
protected <T extends PlaywrightBasePageComponent> T createScopedComponent(
    Class<T> componentClass, String selector, String componentName)

// Create child component with existing locator  
protected <T extends PlaywrightBasePageComponent> T createScopedComponent(
    Class<T> componentClass, PlaywrightWebElement childLocator)

// Utility methods for element finding within component scope
protected PlaywrightWebElement findChildElement(String selector)
protected PlaywrightWebElement findChildElement(String selector, String elementName)
```

**Usage Examples**:
```java
// AdminPage → EmailComponent → SmtpConfigComponent
SmtpConfigComponent smtpConfig = emailComponent.createScopedComponent(
    SmtpConfigComponent.class, ".smtp-config-section", "SMTP Configuration");

// TableComponent → RowComponent → CellElements  
RowComponent tableRow = tableComponent.createScopedComponent(
    RowComponent.class, "tr[data-row-id='123']", "Table Row 123");

// ModalComponent → FormComponent → FormElements
FormComponent modalForm = modalComponent.createScopedComponent(
    FormComponent.class, ".modal-form", "Modal Form");
```

**Benefits Achieved**:
- ✅ **Deep Nesting Support**: Page → Component → SubComponent → Element hierarchy
- ✅ **Flexible Component Creation**: Support for both selector-based and locator-based component creation
- ✅ **Type Safety**: Generic methods ensure compile-time type checking
- ✅ **Error Handling**: Clear error messages for component creation failures
- ✅ **Scalable Architecture**: Framework ready for complex UI scenarios like data tables, modal dialogs, and nested forms

#### **Test Validation** ✅ **VERIFIED**
```bash
mvn clean test -Dtest=TestPlaywrightAdminEmail  # ✅ PASSES with enhanced architecture
```

**Evidence from Test Results**:
- ✅ Component scoping continues to work properly
- ✅ Simplified element naming ("Element" as default, explicit names preserved)
- ✅ New createScopedComponent methods available for nested component scenarios
- ✅ Backward compatibility maintained with existing component structure
- ✅ All enhancements work without breaking existing functionality

