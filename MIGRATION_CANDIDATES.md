# Legacy Tests Migration Analysis

**Generated:** 2026-02-17
**Source:** `/Users/dmitryminchuk/Projects/eis/openl-tests`
**Target:** `/Users/dmitryminchuk/Projects/java-taf-template`
**Analyzed:** 73 tests from SmokeStudio, RulesEditor, and Repository suites

---

## Executive Summary

- **Total tests analyzed:** 73 (from active XML suites)
- **Already migrated:** 59 tests
- **Migration candidates:** 15 top-priority tests
- **Estimated timeline:** 3 weeks for top-15 tests
- **Coverage areas:** Deploy (4), Repository (5), Editor (4), Admin (2)

---

## 🥇 TOP-15 TESTS FOR MIGRATION

### Tier 1: VERY SIMPLE (< 45 lines, 4-8 hours per test)

| # | Test Name | Suite | Lines | Functionality | Time Estimate |
|---|-----------|-------|-------|---------------|---------------|
| 1 | **FileAddDelete** | SmokeStudio | 36 | Upload and delete files in repository | 🟢 4-6 hours |
| 2 | **CreateDataTypeTable** | SmokeStudio | 37 | Create Datatype table | 🟢 4-6 hours |
| 3 | **DeployClosedProject** | SmokeStudio | 40 | Deploy closed project and verify buttons | 🟢 4-6 hours |
| 4 | **DeployCopyDelete** | SmokeStudio | 41 | Copy and delete deployments | 🟢 4-6 hours |
| 5 | **TestReselectValueFromArrayDropDown** | RulesEditor | 41 | Switch array values in dropdown | 🟢 6-8 hours |

**Why simple:**
- Use high-level API methods (`RepositoryTab.createProject()`)
- Standard UI patterns (create → edit → verify)
- Minimal XPath/CSS selectors
- No complex infrastructure dependencies

---

### Tier 2: SIMPLE (42-50 lines, 6-10 hours per test)

| # | Test Name | Suite | Lines | Functionality | Time Estimate |
|---|-----------|-------|-------|---------------|---------------|
| 6 | **MainMenuEditorButtonsForProjectTypesView** | SmokeStudio | 42 | Check Test button visibility for templates | 🟢 6-8 hours |
| 7 | **ProjectClose** | SmokeStudio | 42 | Close project and verify status | 🟢 6-8 hours |
| 8 | **ProjectUploadZipFile** | SmokeStudio | 42 | Upload ZIP projects (with/without root) | 🟢 6-8 hours |
| 9 | **ProjectOpenPrevVersionReturnToCurrent** | SmokeStudio | 42 | Navigate project versions | 🟢 8 hours |
| 10 | **TestUserNameGroupNameChineseSymbols** | SmokeStudio | 43 | Localization: Chinese symbols | 🟢 6-8 hours |
| 11 | **DeploySingleDeployAll** | SmokeStudio | 45 | Deploy single vs deploy all | 🟡 8-10 hours |
| 12 | **TestExcelFilesWithSpaces** | RulesEditor | 46 | Excel files with spaces in names | 🟡 8-10 hours |
| 13 | **ProjectUploadDeleteFromXLS** | SmokeStudio | 46 | Upload and delete XLS projects | 🟡 8-10 hours |
| 14 | **CreateDataTable** | SmokeStudio | 47 | Create Data table | 🟡 8-10 hours |
| 15 | **TestAllMethodFunctionality** | SmokeStudio | 50 | Run all tests for method | 🟡 10 hours |

**Characteristics:**
- Straightforward workflows
- Standard component interactions
- Use built-in templates/configurations
- Linear test flow without complex conditionals

---

## 🚀 RECOMMENDED MIGRATION STRATEGY

### Sprint 1 (Week 1): Quick Wins
**Goal:** Establish baseline, test infrastructure

**Tests:** #1-5 (Tier 1)
- **Day 1-2:** FileAddDelete + CreateDataTypeTable
- **Day 3-4:** DeployClosedProject + DeployCopyDelete
- **Day 5:** TestReselectValueFromArrayDropDown

**Deliverables:**
- 5 tests migrated
- ~200 lines of test code
- FileUploadComponent created
- DeploymentComponent created
- Coverage boost for basic functionality

---

### Sprint 2 (Week 2): Consolidation
**Goal:** Expand Editor and Repository coverage

**Tests:** #6-10 (Tier 2)
- **Day 1-2:** MainMenuEditorButtons... + ProjectClose
- **Day 3-4:** ProjectUploadZipFile + ProjectOpenPrev...
- **Day 5:** TestUserNameGroupNameChineseSymbols

**Deliverables:**
- +5 tests migrated (10 total)
- Coverage for Editor core functions
- Repository navigation components

---

### Sprint 3 (Week 3): Expansion
**Goal:** Round out basic coverage

**Tests:** #11-15 (Tier 2)
- **Day 1-3:** DeploySingleDeployAll + TestExcelFilesWithSpaces
- **Day 4-5:** ProjectUploadDeleteFromXLS + CreateDataTable

**Deliverables:**
- +5 tests migrated (15 total)
- 3-week milestone achieved
- Comprehensive basic functionality coverage

---

## 📋 ADDITIONAL CANDIDATES (Tier 3: Medium Complexity)

