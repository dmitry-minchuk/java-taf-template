# ACL Test Coverage Gaps

Source: ACL in OpenL BRD.txt (EPBDS-14295)

---

## What Changed: Old vs New ACL Model

The BRD introduces a **completely new permission model**. Old IPBQA tickets (32455, 32470, 32474, 32492, 32493, 32517, 32530, 32532) test the **OLD** system and are NOT relevant to this new ACL.

| Aspect | OLD system | NEW system (BRD) |
|---|---|---|
| Permissions | CREATE, ADD, EDIT, DELETE, ERASE, DEPLOY, RUN, BENCHMARK, VIEW | M (Manage), V (View), E (Edit), C (Create), D (Delete) |
| Roles | Groups: Developers, Testers, Deployers, Viewers | Business roles: Manager, Contributor, Viewer |
| Deploy | A permission that could be assigned | System action — available if user has ≥ Viewer on design AND Editor on deploy repo |
| Run/Benchmark | Assignable permission (Testers group) | System action — available for ALL users |
| Lock/Unlock | Existed | DEPRECATED |
| Erase | Separate permission | Merged into Delete |
| Add | Separate permission | Merged into Create |
| Role assignment | Via API JSON payloads | Via UI in Admin tab or group name templates |

---

## New Role Matrix (FR2)

| Role | M (Manage) | V (View) | C (Create) | E (Edit) | D (Delete) |
|---|---|---|---|---|---|
| Manager | x | x | x | x | x |
| Contributor | — | x | x | x | x |
| Viewer | — | x | — | — | — |

Key rule (BR2): **Principle of least privilege** — "resources and groups should not have priority."

---

## Currently Covered by Migrated Tests

| Test Class | BRD Requirement | What is tested |
|---|---|---|
| `TestACLUserManagementAndRepositoryRoles` | FR4, Use Case 3 | User CRUD, repository-level Manager + Viewer assignment, permission enforcement in Repository + Editor |
| `TestACLProjectLevelRoles` | FR4, Use Case 3 | Project-level Manager + Viewer assignment for multiple projects, permission enforcement |

---

## NOT Covered — MVP Scope

### 1. Contributor Role (FR2)
**Priority: CRITICAL — role exists in production but is not tested at all**
**Test class to create:** `TestACLContributorRole`

Scenarios:
- Assign **Contributor** on repository level → user can Edit, Create, Delete but CANNOT assign roles (no Manage permission)
- Assign **Contributor** on project level → same restrictions at project scope
- Verify Contributor CANNOT see "Manage ACL" / "Access Management" button (no M permission)
- Verify Contributor CAN edit tables in Editor (E permission)
- Verify Contributor CAN delete a project (D permission)
- Verify Contributor CANNOT see the option to add roles for other users
- Compare Contributor vs Viewer: Contributor sees Edit/Delete buttons, Viewer does not
- Compare Contributor vs Manager: Manager sees role management UI, Contributor does not

---

### 2. Manage Permission — Only Manager Can Assign Roles (FR1, FR2)
**Priority: HIGH**
**Test class to create:** `TestACLManagePermission`

The M (Manage) permission allows the user to assign roles to resources they manage. Only Manager has it.

Scenarios:
- Login as user with Manager role on a repository
- Navigate to Admin → Users → verify Manager CAN see and use the "Access Management" tab to assign roles
- Assign Contributor role on same repository to another user
- Login as user with Contributor role on same repository
- Verify Contributor CANNOT access the role assignment UI
- Verify Contributor CANNOT see any "Manage access" action on repository or project

---

### 3. Deploy — System Action, Not a Permission (TR2, BRD)
**Priority: HIGH**
**Test class to create:** `TestACLDeploySystemAction`

BRD states: Deploy must be available for users with **at least Viewer rights in the design repository AND editor rights in the deploy repository**.

Scenarios:
- User has Viewer on design repo only → verify Deploy button is NOT visible (no access to deploy repo)
- User has Viewer on design repo + Contributor (Edit) on deploy/production repo → verify Deploy button IS visible
- User has Manager on design repo but no access to deploy repo → verify Deploy button is NOT visible
- User has Contributor on design repo + Viewer on deploy repo → verify Deploy button is NOT visible (Viewer is not enough on deploy side)
- Admin (full access) → Deploy button should be visible
- Verify this behavior is consistent in both Repository tab and Editor tab

---

### 4. Run and Benchmark — Available for ALL Users (TR2)
**Priority: MEDIUM**
**Test class to create:** `TestACLRunBenchmarkSystemAction`

BRD states: Run and Benchmark are system actions, NOT permissions — must be available for all users.

Scenarios:
- Create user with Viewer role only (no other permissions)
- Open a project in Editor
- Open a rules table that has Run/Test button
- Verify Run (Test) button IS visible and functional for Viewer
- Verify Benchmark button IS visible and functional for Viewer
- These should NOT be controlled by role assignment

---

### 5. Lock/Unlock — Deprecated (TR2)
**Priority: MEDIUM**

BRD states: Lock/Unlock functionality must be deprecated from the system.

