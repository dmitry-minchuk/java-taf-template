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

## 🔴 CRITICAL GAPS (0–10% coverage)

### 1. REPOSITORY (2.2) — ~3%
**Legacy tests:** 30+ | **New framework:** 1 test (TestNewDeployPopup, suite is empty)

| Feature | Ticket | Legacy test |
|---------|--------|-------------|
| Browsing Design/Deployment repo: project pictures by status | EPBDS-9847 | TestBrowsingFilterStatusInRepositories |
| Filter by name / Advanced filter | EPBDS-9847 | TestBrowsingFilterStatusInRepositories |
| Creating a project from template / zip | — | ProjectCreatingEditingErasing |
| Copying a project | — | Test060 |
| Removing a project (Delete + Erase) | — | ProjectCreatingEditingErasing |
| Creating a folder | — | Test048 |
| Uploading a file | — | test048AddFolder |
| Delete folder and file | — | Test050, Test055 |
| Copy a file | — | Test073–Test079 |
| Opening Project Revision (Revisions tab) | IPBQA-29644 | testProjectRestoreFromOldRevision |
| Opening Project Revision (Open Revision button) | — | Repository.Test056 |
| Comparing Project Revisions | EPBDS-8517, EPBDS-8536 | Test035 |
| Exporting a Project or File | EPBDS-10703, IPBQA-31329 | TestExportProject |
| Unlocking a Project | IPBQA-30550 | TestUnlockProjectDeployConf |
| Resolve Conflicts (by Sheets) | EPBDS-13488, IPBQA-32406 | TestResolveConflictFunctionality |
| Resolve Conflicts dialog improvements | EPBDS-9158, IPBQA-29110 | TestImproveResolveConflictDialog |
| Revisions on Resolve Conflicts screen | EPBDS-9717, IPBQA-29901 | — |
| Git LFS support | EPBDS-12651, IPBQA-32116 | — |
| Azure BLOB storage support | EPBDS-12457, IPBQA-32109 | TestAzureBlobRepoStudio |
| Show technical revisions on Revisions tab | EPBDS-13652, IPBQA-32483 | — |
| Project design revision tracking in deployment | EPBDS-9687, IPBQA-29847 | — |
| Apply settings from rules-deploy.xml on JSON deserialization | EPBDS-10737, IPBQA-30930 | TestRulesDeploySettingsOnDeserializationJson |
| Non-flat git folder structure: create/copy/delete/edit | EPBDS-10853, IPBQA-30903 | TestNonFlatRepoIssues |
| Table action buttons (open, close, deploy) | EPBDS-12712, IPBQA-32158 | TestTableActionButtons |
| Change validation pattern for Repository Name | EPBDS-11533, IPBQA-31641 | TestRepositoryNameValidation |

### 2. OpenAPI (2.8) — 0%
**Legacy tests:** 7 | **New framework:** 0 tests

| Feature | Ticket | Legacy test |
|---------|--------|-------------|
| Create Project from OpenAPI | EPBDS-10846, IPBQA-30678 | TestCreateProjectFromOpenApiFile |
| Import OpenAPI file for scaffolding in existing project | EPBDS-10812, IPBQA-31035 | TestOpenApiImport |
| Create Data tables from OpenAPI GET methods | EPBDS-10770, IPBQA-31073 | TestCreateDataTablesFromOpenApiGetMethod |
| OpenAPI reconciliation feature | IPBQA-30902 | OpenApiReconciliationFeature |
| OpenAPI reconciliation with multiple merged files | EPBDS-10620, IPBQA-30970 | TestOpenApiReconciliationWithMultipleMergedFiles |
| OpenAPI file operations | EPBDS-10543, IPBQA-30922 | — |
| Auto-add/update OpenAPI file in reconciliation mode | EPBDS-12260, IPBQA-32071 | TestOpenApiImportLocalChanges |

### 3. Single/Multi Mode (2.11) — 0%
**Legacy tests:** 6 | **New framework:** 0 tests

