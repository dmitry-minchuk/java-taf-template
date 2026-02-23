# Coverage Gap Analysis: Legacy → New Framework

> Generated: 2026-02-20
> Based on: `OpenL covered features - UI-Autotest.csv` traceability matrix

## Statistics

| Metric | Value |
|--------|-------|
| Total features in matrix | 317 |
| Covered by legacy autotests | 277 (93.4%) |
| Migrated to new framework | ~62 test classes |
| **New framework overall coverage** | **~22% of legacy** |

---

## Composite Tests — Priority Migration List

> These tests each cover **multiple atomic features** in a single scenario. Migrate these first to maximise coverage with minimum test count.

| ID | Test to Create | Legacy class | Atomic features covered | Priority |
|----|----------------|--------------|-------------------------|----------|
| **C1** | **`TestRepositoryBrowsingFilterStatus`** ✅ PARTIAL | TestBrowsingFilterStatusInRepositories | ✅ Status lifecycle (2.2.1), ✅ Filter by name (2.2.2), ✅ Advanced filter show/hide deleted (2.2.3), ✅ Closing a project (2.2.12), ✅ Saving a project (2.2.14), ✅ Multi-user locking; ⛔ Deploy-blocked: Deployment repo status (2.2.4), Deployment filter (2.2.5), Production repo browsing | `repository.xml` (deploy steps blocked) |
| **C2** | ~~TestRepositoryExportAndRevisions~~ → **`TestExportProjectFunctionality`** ✅ DONE | TestExportProjectFunctionality | Export project/file (2.2.29), Opening project revision via Revisions tab (2.2.11), Revision selection, Multi-user export | ✅ `rules_editor.xml` |
| **C3** | TestOpenApiImportAndReconciliation | TestOpenApiImport | Import OpenAPI for scaffolding (2.8.2), Reconciliation mode (2.8.4), Auto-add/update OpenAPI in reconciliation (2.8.7), OpenAPI file operations (2.8.6) | 🔴 HIGH |
| **C4** | TestOpenApiReconciliationEdgeCases | OpenApiReconciliationFeature | OpenAPI reconciliation with multiple merged files (2.8.5), Circular datatype validation, Datatype error validation, Spreadsheet reconciliation errors | 🔴 HIGH |
| **C5** | TestCreateProjectFromOpenApi | TestCreateProjectFromOpenApiFile + TestCreateDataTablesFromOpenApiGetMethod | Create project from OpenAPI (2.8.1), Create Data tables from GET methods (2.8.3) | 🔴 HIGH |
| **C6** | TestAclPermissions | ACLTest | All 10 ACL permission scenarios (read / create / edit / run benchmark / add / delete / erase), user+group creation, repo-level and module-level verification | 🔴 HIGH |
| **C7** | TestProjectCompilationAndModuleMode | TestProjectCompilation + TestCompileThisModuleOnly | Project compilation main scenarios (2.11.2), Progress bar behavior (2.11.1), Run/Trace/Test in opened module (2.11.3), Duplicate tables errors (2.11.6), Breadcrumb navigation (2.11.7) | 🔴 HIGH |
| **C8** | TestCompareExcelFilesAndChanges | TestCompareExcelFiles + TestDisplayChangedRows | Compare Excel files (2.1.55), Display Changed Rows Only (EPBDS-10790), Identical files info message (EPBDS-10162), Comparing project revisions (2.2.28) | 🟡 MEDIUM |
| **C9** | TestEditorDeployAndRevisions | TestDeployButton + TestTabRevisionsOnEditorTab | Deploy button in Editor (EPBDS-9423), Revision page in Editor (EPBDS-9815), Local Changes page: Restore/Compare (EPBDS-10399) | 🟡 MEDIUM |
| **C10** | TestRepositoryTableActions | TestUIRepositoryTab + TestTableActionButtons | Table action buttons: open/close/deploy (EPBDS-11936), Repository tab operations, Multi-user operations, Deployment status verification | 🟡 MEDIUM |
| **C11** | TestEditorOrderingAndSearch | TestOrderingMode + TestSearchOnProjectLevel | Table ordering mode – default setting (EPBDS-13592), Search on Project level (EPBDS-13988), User preference persistence | 🟡 MEDIUM |
| **C12** | TestDesignRepositoryManagement | TestAddDeleteDesignRepository + TestSupportedRepositories | Multiple Design Repos (EPBDS-9983), Repository Name Validation (EPBDS-11289), Webstudio with wrong repo settings (EPBDS-11420), Supported repositories availability | 🟡 MEDIUM |
| **C13** | TestVersioningByFolders | TestVersioningByFolders | Versioning by folders (EPBDS-10363), Table properties across versions, Property inheritance per version | 🟢 LOW |
| **C14** | TestGitCommentAndCommitter | TestGitCustomizeCommentFields + TestGitCommitterName + TestGitCommentsGenerationOnProjectName | Customized Comment fields (EPBDS-8371), Committer's name (EPBDS-8362), Comments generation on project name (EPBDS-8460) | 🟢 LOW |