Scenarios:
- Verify there is no Lock Project / Unlock Project button visible in Repository tab for any user role
- Verify there is no Lock/Unlock option in any context menu or toolbar
- Note: Current tests call `repositoryPage.unlockAllProjects()` — this should be investigated whether it's still needed or is a leftover

---

### 6. ACL Management UI in Admin Tab (FR5, EPBDS-14584)
**Priority: HIGH**
**Test class to create:** `TestACLManagementAdminTab`

BRD requires a dedicated ACL management interface within the Admin tab (separate from the Users tab).

Scenarios:
- Navigate to Admin tab → find the ACL management section (separate from Users sub-tab)
- Verify repository list is displayed with current access entries
- Add a role entry for a user directly from ACL management page (not via Users > Edit)
- Verify the entry appears correctly
- Remove a role entry → verify user loses access on next login
- Verify the UI correctly shows: who has access, to which resource, with which role

---

### 7. Viewing Parsed ACL per User — EUMS Groups (FR5, EPBDS-14577)
**Priority: HIGH (MVP scope)**
**Test class to create:** `TestACLParsedGroupsUserView`

BRD requires a UI for viewing parsed and mapped ACLs of each user (this is the "ACL parsing on User view" in Admin tab).

Scenarios:
- Admin navigates to Admin → Users → select a user
- Verify the user detail view shows which groups were parsed and which roles were auto-assigned
- Visual feedback: successfully parsed group permissions must have **green highlight** (FR3.5)
- If group doesn't match any template → permission row should show different visual state (no green)
- Verify the distinction between: manually assigned roles vs auto-assigned from EUMS groups

---

### 8. Group Name Templates Configuration (FR3.1, FR3.2 — MVP with EUMS)
**Priority: HIGH for EUMS environments**
**Test class to create:** `TestACLGroupNameTemplates`

Note: Requires OpenL Studio configured with EUMS (e.g., Active Directory or LDAP).

Scenarios (FR3.1 — Template creation):
- Admin navigates to Admin → ACL / Group Templates section
- Create first template: `git_<repositoryName>_<role>`
- Verify template saved and displayed
- Create second template: `<repositoryName>_<role>`
- Verify both templates active simultaneously (multiple template support is MVP)
- Try invalid template format → verify error message

Scenarios (FR3.2 — Role mapping):
- Configure role label mapping: `ro` = Viewer, `rw` = Contributor, `mnt` = Manager
- Save configuration
- Verify mapping is persisted

Scenarios (FR3.3-FR3.4 — Auto-assignment on login):
- Add user to EUMS group `git_Design_rw`
- User logs in → verify Contributor role auto-assigned on Design repository
- Add user to EUMS group `git_Design_mnt` → verify Manager auto-assigned on next login
- Remove user from group → re-login → verify role revoked (changes in EUMS reflected on next login, FR3.4)

Alternate flows:
- User's group name doesn't match any template → login succeeds but user sees no resources + warning message (Use Case 1, Alternate Flow 1)

---

### 9. Changing Access Rights — Principle of Least Privilege (BR2, Use Case 4)
**Priority: MEDIUM**
**Test class to create:** `TestACLPrincipleOfLeastPrivilege`

BRD states: "resources and groups should not have priority." Uses least privilege principle.

Scenarios:
- User has Contributor on a project AND Viewer on the repository containing that project
- Verify that the effective permission is the **least** between conflicting entries: Viewer behavior applies for repository-level actions
- User has Manager on repository AND Viewer on a specific project within that repository
- Verify that project-level access is governed by project-level role (Viewer), not inherited Manager
- Admin removes user from project-level group but forgets to remove from repository-level group
- Verify user retains repository-level access (both entries remain active)

---

### 10. User With No Matching Group / No Access — Warning Message (Use Case 1)
**Priority: MEDIUM**
**Test class to create:** part of `TestACLUserWithNoAccess` (already partially covered in `TestACLUserManagementAndRepositoryRoles`)

Currently covered: user with no roles sees empty repository.
NOT covered: the **warning message** that BRD specifies should be shown.

Scenarios:
- Login as user with no roles assigned
- Verify warning message is displayed: "You have no rights to view any resources" (or similar text)
- Verify the warning is visible in both Repository and Editor tabs
- Assign a role → re-login → verify warning disappears

---

## Out of Scope (Non-MVP — do NOT create tests yet)

Per BRD "Non-MVP Scope":
1. **Granular access** at file/module/folder level via UI or templates
2. **Access based on tags or geographical divisions**
3. **Audit logging** (view/export ACL-related action logs)
4. **Direct access management from resource views** (e.g., clicking a project and managing ACL inline)
5. **Real-time EUMS synchronization** (changes applied without re-login)
6. **System Admin vs Business Admin role split** (EPBDS-14575)

---

## Notes on Old IPBQA Tickets

The following tickets test the **OLD permission model** and are not applicable to new ACL:
- IPBQA-32455, 32465, 32466, 32470, 32474, 32492, 32493, 32517, 32530, 32532 — all use old `CREATE/ADD/ERASE/DEPLOY/RUN/BENCHMARK` permission strings and old `Developers/Testers/Deployers` groups

IPBQA-32912 is the only ticket aligned with the new model (Manager/Contributor/Viewer roles).