| Feature | Ticket | Legacy test |
|---------|--------|-------------|
| Project compilation – main scenarios | EPBDS-11873, IPBQA-31701 | TestProjectCompilation |
| Hide Progress Bar after compilation finished | EPBDS-11812, IPBQA-31733 | TestCompilationProgressBar |
| Run/Trace/Test in currently opened module only | EPBDS-11813, IPBQA-31729 | TestCompileThisModuleOnly |
| Single/Multi-module setting on module level | EPBDS-11799, IPBQA-31758 | — |
| Compile This Module Only redesign | EPBDS-11799, IPBQA-31895 | TestCompileThisModuleOnly |
| Errors and Run/Trace/Test buttons for duplicate tables | EPBDS-11791, IPBQA-31790 | TestWorkWithDuplicateTables |
| No errors on switching projects via breadcrumb | EPBDS-11827, IPBQA-31804 | TestSwitchModuleViaBreadcrumbsNavigation |

### 4. ACL Functionality (2.3.1) — 0%
**Legacy tests:** 10 | **New framework:** 0 tests

| Feature | Ticket | Legacy test |
|---------|--------|-------------|
| ACL: adding permissions to design repo | EPBDS-13876, IPBQA-32455 | ACLTest |
| ACL: permissions to deployConfig when user has no default access | EPBDS-13875, IPBQA-32465 | ACLTest |
| ACL: permissions to deployConfig repo | EPBDS-13874, IPBQA-32466 | ACLTest |
| ACL: permissions to users and different repos | EPBDS-13873, IPBQA-32470 | ACLTest |
| ACL: permissions to users and different productions | EPBDS-13872, IPBQA-32474 | ACLTest |
| ACL: add/remove permissions to module | EPBDS-13871, IPBQA-32492 | ACLTest |
| ACL: add/remove permissions in design repo (no default access) | EPBDS-13870, IPBQA-32493 | ACLTest |
| ACL: permissions in git non-flat design repo | EPBDS-13869, IPBQA-32517 | ACLTest |
| ACL: add/remove permissions to Deploy Configuration | EPBDS-13868, IPBQA-32530 | ACLTest |
| ACL: permissions to different Deploy Configurations | EPBDS-13867, IPBQA-32532 | ACLTest |

---

## 🟡 PARTIALLY COVERED (20–50%)

### 5. Editor – Advanced Features (2.1.x) — ~20%
**Legacy tests:** 65+ | **New framework:** ~10 (rules_editor) + partial studio_issues

