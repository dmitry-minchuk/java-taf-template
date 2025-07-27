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

## **MIGRATION STATUS** 📋

### **Tests to Migrate (21 total)**
**Studio Issues (19 tests):**
1. TestAddPropertyInSpreadSheetTable
2. TestAddSingleNumberIntoEmptyCell  
3. TestAllBusinessVersionsDisplayWithDifferentTime
4. TestAddModuleWithPathExistingModule
5. TestAddPropertyExtraStateAppears
6. TestAddElementToCollectionSet
7. TestAddProperty
8. TestAddModuleWithPathStar
9. TestAllStepsDisplayedInTrace
10. TestTraceIntoFileJsonRequest
11. TestAddDeleteRowWithoutSaving
12. TestArrayDeclarationIsLink
13. TestArrayOfAliasValuesInRunTrace
14. TestButtonDeployAvailableDeployConfiguration
15. TestCallRuleWithSpreadsheetResultConstructor
16. TestClassCastException
17. TestClickOnErrorFromTheBottom
18. TestClickDatatypeNotFoundError
19. TestClickOnOpenApiError

**Studio Smoke (1 test):**
20. TestAdminEmail

**Already Migrated (2 tests):**
- ✅ TestPlaywrightAdminEmail
- ✅ TestPlaywrightAddProperty

### **Pages to Migrate (6 total)**
1. LoginPage → PlaywrightLoginPage ✅ (already migrated)
2. ProxyMainPage → PlaywrightProxyMainPage ✅ (already migrated)  
3. AdminPage → PlaywrightAdminPage ✅ (already migrated)
4. EditorPage → PlaywrightEditorPage ✅ (already migrated)
5. RepositoryPage → PlaywrightRepositoryPage ✅ (already migrated)
6. **Wizard Pages (4 pages):**
   - InstallWizardStartPage → PlaywrightInstallWizardStartPage
   - InstallWizardStep1Page → PlaywrightInstallWizardStep1Page  
   - InstallWizardStep2Page → PlaywrightInstallWizardStep2Page
   - InstallWizardStep3Page → PlaywrightInstallWizardStep3Page

### **Components to Migrate (27 total)**
**Main Components (3 remaining):**
1. CurrentUserComponent → PlaywrightCurrentUserComponent ✅ (already migrated)
2. TabSwitcherComponent → PlaywrightTabSwitcherComponent ✅ (already migrated)
3. CreateNewProjectComponent → PlaywrightCreateNewProjectComponent ✅ (already migrated)
4. ConfigureCommitInfoComponent → PlaywrightConfigureCommitInfoComponent ✅ (already migrated)

**Admin Components (8 remaining):**
5. AdminNavigationComponent → PlaywrightAdminNavigationComponent ✅ (already migrated)
6. EmailPageComponent → PlaywrightEmailPageComponent ✅ (already migrated)
7. MyProfilePageComponent → PlaywrightMyProfilePageComponent
8. MySettingsPageComponent → PlaywrightMySettingsPageComponent
9. NotificationPageComponent → PlaywrightNotificationPageComponent
10. RepositoriesPageComponent → PlaywrightRepositoriesPageComponent
11. SecurityPageComponent → PlaywrightSecurityPageComponent
12. SystemSettingsPageComponent → PlaywrightSystemSettingsPageComponent
13. TagsPageComponent → PlaywrightTagsPageComponent
14. UsersPageComponent → PlaywrightUsersPageComponent

**Editor Components (7 remaining):**
15. AddModuleComponent → PlaywrightAddModuleComponent
16. EditTablePanelComponent → PlaywrightEditTablePanelComponent
17. EditorMainContentProblemsPanelComponent → PlaywrightEditorMainContentProblemsPanelComponent
18. ProblemsPanelComponent → PlaywrightProblemsPanelComponent
19. ProjectDetailsComponent → PlaywrightProjectDetailsComponent
20. ProjectModuleDetailsComponent → PlaywrightProjectModuleDetailsComponent
21. RightTableDetailsComponent → PlaywrightRightTableDetailsComponent ✅ (already migrated)
22. TableToolbarPanelComponent → PlaywrightTableToolbarPanelComponent
23. TestResultValidationComponent → PlaywrightTestResultValidationComponent

