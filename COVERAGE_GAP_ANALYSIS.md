# Coverage Gap Analysis: Legacy → New Framework

> Updated: 2026-03-06
> Based on: `OpenL covered features - UI-Autotest.csv` traceability matrix

## Statistics

| Metric | Value |
|--------|-------|
| Total features in matrix | 317 |
| Covered by legacy autotests | 277 (93.4%) |
| Migrated to new framework | 110 test classes |
| Suites | `rules_editor.xml` (21) · `studio_issues.xml` (43) · `studio_smoke.xml` (28) · `studio_git.xml` (11) |
| ACL functionality | Новая ACL модель (BRD EPBDS-14295): 6 тест-классов покрывают роли Manager/Contributor/Viewer, permissions V/C/E/D, системные действия Run+Benchmark. 2 фичи disabled — не реализованы в UI (Deploy button, Manager→Admin) |
| **New framework overall coverage** | **~60% of legacy** (up from ~57%; +4 ACL класса + 2 JDBC integration tests с Testcontainers) |

---

## Composite Tests — Priority Migration List

> These tests each cover **multiple atomic features** in a single scenario. Migrate these first to maximise coverage with minimum test count.

| ID | Test to Create | Legacy class | Atomic features covered | Priority |
|----|----------------|--------------|-------------------------|----------|
| **C1** | **`TestRepositoryBrowsingFilterStatus`** ✅ DONE | TestBrowsingFilterStatusInRepositories | ✅ Status lifecycle (2.2.1), ✅ Filter by name (2.2.2), ✅ Advanced filter show/hide deleted (2.2.3), ✅ Closing a project (2.2.12), ✅ Saving a project (2.2.14), ✅ Multi-user locking; ⛔ Deploy-blocked: Deployment repo status (2.2.4), Deployment filter (2.2.5), Production repo browsing | `repository.xml` (deploy steps blocked) |
| **C2** | ~~TestRepositoryExportAndRevisions~~ → **`TestExportProjectFunctionality`** ✅ DONE | TestExportProjectFunctionality | Export project/file (2.2.29), Opening project revision via Revisions tab (2.2.11), Revision selection, Multi-user export | ✅ `rules_editor.xml` |
| **C3** | **`TestOpenApiImportAndReconciliation`** ✅ DONE | TestOpenApiImport | ✅ Reconciliation mode (2.8.4), ✅ Tables Generation mode (2.8.2), ✅ Module settings warning dialog (2.8.6), ✅ Same module names validation (2.8.6), ✅ Module name retention on mode switch (1.1), ✅ Overwrite warning (3-3.2), ✅ Non-OpenAPI project defaults (8-8.2), ✅ Tables generation for non-OpenAPI project (10-10.1), ✅ Path validation errors (12-13), ✅ New modules + path editing (4-5.2), ✅ Mode cycling (6-6.3), ✅ Two-file project (7), ✅ Corporate Rating template (14) | ✅ `rules_editor.xml` |
| **C3b** | **`TestOpenApiImportLocalChanges`** ✅ DONE | TestOpenApiImportLocalChanges | ✅ Local Changes history after re-generation (Step 1), ✅ Template project + Compare window (Step 2-2.2), ✅ No Local Changes after Reconciliation mode import (Step 3), ✅ No new record for same file content (Step 4), ✅ New record for different file (Step 5) | ✅ `rules_editor.xml` |
| **C4** | **`TestOpenApiReconciliationEdgeCases`** ✅ DONE | OpenApiReconciliationFeature | ✅ Circular datatype validation (EPBDS-13215), ✅ Datatype error validation, ✅ Dependent project errors, ✅ Spreadsheet reconciliation errors, ✅ Multiple merged files JSON+YAML (IPBQA-30970) | ✅ `rules_editor.xml` |
| **C5** | **`TestCreateProjectFromOpenApiFile`** + **`TestCreateDataTablesFromOpenApiGetMethod`** ✅ DONE | TestCreateProjectFromOpenApiFile + TestCreateDataTablesFromOpenApiGetMethod | ✅ Create project from OpenAPI JSON/YAML (2.8.1), ✅ Custom module names/paths, ✅ Delete OpenAPI file removes properties, ✅ Form validation errors, ✅ Create Data tables from GET methods (2.8.3), ✅ Data table editing | ✅ `rules_editor.xml` |
| **C6** | **ACL tests** ✅ DONE (6 классов) | TestACLUserManagementAndRepositoryRoles, TestACLProjectLevelRoles, TestACLContributorRole, TestACLDeploySystemAction, TestACLRunBenchmarkSystemAction, TestACLManagePermission | Новая ACL модель (BRD EPBDS-14295): Manager/Contributor/Viewer роли, V+C+E+D+M permissions, Run+Benchmark visible all users (тест-таблицы). 2 теста disabled (Deploy button, Manager→Admin — не имплементированы в UI) | ✅ `studio_smoke.xml` |
| **C7** | **`TestProjectCompilation`** + **`TestCompileThisModuleOnly`** + **`TestCompilationProgressBar`** + **`TestWorkWithDuplicateTables`** + **`TestSwitchModuleViaBreadcrumbsNavigation`** ✅ DONE | TestProjectCompilation + TestCompileThisModuleOnly + TestCompilationProgressBar + TestWorkWithDuplicateTables + TestSwitchModuleViaBreadcrumbsNavigation | ✅ Project compilation main scenarios (2.11.2), ✅ Progress bar behavior (2.11.1), ✅ Run/Trace/Test in opened module (2.11.3), ✅ Duplicate tables errors (2.11.6), ✅ Breadcrumb navigation (2.11.7) | ✅ `rules_editor.xml` |
| **C8** | **`TestCompareExcelFiles`** + **`TestDisplayChangedRows`** ✅ DONE | TestCompareExcelFiles + TestDisplayChangedRows | ✅ Compare Excel files (2.1.55), ✅ Display Changed Rows Only (EPBDS-10790), ✅ Comparing project revisions (2.2.28) | ✅ `rules_editor.xml` |
| **C9** | **`TestTabRevisionsInEditor`** + **`TestLocalChangesRestoreCompare`** ✅ DONE | TestDeployButton (⛔ deploy-blocked) + TestTabRevisionsOnEditorTab + TestChangesRestoreCompareHistorySettings | ⛔ Deploy button (deploy not available for testing), ✅ Revision page in Editor (IPBQA-30123) → `TestTabRevisionsInEditor` (1 method), ✅ Local Changes: Restore/Compare (IPBQA-30730) → `TestLocalChangesRestoreCompare` (10 methods) | ✅ `rules_editor.xml` |
| **C10** | **`TestRepositoryTableActions`** ✅ DONE | TestUIRepositoryTab + TestTableActionButtons | ✅ Table action buttons open/close (EPBDS-12712, IPBQA-32158): Deploy/Close/Open icons in Actions column, ButtonsPanel open/close, viewer user access; ✅ Repository tab properties (IPBQA-29847): ModifiedBy/ModifiedAt/Revision multi-user; ⛔ Deploy-blocked: Deploy table action "already deployed" dialog, Deploy Configuration properties, Production repository verification | ✅ `studio_smoke.xml` (deploy steps blocked) |
| **C11** | TestEditorOrderingAndSearch | TestOrderingMode + TestSearchOnProjectLevel | Table ordering mode – default setting (EPBDS-13592), Search on Project level (EPBDS-13988), User preference persistence | 🟡 MEDIUM |
| **C12** | TestDesignRepositoryManagement | TestAddDeleteDesignRepository + TestSupportedRepositories | Multiple Design Repos (EPBDS-9983), Repository Name Validation (EPBDS-11289), Webstudio with wrong repo settings (EPBDS-11420), Supported repositories availability | 🟡 MEDIUM |
| **C12b** | **`TestMultipleDesignRepositoriesWithPostgres`** ✅ DONE | TestMultipleDesignReposGitFlatNonFlatAndJDBC | ✅ Multiple Design Repos: Git flat (Design) + Git non-flat (Design1) (IPBQA-30859), ✅ PostgreSQL JDBC security DB via Testcontainers, ✅ Copy project across repos with path-in-repository, ✅ Duplicate project name error, ✅ Edit Project dialog for flat/non-flat | `studio_smoke.xml` |
| **C12c** | **`TestDeploymentConfigurationRepositoryConnection`** ✅ DONE | TestDeploymentConfigurationRepositoryConnection | ✅ Deployment Configuration Repository via Oracle JDBC (IPBQA-27365), ✅ Oracle container via Testcontainers, ✅ Deploy project to Oracle JDBC deployment repo, ✅ Verify deployed data in Oracle DB | `studio_smoke.xml` |
| **C13** | TestVersioningByFolders | TestVersioningByFolders | Versioning by folders (EPBDS-10363), Table properties across versions, Property inheritance per version | 🟢 LOW |
| **C14** | TestGitCommentAndCommitter | TestGitCustomizeCommentFields + TestGitCommitterName + TestGitCommentsGenerationOnProjectName | Customized Comment fields (EPBDS-8371), Committer's name (EPBDS-8362), Comments generation on project name (EPBDS-8460) | 🟢 LOW |

