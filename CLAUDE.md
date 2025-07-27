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
1. At most CORE already migrated to Playwright. Just some components and pages left. And some tests.

Rules of Engagement
1. Wait for my commands - do not proceed with migration bny yourself
2. One Step at a Time: We will proceed strictly according to the plan. Do not move to the next step until we have completed and confirmed the current one.
3. Ask Questions: If you lack information, ask clarifying questions.
4. Explain Your Code: For every code snippet, provide a brief explanation of what it does and why you chose that specific solution.
5. Maintain a Log: After each successful step, we will update CLAUDE.md, adding the decisions made and the final code. Start every response with an update to this file.
6. Do not add Java-doc. 
7. Do not use Selenium style for new logic. You must copy Page -> Component -> Element hierarchy and inner methods logic, but use Playwright specific functionality in its native way (check with Context7) - no selenium-like waiters, no timeouts

### **ARCHITECTURAL OVERVIEW** 🏗️
```
Components → PlaywrightDriverPool (Unified Interface)
                    ↓
            [Automatic Mode Detection]
                    ↓
    LOCAL Mode → Direct Playwright → Container App
    DOCKER Mode → Container Playwright → Container App
                    ↓
            [File Operations Support]
                    ↓
    Upload: Volume Mapping + TestDataUtil
    Download: PlaywrightDownloadUtil (mode-aware)
```

## **TEST EXECUTION GUIDE** 🚀

### **Execution Modes**
The framework supports multiple execution modes controlled by the `execution.mode` system property:

- **PLAYWRIGHT_LOCAL** (default): Playwright runs directly on the host machine
- **PLAYWRIGHT_DOCKER**: Playwright runs inside Docker containers for isolation
- **SELENIUM**: Legacy Selenium mode (for compatibility during migration)

### **Running Test Suites**

#### **Available Test Suites**
Located in `src/test/resources/testng_suites/`:
- `playwright_parallel_suite.xml` - Playwright tests with parallel execution (2 threads)
- `studio_smoke.xml` - Smoke tests
- `studio_issues.xml` - Issue regression tests
- `studio_rules_editor.xml` - Rules editor tests

#### **Basic Suite Execution**
```bash
# Run default suite (LOCAL mode)
mvn clean test -Dsuite=playwright_parallel_suite

# Run specific suite with mode
mvn clean test -Dsuite=playwright_parallel_suite -Dexecution.mode=PLAYWRIGHT_LOCAL
mvn clean test -Dsuite=playwright_parallel_suite -Dexecution.mode=PLAYWRIGHT_DOCKER

# Run other suites
mvn clean test -Dsuite=studio_smoke -Dexecution.mode=PLAYWRIGHT_LOCAL
mvn clean test -Dsuite=studio_issues -Dexecution.mode=PLAYWRIGHT_DOCKER
```

#### **Parallel Execution Verification**
The `playwright_parallel_suite.xml` runs with `parallel="methods"` and `thread-count="2"`:
- **LOCAL Mode**: ~25.8s execution time with 2 parallel threads
- **DOCKER Mode**: ~23.9s execution time with 2 parallel Docker containers

Look for log entries like:
```
[TestNG-test-PlaywrightParallelTest-1] [INFO] Initializing test with Playwright: testPlaywrightAdminEmail
[TestNG-test-PlaywrightParallelTest-2] [INFO] Initializing test with Playwright: testPlaywrightAddProperty
```

### **Running Individual Tests**

#### **Single Test Class**
```bash
# Run single test class in LOCAL mode
mvn clean test -Dtest=TestPlaywrightAdminEmail -Dexecution.mode=PLAYWRIGHT_LOCAL

# Run single test class in DOCKER mode
mvn clean test -Dtest=TestPlaywrightAddProperty -Dexecution.mode=PLAYWRIGHT_DOCKER
```

#### **Single Test Method**
```bash
# Run specific test method
mvn clean test -Dtest=TestPlaywrightAdminEmail#testPlaywrightAdminEmail -Dexecution.mode=PLAYWRIGHT_LOCAL
mvn clean test -Dtest=TestPlaywrightAddProperty#testPlaywrightAddProperty -Dexecution.mode=PLAYWRIGHT_DOCKER
```