---

## 🔴 CRITICAL GAPS (0–10% coverage)

### 1. REPOSITORY (2.2) — ~3%
**Legacy tests:** 30+ | **New framework:** 1 test (TestNewDeployPopup, suite is empty)

| Feature | Ticket | Legacy test | Covered by |
|---------|--------|-------------|------------|
| Browsing Design repo: project pictures by status | EPBDS-9847 | TestBrowsingFilterStatusInRepositories | → **C1** |
| Design repo: Filter by name | EPBDS-9847 | TestBrowsingFilterStatusInRepositories | → **C1** |
| Design repo: Advanced filter | EPBDS-9847 | TestBrowsingFilterStatusInRepositories | → **C1** |
| Browsing Deployment repo: project pictures by status | EPBDS-9847 | TestBrowsingFilterStatusInRepositories | → **C1** |
| Deployment repo: Filter by name | EPBDS-9847 | TestBrowsingFilterStatusInRepositories | → **C1** |
| Closing a Project | 2.2.12 | test066 | → **C1** |
| Saving a Project | 2.2.14 | various | → **C1** |
| Opening Project Revision via Revisions tab | IPBQA-29644 | testProjectRestoreFromOldRevision | → **C2** |
| Opening Project Revision via Open Revision button | — | Repository.Test056 | → **C2** |
| Comparing Project Revisions | EPBDS-8517, EPBDS-8536 | Test035 | → **C2**, **C8** |
| Exporting a Project or File | EPBDS-10703, IPBQA-31329 | TestExportProjectFunctionality | → **C2** |
| Table action buttons (open/close/deploy) | EPBDS-12712, IPBQA-32158 | TestTableActionButtons | → **C10** |
| Copying a project | 2.2.15 | Test060 | → **C1** (partial) |
| Creating a Project from a Template | 2.2.6 | test040 | standalone test |
| Creating a Project from Excel file | 2.2.7 | — | standalone test |
| Creating a Project from Zip archive | 2.2.8 | Test017 | standalone test |
| Creating a folder | 2.2.17 | Test048 | standalone test |
| Uploading a file | 2.2.18 | test048AddFolder | standalone test |
| Delete folder and file | 2.2.19 | Test050, Test055 | standalone test |
| Copy a file | 2.2.20 | Test073–Test079 | standalone test |
| Unlocking a Project | IPBQA-30550 | TestUnlockProjectDeployConf | standalone test |
| Resolve Conflicts (by Sheets) | EPBDS-13488, IPBQA-32406 | TestResolveConflictFunctionality | standalone test |
| Resolve Conflicts dialog improvements | EPBDS-9158, IPBQA-29110 | TestImproveResolveConflictDialog | standalone test |
| Revisions on Resolve Conflicts screen | EPBDS-9717, IPBQA-29901 | — | standalone test |
| Git LFS support | EPBDS-12651, IPBQA-32116 | — | standalone test |
| Azure BLOB storage support | EPBDS-12457, IPBQA-32109 | TestAzureBlobRepoStudio | standalone test |
| Show technical revisions on Revisions tab | EPBDS-13652, IPBQA-32483 | — | standalone test |
| Project design revision tracking in deployment | EPBDS-9687, IPBQA-29847 | — | standalone test |
| Apply settings from rules-deploy.xml on JSON deserialization | EPBDS-10737, IPBQA-30930 | TestRulesDeploySettingsOnDeserializationJson | standalone test |
| Non-flat git folder structure: create/copy/delete/edit | EPBDS-10853, IPBQA-30903 | TestNonFlatRepoIssues | → **C12** (partial) |
| Change validation pattern for Repository Name | EPBDS-11533, IPBQA-31641 | TestRepositoryNameValidation | → **C12** |