**For future sprints:**

| Test Name | Suite | Lines | Estimate | Notes |
|-----------|-------|-------|----------|-------|
| **TestUpdateModuleInExcel** | RulesEditor | 51 | 🟡 1-2 days | Module operations in Excel |
| **TestUserEmailInHints** | SmokeStudio | 53 | 🟡 1-2 days | Email in tooltips verification |
| **TestFileSettingsGeneration** | Repository | 54 | 🟡 1-2 days | File settings generation |
| **TestAddBusinessDimProperties** | RulesEditor | 56 | 🟡 2 days | Business dimension properties |
| **TestUserNameInStatusField** | Repository | 68 | 🟡 2-3 days | Username display in status |
| **TestTabRevisionsOnEditorTab** | SmokeStudio | 69 | 🟡 2-3 days | Revisions tab in Editor |
| **TestTableActionButtons** | Repository | 73 | 🟡 2-3 days | Table action buttons |
| **TestNavigationToTable** | RulesEditor | 78 | 🟡 2-3 days | Table navigation functionality |

---

## ❌ NOT RECOMMENDED FOR MIGRATION

**Reason:** High complexity, large size, infrastructure dependencies

| Test Name | Lines | Reason |
|-----------|-------|--------|
| **TestOauth** | 31 | Requires OAuth container configuration |
| **TestOauthSettingsMenu** | 96 | OAuth infrastructure dependency |
| **TestConstantUsage** | 252 | Complex logic, 250+ lines |
| **TestProtectedBranches** | 308 | Complex branching logic |
| **TestDisplayChangedRows** | 296 | Large test with many edge cases |
| **TestOpenApiImport** | 425 | External API integration |
| **TestCreateProjectFromOpenApiFile** | 530 | 500+ lines complex workflow |
| **TestProjectTagsFilteringGrouping** | 810 | Largest, most complex test in suite |

---

## 🔧 REQUIRED COMPONENTS

### New Components to Create

1. **DeploymentComponent**
   - For tests: #3, #4, #11
   - Methods: `createDeploy()`, `deleteDeploy()`, `copyDeploy()`

2. **FileUploadComponent**
   - For tests: #1, #8, #13
   - Methods: `uploadFile()`, `deleteFile()`, `verifyFileUploaded()`

3. **CreateTableWizard**
   - For tests: #2, #14
   - Methods: `selectTableType()`, `fillTableProperties()`, `createTable()`

4. **VersionNavigationComponent**
   - For tests: #9
   - Methods: `openPreviousVersion()`, `returnToCurrent()`

### Existing Components to Extend

- **EditorPage** - add button visibility checks (#6)
- **RepositoryPage** - add Chinese character support (#10)
- **AdminPage** - localization testing (#10)

---

## 💡 KEY SUCCESS FACTORS

### What Makes Tests EASY to Migrate

1. ✅ **High-level methods** - `RepositoryTab.createProject()` vs raw XPath
2. ✅ **Linear flow** - create → edit → verify, no complex branching
3. ✅ **Standard components** - EditorPage, RepositoryPage already exist
4. ✅ **Self-contained** - no special infrastructure required
5. ✅ **Clear assertions** - simple `assertThat()` verifications

### What Makes Tests HARD to Migrate

1. ❌ **Direct XPath usage** - `By.xpath()` throughout test
2. ❌ **Coordinate-based checks** - element position verification
3. ❌ **Complex dropdowns** - conditional logic based on dropdown state
4. ❌ **Infrastructure dependencies** - OAuth, SAML, LDAP, external APIs
5. ❌ **Large size** - 200+ lines with many edge cases

---

## 📊 ROI ANALYSIS

### 3-Week Migration Sprint

**Input:**
- 3 weeks × 5 days = 15 working days
- 1 engineer full-time

**Output:**
- 15 tests migrated
- ~600 lines of test code
- 4 new components created
- Coverage areas: Deploy (27%), Repository (33%), Editor (27%), Admin (13%)

**Risk Mitigation:**
- Start with shortest tests (36-41 lines)
- Validate infrastructure with first test
- Parallel component development possible

**Quality Metrics:**
- All tests < 80 lines → maintainable
- Standard patterns → consistent codebase
- No infrastructure blockers → predictable timeline

---

## 🎯 NEXT STEPS

1. **Immediate (Day 1):**
   - Review and approve migration plan
   - Set up test data for FileAddDelete
   - Create FileUploadComponent skeleton

2. **Week 1:**
   - Migrate Tier 1 tests (#1-5)
   - Create required components (FileUpload, Deployment)
   - Run tests in both LOCAL and DOCKER modes

3. **Week 2-3:**
   - Continue with Tier 2 tests (#6-15)
   - Refactor common patterns
   - Document component usage

4. **Post-Sprint:**
   - Retrospective on migration process
   - Update component library documentation
   - Plan Tier 3 migration (medium complexity tests)

---

## 📝 NOTES

- All tests verified to be in active XML suites
- Line counts are from actual source files
- Time estimates based on Tier 1 test complexity
- Components can be developed in parallel
- Tests #1-5 can establish baseline for remaining migrations

**Recommendation:** Start with **FileAddDelete** (36 lines) - shortest test, validates FileUpload component, builds confidence for remaining migrations.