---

## 🔴 CRITICAL GAPS (0–10% coverage)

### 1. OpenAPI (2.8) — ~95%
**Legacy tests:** 7 | **New framework:** C3 + C3b + C4 + C5 + studio_issues all done ✅

| Feature | Ticket | Legacy test | Covered by |
|---------|--------|-------------|------------|
| Create Project from OpenAPI | EPBDS-10846, IPBQA-30678 | TestCreateProjectFromOpenApiFile | ✅ **C5** |
| Import OpenAPI file for scaffolding in existing project | EPBDS-10812, IPBQA-31035 | TestOpenApiImport | ✅ **C3** |
| Create Data tables from OpenAPI GET methods | EPBDS-10770, IPBQA-31073 | TestCreateDataTablesFromOpenApiGetMethod | ✅ **C5** (~95%: minor empty-cell assertions in reference rows not migrated) |
| OpenAPI reconciliation feature | IPBQA-30902 | OpenApiReconciliationFeature | ✅ **C4** |
| OpenAPI reconciliation with multiple merged files | EPBDS-10620, IPBQA-30970 | TestOpenApiReconciliationWithMultipleMergedFiles | ✅ **C4** |
| OpenAPI file operations (Compare screen, error messages, default date) | EPBDS-10543, EPBDS-10789, EPBDS-10548 | TestOpenApiErrorMessages + TestGenerateOpenApiDefaultDate + TestCompareScreenForOpenApiFiles | ✅ **studio_issues** (~93% avg: `.contains()` vs exact match in error messages; compare locator uncertainty) |
| Auto-add/update OpenAPI file in reconciliation mode | EPBDS-12260, IPBQA-32071 | TestOpenApiImportLocalChanges | ✅ **C3b** |

