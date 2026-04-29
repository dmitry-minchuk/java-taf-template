# Coverage Gap Analysis: Legacy → New Framework

> Updated: 2026-04-29 (TestRangeDataTypes migrated, regression verified)
> Based on: `OpenL covered features - UI-Autotest.csv` traceability matrix

## Statistics

| Metric | Value |
|--------|-------|
| Total features in matrix | 317 |
| Covered by legacy autotests | 277 (93.4%) |
| New framework — total test classes | **106** (active in tracked `testng_suites`) |
| Deleted legacy artifacts | `TestButtonDeployAvailableDeployConfiguration` (deleted — Deploy Configuration removed from WebStudio per EPBDS-15093), `DeployConfigurationTabsComponent` (deleted) |
| Suites | `rules_editor.xml` (28) · `studio_issues.xml` (28) · `studio_smoke.xml` (30) · `studio_git.xml` (12) · `service_smoke.xml` (6) · `central_projects_regression.xml` (1) · `zip_projects_regression.xml` (1) · **Total: 106** |
| ACL functionality | New ACL model (BRD EPBDS-14295): 10 test classes, 23 methods (22 active + 1 disabled) covering Manager/Contributor/Viewer roles, V/C/E/D/M permissions, Run+Benchmark system actions for all roles, deploy repo access (incl. Viewer+Contributor minimum combo per BRD TR2), lock/unlock deprecated, no-access warning, parsed groups view. 1 test disabled — Manager Administration access not yet implemented in UI |
| Multi-container infra tests | 3 tests using `DeployInfrastructureService`: TestNewDeployPopup (Postgres + WS), TestDeploymentConfigurationRepositoryConnection (Oracle), TestMultipleDesignRepositoriesWithPostgres (Postgres security DB) |
| Auth/SSO/AD coverage strategy | Authentication (OAuth2, SAML, AD, LDAP) tested via backend API by dev team. Authorization/permissions tested via UI ACL tests (10 classes, 23 methods). 11 legacy auth features reclassified: ~10 covered (backend API + UI ACL), ~1 partial (AD Groups requires EUMS). See Section 9 |
| Removed from product (N/A) | Deploy Configuration (EPBDS-15093), Unlock Project (deploy config dependent), Installation Wizard, Azure BLOB storage (requires Azure account — won't automate) — excluded from coverage denominator |
| **New framework overall coverage** | **~83.3% of legacy feature areas** (+ TestRangeDataTypes: Range Editor open/close across Decision/SimpleLookup/SimpleRules/SmartLookup/SmartRules/Data/Run/Test/Vocabulary/Constants tables. Completed: C1-C13, C12b, C12c, C12d, C14 partial, SmartLookup/SmartRules, SimpleLookup/SimpleRules, Range data types, ACL full, OpenAPI full; remaining notable git gap: C14 comment-generation check) |

---

## Composite Tests — Priority Migration List

> These tests each cover **multiple atomic features** in a single scenario. Migrate these first to maximise coverage with minimum test count.

| ID | Test to Create | Legacy class | Atomic features covered | Priority |
|----|----------------|--------------|-------------------------|----------|
| **C1** | **`TestRepositoryBrowsingFilterStatus`** ✅ DONE | TestBrowsingFilterStatusInRepositories | ✅ Status lifecycle (2.2.1), ✅ Filter by name (2.2.2), ✅ Advanced filter show/hide deleted (2.2.3), ✅ Closing a project (2.2.12), ✅ Saving a project (2.2.14), ✅ Multi-user locking, ✅ Creating a folder inside project; Deployment repo status (2.2.4), Deployment filter (2.2.5), Production repo browsing — not yet migrated | `repository.xml` |
| **C2** | ~~TestRepositoryExportAndRevisions~~ → **`TestExportProjectFunctionality`** ✅ DONE | TestExportProjectFunctionality | ✅ Export project/file (2.2.29), ✅ Opening project revision via Revisions tab (2.2.11), ✅ Revision selection, ✅ Multi-user export | ✅ `rules_editor.xml` |
| **C3** | **`TestOpenApiImportAndReconciliation`** ✅ DONE | TestOpenApiImport | ✅ Reconciliation mode (2.8.4), ✅ Tables Generation mode (2.8.2), ✅ Module settings warning dialog (2.8.6), ✅ Same module names validation (2.8.6), ✅ Module name retention on mode switch (1.1), ✅ Overwrite warning (3-3.2), ✅ Non-OpenAPI project defaults (8-8.2), ✅ Tables generation for non-OpenAPI project (10-10.1), ✅ Path validation errors (12-13), ✅ New modules + path editing (4-5.2), ✅ Mode cycling (6-6.3), ✅ Two-file project (7), ✅ Corporate Rating template (14) | ✅ `rules_editor.xml` |
| **C3b** | **`TestOpenApiImportLocalChanges`** ✅ DONE | TestOpenApiImportLocalChanges | ✅ Local Changes history after re-generation (Step 1), ✅ Template project + Compare window (Step 2-2.2), ✅ No Local Changes after Reconciliation mode import (Step 3), ✅ No new record for same file content (Step 4), ✅ New record for different file (Step 5) | ✅ `rules_editor.xml` |
| **C4** | **`TestOpenApiReconciliationEdgeCases`** ✅ DONE | OpenApiReconciliationFeature | ✅ Circular datatype validation (EPBDS-13215), ✅ Datatype error validation, ✅ Dependent project errors, ✅ Spreadsheet reconciliation errors, ✅ Multiple merged files JSON+YAML (IPBQA-30970) | ✅ `rules_editor.xml` |
| **C5** | **`TestCreateProjectFromOpenApiFile`** + **`TestCreateDataTablesFromOpenApiGetMethod`** ✅ DONE | TestCreateProjectFromOpenApiFile + TestCreateDataTablesFromOpenApiGetMethod | ✅ Create project from OpenAPI JSON/YAML (2.8.1), ✅ Custom module names/paths, ✅ Delete OpenAPI file removes properties, ✅ Form validation errors, ✅ Create Data tables from GET methods (2.8.3), ✅ Data table editing | ✅ `rules_editor.xml` |
| **C6** | **ACL tests** ✅ DONE (10 classes, 23 methods) | TestACLUserManagementAndRepositoryRoles, TestACLProjectLevelRoles, TestACLContributorRole, TestACLDeploySystemAction, TestACLRunBenchmarkSystemAction, TestACLManagePermission, TestACLDeployWithDeployRepo, TestACLLockUnlockDeprecated, TestACLNoAccessWarning, TestACLParsedGroupsUserView | ✅ New ACL model (BRD EPBDS-14295): Manager/Contributor/Viewer roles, V+C+E+D+M permissions, Run+Benchmark system actions for all roles (Viewer/Contributor/Manager), deploy repo access incl. minimum combo Viewer(design)+Contributor(deploy) per BRD TR2, Lock/Unlock deprecated, no-access warning + role assignment flow, parsed groups view in Admin. ⛔ 1 test disabled (Manager to Admin — not implemented in UI). Group Templates skipped (requires EUMS/LDAP) | ✅ `studio_smoke.xml` |
| **C7** | **`TestProjectCompilation`** + **`TestCompileThisModuleOnly`** + **`TestCompilationProgressBar`** + **`TestWorkWithDuplicateTables`** + **`TestSwitchModuleViaBreadcrumbsNavigation`** ✅ DONE | TestProjectCompilation + TestCompileThisModuleOnly + TestCompilationProgressBar + TestWorkWithDuplicateTables + TestSwitchModuleViaBreadcrumbsNavigation | ✅ Project compilation main scenarios (2.11.2), ✅ Progress bar behavior (2.11.1), ✅ Run/Trace/Test in opened module (2.11.3), ✅ Duplicate tables errors (2.11.6), ✅ Breadcrumb navigation (2.11.7) | ✅ `rules_editor.xml` |
| **C8** | **`TestCompareExcelFiles`** + **`TestDisplayChangedRows`** ✅ DONE | TestCompareExcelFiles + TestDisplayChangedRows | ✅ Compare Excel files (2.1.55), ✅ Display Changed Rows Only (EPBDS-10790), ✅ Comparing project revisions (2.2.28) | ✅ `rules_editor.xml` |
| **C9** | **`TestTabRevisionsInEditor`** + **`TestLocalChangesRestoreCompare`** ✅ DONE | TestDeployButton + TestTabRevisionsOnEditorTab + TestChangesRestoreCompareHistorySettings | ⚠️ Deploy button in Editor — exists in UI (verified 6.1.0), partially covered via TestRepositoryBrowsingFilterStatus + ACL deploy tests; dedicated Editor-level deploy visibility test not migrated. ✅ Revision page in Editor (IPBQA-30123) → `TestTabRevisionsInEditor` (1 method), ✅ Local Changes: Restore/Compare (IPBQA-30730) → `TestLocalChangesRestoreCompare` (10 methods) | ✅ `rules_editor.xml` |
| **C10** | **`TestRepositoryTableActions`** ✅ DONE | TestUIRepositoryTab + TestTableActionButtons | ✅ Table action buttons open/close (EPBDS-12712, IPBQA-32158): Deploy/Close/Open icons in Actions column, ButtonsPanel open/close, viewer user access; ✅ Repository tab properties (IPBQA-29847): ModifiedBy/ModifiedAt/Revision multi-user; ⛔ Deploy-blocked: Deploy table action "already deployed" dialog, Deploy Configuration properties, Production repository verification | ✅ `studio_smoke.xml` (deploy steps blocked) |
| **C11** | **`TestOrderingMode`** + **`TestSearchOnProjectLevel`** ✅ DONE | TestOrderingMode + TestSearchOnProjectLevel | ✅ Table ordering mode – default setting (EPBDS-13592), ✅ Search on Project level (EPBDS-13988), ✅ User preference persistence, ✅ Advanced search with scope/type/property filters | ✅ `rules_editor.xml` |
| **C12** | **`TestAddDeleteDesignRepository`** + **`TestSupportedRepositories`** ✅ DONE | TestAddDeleteDesignRepository + TestSupportedRepositories | ✅ Multiple Design Repos add/delete (EPBDS-9983), ✅ Supported repositories availability (IPBQA-29276); Installation Wizard checks replaced by Admin Settings. DeploymentConfigurationRepository checks skipped (EPBDS-15093) | ✅ `studio_smoke.xml` |
| **C12b** | **`TestMultipleDesignRepositoriesWithPostgres`** ✅ DONE | TestMultipleDesignReposGitFlatNonFlatAndJDBC | ✅ Multiple Design Repos: Git flat (Design) + Git non-flat (Design1) (IPBQA-30859), ✅ PostgreSQL JDBC security DB via Testcontainers + DeployInfrastructureService, ✅ Copy project across repos with path-in-repository, ✅ Duplicate project name error, ✅ Edit Project dialog for flat/non-flat | ✅ `studio_smoke.xml` |
| **C12c** | **`TestDeploymentConfigurationRepositoryConnection`** ✅ DONE | TestDeploymentConfigurationRepositoryConnection | ✅ Deployment Repository via Oracle JDBC (IPBQA-27365), ✅ Oracle container via Testcontainers + DeployInfrastructureService, ✅ Deploy project to Oracle JDBC deployment repo, ✅ Verify deployed data in Oracle DB | ✅ `studio_smoke.xml` |
| **C12d** | **`TestNewDeployPopup`** ✅ DONE | TestNewDeployPopup (IPBQA-30049) | ✅ Deploy project to production via DeployModal (new UI, replaces legacy Deploy Configuration), ✅ Deploy dependent projects with auto-resolved dependencies, ✅ Edit table + save + redeploy, ✅ WS REST verification via GetWsServicesMethod API. Uses DeployInfrastructureService (Postgres + WS container). Note: Legacy Deploy Configuration was removed from WebStudio (EPBDS-15093, commit ff754010d0) | ✅ `studio_smoke.xml` |
| **C13** | **`TestVersioningByFolders`** ✅ DONE | TestVersioningByFolders | ✅ Versioning by folders (EPBDS-10363), ✅ Table properties across versions, ✅ Property inheritance per version | ✅ `rules_editor.xml` |
| **C14** | ~~TestGitCommentAndCommitter~~ ✅ DONE (merged into TestMergeBranchesNoConflicts) | TestGitCustomizeCommentFields + TestGitCommitterName + TestGitCommentsGenerationOnProjectName | ✅ Custom comment fields (EPBDS-8371) — custom comment set + verified in revisions; ✅ Committer's name (EPBDS-8362) — verified via getRevisionModifiedBy(); ⚠️ Comments generation on project name (EPBDS-8460) — auto-generated comments verified but no dedicated test | ✅ `studio_git.xml` |
| **C15** | **`TestSmartLookupSmartRules`** ✅ DONE | TestSmartLookupSmartRules | ✅ SmartRules table open/content verification, ✅ insert/edit enum column + save/no problems, ✅ remove row, ✅ copy as Business Dimension Version (`Countries=FR`), ✅ remove copied/base table, ✅ SmartLookup open/content verification, ✅ insert row, ✅ remove column, ✅ create default Test table via wizard | ✅ `rules_editor.xml` |
| **C16** | **`TestSimpleLookupSimpleRules`** ✅ DONE | TestSimpleLookupSimpleRules | ✅ SimpleRules table open/content verification, ✅ remove/recreate SimpleRules via wizard, ✅ add/delete SimpleRules rule rows, ✅ run SimpleRules, ✅ insert/edit enum column + save/no problems, ✅ remove row, ✅ copy as Business Dimension Version (`Countries=AU`), ✅ SimpleLookup open/content verification, ✅ run SimpleLookup with filled/empty input, ✅ insert row before, ✅ remove column, ✅ create default Test table via wizard | ✅ `rules_editor.xml` |

---

## 🟡 REMAINING GAPS (< 60% coverage)

### 1. Editor – Advanced Features (2.1.x) — ~55%
**Legacy tests:** 65+ | **New framework:** 18 (rules_editor) + 24 (studio_issues)

| Feature | Ticket | Legacy test | Covered by |
|---------|--------|-------------|------------|
| Filtering projects (incl. advanced filter) | 2.1.1 | Test109 | ⚠️ partial — Repository-level filter ✅ **C1**; Editor-level project filter/grouping (TestGroupProjectsFilter, TestHideDeletedProjectsFilterWithGrouping) ❌ not migrated |
| Comparing & Reverting Module Changes | 2.1.11, EPBDS-8867 | Test027, Test028 | ✅ **C8** |
| Compare Excel files | EPBDS-10472, IPBQA-30875 | TestCompareExcelFiles | ✅ **C8** |
| Display Changed Rows Only in Compare | EPBDS-12481, IPBQA-32105 | TestDisplayChangedRows | ✅ **C8** |
| Identical files info message | EPBDS-10162 | — | ✅ **C8** |
| Deploy button in Editor | EPBDS-9507, IPBQA-29618 | TestDeployButton | ⚠️ partial — Deploy button **exists** in Editor toolbar (verified on 6.1.0, visible when deploy repo configured). Deploy functionality covered via TestRepositoryBrowsingFilterStatus (DeployModal), TestNewDeployPopup, ACL deploy tests. Dedicated Editor-level deploy visibility test (visible when saved / hidden on unsaved changes) not migrated |
| Revision page in Editor (project history) | EPBDS-9997, IPBQA-30123 | TestTabRevisionsOnEditorTab | ✅ **C9** TestTabRevisionsInEditor (1 method) |
| Local Changes page: Restore, Compare | EPBDS-10539, IPBQA-30730 | TestChangesRestoreCompareHistorySettings | ✅ **C9** TestLocalChangesRestoreCompare (10 methods) |
| Table ordering mode (default setting) | EPBDS-13851, IPBQA-32512 | TestOrderingMode | ✅ **C11** TestOrderingMode (4 methods) |
| Search on Project level screen | EPBDS-14181, IPBQA-32590 | TestSearchOnProjectLevel | ✅ **C11** TestSearchOnProjectLevel (2 methods) |
| Versioning by folders | EPBDS-10363, IPBQA-30979 | TestVersioningByFolders | ✅ **C13** TestVersioningByFolders |
| Managing Range data types | EPBDS-7489, IPBQA-25791 | TestRangeDataTypes | ✅ TestRangeDataTypes — Range Editor open/close validated for Decision/SimpleLookup/SimpleRules/SmartLookup/SmartRules/Data/Run/Test/Vocabulary/Constants tables (Spreadsheet skipped per legacy bug EPBDS-7484) |
| Create table by copying existing | IPBQA-31552 | TestCopyTableAsNewTable | ✅ `CopyTableDialogComponent` implemented; `copyTableAsNew()` used in TestMethodTable + TestDisplayChangedRowsTableStructure |
| Create table as new version | IPBQA-31552 | TestCopyTableAsNewVersion | ✅ `copyTableAsNewVersion()` added to TestMethodTable.testTableCopyAndManagement — verifies New Version copy dialog + no compilation errors |
| Create table as Business Dimension version | IPBQA-31601, EPBDS-11436 | TestCopyTableAsNewBusinessDimension | ✅ `copyTableAsBusinessDimension()` added to TestMethodTable.testTableCopyAndManagement — verifies BD Version copy dialog + no compilation errors |
| Creating a Test table via wizard | 2.1.25 | CreateTestMethod | ⚠️ partial — `TestSmartLookupSmartRules` creates `MySmartLookupTest`; `TestSimpleLookupSimpleRules` creates `SimpleLEx2Test`; dedicated standalone CreateTestMethod scenarios not migrated |
| Creating a Test table with ID column | RulesEditor.Test100 | — | ❌ not migrated |
| Editing Comma-Separated Array of values (DDL) | EPBDS-7508, IPBQA-25824 | TestEditingCommaSeparatedArrayValues | ❌ not migrated |
| Tracing rules | test113, test115 | TracingRunTables/* | ⚠️ partial — TestAllStepsDisplayedInTrace, TestTraceIntoFileJsonRequest, TestArrayOfAliasValuesInRunTrace, TestAdminUserSettings (trace window), TestViewStackTraceFunctionality cover core trace scenarios; dedicated TracingRunTables suite not migrated |
| Trace in file | EPBDS-7715, IPBQA-25978 | TestTraceInFileFunctionality | ✅ TestTraceIntoFileJsonRequest (trace into file with JSON request) |
| Benchmark Tools | test037 | — | ⚠️ partial — TestACLRunBenchmarkSystemAction verifies Benchmark button visibility for all roles (Viewer/Contributor/Manager); benchmark execution workflow not tested |
| Edit table: Undo/Redo | test001, test002 | — | ✅ TestAddDeleteRowWithoutSaving — undo reverts cell edit, redo restores it |
| Edit table: Insert/Delete row, column | 2.1.49, 2.1.50 | MainActionsInsertRemoveRow | ✅ TestAddDeleteRowWithoutSaving covers add/delete row; `TestSmartLookupSmartRules` covers insert column before, remove row, insert row after, remove column; `TestSimpleLookupSimpleRules` covers insert column before, insert row before, remove row, remove column |
| Edit table: Bold/Italic/Underline / fill color | 2.1.52, 2.1.53 | MainActionsFont | ❌ not migrated |
| History – Recently visited table | 2.1.58 | test038 | ❌ not migrated |
| SmartLookup / SmartRules tables Open/Edit/Save | EPBDS-9293, IPBQA-29358 | TestSmartLookupSmartRules | ✅ `TestSmartLookupSmartRules` migrated (IPBQA-29358): open/content checks, edit/save/no problems, copy as BD version, remove table, create default Test table |
| SimpleLookup/SimpleRules tables Create via Wizard | EPBDS-9818, IPBQA-29967 | TestSimpleLookupSimpleRules | ✅ `TestSimpleLookupSimpleRules` migrated (IPBQA-29967): import/open checks, remove/recreate SimpleRules via wizard, run SimpleRules, edit/save SimpleRules, copy as BD version, open/run/edit SimpleLookup, create default Test table |
| TBasic tables Open/work/edit | Test013 | — | ❌ not migrated |
| Run tables Open/work/edit | IPBQA-29970 | TestRunTable | ⚠️ partial — TestViewStackTraceFunctionality navigates Run→RunTable; TestDefaultProperties opens Run SpreadsheetTable; TestWorkWithDuplicateTables checks Run/Trace buttons; TestTableIcons verifies run.gif icon; no dedicated Run table CRUD test |
| Collapsing Error Message in Editor | EPBDS-11587, IPBQA-25869 | — | ❌ not migrated |
| Navigation to table | EPBDS-7537, IPBQA-25912 | TestNavigationToTable | ❌ not migrated |
| Explanation feature | EPBDS-8876, IPBQA-28386 | — | ❌ not migrated |
| Refresh button | EPBDS-8869, IPBQA-28382 | TestRefreshButton | ❌ not migrated |
| Rename project in Editor (non-flat git) | EPBDS-10845, IPBQA-30937 | TestRenameProjectInEditor | ✅ TestMultipleDesignRepositoriesWithPostgres step 9.2 — rename project in non-flat Git repo via EditProjectDialog, verify rename persists, rename back |
| Run/Trace buttons always visible | EPBDS-11722, IPBQA-31761 | TestRunTraceButtonsVisibleForAllTypeTables | ⚠️ partial — TestACLRunBenchmarkSystemAction verifies Run button visible for all ACL roles; "visible for all table types" not tested |

### 2. Git (2.5) — ~60%
**Legacy tests:** 25+ | **New framework:** 12 tests

| Feature | Ticket | Legacy test | Covered by |
|---------|--------|-------------|------------|
| Customized Comment fields | EPBDS-8783, IPBQA-27845 | TestGitCustomizeCommentFields | ✅ TestMergeBranchesNoConflicts — custom comment set via SaveChangesComponent.commentField, verified in revision history |
| Committer's name verification | EPBDS-8631, IPBQA-27640 | TestGitCommitterName | ✅ TestMergeBranchesNoConflicts — committer name verified via getRevisionModifiedBy() in revision history |
| Comments generation on project name entering | EPBDS-8547, IPBQA-27497 | TestGitCommentsGenerationOnProjectName | ⚠️ partial — auto-generated comments verified in TestMergeBranchesNoConflicts revision history (e.g. "Project X is saved.", "Project X is created."); dedicated project-name-in-comment generation test not migrated |
| Configure Git repo using dev sources | EPBDS-8527, IPBQA-27456 | TestGitConfigureUseDevSources | ❌ not migrated |
| Changes committed outside Webstudio | EPBDS-8839, IPBQA-27179 | — | ❌ not migrated |
| Copy branch functionality | EPBDS-11160, IPBQA-27264 | — | ❌ not migrated |
| New Branch pattern | EPBDS-8855, IPBQA-27623 | — | ❌ not migrated |
| Branch links in Editor tab | EPBDS-8579, IPBQA-27551 | TestGitBranchLinksInEditorTab | ❌ not migrated |
| Delete Branch improvements | EPBDS-10849, IPBQA-31011 | TestDeleteBranch | ⚠️ partial — TestGitSwitchDeletedBranchPreset + TestGitSwitchToDeletedBranch cover behavior with deleted branches; delete branch action/UI improvements not tested |
| Check shortened revision numbers | EPBDS-9649, IPBQA-29690 | — | ❌ not migrated |
| Changes check interval | EPBDS-8806, IPBQA-28116 | — | ❌ not migrated |
| HTTP → HTTPS git repo URL change | EPBDS-11370, IPBQA-31528 | TestGitHttpToHttpsAndViceVersa | ❌ not migrated |
| Protected Branches | IPBQA-31896, EPBDS-15753 | TestProtectedBranches | ✅ TestProtectedBranchMergeProtection |

### 3. WebService (Section 1) — ~55%
**Legacy tests:** 10+ | **New framework:** 6 tests (`service_smoke.xml`: TestWebservicesDeployUI + TestDeployProjectsWithoutServiceNameInRulesDeploy + TestWebservicesGitRepo + TestRuleServicesNewUI + TestRuleServiceS3DeployClasspathJarProperty + TestWebservicesSwaggerUi)

| Feature | Ticket | Legacy test | Covered by |
|---------|--------|-------------|------------|
| Available services list | — | TestWebservicesDisplay | ❌ not migrated |
| Swagger UI / JSON / YAML | IPBQA-32142, EPBDS-12603 | TestSwaggerUI | ✅ TestWebservicesSwaggerUi |
| Verification of Project Status | EPBDS-9421, IPBQA-29488 | TestProjectStatus | ❌ not migrated |
| Webservices displayed on tab | EPBDS-8887, IPBQA-28417 | TestWebservicesDisplay | ❌ not migrated |
| New RuleServices UI | EPBDS-14196, IPBQA-32607 | — | ✅ TestRuleServicesNewUI |
| Write OpenL runtime errors in log | EPBDS-12576, IPBQA-32104 | — | ❌ not migrated |
| Alphabetical sorting of deployments | EPBDS-13121, IPBQA-32307 | — | ✅ TestWebservicesSwaggerUi |

### 4. Admin: System Settings – Repositories (2.3.1.8-12) — ~40%
**Legacy tests:** 10+ | **New framework:** 3 tests (Testcontainers-based JDBC integration + deploy)

| Feature | Ticket | Legacy test | Covered by |
|---------|--------|-------------|------------|
| Supported repositories availability | EPBDS-9227, IPBQA-29276 | TestSupportedRepositories | ✅ **C12** TestSupportedRepositories |
| Multiple Design Repos: Git flat + non-flat + PostgreSQL JDBC security DB | EPBDS-10968, IPBQA-30682 | TestMultipleDesignReposGitFlatNonFlatAndJDBC | ✅ **C12b** TestMultipleDesignRepositoriesWithPostgres |
| Deployment Repository — Oracle JDBC | IPBQA-27365 | TestDeploymentConfigurationRepositoryConnection | ✅ **C12c** TestDeploymentConfigurationRepositoryConnection |
| Deploy to production via DeployModal (new UI) | IPBQA-30049 | TestNewDeployPopup | ✅ **C12d** TestNewDeployPopup |
| Webstudio works if one design repo has wrong settings | EPBDS-11441, IPBQA-31634 | — | → **C12** |
| Restore default (Repositories) | EPBDS-9628, IPBQA-29638 | TestRestoreDefault | ❌ not migrated |
| Common: History max count & Clean history | EPBDS-10539, IPBQA-30730 | TestCommonSettingsHistory | ❌ not migrated |
| Common: Other settings (update table properties, date format) | IPBQA-30650 | TestCommonSettingsOther | ❌ not migrated |

---

## 🟢 WELL COVERED (50%+)

| Section | Coverage | New fw tests |
|---------|----------|--------------|
| Admin: System Settings (dispatch/verify/threads) | ~80% | TestAdminSystemSettings ✅ |
| Admin: Notifications | ~90% | TestAdminNotifications ✅ |
| Admin: User management + ACL (BRD EPBDS-14295) | ~95% | 10 ACL test classes, 23 methods (22 active + 1 disabled): UserManagement, ProjectLevelRoles, ContributorRole, DeploySystemAction, DeployWithDeployRepo (incl. Viewer+Contributor min combo), RunBenchmarkSystemAction (all 3 roles), ManagePermission, LockUnlockDeprecated, NoAccessWarning, ParsedGroupsUserView ✅ (1 test disabled — Manager Admin access not implemented; Group Templates skipped — requires EUMS/LDAP) |
| User Settings / Profile | ~75% | TestAdminUserSettings ✅ |
| Tags (basic creation + validation only) | ~25% | TestProjectTagsCreation* ✅ (3 tests) — filtering, grouping, auto-fill not yet migrated |
| Rules Editor (core) | ~73% | 49 tracked rules-editor scenarios in active suites (incl. OpenAPI, Compare, C7, C8, C13, SmartLookup/SmartRules, SimpleLookup/SimpleRules, Range data types) ✅ + trace coverage: TestTraceIntoFileJsonRequest, TestAllStepsDisplayedInTrace, TestArrayOfAliasValuesInRunTrace, TestViewStackTraceFunctionality |
| Single/Multi Mode (compilation) | ~100% | C7: 5 test classes, 9 methods ✅ |
| Git (core operations) | ~60% | 12 git tests ✅ + Resolve Conflicts covered by TestMergeBranchesWithConflicts (Use Yours/Theirs) + Custom comments + Committer name verification in TestMergeBranchesNoConflicts + Protected Branches ✅ |
| WebService (Section 1) | ~55% | 6 service_smoke tests ✅ including TestRuleServicesNewUI + TestWebservicesSwaggerUi |
| Studio Issues (bug regression) | ~50% | 28 studio_issues suite tests ✅ |
| Repository (2.2) | ~75% | C1 + C2 + basic ops across suites ✅ + Resolve Conflicts (TestMergeBranchesWithConflicts) + Non-flat ✅ C12b + Folder creation ✅ C1. Unlock Project + Azure BLOB excluded from denominator (N/A). Remaining gaps: copy file, Git LFS, technical revisions, rules-deploy settings |
| Admin: Repositories (JDBC integration + deploy) | ~40% | C12b (PostgreSQL security DB) + C12c (Oracle JDBC) + C12d (Deploy to production via DeployModal) ✅ — all using DeployInfrastructureService |
| OpenAPI | ~95% | C3 + C3b + C4 + C5 + studio_issues ✅ all done |
| Compare (Excel/revisions/local changes) | ~80% | C8: TestCompareExcelFiles + TestDisplayChangedRows ✅ |
| Client: Central + Zip projects | ~80% | `central_projects_regression.xml` (TestLocalCentralProjects) + `zip_projects_regression.xml` (TestLocalZippedProjects) ✅ |
| Auth / SSO / AD | ~90% | Authentication: backend API tests (dev team). Authorization: 10 ACL UI test classes, 23 methods. 10/11 legacy features covered, 1 partial (AD Groups — requires EUMS). See Section 9 |

---

## Priority Migration Roadmap

| Priority | Composite Test | Atomic features consolidated | Effort |
|----------|----------------|------------------------------|--------|
| ~~🔴~~ | ~~**C5** TestCreateProjectFromOpenApi~~ | ~~2 remaining OpenAPI features~~ | ✅ DONE |
| ~~🔴~~ | ~~**C7** TestProjectCompilationAndModuleMode~~ | ~~7 Single/Multi Mode features → 5 test classes, 9 methods~~ | ✅ DONE |
| ~~🟡 2~~ | ~~**C8** TestCompareExcelFilesAndChanges~~ | ~~4 compare features → 2 tests~~ | ✅ DONE |
| ~~🟡 3~~ | ~~**C9** TestEditorDeployAndRevisions~~ | ~~3 editor features → TestTabRevisionsInEditor + TestLocalChangesRestoreCompare~~ | ✅ DONE |
| ~~🟡 4~~ | ~~**C10** TestRepositoryTableActions~~ | ~~3 table action features → 2 tests~~ | ✅ DONE |
| ~~🟡~~ | ~~**C12b** TestMultipleDesignRepositoriesWithPostgres~~ | ~~Multiple Design Repos + PostgreSQL JDBC (IPBQA-30859)~~ | ✅ DONE |
| ~~🟡~~ | ~~**C12c** TestDeploymentConfigurationRepositoryConnection~~ | ~~Deployment Repo via Oracle JDBC (IPBQA-27365)~~ | ✅ DONE |
| ~~🟡~~ | ~~**C12d** TestNewDeployPopup~~ | ~~Deploy to production + WS REST verification (IPBQA-30049)~~ | ✅ DONE |
| ~~🟡 5~~ | ~~**C11** TestEditorOrderingAndSearch~~ | ~~2 editor features → TestOrderingMode (4 methods) + TestSearchOnProjectLevel (2 methods)~~ | ✅ DONE |
| ~~🟡 6~~ | ~~**C12** TestDesignRepositoryManagement~~ | ~~4 repo features → TestAddDeleteDesignRepository + TestSupportedRepositories~~ | ✅ DONE |
| ~~🟢 7~~ | ~~**C13** TestVersioningByFolders~~ | ~~3 versioning features → 1 test~~ | ✅ DONE |
| ~~🟢 8~~ | ~~**C14** TestGitCommentAndCommitter~~ | ~~3 git comment features → 1 test~~ | ✅ DONE (merged into TestMergeBranchesNoConflicts: custom comment + committer name; 1 partial — comment generation) |

---

## Next Test To Migrate

**Selected next target:** `TestNavigationToTable`

Why this is the next migration candidate:
- `TestRangeDataTypes` is now migrated and regression-checked in `rules_editor.xml`.
- `TestNavigationToTable` (EPBDS-7537, IPBQA-25912) covers in-editor navigation between linked tables — a functional workflow path with no current coverage in the new framework.
- Better than the remaining 0%-coverage candidates: Admin Common Settings is settings-only, font formatting is cosmetic, Copy-a-file is simple but low-value, Git LFS / HTTP→HTTPS need special infra/repo URL changes — each is lower-signal than navigation flow.

**Truly uncovered feature areas (0% coverage):**
1. Navigation to table (EPBDS-7537)
2. Admin Common Settings (history max count, date format)
3. Edit table: Bold/Italic/Underline/fill color
4. Copy a file (file-level, not module)
5. Git LFS support
6. HTTP → HTTPS git repo URL change

**Removed from coverage denominator (N/A):**
- Deploy Configuration (EPBDS-15093) — removed from product
- Unlock Project (TestUnlockProjectDeployConf) — dependent on Deploy Configuration which was removed per EPBDS-15093
- Azure BLOB storage (TestAzureBlobRepoStudio) — requires Azure account, won't automate; TestSupportedRepositories verifies Azure listed as repo type
- Installation Wizard — legacy, replaced by Admin Settings

---

## Notes
- **Deploy Configuration** — removed from WebStudio (EPBDS-15093, commit ff754010d0). Legacy `DeployConfigurationTabsComponent` and `TestButtonDeployAvailableDeployConfiguration` deleted from the new framework. Deploy now works directly from project via React DeployModal with auto-resolved dependencies.
- **DeployInfrastructureService** — builder-pattern service (`helpers.service.DeployInfrastructureService`) encapsulating Docker infrastructure for multi-container tests. Supports `.withPostgres()` (production repo), `.withPostgresAsSecurityDb()` (security DB), `.withOracle()`, `.withWsContainer()`. See README.md for full documentation.
- **Installation Wizard** — marked as legacy, not migrated
- **Shared Data (2.6)** — 3 of 4 features marked as "Can't be automated", skipped
- **Docker (2.9)** — infrastructure concern, not UI test scope
- **AI Tools (4)** — not yet automatable
- **SSO/AD/OAuth** — authentication methods (OAuth2, SAML, Active Directory, LDAP) are tested via backend API integration tests by the dev team. Authorization and permission enforcement is covered by UI ACL tests (10 classes, 23 methods). Legacy UI auth tests are not migrated — their coverage is split between backend API tests (auth) and UI ACL tests (authorization)
- **Basic repository operations** (create project from template/zip/excel, close, save, copy, upload/delete files) are broadly covered as setup steps across `git/`, `studio_smoke/`, `rules_editor/`, and `studio_issues/` suites — not as dedicated standalone tests, but functionally verified