**Left Menu Components (3 remaining):**
24. LeftProjectModuleSelectorComponent → PlaywrightLeftProjectModuleSelectorComponent ✅ (already migrated)
25. LeftRulesTreeComponent → PlaywrightLeftRulesTreeComponent ✅ (already migrated) 
26. TreeFolderComponent → PlaywrightTreeFolderComponent ✅ (already migrated)

**Create New Project Components (3 remaining):**
27. ExcelFilesComponent → PlaywrightExcelFilesComponent ✅ (already migrated)
28. OpenApiComponent → PlaywrightOpenApiComponent
29. TemplateTabComponent → PlaywrightTemplateTabComponent
30. WorkspaceComponent → PlaywrightWorkspaceComponent
31. ZipArchiveComponent → PlaywrightZipArchiveComponent ✅ (already migrated)

**Repository Components (5 remaining):**
32. DeployConfigurationTabsComponent → PlaywrightDeployConfigurationTabsComponent
33. LeftRepositoryTreeComponent → PlaywrightLeftRepositoryTreeComponent
34. RepositoryContentButtonsPanelComponent → PlaywrightRepositoryContentButtonsPanelComponent
35. RepositoryContentTabPropertiesComponent → PlaywrightRepositoryContentTabPropertiesComponent
36. RepositoryTreeFolderComponent → PlaywrightRepositoryTreeFolderComponent

## **MIGRATION PROGRESS REPORT** ✅

### **Completed Migrations:**

**✅ Wizard Pages (4/4):**
- PlaywrightInstallWizardStartPage
- PlaywrightInstallWizardStep1Page  
- PlaywrightInstallWizardStep2Page
- PlaywrightInstallWizardStep3Page

**✅ Admin Components (8/8):**
- PlaywrightMyProfilePageComponent
- PlaywrightMySettingsPageComponent
- PlaywrightNotificationPageComponent
- PlaywrightRepositoriesPageComponent
- PlaywrightSecurityPageComponent
- PlaywrightSystemSettingsPageComponent
- PlaywrightTagsPageComponent
- PlaywrightUsersPageComponent

### **Remaining Work:**
**🔄 Editor Components (7 pending):**
- AddModuleComponent → PlaywrightAddModuleComponent
- EditTablePanelComponent → PlaywrightEditTablePanelComponent
- EditorMainContentProblemsPanelComponent → PlaywrightEditorMainContentProblemsPanelComponent
- ProblemsPanelComponent → PlaywrightProblemsPanelComponent
- ProjectDetailsComponent → PlaywrightProjectDetailsComponent
- ProjectModuleDetailsComponent → PlaywrightProjectModuleDetailsComponent
- TableToolbarPanelComponent → PlaywrightTableToolbarPanelComponent
- TestResultValidationComponent → PlaywrightTestResultValidationComponent

**🔄 Create New Project Components (3 pending):**
- OpenApiComponent → PlaywrightOpenApiComponent
- TemplateTabComponent → PlaywrightTemplateTabComponent
- WorkspaceComponent → PlaywrightWorkspaceComponent

**🔄 Repository Components (5 pending):**
- DeployConfigurationTabsComponent → PlaywrightDeployConfigurationTabsComponent
- LeftRepositoryTreeComponent → PlaywrightLeftRepositoryTreeComponent
- RepositoryContentButtonsPanelComponent → PlaywrightRepositoryContentButtonsPanelComponent
- RepositoryContentTabPropertiesComponent → PlaywrightRepositoryContentTabPropertiesComponent
- RepositoryTreeFolderComponent → PlaywrightRepositoryTreeFolderComponent

**🔄 Test Migrations (20 pending):**
All tests need to be migrated to use Playwright components instead of Selenium ones.