### 2. Single/Multi Mode (2.11) — ~100% ✅
**Legacy tests:** 6 | **New framework:** 5 test classes, 9 methods (C7 fully migrated)

| Feature | Ticket | Legacy test | Covered by |
|---------|--------|-------------|------------|
| Project compilation – main scenarios | EPBDS-11873, IPBQA-31701 | TestProjectCompilation | ✅ **C7** TestProjectCompilation (4 methods) |
| Hide Progress Bar after compilation finished | EPBDS-11812, IPBQA-31733 | TestCompilationProgressBar | ✅ **C7** TestCompilationProgressBar (2 methods) |
| Run/Trace/Test in currently opened module only | EPBDS-11813, IPBQA-31729 | TestCompileThisModuleOnly | ✅ **C7** TestCompileThisModuleOnly (1 method) |
| Single/Multi-module setting on module level | EPBDS-11799, IPBQA-31758 | — | ✅ **C7** TestCompileThisModuleOnly |
| Compile This Module Only redesign | EPBDS-11799, IPBQA-31895 | TestCompileThisModuleOnly | ✅ **C7** TestCompileThisModuleOnly |
| Errors and Run/Trace/Test buttons for duplicate tables | EPBDS-11791, IPBQA-31790 | TestWorkWithDuplicateTables | ✅ **C7** TestWorkWithDuplicateTables (1 method) |
| No errors on switching projects via breadcrumb | EPBDS-11827, IPBQA-31804 | TestSwitchModuleViaBreadcrumbsNavigation | ✅ **C7** TestSwitchModuleViaBreadcrumbsNavigation (1 method) |