### 2. OpenAPI (2.8) — 0%
**Legacy tests:** 7 | **New framework:** 0 tests

| Feature | Ticket | Legacy test | Covered by |
|---------|--------|-------------|------------|
| Create Project from OpenAPI | EPBDS-10846, IPBQA-30678 | TestCreateProjectFromOpenApiFile | → **C5** |
| Import OpenAPI file for scaffolding in existing project | EPBDS-10812, IPBQA-31035 | TestOpenApiImport | → **C3** |
| Create Data tables from OpenAPI GET methods | EPBDS-10770, IPBQA-31073 | TestCreateDataTablesFromOpenApiGetMethod | → **C5** |
| OpenAPI reconciliation feature | IPBQA-30902 | OpenApiReconciliationFeature | → **C4** |
| OpenAPI reconciliation with multiple merged files | EPBDS-10620, IPBQA-30970 | TestOpenApiReconciliationWithMultipleMergedFiles | → **C4** |
| OpenAPI file operations | EPBDS-10543, IPBQA-30922 | — | → **C3** (partial) |
| Auto-add/update OpenAPI file in reconciliation mode | EPBDS-12260, IPBQA-32071 | TestOpenApiImportLocalChanges | → **C3** |

### 3. Single/Multi Mode (2.11) — 0%
**Legacy tests:** 6 | **New framework:** 0 tests

| Feature | Ticket | Legacy test | Covered by |
|---------|--------|-------------|------------|
| Project compilation – main scenarios | EPBDS-11873, IPBQA-31701 | TestProjectCompilation | → **C7** |
| Hide Progress Bar after compilation finished | EPBDS-11812, IPBQA-31733 | TestCompilationProgressBar | → **C7** |
| Run/Trace/Test in currently opened module only | EPBDS-11813, IPBQA-31729 | TestCompileThisModuleOnly | → **C7** |
| Single/Multi-module setting on module level | EPBDS-11799, IPBQA-31758 | — | → **C7** |
| Compile This Module Only redesign | EPBDS-11799, IPBQA-31895 | TestCompileThisModuleOnly | → **C7** |
| Errors and Run/Trace/Test buttons for duplicate tables | EPBDS-11791, IPBQA-31790 | TestWorkWithDuplicateTables | → **C7** |
| No errors on switching projects via breadcrumb | EPBDS-11827, IPBQA-31804 | TestSwitchModuleViaBreadcrumbsNavigation | → **C7** |

### 4. ACL Functionality (2.3.1) — 0%
**Legacy tests:** 10 | **New framework:** 0 tests

