# Rules Editor Migration Status

## Overview

Migration from legacy Selenide-based framework to new Playwright + TestContainers framework.

**Legacy Repository:** `/Users/dmitryminchuk/Projects/eis/openl-tests/src/test/java/RulesEditor/`
**New Repository:** `src/test/java/tests/ui/webstudio/rules_editor/`

## Statistics

- **Total Legacy Tests:** 157
- **Migrated:** 9 tests (7 from legacy + 2 new)
- **Remaining:** ~150 tests (~5% coverage)

## ✅ Migrated Tests (9)

### From Legacy (7)
1. TestAddDeleteEditProperties
2. TestDefaultProperties
3. TestDetermineSpreadsheetResultCellType
4. TestModuleCategoryInheritedProperties
5. TestRestrictionsErrorMessagesForProperties
6. TestReturnCellsMarksWithStar
7. TestViewStackTraceFunctionality

### New Tests (2)
1. TestCompareSelectedVersions
2. TestMethodTable

## 🎯 Migration Priorities

### Priority 1: Critical Functionality
- Tracing & Run functionality
- Export/Import functionality
- Compilation tests
- Test functionality

### Priority 2: Important Features
- Hints & Navigation
- Compare files
- Search functionality
- Git Integration in Editor

### Priority 3: Supporting Features
- Editing & Locking
- Versioning & History
- UI/UX elements

## ❌ Unmigrated Tests by Category

### 1. Tracing & Run Functionality (HIGH PRIORITY) - ~35 tests

#### Main Tests
- [ ] TestTraceForRuleTable
- [ ] TestTraceForTestsTable
- [ ] TestTraceView
- [ ] TestRunTestsForRuleTable
- [ ] TestRunTestsForTestsTable
- [ ] TestRunTraceWithJson
- [ ] TestTraceInFileFunctionality
- [ ] TestRunIntoFileFlatParameterLayout
- [ ] TestDatatypesInheritanceInRunTrace

#### TracingRunTables Subfolder (~30 tests)
- [ ] Test104
- [ ] Test105
- [ ] Test106
- [ ] Test107
- [ ] Test108
- [ ] Test109
- [ ] Test110
- [ ] Test111
- [ ] Test113
- [ ] Test115
- [ ] Test116
- [ ] Test117
- [ ] Test124
- [ ] Test125
- [ ] Test126
- [ ] Test127
- [ ] Test129
- [ ] Test130
- [ ] Test131
- [ ] Test132
- [ ] Test133
- [ ] Test137
- [ ] Test140
- [ ] Test141
- [ ] Test164
- [ ] Test165
- [ ] Test168
- [ ] Test169
- [ ] Test170
- [ ] Test173
- [ ] Test174
- [ ] Test176

### 2. Export/Import Functionality (HIGH PRIORITY) - 4 tests
- [ ] TestExportProjectFunctionality
- [ ] TestExportModuleFunctionality
- [ ] TestExportTestResults
- [ ] TestIncludeNullsInExportedTestResult

### 3. Compilation (HIGH PRIORITY) - 4 tests
- [ ] TestProjectCompilation
- [ ] TestCompileThisModuleOnly
- [ ] TestCompilationProgressBar
- [ ] TestSpreadsheetCompilationOrder

### 4. Test Functionality (HIGH PRIORITY) - 4 tests
- [ ] TestCreateTestFunctionality
- [ ] TestFunctionalityOfTest
- [ ] TestTheNumberOfFailedTests
- [ ] TestLimitationOfTestResultsDisplayed

### 5. Hints & Navigation (MEDIUM PRIORITY) - 11 tests
- [ ] TestHintsAccessToParameters
- [ ] TestHintsAndLinksExternalConditionsActionsReturns
- [ ] TestHintsConditionsMatchedWithLongParameters
- [ ] TestHintsForTableWithFailedCompilation
- [ ] TestHintsMinMaxColumns
- [ ] TestHintsReturnColumnsSmartRules
- [ ] TestHintsTrueConditions
- [ ] TestThreeRowHintSimpleSmartRules
- [ ] TestNavigationOfExternalConditionsActionsReturns
- [ ] TestNavigationToTable
- [ ] TestNoLinkAndHintsDisplayedForMethodsInRuleWithMergedCondition

### 6. Compare Files (MEDIUM PRIORITY) - 3 tests
- [ ] TestCompareExcelFiles
- [ ] TestCompareIdenticalFiles
- [ ] TestCompareConflictedTextFiles

