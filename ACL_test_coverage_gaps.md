# ACL Test Coverage Gaps

Based on: [ACL in OpenL BRD.txt](EPBDS-14295) and existing IPBQA Jira tickets.

## Currently Covered

| Test Class | Jira | What is covered |
|---|---|---|
| `TestACLUserManagementAndRepositoryRoles` | IPBQA-32912 | User CRUD + repository-level Manager/Viewer roles |
| `TestACLProjectLevelRoles` | IPBQA-32912 | Project-level Manager/Viewer roles for multiple projects |

---

## NOT Covered — UI Scenarios

### 1. Contributor Role (all levels)
**Jira:** IPBQA-32912 (missing from E2E test), BRD FR2
**Priority:** HIGH
**Test class to create:** `TestACLContributorRole`

Scenarios:
- Assign Contributor role on repository level → verify user can create/edit/delete projects but cannot manage ACL
- Assign Contributor role on project level → verify user can edit/delete content within project but cannot see Manage ACL button
- Verify Contributor cannot deploy (Deploy is a system action, not a permission per BRD TR2)
- Verify role hierarchy: Manager > Contributor > Viewer (same resource, most restrictive wins per BRD BR2)

---

### 2. User Without Permissions By Default — Design Repository
**Jira:** IPBQA-32493
**Priority:** HIGH
**Test class to create:** `TestACLDesignRepoUserWithoutDefaultPermissions`

Scenarios:
- Create user with no groups/roles assigned
- Verify user sees no projects in Repository tab
- Via API: grant VIEW permission for specific project to this user
- Verify user now sees that project with read-only buttons (Open, OpenRevision, Compare, Export only)
- Verify Deployments button is NOT visible for VIEW-only user
- Verify no edit actions available on file level
- Via API: remove VIEW permission
- Verify user loses access immediately on next page load

---

### 3. Module-Level ACL (granular file/module permissions)
**Jira:** IPBQA-32492
**Priority:** HIGH
**Test class to create:** `TestACLModuleLevelPermissions`

Scenarios:
- Assign VIEW on project + EDIT on specific module file (.xlsx) to a user
- Login as that user, open module in Editor
- Verify Edit button IS visible for the permitted module
- Verify Edit button is NOT visible for other modules (only VIEW)
- Edit a table in the permitted module, save → verify changes saved
- Verify user cannot copy the module to a different path (Create permission absent)
- Via API: verify ACL list shows both project VIEW and module EDIT entries

---

### 4. ACL for Non-Flat Git Repository
**Jira:** IPBQA-32517
**Priority:** MEDIUM
**Test class to create:** `TestACLNonFlatGitRepository`

Preconditions: Studio configured with Git design repo, Flat folder structure UNCHECKED.

Scenarios:
- Create projects in a subfolder path (e.g., /folder1)
- Assign VIEW permission to user for the design repo
- Verify user sees projects in nested folder structure
- Verify Elements tab Actions column is empty for VIEW-only user (no edit icons)
- Verify Rules deploy configuration tab shows "absent" message for Viewer
- Open project in Editor, verify toolbar panel is empty (no edit actions)

---

### 5. ACL for Deployment Configurations — User Without Permissions
**Jira:** IPBQA-32530
**Priority:** MEDIUM
**Test class to create:** `TestACLDeployConfigUserWithoutPermissions`

Preconditions: Deploy Configuration repo = Database (JDBC).

Scenarios:
- Create user without any privileges
- Via API: grant VIEW on design repo + VIEW on specific DeployConfig
- Login as user, navigate to Repository
- Verify user sees projects list (VIEW on design)
- Verify Deployments button is NOT visible on top navigation
- Open Deploy Configurations tab → verify DeployConfig IS visible
- Verify Actions column is absent (no edit icons) for deploy configuration
- Via API: grant DEPLOY permission on specific DeployConfig
- Re-login → verify Deploy button appears for that DeployConfig

---

### 6. ACL for Multiple Deployment Configurations
**Jira:** IPBQA-32532
**Priority:** MEDIUM
**Test class to create:** `TestACLMultipleDeployConfigurations`

Scenarios:
- Create two deploy configurations: DeployConfig, DeployConfig2
- Assign different permissions to different users via API:
  - user: full access to DeployConfig only
  - user1: DEPLOY + ERASE on DeployConfig2 only
- Login as user → verify only DeployConfig is editable, DeployConfig2 is read-only or hidden
- Login as user1 → verify only DeployConfig2 is accessible with Deploy button
- Verify Viewer group permissions are applied by default for both configs

---

### 7. ACL for Multiple Design Repositories
**Jira:** IPBQA-32470
**Priority:** MEDIUM
**Test class to create:** `TestACLMultipleDesignRepositories`

Preconditions: Two design repos configured: Design (Git) and Design1 (JDBC).