| Feature | Ticket | Legacy test | Covered by |
|---------|--------|-------------|------------|
| ACL: adding permissions to design repo | EPBDS-13876, IPBQA-32455 | ACLTest | → **C6** |
| ACL: permissions to deployConfig when user has no default access | EPBDS-13875, IPBQA-32465 | ACLTest | → **C6** |
| ACL: permissions to deployConfig repo | EPBDS-13874, IPBQA-32466 | ACLTest | → **C6** |
| ACL: permissions to users and different repos | EPBDS-13873, IPBQA-32470 | ACLTest | → **C6** |
| ACL: permissions to users and different productions | EPBDS-13872, IPBQA-32474 | ACLTest | → **C6** |
| ACL: add/remove permissions to module | EPBDS-13871, IPBQA-32492 | ACLTest | → **C6** |
| ACL: add/remove permissions in design repo (no default access) | EPBDS-13870, IPBQA-32493 | ACLTest | → **C6** |
| ACL: permissions in git non-flat design repo | EPBDS-13869, IPBQA-32517 | ACLTest | → **C6** |
| ACL: add/remove permissions to Deploy Configuration | EPBDS-13868, IPBQA-32530 | ACLTest | → **C6** |
| ACL: permissions to different Deploy Configurations | EPBDS-13867, IPBQA-32532 | ACLTest | → **C6** |

---

## 🟡 PARTIALLY COVERED (20–50%)

### 5. Editor – Advanced Features (2.1.x) — ~20%
**Legacy tests:** 65+ | **New framework:** ~10 (rules_editor) + partial studio_issues