| Feature | Ticket | Legacy test | New fw |
|---------|--------|-------------|--------|
| Filtering projects (incl. advanced filter) | 2.1.1 | Test109 | ❌ |
| Comparing & Reverting Module Changes | 2.1.11, EPBDS-8867 | Test027, Test028 | ❌ |
| Copying a Module | 2.1.12 | Test129 | ❌ |
| Table icons (full coverage) | EPBDS-7458, IPBQA-25719 | — | ⚠️ partial |
| Managing Range data types | EPBDS-7489, IPBQA-25791 | TestRangeDataTypes | ❌ |
| Create table by copying existing | IPBQA-31552 | TestCopyTableAsNewTable | ❌ |
| Create table as new version | IPBQA-31552 | TestCopyTableAsNewVersion | ❌ |
| Create table as Business Dimension version | IPBQA-31601, EPBDS-11436 | TestCopyTableAsNewBusinessDimension | ❌ |
| Creating a Test table via wizard | 2.1.25 | CreateTestMethod | ❌ |
| Creating a Test table with ID column | RulesEditor.Test100 | — | ❌ |
| Editing Comma-Separated Array of values (DDL) | EPBDS-7508, IPBQA-25824 | TestEditingCommaSeparatedArrayValues | ❌ |
| Tracing rules | test113, test115 | TracingRunTables tests | ❌ |
| Trace in file | EPBDS-7715, IPBQA-25978 | TestTraceInFileFunctionality | ❌ |
| Benchmark Tools | test037 | — | ❌ |
| Edit table: Undo/Redo | test001, test002 | — | ❌ |
| Edit table: Insert/Delete row, column | testMainActionsInsertRemoveRow | MainActionsInsertRemoveRow | ❌ |
| Edit table: Bold/Italic/Underline / fill color | test005 | MainActionsFont | ❌ |
| Compare Excel files | EPBDS-10472, IPBQA-30875 | TestCompareExcelFiles | ❌ |
| History – Recently visited table | test038 | — | ❌ |
| SmartLookup / SmartRules tables Open/Edit/Save | EPBDS-9293, IPBQA-29358 | TestSmartLookupSmartRules | ❌ |
| Simple/SmartRules tables Create via Wizard | EPBDS-9818, IPBQA-29967 | TestSimpleLookupSimpleRules | ❌ |
| TBasic tables Open/work/edit | Test013 | — | ❌ |
| Run tables Open/work/edit | IPBQA-29970 | TestRunTable | ❌ |
| Local Changes page: Restore, Compare | EPBDS-10539, IPBQA-30730 | TestChangesRestoreCompareHistorySettings | ❌ |
| Display Changed Rows Only in Compare | EPBDS-12481, IPBQA-32105 | TestDisplayChangedRows | ❌ |
| Collapsing Error Message in Editor | EPBDS-11587, IPBQA-25869 | — | ❌ |
| Table ordering mode (default setting) | EPBDS-13851, IPBQA-32512 | TestOrderingMode | ❌ |
| Navigation to table | EPBDS-7537, IPBQA-25912 | TestNavigationToTable | ❌ |
| Explanation feature | EPBDS-8876, IPBQA-28386 | — | ❌ |
| Refresh button | EPBDS-8869, IPBQA-28382 | TestRefreshButton | ❌ |
| Revision page in Editor (project history) | EPBDS-9997, IPBQA-30123 | TestTabRevisionsOnEditorTab | ❌ |
| Rename project in Editor (non-flat git) | EPBDS-10845, IPBQA-30937 | TestRenameProjectInEditor | ❌ |
| Deploy button in Editor | EPBDS-9507, IPBQA-29618 | TestDeployButton | ❌ |
| Search on Project level screen | EPBDS-14181, IPBQA-32590 | TestSearchOnProjectLevel | ❌ |
| Run/Trace buttons always visible | EPBDS-11722, IPBQA-31761 | TestRunTraceButtonsVisibleForAllTypeTables | ❌ |

### 6. Git (2.5) — ~44%
**Legacy tests:** 25+ | **New framework:** 11 tests

| Feature | Ticket | Legacy test | New fw |
|---------|--------|-------------|--------|
| Configure Git repo using dev sources | EPBDS-8527, IPBQA-27456 | TestGitConfigureUseDevSources | ❌ |
| Changes committed outside Webstudio | EPBDS-8839, IPBQA-27179 | — | ❌ |
| Committer's name verification | EPBDS-8631, IPBQA-27640 | TestGitCommitterName | ❌ |
| Customized Comment fields | EPBDS-8783, IPBQA-27845 | TestGitCustomizeCommentFields | ❌ |
| Copy branch functionality | EPBDS-11160, IPBQA-27264 | — | ❌ |
| Verify comments generation on project name entering | EPBDS-8547, IPBQA-27497 | TestGitCommentsGenerationOnProjectName | ❌ |
| New Branch pattern | EPBDS-8855, IPBQA-27623 | — | ❌ |
| Branch links in Editor tab | EPBDS-8579, IPBQA-27551 | TestGitBranchLinksInEditorTab | ❌ |
| Delete Branch improvements | EPBDS-10849, IPBQA-31011 | TestDeleteBranch | ❌ |
| Check shortened revision numbers | EPBDS-9649, IPBQA-29690 | — | ❌ |
| Changes check interval | EPBDS-8806, IPBQA-28116 | — | ❌ |
| Non-flat folder structure (order of properties) | EPBDS-9213, IPBQA-29270 | — | ❌ |
| HTTP → HTTPS git repo URL change | EPBDS-11370, IPBQA-31528 | TestGitHttpToHttpsAndViceVersa | ❌ |
| Protected Branches | IPBQA-31896 | TestProtectedBranches | ❌ |