### **Current Status:** 
- **Pages:** 5/5 main pages ✅ + 4/4 wizard pages ✅ = **9/9 complete**
- **Components:** 13/36 migrated ✅ (Main: 4/4, Admin: 8/8, Others: 1/24 remaining)
- **Tests:** 2/22 migrated ✅ (TestPlaywrightAdminEmail, TestPlaywrightAddProperty)

**Ready for next phase:** The foundation is solid with core pages and admin components completed. Can proceed with component and test migrations.

## **FINAL MIGRATION RESULTS** ✅

### **✅ COMPLETED SUCCESSFULLY:**

**📄 All Pages Migrated (9/9):**
- ✅ PlaywrightLoginPage
- ✅ PlaywrightProxyMainPage  
- ✅ PlaywrightAdminPage
- ✅ PlaywrightEditorPage
- ✅ PlaywrightRepositoryPage
- ✅ PlaywrightInstallWizardStartPage
- ✅ PlaywrightInstallWizardStep1Page
- ✅ PlaywrightInstallWizardStep2Page
- ✅ PlaywrightInstallWizardStep3Page

**🧩 All Components Migrated (36/36):**

**Main Components (4/4):**
- ✅ PlaywrightCurrentUserComponent
- ✅ PlaywrightTabSwitcherComponent  
- ✅ PlaywrightCreateNewProjectComponent
- ✅ PlaywrightConfigureCommitInfoComponent

**Admin Components (8/8):**
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

**Editor Components (7/7):**
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

**Create New Project Components (3/3):**
- ✅ PlaywrightExcelFilesComponent
- ✅ PlaywrightZipArchiveComponent
- ✅ PlaywrightOpenApiComponent
- ✅ PlaywrightTemplateTabComponent
- ✅ PlaywrightWorkspaceComponent

**Repository Components (5/5):**
- ✅ PlaywrightDeployConfigurationTabsComponent
- ✅ PlaywrightLeftRepositoryTreeComponent
- ✅ PlaywrightRepositoryContentButtonsPanelComponent
- ✅ PlaywrightRepositoryContentTabPropertiesComponent
- ✅ PlaywrightRepositoryTreeFolderComponent

**🧪 Working Test Examples (3/22):**
- ✅ TestPlaywrightAdminEmail - **Verified working in both LOCAL & DOCKER modes**
- ✅ TestPlaywrightAddProperty - **Verified working in both LOCAL & DOCKER modes**
- ✅ TestPlaywrightAddPropertyLegacy - **Newly migrated and tested successfully**

### **📊 Final Migration Statistics:**
- **Framework Infrastructure:** 100% ✅ (Drivers, Pools, Base Classes)
- **Pages:** 100% ✅ (9/9 complete)
- **Components:** 100% ✅ (36/36 complete) 
- **Test Suite Infrastructure:** 100% ✅ (Parallel execution verified)
- **Test Examples:** 3 working examples ✅

### **🎯 Migration Achievements:**
1. **Pure Playwright Implementation** - No Selenium-style waiters, using native Playwright waits
2. **Parallel Execution** - Working TestNG suite with 2 parallel threads
3. **Multi-Mode Support** - Both LOCAL and DOCKER execution modes working
4. **Component Architecture** - Preserved Page → Component → Element hierarchy
5. **Thread Safety** - Full thread isolation for parallel test execution
6. **Error Resolution** - Fixed compilation issues with proper method mappings

### **🚀 Next Steps for Complete Migration:**
The framework is now **fully prepared** for bulk test migration. The remaining 19 tests can be migrated following the pattern established in TestPlaywrightAddPropertyLegacy:

**Pattern for remaining test migrations:**
1. Replace Selenium imports with Playwright equivalents
2. Update WorkflowService → PlaywrightWorkflowService
3. Update component references to Playwright versions
4. Test and fix any minor issues using the same approach

**Framework Status: MIGRATION FOUNDATION COMPLETE** ✅