| Feature | Ticket | Legacy test | Covered by |
|---------|--------|-------------|------------|
| Filtering projects (incl. advanced filter) | 2.1.1 | Test109 | → **C1** (partial via repo filter) |
| Comparing & Reverting Module Changes | 2.1.11, EPBDS-8867 | Test027, Test028 | → **C8** |
| Compare Excel files | EPBDS-10472, IPBQA-30875 | TestCompareExcelFiles | → **C8** |
| Display Changed Rows Only in Compare | EPBDS-12481, IPBQA-32105 | TestDisplayChangedRows | → **C8** |
| Identical files info message | EPBDS-10162 | — | → **C8** |
| Deploy button in Editor | EPBDS-9507, IPBQA-29618 | TestDeployButton | → **C9** |
| Revision page in Editor (project history) | EPBDS-9997, IPBQA-30123 | TestTabRevisionsOnEditorTab | → **C9** |
| Local Changes page: Restore, Compare | EPBDS-10539, IPBQA-30730 | TestChangesRestoreCompareHistorySettings | → **C9** |
| Table ordering mode (default setting) | EPBDS-13851, IPBQA-32512 | TestOrderingMode | → **C11** |
| Search on Project level screen | EPBDS-14181, IPBQA-32590 | TestSearchOnProjectLevel | → **C11** |
| Versioning by folders | EPBDS-10363, IPBQA-30979 | TestVersioningByFolders | → **C13** |
| Copying a Module | 2.1.12 | Test129 | standalone test |
| Managing Range data types | EPBDS-7489, IPBQA-25791 | TestRangeDataTypes | standalone test |
| Create table by copying existing | IPBQA-31552 | TestCopyTableAsNewTable | standalone test |
| Create table as new version | IPBQA-31552 | TestCopyTableAsNewVersion | standalone test |
| Create table as Business Dimension version | IPBQA-31601, EPBDS-11436 | TestCopyTableAsNewBusinessDimension | standalone test |
| Creating a Test table via wizard | 2.1.25 | CreateTestMethod | standalone test |
| Creating a Test table with ID column | RulesEditor.Test100 | — | standalone test |
| Editing Comma-Separated Array of values (DDL) | EPBDS-7508, IPBQA-25824 | TestEditingCommaSeparatedArrayValues | standalone test |
| Tracing rules | test113, test115 | TracingRunTables/* | standalone test |
| Trace in file | EPBDS-7715, IPBQA-25978 | TestTraceInFileFunctionality | standalone test |
| Benchmark Tools | test037 | — | standalone test |
| Edit table: Undo/Redo | test001, test002 | — | standalone test |
| Edit table: Insert/Delete row, column | 2.1.49, 2.1.50 | MainActionsInsertRemoveRow | standalone test |
| Edit table: Bold/Italic/Underline / fill color | 2.1.52, 2.1.53 | MainActionsFont | standalone test |
| History – Recently visited table | 2.1.58 | test038 | standalone test |
| SmartLookup / SmartRules tables Open/Edit/Save | EPBDS-9293, IPBQA-29358 | TestSmartLookupSmartRules | standalone test |
| Simple/SmartRules tables Create via Wizard | EPBDS-9818, IPBQA-29967 | TestSimpleLookupSimpleRules | standalone test |
| TBasic tables Open/work/edit | Test013 | — | standalone test |
| Run tables Open/work/edit | IPBQA-29970 | TestRunTable | standalone test |
| Collapsing Error Message in Editor | EPBDS-11587, IPBQA-25869 | — | standalone test |
| Navigation to table | EPBDS-7537, IPBQA-25912 | TestNavigationToTable | standalone test |
| Explanation feature | EPBDS-8876, IPBQA-28386 | — | standalone test |
| Refresh button | EPBDS-8869, IPBQA-28382 | TestRefreshButton | standalone test |
| Rename project in Editor (non-flat git) | EPBDS-10845, IPBQA-30937 | TestRenameProjectInEditor | standalone test |
| Run/Trace buttons always visible | EPBDS-11722, IPBQA-31761 | TestRunTraceButtonsVisibleForAllTypeTables | standalone test |

### 6. Git (2.5) — ~44%
**Legacy tests:** 25+ | **New framework:** 11 tests

| Feature | Ticket | Legacy test | Covered by |
|---------|--------|-------------|------------|
| Customized Comment fields | EPBDS-8783, IPBQA-27845 | TestGitCustomizeCommentFields | → **C14** |
| Committer's name verification | EPBDS-8631, IPBQA-27640 | TestGitCommitterName | → **C14** |
| Comments generation on project name entering | EPBDS-8547, IPBQA-27497 | TestGitCommentsGenerationOnProjectName | → **C14** |
| Configure Git repo using dev sources | EPBDS-8527, IPBQA-27456 | TestGitConfigureUseDevSources | standalone test |
| Changes committed outside Webstudio | EPBDS-8839, IPBQA-27179 | — | standalone test |
| Copy branch functionality | EPBDS-11160, IPBQA-27264 | — | standalone test |
| New Branch pattern | EPBDS-8855, IPBQA-27623 | — | standalone test |
| Branch links in Editor tab | EPBDS-8579, IPBQA-27551 | TestGitBranchLinksInEditorTab | standalone test |
| Delete Branch improvements | EPBDS-10849, IPBQA-31011 | TestDeleteBranch | standalone test |
| Check shortened revision numbers | EPBDS-9649, IPBQA-29690 | — | standalone test |
| Changes check interval | EPBDS-8806, IPBQA-28116 | — | standalone test |
| HTTP → HTTPS git repo URL change | EPBDS-11370, IPBQA-31528 | TestGitHttpToHttpsAndViceVersa | standalone test |
| Protected Branches | IPBQA-31896 | TestProtectedBranches | standalone test |

### 7. WebService (Section 1) — ~25%
**Legacy tests:** 10+ | **New framework:** 3 tests

| Feature | Ticket | Legacy test | Covered by |
|---------|--------|-------------|------------|
| Available services list | — | TestWebservicesDisplay | standalone test |
| Swagger UI / JSON / YAML | IPBQA-32142, EPBDS-12603 | TestSwaggerUI | standalone test |
| Verification of Project Status | EPBDS-9421, IPBQA-29488 | TestProjectStatus | standalone test |
| Webservices displayed on tab | EPBDS-8887, IPBQA-28417 | TestWebservicesDisplay | standalone test |
| New RuleServices UI | EPBDS-14196, IPBQA-32607 | — | standalone test |
| Write OpenL runtime errors in log | EPBDS-12576, IPBQA-32104 | — | standalone test |
| Alphabetical sorting of deployments | EPBDS-13121, IPBQA-32307 | — | standalone test |

### 8. Admin: System Settings – Repositories (2.3.1.8-12) — ~5%
**Legacy tests:** 10+ | **New framework:** 0 tests

| Feature | Ticket | Legacy test | Covered by |
|---------|--------|-------------|------------|
| Supported repositories availability | EPBDS-9227, IPBQA-29276 | TestSupportedRepositories | → **C12** |
| Multiple Design Repos (flat git + JDBC) | EPBDS-10968, IPBQA-30682 | TestMultipleDesignReposGitFlatNonFlatAndJDBC | → **C12** |
| Webstudio works if one design repo has wrong settings | EPBDS-11441, IPBQA-31634 | — | → **C12** |
| Restore default (Repositories) | EPBDS-9628, IPBQA-29638 | TestRestoreDefault | standalone test |
| Common: History max count & Clean history | EPBDS-10539, IPBQA-30730 | TestCommonSettingsHistory | standalone test |
| Common: Other settings (update table properties, date format) | IPBQA-30650 | TestCommonSettingsOther | standalone test |

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
| Admin: User management (basic) | ~60% | TestAdminUsers, TestAdminUsersProjects ✅ |
| User Settings / Profile | ~75% | TestAdminUserSettings ✅ |
| Tags (basic creation) | ~40% | TestProjectTags* ✅ (3 tests) |
| Rules Editor (core) | ~50% | 10 rules_editor tests ✅ |
| Git (core operations) | ~44% | 11 git tests ✅ |
| Studio Issues (bug regression) | ~40% | 21 studio_issues tests ✅ |

---

## Priority Migration Roadmap

| Priority | Composite Test | Atomic features consolidated | Effort |
|----------|----------------|------------------------------|--------|
| 🔴 1 | **C6** TestAclPermissions | 10 ACL scenarios → 1 test | High |
| 🔴 2 | **C7** TestProjectCompilationAndModuleMode | 7 Single/Multi Mode features → 1 test | Medium |
| 🔴 3 | **C3** TestOpenApiImportAndReconciliation | 4 OpenAPI features → 1 test | Medium |
| 🔴 4 | **C5** TestCreateProjectFromOpenApi | 2 OpenAPI features → 1 test | Low |
| 🔴 5 | **C4** TestOpenApiReconciliationEdgeCases | 5 OpenAPI validation features → 1 test | Medium |
| 🔴 6 | **C1** TestRepositoryBrowsingFilterStatus | 7 repo browsing/filter features → 1 test | High |
| 🔴 7 | **C2** TestRepositoryExportAndRevisions | 4 export/revision features → 1 test | Medium |
| 🟡 8 | **C8** TestCompareExcelFilesAndChanges | 4 compare features → 1 test | Medium |
| 🟡 9 | **C9** TestEditorDeployAndRevisions | 3 editor features → 1 test | Medium |
| 🟡 10 | **C10** TestRepositoryTableActions | 3 table action features → 1 test | Medium |
| 🟡 11 | **C11** TestEditorOrderingAndSearch | 2 editor features → 1 test | Low |
| 🟡 12 | **C12** TestDesignRepositoryManagement | 4 repo management features → 1 test | High |
| 🟢 13 | **C13** TestVersioningByFolders | 3 versioning features → 1 test | Low |
| 🟢 14 | **C14** TestGitCommentAndCommitter | 3 git comment features → 1 test | Low |

---

## Notes
- **Deploy Configuration** — marked as legacy, not migrated
- **Installation Wizard** — marked as legacy, not migrated
- **Shared Data (2.6)** — 3 of 4 features marked as "Can't be automated", skipped
- **Docker (2.9)** — infrastructure concern, not UI test scope
- **AI Tools (4)** — not yet automatable
- **SSO/AD/OAuth** — require external system mocks, lowest migration priority