---

## 🟡 PARTIALLY COVERED (20–60%)

### 4. REPOSITORY (2.2) — ~55%
**Legacy tests:** 30+ | **New framework:** C1 + C2 done; basic operations widely covered across suites

> Note: Many "basic operations" (create from zip/template/excel, close, save, copy project, upload/delete files) are covered **indirectly** across many existing tests as test setup steps. Only a few advanced features remain uncovered.

| Feature | Ticket | Legacy test | Covered by |
|---------|--------|-------------|------------|
| Browsing Design repo: project pictures by status | EPBDS-9847 | TestBrowsingFilterStatusInRepositories | ✅ **C1** |
| Design repo: Filter by name | EPBDS-9847 | TestBrowsingFilterStatusInRepositories | ✅ **C1** |
| Design repo: Advanced filter (show/hide deleted) | EPBDS-9847 | TestBrowsingFilterStatusInRepositories | ✅ **C1** |
| Browsing Deployment repo: project pictures by status | EPBDS-9847 | TestBrowsingFilterStatusInRepositories | ⛔ deploy-blocked in **C1** |
| Deployment repo: Filter by name | EPBDS-9847 | TestBrowsingFilterStatusInRepositories | ⛔ deploy-blocked in **C1** |
| Closing a Project | 2.2.12 | test066 | ✅ **C1** + TestGitStatusCopyClosedProject |
| Saving a Project | 2.2.14 | various | ✅ **C1** + many git/rules_editor tests |
| Opening Project Revision via Revisions tab | IPBQA-29644 | testProjectRestoreFromOldRevision | ✅ **C2** |
| Opening Project Revision via Open Revision button | — | Repository.Test056 | ✅ **C2** |
| Comparing Project Revisions | EPBDS-8517, EPBDS-8536 | Test035 | ✅ **C8** |
| Exporting a Project or File | EPBDS-10703, IPBQA-31329 | TestExportProjectFunctionality | ✅ **C2** |
| Table action buttons (open/close/deploy) | EPBDS-12712, IPBQA-32158 | TestTableActionButtons | ✅ **C10** TestRepositoryTableActions (deploy-blocked steps excluded) |
| Copying a project | 2.2.15 | Test060 | ✅ TestGitStatusCopyClosedProject + git tests |
| Creating a Project from a Template | 2.2.6 | test040 | ✅ TestRepositoryBrowsingFilterStatus + many git tests |
| Creating a Project from Excel file | 2.2.7 | — | ✅ CreateDataTypeTableTest |
| Creating a Project from Zip archive | 2.2.8 | Test017 | ✅ TestExportProjectFunctionality + many tests |
| Creating a folder | 2.2.17 | Test048 | ❌ not migrated |
| Uploading a file | 2.2.18 | test048AddFolder | ✅ TestFileAddDelete + TestOpenApiImportAndReconciliation |
| Delete folder and file | 2.2.19 | Test050, Test055 | ✅ TestFileAddDelete |
| Copy a file | 2.2.20 | Test073–Test079 | ❌ not migrated (file-level copy, not module) |
| Copying a Module | 2.1.12 | Test129 | ✅ TestExportProjectFunctionality |
| Unlocking a Project | IPBQA-30550 | TestUnlockProjectDeployConf | ⛔ deploy-blocked (requires deploy configurations) |
| Resolve Conflicts (by Sheets) | EPBDS-13488, IPBQA-32406 | TestResolveConflictFunctionality | standalone test (not migrated) |
| Resolve Conflicts dialog improvements | EPBDS-9158, IPBQA-29110 | TestImproveResolveConflictDialog | standalone test (not migrated) |
| Revisions on Resolve Conflicts screen | EPBDS-9717, IPBQA-29901 | — | standalone test (not migrated) |
| Git LFS support | EPBDS-12651, IPBQA-32116 | — | standalone test (not migrated) |
| Azure BLOB storage support | EPBDS-12457, IPBQA-32109 | TestAzureBlobRepoStudio | standalone test (not migrated) |
| Show technical revisions on Revisions tab | EPBDS-13652, IPBQA-32483 | — | standalone test (not migrated) |
| Project design revision tracking in deployment | EPBDS-9687, IPBQA-29847 | — | standalone test (not migrated) |
| Apply settings from rules-deploy.xml on JSON deserialization | EPBDS-10737, IPBQA-30930 | TestRulesDeploySettingsOnDeserializationJson | standalone test (not migrated) |
| Non-flat git folder structure: create/copy/delete/edit | EPBDS-10853, IPBQA-30903 | TestNonFlatRepoIssues | → **C12** (partial) |
| Change validation pattern for Repository Name | EPBDS-11533, IPBQA-31641 | TestRepositoryNameValidation | → **C12** |