### 7. Git Integration in Editor (MEDIUM PRIORITY) - 2 tests
- [ ] TestGitBranchLinksInEditorTab
- [ ] TestResolveConflictFunctionality

### 8. Search Functionality (MEDIUM PRIORITY) - 2 tests
- [ ] TestSearchOnProjectLevel
- [ ] TestSearchByCellValue

### 9. Editing & Locking (MEDIUM PRIORITY) - 5 tests
- [ ] TestEditingLockingProject
- [ ] TestEditingCommaSeparatedArrayValues
- [ ] TestFillMapsAndListFields
- [ ] TestReselectValueFromArrayDropDown
- [ ] TestWorkWithDuplicateTables

### 10. Spreadsheet Functionality (MEDIUM PRIORITY) - 2 tests
- [ ] TestCustomSpreadsheetResultDataDisplaying
- [ ] TestSpreadsheetCompilationOrder (also in Compilation)

### 11. UI/UX Elements (LOW PRIORITY) - 7 tests
- [ ] TestOrderingMode
- [ ] TestChangesOrder
- [ ] TestSwitchModuleViaBreadcrumbsNavigation
- [ ] TestRefreshButton
- [ ] TestDeployButton
- [ ] TestExcelFilesWithSpaces
- [ ] TestUpdateModuleInExcel

### 12. Properties & Configuration (MEDIUM PRIORITY) - 2 tests
- [ ] TestAddBusinessDimProperties
- [ ] TestConstantUsage

### 13. Versioning & History (MEDIUM PRIORITY) - 1 test
- [ ] TestTableVersioningNonActiveModule

### 14. Domain-Specific (LOW PRIORITY) - 1 test
- [ ] TestUSTerritories

### 15. Numbered Tests (Legacy) - ~70 tests
These tests have numeric names (Test001-Test176) and need analysis to determine their functionality:

- [ ] Test001, Test002, Test003
- [ ] Test006, Test007, Test009
- [ ] Test014, Test015, Test016, Test017, Test018
- [ ] Test030, Test031
- [ ] Test059, Test063, Test065, Test066
- [ ] Test075, Test076, Test077, Test078, Test079
- [ ] Test080, Test081, Test082
- [ ] Test084, Test085, Test086, Test087, Test088, Test089
- [ ] Test091, Test092
- [ ] Test094, Test095, Test096, Test097, Test098, Test099
- [ ] Test100, Test101, Test103
- [ ] Test112
- [ ] Test118
- [ ] Test128
- [ ] Test134, Test135
- [ ] Test143, Test144, Test145
- [ ] Test147, Test148
- [ ] Test150, Test151, Test152
- [ ] Test154, Test155
- [ ] Test157, Test159
- [ ] Test161, Test162, Test163

**Note:** These numbered tests need to be analyzed in the legacy repository to understand their purpose and map them to functional categories.

## 📋 Migration Plan

### Phase 1: Core Functionality (Current Phase)
1. ✅ Properties management (9 tests completed)
2. 🔄 Export/Import functionality (Next: 4 tests)
3. ⏳ Compilation tests (4 tests)
4. ⏳ Test functionality (4 tests)

### Phase 2: Extended Functionality
1. Tracing & Run functionality (~35 tests)
2. Hints & Navigation (11 tests)
3. Compare files (3 tests)

### Phase 3: Integration & UI
1. Git Integration (2 tests)
2. Search functionality (2 tests)
3. Editing & Locking (5 tests)

### Phase 4: Remaining Tests
1. Analyze numbered tests (Test001-Test176)
2. Migrate domain-specific tests
3. UI/UX elements
4. Versioning & History

## 📝 Notes

- Legacy tests use Selenide + JSF components
- New framework uses Playwright + React (Ant Design) components
- Component locators need complete rewrite
- Page Object Model structure differs between frameworks
- Some legacy tests may be obsolete due to UI redesign
- Tests need TestContainers integration
- ReportPortal annotations must be preserved

## 🔗 Related Files

- Legacy Suite: `/Users/dmitryminchuk/Projects/eis/openl-tests/src/test/resources/testsuites/RulesEditor/`
- New Suite: `src/test/resources/testng_suites/rules_editor.xml`
- Legacy Components: `/Users/dmitryminchuk/Projects/eis/openl-tests/src/main/java/ui/pages/studio/editor/`
- New Components: `src/main/java/domain/ui/webstudio/components/editortabcomponents/`