#### **Multiple Test Classes**
```bash
# Run multiple test classes
mvn clean test -Dtest=TestPlaywrightAdminEmail,TestPlaywrightAddProperty -Dexecution.mode=PLAYWRIGHT_LOCAL
```

### **Mode-Specific Configurations**

#### **PLAYWRIGHT_LOCAL Mode**
- ✅ Fastest execution
- ✅ Direct host machine resources
- ✅ Easier debugging
- ❌ Less isolation between tests

#### **PLAYWRIGHT_DOCKER Mode**
- ✅ Complete test isolation
- ✅ Consistent execution environment
- ✅ Container-based file operations
- ❌ Slower startup due to container initialization

### **Troubleshooting Test Execution**

#### **Common Issues**
1. **Port conflicts**: Each test gets unique ports (visible in logs)
2. **File access**: DOCKER mode uses volume mapping `/test_resources`
3. **Parallel execution**: Thread names show in logs for debugging

#### **Debug Logging**
```bash
# Enable verbose logging
mvn clean test -Dsuite=playwright_parallel_suite -Dexecution.mode=PLAYWRIGHT_LOCAL -X

# Check specific test execution
mvn clean test -Dtest=TestPlaywrightAdminEmail -Dexecution.mode=PLAYWRIGHT_DOCKER -Dverbose=true
```

#### **Successful Test Indicators**
- ✅ `Tests run: 2, Failures: 0, Errors: 0, Skipped: 0`
- ✅ `BUILD SUCCESS`
- ✅ Parallel thread logs showing concurrent execution
- ✅ Container initialization logs (DOCKER mode)

## **MIGRATION VALIDATION PLAN** 🔍

### **Phase 1: Complete Migration Inventory**

**All Migrated Pages (9 total):**
1. **Main Pages (5/5):**
   - ✅ PlaywrightLoginPage
   - ✅ PlaywrightProxyMainPage  
   - ✅ PlaywrightAdminPage
   - ✅ PlaywrightEditorPage
   - ✅ PlaywrightRepositoryPage

2. **Wizard Pages (4/4):**
   - ✅ PlaywrightInstallWizardStartPage
   - ✅ PlaywrightInstallWizardStep1Page
   - ✅ PlaywrightInstallWizardStep2Page
   - ✅ PlaywrightInstallWizardStep3Page

**All Migrated Components (36 total):**

3. **Core Infrastructure (4/4):**
   - ✅ PlaywrightBasePageComponent
   - ✅ PlaywrightBasePage
   - ✅ PlaywrightWebElement
   - ✅ PlaywrightTableComponent

4. **Main Components (4/4):**
   - ✅ PlaywrightCurrentUserComponent
   - ✅ PlaywrightTabSwitcherComponent  
   - ✅ PlaywrightCreateNewProjectComponent
   - ✅ PlaywrightConfigureCommitInfoComponent

5. **Admin Components (8/8):**
   - ✅ PlaywrightEmailPageComponent
   - ✅ PlaywrightAdminNavigationComponent
   - ✅ PlaywrightMyProfilePageComponent
   - ✅ PlaywrightMySettingsPageComponent
   - ✅ PlaywrightNotificationPageComponent
   - ✅ PlaywrightRepositoriesPageComponent
   - ✅ PlaywrightSecurityPageComponent
   - ✅ PlaywrightSystemSettingsPageComponent
   - ✅ PlaywrightTagsPageComponent
   - ✅ PlaywrightUsersPageComponent

6. **Editor Components (8/8):**
   - ✅ PlaywrightRightTableDetailsComponent
   - ✅ PlaywrightLeftRulesTreeComponent
   - ✅ PlaywrightLeftProjectModuleSelectorComponent
   - ✅ PlaywrightTreeFolderComponent
   - ✅ PlaywrightAddModuleComponent
   - ✅ PlaywrightEditTablePanelComponent
   - ✅ PlaywrightEditorMainContentProblemsPanelComponent
   - ✅ PlaywrightProblemsPanelComponent
   - ✅ PlaywrightProjectDetailsComponent
   - ✅ PlaywrightProjectModuleDetailsComponent
   - ✅ PlaywrightTableToolbarPanelComponent
   - ✅ PlaywrightTestResultValidationComponent