### 5. Editor – Advanced Features (2.1.x) — ~25%
**Legacy tests:** 65+ | **New framework:** 15 (rules_editor) + 24 (studio_issues)

| Feature | Ticket | Legacy test | Covered by |
|---------|--------|-------------|------------|
| Filtering projects (incl. advanced filter) | 2.1.1 | Test109 | → **C1** (partial via repo filter) |
| Comparing & Reverting Module Changes | 2.1.11, EPBDS-8867 | Test027, Test028 | ✅ **C8** |
| Compare Excel files | EPBDS-10472, IPBQA-30875 | TestCompareExcelFiles | ✅ **C8** |
| Display Changed Rows Only in Compare | EPBDS-12481, IPBQA-32105 | TestDisplayChangedRows | ✅ **C8** |
| Identical files info message | EPBDS-10162 | — | ✅ **C8** |
| Deploy button in Editor | EPBDS-9507, IPBQA-29618 | TestDeployButton | ⛔ deploy-blocked in **C9** |
| Revision page in Editor (project history) | EPBDS-9997, IPBQA-30123 | TestTabRevisionsOnEditorTab | ✅ **C9** TestTabRevisionsInEditor (1 method) |
| Local Changes page: Restore, Compare | EPBDS-10539, IPBQA-30730 | TestChangesRestoreCompareHistorySettings | ✅ **C9** TestLocalChangesRestoreCompare (10 methods) |
| Table ordering mode (default setting) | EPBDS-13851, IPBQA-32512 | TestOrderingMode | → **C11** |
| Search on Project level screen | EPBDS-14181, IPBQA-32590 | TestSearchOnProjectLevel | → **C11** |
| Versioning by folders | EPBDS-10363, IPBQA-30979 | TestVersioningByFolders | → **C13** |
| Managing Range data types | EPBDS-7489, IPBQA-25791 | TestRangeDataTypes | standalone test (not migrated) |
| Create table by copying existing | IPBQA-31552 | TestCopyTableAsNewTable | standalone test (not migrated) |
| Create table as new version | IPBQA-31552 | TestCopyTableAsNewVersion | standalone test (not migrated) |
| Create table as Business Dimension version | IPBQA-31601, EPBDS-11436 | TestCopyTableAsNewBusinessDimension | standalone test (not migrated) |
| Creating a Test table via wizard | 2.1.25 | CreateTestMethod | standalone test (not migrated) |
| Creating a Test table with ID column | RulesEditor.Test100 | — | standalone test (not migrated) |
| Editing Comma-Separated Array of values (DDL) | EPBDS-7508, IPBQA-25824 | TestEditingCommaSeparatedArrayValues | standalone test (not migrated) |
| Tracing rules | test113, test115 | TracingRunTables/* | standalone test (not migrated) |
| Trace in file | EPBDS-7715, IPBQA-25978 | TestTraceInFileFunctionality | standalone test (not migrated) |
| Benchmark Tools | test037 | — | standalone test (not migrated) |
| Edit table: Undo/Redo | test001, test002 | — | standalone test (not migrated) |
| Edit table: Insert/Delete row, column | 2.1.49, 2.1.50 | MainActionsInsertRemoveRow | standalone test (not migrated) |
| Edit table: Bold/Italic/Underline / fill color | 2.1.52, 2.1.53 | MainActionsFont | standalone test (not migrated) |
| History – Recently visited table | 2.1.58 | test038 | standalone test (not migrated) |
| SmartLookup / SmartRules tables Open/Edit/Save | EPBDS-9293, IPBQA-29358 | TestSmartLookupSmartRules | standalone test (not migrated) |
| Simple/SmartRules tables Create via Wizard | EPBDS-9818, IPBQA-29967 | TestSimpleLookupSimpleRules | standalone test (not migrated) |
| TBasic tables Open/work/edit | Test013 | — | standalone test (not migrated) |
| Run tables Open/work/edit | IPBQA-29970 | TestRunTable | standalone test (not migrated) |
| Collapsing Error Message in Editor | EPBDS-11587, IPBQA-25869 | — | standalone test (not migrated) |
| Navigation to table | EPBDS-7537, IPBQA-25912 | TestNavigationToTable | standalone test (not migrated) |
| Explanation feature | EPBDS-8876, IPBQA-28386 | — | standalone test (not migrated) |
| Refresh button | EPBDS-8869, IPBQA-28382 | TestRefreshButton | standalone test (not migrated) |
| Rename project in Editor (non-flat git) | EPBDS-10845, IPBQA-30937 | TestRenameProjectInEditor | standalone test (not migrated) |
| Run/Trace buttons always visible | EPBDS-11722, IPBQA-31761 | TestRunTraceButtonsVisibleForAllTypeTables | standalone test (not migrated) |

### 6. Git (2.5) — ~44%
**Legacy tests:** 25+ | **New framework:** 11 tests

| Feature | Ticket | Legacy test | Covered by |
|---------|--------|-------------|------------|
| Customized Comment fields | EPBDS-8783, IPBQA-27845 | TestGitCustomizeCommentFields | → **C14** |
| Committer's name verification | EPBDS-8631, IPBQA-27640 | TestGitCommitterName | → **C14** |
| Comments generation on project name entering | EPBDS-8547, IPBQA-27497 | TestGitCommentsGenerationOnProjectName | → **C14** |
| Configure Git repo using dev sources | EPBDS-8527, IPBQA-27456 | TestGitConfigureUseDevSources | standalone test (not migrated) |
| Changes committed outside Webstudio | EPBDS-8839, IPBQA-27179 | — | standalone test (not migrated) |
| Copy branch functionality | EPBDS-11160, IPBQA-27264 | — | standalone test (not migrated) |
| New Branch pattern | EPBDS-8855, IPBQA-27623 | — | standalone test (not migrated) |
| Branch links in Editor tab | EPBDS-8579, IPBQA-27551 | TestGitBranchLinksInEditorTab | standalone test (not migrated) |
| Delete Branch improvements | EPBDS-10849, IPBQA-31011 | TestDeleteBranch | standalone test (not migrated) |
| Check shortened revision numbers | EPBDS-9649, IPBQA-29690 | — | standalone test (not migrated) |
| Changes check interval | EPBDS-8806, IPBQA-28116 | — | standalone test (not migrated) |
| HTTP → HTTPS git repo URL change | EPBDS-11370, IPBQA-31528 | TestGitHttpToHttpsAndViceVersa | standalone test (not migrated) |
| Protected Branches | IPBQA-31896 | TestProtectedBranches | standalone test (not migrated) |

### 7. WebService (Section 1) — ~25%
**Legacy tests:** 10+ | **New framework:** 3 tests

| Feature | Ticket | Legacy test | Covered by |
|---------|--------|-------------|------------|
| Available services list | — | TestWebservicesDisplay | standalone test (not migrated) |
| Swagger UI / JSON / YAML | IPBQA-32142, EPBDS-12603 | TestSwaggerUI | standalone test (not migrated) |
| Verification of Project Status | EPBDS-9421, IPBQA-29488 | TestProjectStatus | standalone test (not migrated) |
| Webservices displayed on tab | EPBDS-8887, IPBQA-28417 | TestWebservicesDisplay | standalone test (not migrated) |
| New RuleServices UI | EPBDS-14196, IPBQA-32607 | — | standalone test (not migrated) |
| Write OpenL runtime errors in log | EPBDS-12576, IPBQA-32104 | — | standalone test (not migrated) |
| Alphabetical sorting of deployments | EPBDS-13121, IPBQA-32307 | — | standalone test (not migrated) |

### 8. Admin: System Settings – Repositories (2.3.1.8-12) — ~30%
**Legacy tests:** 10+ | **New framework:** 2 tests (Testcontainers-based JDBC integration)

| Feature | Ticket | Legacy test | Covered by |
|---------|--------|-------------|------------|
| Supported repositories availability | EPBDS-9227, IPBQA-29276 | TestSupportedRepositories | → **C12** |
| Multiple Design Repos: Git flat + non-flat + PostgreSQL JDBC security DB | EPBDS-10968, IPBQA-30682 | TestMultipleDesignReposGitFlatNonFlatAndJDBC | ✅ **C12b** TestMultipleDesignRepositoriesWithPostgres |
| Deployment Configuration Repository — Oracle JDBC | IPBQA-27365 | TestDeploymentConfigurationRepositoryConnection | ✅ **C12c** TestDeploymentConfigurationRepositoryConnection |
| Webstudio works if one design repo has wrong settings | EPBDS-11441, IPBQA-31634 | — | → **C12** |
| Restore default (Repositories) | EPBDS-9628, IPBQA-29638 | TestRestoreDefault | standalone test (not migrated) |
| Common: History max count & Clean history | EPBDS-10539, IPBQA-30730 | TestCommonSettingsHistory | standalone test (not migrated) |
| Common: Other settings (update table properties, date format) | IPBQA-30650 | TestCommonSettingsOther | standalone test (not migrated) |

### 9. Auth / SSO / AD (2.3.2, 2.3.3, SSO sections) — ~5%
> ⚠️ These tests require external systems (LDAP/AD/SAML/OAuth). Migrating requires container mocks.

| Feature | Ticket | Legacy test |
|---------|--------|-------------|
| Multi-User mode with View Access | IPBQA-31411 | TestMultiUserModeViewAccess |
| SSO SAML + Local user management | IPBQA-23489 | TestSSOLocalManagement |
| SSO SAML + External user management | IPBQA-23521 | TestSSOExternalManagement |
| SSO SAML Key Field validation | IPBQA-31649 | TestSSOKeyField |
| OAuth2 (OIDC) authentication | IPBQA-32155 | TestOauth |
| OAuth2 settings menu | IPBQA-32156, IPBQA-32157 | TestOauthSettingsMenu |
| Configure Initial Users without admin | IPBQA-31407 | TestInitialUsersWithoutAdminUser |
| Active Directory Groups | IPBQA-23052 | ADGroupsTest |
| LDAP filter | IPBQA-29211 | TestLDAPFilter |
| Username validation | EPBDS-10893, IPBQA-31196 | — |
| Validate user email address | EPBDS-12554, IPBQA-32118 | TestUserEmailValidation |

---

## 🟢 WELL COVERED (50%+)

| Section | Coverage | New fw tests |
|---------|----------|--------------|
| Admin: System Settings (dispatch/verify/threads) | ~80% | TestAdminSystemSettings ✅ |
| Admin: Notifications | ~90% | TestAdminNotifications ✅ |
| Admin: User management + ACL (новая модель BRD EPBDS-14295) | ~85% | TestACLUserManagementAndRepositoryRoles, TestACLProjectLevelRoles, TestACLContributorRole, TestACLDeploySystemAction, TestACLRunBenchmarkSystemAction, TestACLManagePermission ✅ (2 теста disabled — не имплементировано в UI) |
| User Settings / Profile | ~75% | TestAdminUserSettings ✅ |
| Tags (basic creation + validation only) | ~25% | TestProjectTagsCreation* ✅ (3 tests) — filtering, grouping, auto-fill not yet migrated |
| Rules Editor (core) | ~65% | 45 tests in rules_editor package (incl. OpenAPI, Compare, C7, C8) ✅ |
| Single/Multi Mode (compilation) | ~100% | C7: 5 test classes, 9 methods ✅ |
| Git (core operations) | ~44% | 11 git tests ✅ |
| Studio Issues (bug regression) | ~45% | 25 studio_issues tests ✅ |
| Repository (basic operations) | ~55% | C1 + C2 + basic ops across suites ✅ |
| Admin: Repositories (JDBC integration) | ~30% | C12b TestMultipleDesignRepositoriesWithPostgres + C12c TestDeploymentConfigurationRepositoryConnection ✅ (Testcontainers: PostgreSQL + Oracle) |
| OpenAPI | ~95% | C3 + C3b + C4 + C5 + studio_issues ✅ all done |
| Compare (Excel/revisions/local changes) | ~80% | C8: TestCompareExcelFiles + TestDisplayChangedRows ✅ |

---

## Priority Migration Roadmap

| Priority | Composite Test | Atomic features consolidated | Effort |
|----------|----------------|------------------------------|--------|
| ~~🔴~~ | ~~**C5** TestCreateProjectFromOpenApi~~ | ~~2 remaining OpenAPI features~~ | ✅ DONE |
| ~~🔴~~ | ~~**C7** TestProjectCompilationAndModuleMode~~ | ~~7 Single/Multi Mode features → 5 test classes, 9 methods~~ | ✅ DONE |
| ~~🟡 2~~ | ~~**C8** TestCompareExcelFilesAndChanges~~ | ~~4 compare features → 2 tests~~ | ✅ DONE |
| ~~🟡 3~~ | ~~**C9** TestEditorDeployAndRevisions~~ | ~~3 editor features → TestTabRevisionsInEditor + TestLocalChangesRestoreCompare~~ | ✅ DONE (deploy-blocked) |
| ~~🟡 4~~ | ~~**C10** TestRepositoryTableActions~~ | ~~3 table action features → 2 tests~~ | ✅ DONE (deploy steps blocked) |
| 🟡 5 | **C11** TestEditorOrderingAndSearch | 2 editor features → 1 test | Low |
| ~~🟡~~ | ~~**C12b** TestMultipleDesignRepositoriesWithPostgres~~ | ~~Multiple Design Repos + PostgreSQL JDBC (IPBQA-30859)~~ | ✅ DONE |
| ~~🟡~~ | ~~**C12c** TestDeploymentConfigurationRepositoryConnection~~ | ~~Deployment Repo via Oracle JDBC (IPBQA-27365)~~ | ✅ DONE |
| 🟡 6 | **C12** TestDesignRepositoryManagement | 4 repo management features → 1 test (remaining: add/delete repo, name validation, wrong settings) | High |
| 🟢 7 | **C13** TestVersioningByFolders | 3 versioning features → 1 test | Low |
| 🟢 8 | **C14** TestGitCommentAndCommitter | 3 git comment features → 1 test | Low |

---

## Notes
- **Deploy Configuration** — marked as legacy, not migrated
- **Installation Wizard** — marked as legacy, not migrated
- **Shared Data (2.6)** — 3 of 4 features marked as "Can't be automated", skipped
- **Docker (2.9)** — infrastructure concern, not UI test scope
- **AI Tools (4)** — not yet automatable
- **SSO/AD/OAuth** — require external system mocks, lowest migration priority
- **Basic repository operations** (create project from template/zip/excel, close, save, copy, upload/delete files) are broadly covered as setup steps across `git/`, `studio_smoke/`, `rules_editor/`, and `studio_issues/` suites — not as dedicated standalone tests, but functionally verified