### 7. WebService (Section 1) — ~25%
**Legacy tests:** 10+ | **New framework:** 3 tests

| Feature | Ticket | Legacy test | New fw |
|---------|--------|-------------|--------|
| Available services list | — | TestWebservicesDisplay | ❌ |
| WSDL | — | — | ❌ |
| WADL | — | — | ❌ |
| Swagger UI | IPBQA-32142, EPBDS-12603 | TestSwaggerUI | ❌ |
| Swagger JSON / YAML | — | TestSwaggerUI | ❌ |
| New RuleServices UI | EPBDS-14196, IPBQA-32607 | — | ❌ |
| Write OpenL runtime errors in log | EPBDS-12576, IPBQA-32104 | — | ❌ |
| Alphabetical sorting of deployments | EPBDS-13121, IPBQA-32307 | — | ❌ |
| Verification of Project Status | EPBDS-9421, IPBQA-29488 | TestProjectStatus | ❌ |
| Webservices displayed on tab | EPBDS-8887, IPBQA-28417 | TestWebservicesDisplay | ❌ |

### 8. Admin: System Settings – Repositories (2.3.1.8-12) — ~5%
**Legacy tests:** 10+ | **New framework:** 0 tests

| Feature | Ticket | Legacy test | New fw |
|---------|--------|-------------|--------|
| Supported repositories availability | EPBDS-9227, IPBQA-29276 | TestSupportedRepositories | ❌ |
| Multiple Design Repos (flat git + JDBC) | EPBDS-10968, IPBQA-30682 | TestMultipleDesignReposGitFlatNonFlatAndJDBC | ❌ |
| Multiple Design Repos (git flat+non-flat+JDBC) | EPBDS-10945, IPBQA-30859 | TestMultipleDesignReposGitFlatNonFlatAndJDBC | ❌ |
| Webstudio works if one design repo has wrong settings | EPBDS-11441, IPBQA-31634 | — | ❌ |
| Restore default (Repositories) | EPBDS-9628, IPBQA-29638 | TestRestoreDefault | ❌ |
| Common: History max count & Clean history | EPBDS-10539, IPBQA-30730 | TestCommonSettingsHistory | ❌ |
| Common: Other settings (update table properties, date format) | IPBQA-30650 | TestCommonSettingsOther | ❌ |

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

| Priority | Section | Effort | Value |
|----------|---------|--------|-------|
| 🔴 1 | OpenAPI (2.8) | Medium | High — 0% coverage, 7 features |
| 🔴 2 | Single/Multi Mode (2.11) | Low | High — 0% coverage, core functionality |
| 🔴 3 | ACL Functionality | High | High — security/permissions |
| 🔴 4 | Repository operations (2.2) | High | Critical — ~3% coverage |
| 🟡 5 | Editor advanced features | High | Medium — large surface area |
| 🟡 6 | Git remaining scenarios | Medium | Medium — 44% → 80% |
| 🟡 7 | WebService additional | Low | Medium — display/swagger |
| 🟡 8 | Admin repos / Common settings | Medium | Medium |
| 🟢 9 | SSO / AD / OAuth | Very High | Low (requires external mocks) |

---

## Notes
- **Deploy Configuration** — marked as legacy, not migrated
- **Installation Wizard** — marked as legacy, not migrated
- **Shared Data (2.6)** — 3 of 4 features marked as "Can't be automated", skipped
- **Docker (2.9)** — infrastructure concern, not UI test scope
- **AI Tools (4)** — not yet automatable