7. **Create New Project Components (5/5):**
   - ✅ PlaywrightExcelFilesComponent
   - ✅ PlaywrightZipArchiveComponent
   - ✅ PlaywrightOpenApiComponent
   - ✅ PlaywrightTemplateTabComponent
   - ✅ PlaywrightWorkspaceComponent

8. **Repository Components (5/5):**
   - ✅ PlaywrightDeployConfigurationTabsComponent
   - ✅ PlaywrightLeftRepositoryTreeComponent
   - ✅ PlaywrightRepositoryContentButtonsPanelComponent
   - ✅ PlaywrightRepositoryContentTabPropertiesComponent
   - ✅ PlaywrightRepositoryTreeFolderComponent

**Working Test Examples (3/22):**
- ✅ TestPlaywrightAdminEmail - **Verified working in both LOCAL & DOCKER modes**
- ✅ TestPlaywrightAddProperty - **Verified working in both LOCAL & DOCKER modes**
- ✅ TestAddPropertyExtraStateAppears - **Migrated and fully functional**

### **Phase 2: Component Instantiation Validation** 🏗️

**TASK 1: Validate Instantiation Patterns**

**Validation Rules:**
- **a.** All elements and components instantiated directly on pages can use non-scoped constructors
- **b.** All elements and components instantiated within other components MUST use appropriate scoping constructors

**Pattern Examples:**
```java
// ✅ CORRECT: Page-level instantiation (non-scoped)
public class PlaywrightEditorPage extends PlaywrightProxyMainPage {
    private PlaywrightLeftRulesTreeComponent leftRulesTreeComponent;
    
    private void initializeComponents() {
        PlaywrightWebElement leftLocator = new PlaywrightWebElement(page, "xpath=//div[@id='left']", "leftRulesTreeComponent");
        leftRulesTreeComponent = new PlaywrightLeftRulesTreeComponent(leftLocator);
    }
}

// ✅ CORRECT: Component-level instantiation (scoped)
public class PlaywrightLeftRulesTreeComponent extends PlaywrightBasePageComponent {
    private PlaywrightWebElement viewFilterLink;
    
    private void initializeElements() {
        viewFilterLink = createScopedElement("xpath=.//div[@class='filter-view']/span/a", "viewFilterLink");
    }
}

// ❌ INCORRECT: Component using non-scoped constructor
public class PlaywrightSomeComponent extends PlaywrightBasePageComponent {
    private PlaywrightWebElement element = new PlaywrightWebElement(page, "xpath=//div", "element"); // WRONG!
}
```

### **Phase 3: Locator Accuracy Validation** 🎯

**TASK 2: Compare All Locators with Legacy Variants**

**Validation Process:**
1. **One-by-One Comparison**: Each migrated page/component vs its legacy counterpart
2. **Exact Locator Matching**: Ensure 100% identical XPath/CSS selectors
3. **Method Signature Verification**: Ensure same functionality patterns

**Critical Areas:**
- **Element Locators**: All `@FindBy` annotations vs `createScopedElement()` calls
- **Component Locators**: Root locator consistency  
- **Dynamic Locators**: String.format() patterns match legacy `.format()` usage

**Example Validation:**
```java
// Legacy: LeftRulesTreeComponent.java
@FindBy(xpath = ".//div[@class='filter-view']/span/a")
private SmartWebElement viewFilterLink;

// Playwright: PlaywrightLeftRulesTreeComponent.java  
viewFilterLink = createScopedElement("xpath=.//div[@class='filter-view']/span/a", "viewFilterLink");
// ✅ MATCH: Identical locator
```

### **Phase 4: Agent Task Specifications** 🤖