Scenarios:
- Create projects in different repos: Bank Rating in Design, Corporate Rating in Design1
- Create user with Viewers group (VIEW on all repos)
- Verify user sees projects from both repos with correct read-only buttons
- Via API: grant EDIT on Design1 only
- Re-login → verify user can edit projects in Design1 but not in Design
- Via API: remove all permissions from Design
- Re-login → verify Design projects disappear from user's view

---

### 8. ACL for Multiple Production/Deployment Repositories
**Jira:** IPBQA-32474
**Priority:** MEDIUM
**Test class to create:** `TestACLMultipleProductionRepositories`

Preconditions: Two deployment repos configured: Deployment (JDBC), Deployment1 (local).

Scenarios:
- Assign Testers group to a user (RUN + BENCHMARK only, no deploy)
- Verify user sees no Create Deploy Configuration button
- Verify Deploy button is absent for all projects
- Via API: grant DEPLOY on specific production (Deployment only)
- Re-login → verify Deploy button appears, user can select Deployment but not Deployment1
- Verify user cannot create new deploy configurations

---

### 9. ACL UI Management — Admin Tab (ACL Management Page)
**Jira:** EPBDS-14584, EPBDS-14587
**Priority:** HIGH
**Test class to create:** `TestACLManagementUI`

Scenarios (directly managing ACL from Admin tab, not from User edit form):
- Navigate to Admin → ACL (or Access Management) section
- Verify repository list is displayed with current ACL entries
- Add/remove role for user directly from ACL management page
- Verify visual indication (green highlight) for successfully parsed group permissions per BRD FR3.5
- Verify ACL changes take effect on next user login

---

### 10. ACL Group Name Templates (with EUMS / Active Directory)
**Jira:** EPBDS-14576, FR3.1-FR3.4 in BRD
**Priority:** LOW (requires external EUMS integration)
**Test class to create:** `TestACLGroupNameTemplates`

Scenarios:
- Configure group name template: `git_<repositoryName>_<role>`
- Configure role mapping: ro=Viewer, rw=Contributor, mnt=Manager
- Simulate user login with group `git_Design_rw` → verify Contributor role auto-assigned on Design repo
- Simulate user login with group `git_Design_mnt` → verify Manager role auto-assigned
- Configure second template: `<repositoryName>_<role>`
- Verify both templates active simultaneously
- Test invalid template format → verify error message shown
- User with non-matching group → verify login succeeds but user sees no resources + warning message

---

### 11. User Without Any Access Logs Into Studio
**Jira:** EPBDS-14295 Use Case 1 (Alternate Flow 1)
**Priority:** LOW
**Test class to create:** `TestACLUserWithNoAccessWarning`

Scenarios:
- Create user with no roles and no group memberships
- Login as that user
- Verify warning message: "You have no rights to view any resources" (or equivalent)
- Verify Repository tab shows empty state, no projects visible
- Verify no Create Project option available

---

### 12. E2E ACL Flow — Complete IPBQA-32912
**Jira:** IPBQA-32912 (status: Open — not yet automated)
**Priority:** HIGH
**Note:** Current `TestACLUserManagementAndRepositoryRoles` covers steps 1-15 of this ticket. Remaining gap: Contributor role steps are not included in the original ticket but should be added per BRD.

---

## API-Only Tickets (already Passed, no UI automation needed)

| Jira | Summary | Status |
|---|---|---|
| IPBQA-32455 | ACL: adding permissions to design repo | Passed |
| IPBQA-32465 | ACL: adding permissions to deployConfig when user has no permissions | Passed |
| IPBQA-32466 | ACL: adding permissions to deployConfig repo - API, UI | Passed |
| IPBQA-32470 | ACL: adding permissions to different repos | Passed |
| IPBQA-32474 | ACL: adding permissions to different productions | Passed |
| IPBQA-32492 | ACL: module-level permissions | Passed |
| IPBQA-32493 | ACL: design repo user without default permissions | Passed |
| IPBQA-32517 | ACL: non-flat git repo permissions | Passed |
| IPBQA-32530 | ACL: deployConfig user without default permissions | Passed |
| IPBQA-32532 | ACL: multiple deployment configurations | Passed |
| IPBQA-32723 | API: /acls/repositories/roots/{id} | Passed |
| IPBQA-32728 | API: /acls/roles | Passed |
| IPBQA-32729 | API: /acls/repositories | Passed |
| IPBQA-32734 | API: /acls/projects | Passed |

These are already covered at API level. UI coverage (scenarios 2-8 above) would complement them.

---

## Out of Scope (Non-MVP per BRD)

- Audit logging (view/export ACL logs)
- Granular access control at folder/file level via UI (project-level is MVP, file-level via API exists)
- Real-time EUMS synchronization
- System Administrator vs Business Administrator role split (EPBDS-14575)
- Access control based on geographical divisions or tags