**Agent Task 1: Instantiation Pattern Validation**
```
TASK: Go through the complete list of migrated pages and components and validate instantiation patterns:

VALIDATION CRITERIA:
a. Pages (9 total) - All elements/components can use non-scoped constructors
b. Components (36 total) - All internal elements/components MUST use scoped constructors

DELIVERABLE: Report identifying any violations of scoping rules with specific file locations and corrections needed.
```

**Agent Task 2: Locator Accuracy Validation**  
```
TASK: Compare each migrated page/component with its legacy variant for locator accuracy:

VALIDATION PROCESS:
1. For each of 45 migrated files, find corresponding legacy file
2. Compare every locator (xpath/css) for 100% exact match
3. Verify dynamic locator patterns (String.format vs .format)
4. Check method signatures and functionality patterns

DELIVERABLE: Comprehensive report of any locator mismatches with exact corrections needed.
```

### **Phase 5: Success Criteria** ✅

**Migration Complete When:**
- ✅ All 9 pages use correct instantiation patterns
- ✅ All 36 components use correct scoping patterns  
- ✅ All locators 100% match legacy counterparts
- ✅ All dynamic locator patterns verified
- ✅ Test suite runs successfully with both modes

**Current Status: Ready for Validation Phase** 🚀

## **VALIDATION RESULTS** 📊

### **Agent Task 1 Results: Instantiation Pattern Validation**

**CRITICAL VIOLATIONS FOUND**: **27 files** with instantiation pattern violations

| Category | Total Files | Compliant | Violations | Compliance Rate |
|----------|-------------|-----------|------------|-----------------|
| **Pages** | 9 | 7 | 2 | 78% |
| **Components** | 36 | 11 | 25 | 31% |
| **TOTAL** | 45 | 18 | 27 | **40%** |

**Key Issues:**
- ❌ 25 components using `new PlaywrightWebElement(page, ...)` instead of `createScopedElement(...)`
- ❌ 2 pages using `new Component()` without proper root locators
- ✅ 11 components correctly implemented with scoped patterns

### **Agent Task 2 Results: Locator Accuracy Validation**

**MIXED SUCCESS PATTERN**: Some excellent matches, some critical mismatches

**Perfect Locator Matches (100% Accuracy):**
- ✅ LoginPage ↔ PlaywrightLoginPage
- ✅ LeftRulesTreeComponent ↔ PlaywrightLeftRulesTreeComponent
- ✅ InstallWizardStartPage ↔ PlaywrightInstallWizardStartPage

**Critical Issues:**
- ❌ AdminPage: 8 missing admin components and navigation methods
- ❌ EditorPage: 5 missing critical editor components  
- ❌ EmailPageComponent: All major locators target wrong elements
- ⚠️ Complex xpath → Simple CSS conversions need DOM validation

**Locator Accuracy Statistics:**
- **Pages Perfect Match**: 2/9 (22%) ✅
- **Pages Partial Match**: 4/9 (44%) ⚠️  
- **Pages Major Issues**: 3/9 (33%) ❌

## **PRIORITY ACTION PLAN** 🎯

### **Phase 1: Fix Critical Instantiation Violations (27 files)**
```
URGENT: Fix scoping patterns in 25 components
- Replace all `new PlaywrightWebElement(page, ...)` with `createScopedElement(...)`
- Fix 2 pages to provide proper root locators to components
```

### **Phase 2: Fix Critical Locator Mismatches**
```
URGENT: 
1. EmailPageComponent - All locators target wrong elements
2. AdminPage - Add 8 missing admin components  
3. EditorPage - Add 5 missing editor components
4. Validate all xpath→CSS conversions against actual DOM
```

### **Phase 3: Complete Missing Functionality**
```
MEDIUM PRIORITY:
- Complete AdminPage missing components (8 components)
- Complete EditorPage missing components (5 components)  
- Add missing tab functionality to CreateNewProjectComponent
- Add TabSwitcherComponent to ProxyMainPage
```

### **Phase 4: Quality Validation**
```
LOW PRIORITY:
- Implement automated locator validation
- Create DOM mapping documentation
- Establish locator review process
```

**Current Migration Status: 40% Compliant - Requires Major Fixes Before Production** ⚠️
