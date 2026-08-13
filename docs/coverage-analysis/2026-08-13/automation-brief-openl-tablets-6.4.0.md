# Automation brief: openl-tablets (release OpenL 6.4.0, window 6.3.0..origin/main, 2026-06-30..2026-08-13)

You are working in: /Users/dmitryminchuk/Projects/java-taf-template   ← run all commands from here
System under test: /Users/dmitryminchuk/Projects/eis/openl-tablets (branch origin/main, tag 6.3.0 = base of the release window, 509 commits, 91 distinct EPBDS keys).
Dependency note: the WebStudio container is also the system under test for openl-mcp (webstudio-mcp-tests) — REST changes listed here can break MCP tests with no openl-mcp commit.
JIRA project: EPBDS. Release fixVersion: "OpenL 6.4.0" (102 dev tickets).

Build/tooling notes for THIS repo:
- TestNG + Playwright; suites live in `src/test/resources/testng_suites/`; register every new class in an existing suite (`studio_smoke.xml`, `studio_git.xml`, `studio_rules_editor.xml`, `studio_issues.xml`, `studio_acl.xml`, `studio_sso.xml`, `studio_open_api.xml`, `service_smoke.xml`).
- Run a single test: `mvn test -Dtest=ClassName` (headless). Full suites run only on Jenkins — never locally.
- Docker image under test is pinned in `src/test/resources/config.properties` (`webstudio:6.4.0-*`). Verify the actual Studio build via `build.json`, not the image tag.
- No ad-hoc sleeps; use the framework waits. React re-render churn handling already exists in `configuration/core/ui/WebElement.java` (EPBDS-16241/16261/16275 rationale).
- Auxiliary containers (mail mock, Keycloak, Samba AD, DB) are managed through startX/stopX configuration methods, never by overriding lifecycle methods.
- English only inside the repository. No code comments.
- Anti-masking rule: assert the EXPECTED behavior from the ticket/spec. If actual ≠ expected, file a bug (epbds-bug-reporter) and keep the test as a known-failing regression linked to it — never weaken the assertion.

Structural facts that shape everything below:
- The React SPA is `STUDIO/studio-ui/` (routes in `src/routes/index.tsx`); the REST/WebSocket backend for it is the new `org/openl/studio/` package.
- The legacy JSF Repository tab was DELETED (`a7a5715b65` EPBDS-9535). Legacy JSF repository page objects in this TAF (`ElementsTabComponent`, `RepositoryContent*`, `LeftRepositoryTreeComponent`, `ConfirmEraseDialogComponent`, `ConfirmUndeleteDialogComponent`, `RepositoryPage.unlockAllProjects`, `RepositoryPage.createProjectFromWorkSpace`) are dead code — do not build new tests on them.
- `COVERAGE_GAP_ANALYSIS.md` in the repo root is stale relative to this brief.

## Work items (priority order)

### 1. WebSocket live UI updates — EPBDS-16314 (+16384, 16386, 16389, 16392; all Resolved/Closed)
- What changed (functional view): open screens now refresh themselves when a project changes anywhere — REST API, legacy Editor, Excel write, or an external `git push` picked up by ~10 s polling. STOMP over `/ws`; per-user topics `/user/topic/workspace/changed`, `/user/topic/projects/{id}/changed`, `/user/topic/projects/changed`, `/user/topic/workspace/projects/status`. Each browser tab sends `X-OpenL-Client-Id` on mutating requests so it can skip its own echo. Debouncer + load-generation ordering decide whether a socket ping or the user's own read wins.
- Dev commits: `8a909ee0be`, `eb962be99e` (16314), `9412bb91e5`, `1f96c7496d` (16386), `5368023e2e` (16384), `d181b23d73` (16392), `752a58ef08` (16389). Backend `org/openl/studio/projects/messaging/`, client `studio-ui/src/services/{websocket,stompTopic,changePing,clientId}.ts`. Spec: `Docs/architecture/websocket-change-notifications.md`.
- JIRA: dev EPBDS-16314; linked bugs 16384/16386/16389/16392 are the defect history of this exact mechanism. QA ticket: none — create one.
- Current coverage: **not covered.** TAF asserts only manual-refresh behavior (`TestPermanentDeleteAfterRefreshUi`, `TestRefreshButton`). ITEST in the dev repo covers the socket protocol (`WebSocket{Auth,ChangeOrigin,ProjectStatus}Test`), but nothing covers the UI reacting to pushes.
- Why this is #1: longest dependency chain in the release (file watcher → origin resolver → debouncer → STOMP broker → shared client subscription → Zustand store → screen), race-condition territory by design, and four bugs already escaped here during the release.
- What to do:
  - Infrastructure: add a two-live-sessions capability (two Playwright browser contexts against the same container) — current two-user tests are strictly sequential (logout/login).
  - Primary: user B saves a table / uploads a file in project P; user A's open Projects list and open project page show the change WITHOUT any refresh action (bounded wait, no reload calls).
  - Own-echo suppression (16386): user A performs the action; assert A's screen does not visibly double-reload (no second loading overlay / no state flicker; at minimum, action completes and screen is stable).
  - Overlay race (16384): trigger a change ping during a reload (B pushes a change while A's list is loading); assert the loading overlay always disappears and the list is interactive.
  - Action feedback (16389): on a slow action (deploy/open of a big project) the initiating button shows progress and blocks a second submission until the read behind the action is on screen.
  - Open/Close status ordering (16392): user clicks Open then immediately reads status — button unlocks only after the page shows the new status; repeat for Close.
  - External-origin change: commit to the design repo bypassing Studio (direct git push to the fixture repo container); assert the Projects screen reflects it within the polling window (~10 s + margin).
  - Persistence: after each push-driven update, reload the page and assert the same state (push and read paths agree).
- Definition of done: new classes green headless locally via `mvn test -Dtest=...`; registered in `studio_smoke.xml` (or a new `studio_live_updates.xml` if session infra demands isolation); defects found are filed and kept as known-failing regressions; QA ticket created, linked "Cover" → EPBDS-16314, updated with commit references.

### 2. Interactive trace debugger and business trace — EPBDS-16195 + EPBDS-16292 (57 commits, largest cluster; Closed)
- What changed (functional view): Trace was rebuilt from a static tree into an interactive step debugger (breakpoints, watches, call stack, hotspots/profiling, spreadsheet-cell highlighting) and then split into two modes: a default business view and the full debugger behind an "Advanced" switch chosen at launch (`TraceExecutionModal`). Decision tables are broken into conditions in the tree; the fired rule and its result are highlighted; rule errors surface in the business tree; the business-mode tree downloads in one capped request.
- Dev commits: `a439261705` (16195, 353 files), `83e9b1193c`, `5eabec6303`, `2c7cda18c1`, `1bb87db108`, `cacb6b760d`, `9cc0358d59` (16292), `30d74d199b` (16291), `35cfe238c6` (16406), `71e2caea5c` (16407), `cd4290d613` (16344). REST base `/projects/{projectId}/trace` (`ProjectsTraceDebugController`). Docs: `Docs/api/projects-trace-api.md`, `Docs/user-guides/openl-studio/editing-testing.md` (8 new trace-*.png = the UI spec).
- JIRA: dev EPBDS-16195/16292; bug tail 16291, 16344, 16406, 16407 (all Resolved). QA ticket: none — create one. TAF page objects were already reworked once under closed EPBDS-16243.
- Current coverage: **partially covered.** Trace open/steps/details: `TestAllStepsDisplayedInTrace`, `TestTraceSpecialCharsDisplay`, `TestTraceSpreadsheetNodeDetails`, `TestTraceIntoFileJsonRequest`, `TestArrayOfAliasValuesInRunTrace`. `TraceWindowComponent.stepOver()`/`.resume()` exist but have ZERO callers; the word "breakpoint" appears nowhere in the TAF.
- What to do:
  - Mode selection: launch trace → business view by default; Advanced switch reveals the debugger; the chosen mode persists for the session (re-launch honors the last choice per spec).
  - Breakpoints (core gap): set a breakpoint on a table → run → execution suspends at it → call stack and variables are populated → step over → resume to completion. Negative: breakpoint on a never-executed table never suspends.
  - Instance-suffix breakpoint (16406 guard): breakpoint on a name with an instance suffix suspends instead of running to the end.
  - Watches: add a watch expression, assert its value at a suspension point; invalid expression shows the specific error, not a crash.
  - Business tree fidelity (16292): decision table broken into condition rows; the fired rule row and result highlighted; a failing run shows the error on the failing step in the business tree (16344 guard: error path on failed runs).
  - Result naming (16407 guard): running a test table reports the executed table's name, not a virtual suite name.
  - Boundary (16291 guard): trace a run with a large tree (hundreds of nodes) — the tree renders, capped request succeeds, no unbounded lazy-page storm.
  - State: close the trace tab mid-suspension → session is terminated (useTerminateOnClose) — re-open works cleanly.
- Definition of done: green headless; registered in `studio_rules_editor.xml` (trace block) or `studio_issues.xml` for the bug guards; QA ticket created and linked to 16195/16292.

### 3. Create Table / Copy Table React modals — EPBDS-16313 (+6912; bug tail 16355–16359, 16417, 16418; OPEN defect EPBDS-16388)
- What changed (functional view): the multi-step JSF wizard was replaced by a single editable Ant Design modal with a live skeleton preview (typed + transposed creation); Copy moved server-side (copy by id, `POST /projects/{id}/tables/{tableId}/copy`); a new table version can replace the one it supersedes.
- Dev commits: `d523ba85ac` (172 files), `1004fe7971`, `07e2d39804`, `00a6697448` (16358), `470c7c816c` (16357), `e71bdf006e` (16359), `5acdedc69d` (16417), `9ac3873df3` (16355), `88f0f8ad5a` (16414). Spec: `Docs/architecture/table-creation.md` + ~25 `create-*` screenshots.
- JIRA: dev EPBDS-16313. **EPBDS-16388 is Open**: the React modal dropped wizard guarantees — no author stamp, unchecked version uniqueness, no guidance for versions/dimensions. **EPBDS-16412 (QA Task, Open, assignee dminchuk) directly orders automation of EPBDS-16354** (copy loses table style; no dedicated dev commit — likely absorbed by 16358's server-side copy; reconcile in JIRA before writing).
- Current coverage: **thin.** Only 3 entry points into `CreateTableDialogComponent` (Datatype ×2, Simple Rules ×1); Copy covered by one class (`TestCopyTableVersioningUi`, EPBDS-16357, 3 tests). Decision, Data, Test, Spreadsheet tables are NEVER created through the modal.
- What to do:
  - Per-type creation through the modal: Decision (rules), Data, Test, Spreadsheet — assert the created table compiles and appears in the module; include the transposed variant for at least one type.
  - 16359 guard: a rules condition column typed Integer/Double accepts a range `18-30` and writes a range, not `181`; boundary values (single number, open-ended range).
  - 16355 guard: table title of exactly 31, 32 and >31 symbols — sheet name clipped to Excel's 31-char limit, creation succeeds, no duplicate-sheet clash.
  - 16417 guard: Create enabled for a DT with result type other than String/Boolean; a one-argument Lookup can be created without a title row.
  - 16418 guard: Data table created via the modal has no extra "Name" column.
  - Copy table style (16354/16412 — the ordered task): copy a table that carries a non-default style; assert the copy preserves it (open both, compare rendering/format markers). If the defect still reproduces, keep as known-failing linked to 16354.
  - EPBDS-16388 guarding scenarios (known-failing until fixed, linked to the open bug): copy-as-new-version stamps the author; creating a duplicate version is rejected with a specific message.
  - Regression: after any modal creation, save the project and reopen — the table survives the round-trip (16357 guard: new version replaces the superseded one, module still compiles — extend the existing class).
- Definition of done: green headless (except deliberate known-failing 16388/16354 guards); registered in `studio_rules_editor.xml`; EPBDS-16412 closed with a comment referencing commits; a new QA ticket for the modal coverage linked to 16313.

### 4. rules.xml / rules-deploy.xml Migrate — EPBDS-16327 (+16363, 16364, 16365, 16408; 16275 covered)
- What changed (functional view): Studio offers an in-place Migrate that moves root-level workbooks under `rules/` and rewrites `rules.xml` (plus a rules-deploy.xml rewrite to the minimal modern form). The server REFUSES the rulesXml migrate when it would change which modules compile ("module widening", `newModules` non-empty). The offer appears only for genuinely legacy content, not for formatting differences (16408). Migration must not drop `.xls`/`.xlsm` modules (16364) or method filters (16365).
- Dev commits: `7f457ab443`, `1497fdc5b0`, `39dbcc0f6f` (16327), `b5d99a1d03` (16363/16364/16365), `8aa0c16ac3` (16408). REST: `GET /projects/{id}/migration`, `POST /projects/{id}/migrate?scope=...`. Backend `ProjectMigrationService`.
- JIRA: dev EPBDS-16327 (Resolved). QA ticket: none — create one.
- Current coverage: **minimal.** One test (`TestProjectOverviewEditKeepsModulesUi`, EPBDS-16327) drives Migrate for a template project and re-checks modules; per project memory this test needs rework (Edit on Overview is gated by the Migration feature). `TestMigratedMethodFilterReloadUi` covers only the 16275 reload loop.
- What to do:
  - Offer logic (16408 guard): a project whose rules.xml differs from canonical only in formatting → NO Migrate offer, Edit available; a genuinely legacy descriptor (root-level workbooks) → Migrate offered.
  - Happy path: migrate a legacy project → workbooks land under `rules/`, module list identical before/after, project compiles, a history revision records the migration.
  - Module-widening refusal (16363 guard): a project with an undeclared root workbook → migrate is refused with the specific message; rules.xml unchanged; project state intact.
  - Mixed formats (16364 guard): a legacy project with `.xls` + `.xlsm` + `.xlsx` modules → after migrate ALL modules are present and compile.
  - Method filters (16365 guard): a module with a method filter that cannot become a glob → the filter survives (or migrate refuses); the project must not expose methods it used to hide — assert the method set before/after.
  - State-based: migrate on a project with unsaved local changes; migrate, then revert the revision — project returns to legacy state and the offer reappears.
  - Rules-deploy scope: migrate rules-deploy.xml separately; deployed service still resolves (smoke via `service_smoke` flow).
- Definition of done: green headless; fixtures added as zip projects under test resources (legacy-descriptor variants); registered in `studio_smoke.xml`; the existing Overview test reworked to the gated-Edit reality; QA ticket created and linked.

### 5. Delete Branch UI + cross-branch safety — EPBDS-16378 (+16255, 16381; 16380 In Progress — re-check)
- What changed (functional view): branch deletion is offered only where it can succeed; the dialog warns when deleting a branch would take the last copy of a project with it; deletion is rejected while another user holds the project lock (16255); the dialog normalizes Base64 ids with slashes (16381). `DELETE /projects/{id}/branches/{*branch}` with `force`.
- Dev commits: `7085067830`, `dd9cbe327f` (16378), `900aaa8e54` (16255), `84e0686e73` (16381); cross-branch machinery `8fbfd3bf57`…`e7048a965c` (EPBDS-8537, 12 commits). Doc: `Docs/architecture/cross-branch-projects.md`.
- JIRA: dev EPBDS-16378/16255/16381 Resolved; **EPBDS-16380 In Progress, no commits — do not automate its final behavior yet, re-check when it closes.** QA ticket: none — create one.
- Current coverage: **gap is exactly the dialog.** Branch create/switch/merge/protected-branch flows are strongly covered (23 git classes), but no test asserts the Delete-Branch UI itself — existing tests delete branches only through private helpers as setup.
- What to do:
  - Primary: create branch B from main, delete B via the Projects-list branch action → confirmation dialog → branch gone from the switcher and from Git (verify via `ProjectBranchesMethod` REST oracle).
  - Offer logic (16378 guard): the delete action is absent/disabled for the default branch and for protected branches; deleting the only branch that holds a project shows the "takes the project with it" warning and requires the explicit acknowledgement; after confirming, the project disappears from the list (and reappears if the branch is restored server-side — optional).
  - Lock rejection (16255 guard): user B has project P open for editing on branch X; user A attempts to delete X → specific rejection message, branch survives, B's session unaffected.
  - Id encoding (16381 guard): run the primary flow on a project whose Base64 id contains `/` or `+` (pick repo+path that produces one — see item 6); the dialog's requests succeed.
  - Regression: after any branch deletion, the project's remaining branches and the Sync merge-target list are consistent (ties into covered 16410/16411 behavior).
- Definition of done: green headless; registered in `studio_git.xml`; QA ticket created and linked to 16378.

### 6. URL-safe Base64 ids + non-ASCII project names — EPBDS-16402, EPBDS-16403 (Resolved; filed by us 2026-08-07)
- What changed (functional view): project and deployment ids (Base64 of repo+path) are now URL-safe on both client and server (`services/projectId.ts`, `Base64ProjectResolveStrategy`); previously any id containing `+`/`/`/`=` broke every screen (HTTP 400) and redeploy (HTTP 404).
- Dev commits: `25c27c3023` (16402/16403), `84e0686e73` (16381).
- JIRA: dev EPBDS-16402/16403 Resolved. QA ticket: none — create one.
- Current coverage: **not covered.** No TAF test creates a project or file with a non-ASCII or special-character name; all fixture names are ASCII.
- What to do:
  - Craft names that force `+` and `/` into the standard Base64 encoding of repo+path (compute offline; e.g. names with specific character positions) and a Cyrillic/emoji name (`Проект-тест`), then run an end-to-end pass per name: create → open in Projects → Files tab → History tab → Editor round-trip (legacy bridge `projectIdBridge` re-encodes ids between JSF and React) → deploy → redeploy into the SAME deployment configuration (16403 guard) → delete.
  - Deep-link: paste the `/projects/{id}` URL directly into a fresh session — screen loads (no 400).
  - Boundary: name at the max allowed length; name with leading/trailing spaces rejected with the specific validation message.
- Definition of done: green headless; registered in `studio_smoke.xml`; QA ticket created, linked to 16402 and 16403.

### 7. Admin Users/Groups management — EPBDS-16214 (+15806 no-commit, 16216, 16213 partially covered) — direct QA orders EPBDS-16233, EPBDS-16234 (Open, dminchuk)
- What changed (functional view): admin Users/Groups tables gained search, column sorting, selectable page size, a persisted Last Login column with the online badge moved onto it, and group-membership display on the Groups tab. Online marker fixed for multi and AD modes by registering form-login sessions (16216).
- Dev commits: `e678142276` (search), `a00a2b1014` (sort), `7660b42179` (page size), `b047d59cea`, `3f4b1ae919`, `15492b84b8` (last login/badge/dates), `37add5b272` (group members), `bf35acf6cc` (16216), `3fa19b5232` (16213), `e40105570e` (15640).
- JIRA: **EPBDS-16233 (Open) orders UI coverage for search (15806); EPBDS-16234 (Open) orders UI coverage for the online marker (16216).** Both assigned to dminchuk.
- Current coverage: users CRUD/roles/profile covered (13 classes); **Groups tab UI has NO page object at all; search — zero; online marker — zero.**
- What to do:
  - Build `GroupsPageComponent` (and add the missing Groups entry to `AdminNavigationComponent`).
  - Search (16233): Users tab — match by username/display name/email, no-match shows empty state, clearing restores the full list; same on Groups tab; search + pagination interaction (result spanning pages).
  - Sorting/page size: sort by each sortable column both directions; switch page size and assert persistence within the session.
  - Online marker (16234): admin sees the badge on Last Login for a user with an active session in multi mode; badge disappears after that user logs out; repeat in AD mode (`studio_sso.xml`, PLAYWRIGHT_DOCKER-gated, Samba AD fixture via startX config methods).
  - Group members (16214): create group, add users via REST oracle (`GroupsMethod`), assert the member list renders on the Groups tab.
  - Profile completion (15640): a user missing required profile fields is prompted after authentication; submit and assert persistence; cancel path re-prompts on next login.
- Definition of done: green headless where the mode allows (AD cases run under PLAYWRIGHT_DOCKER only); registered in `studio_smoke.xml` / `studio_sso.xml`; EPBDS-16233 and EPBDS-16234 closed with commit references.

### 8. Deployments tab (React) — EPBDS-16307 slice
- What changed (functional view): a full React Deployments area exists (`/deployments`, `/deployments/:deploymentId` — `DeploymentsHome`, `DeploymentWorkspace`), at production parity with the removed legacy screens.
- Dev commits: inside `ba8563bb74` (16307, 656 files); backend `DeploymentsController`.
- JIRA: dev EPBDS-16307 Closed. QA ticket: none — create one.
- Current coverage: **not covered.** No page object for the Deployments browsing screens; deployment coverage exists only through the Deploy modal and the downstream `service_smoke` checks.
- What to do: build `DeploymentsHomePage`/`DeploymentWorkspacePage` objects; primary: after deploying a project, the deployment appears with correct name/status; open it and assert its content (projects included, version); redeploy updates the entry (cross-check item 6's 16403 guard); negative: the tab renders an empty state with no deploy repositories configured; regression: deployment visible after page reload and re-login.
- Definition of done: green headless; registered in `studio_smoke.xml`; QA ticket created and linked to 16307.

### 9. Datatype editor round-trip fidelity — EPBDS-16425, 16426, 16428 (+16418; all Resolved)
- What changed (functional view): the React datatype read/write path now carries the mandatory/description/example columns (previously only 3 of 6 columns survived), reads the transient `~` marker from the Name column regardless of column order, and recognizes a titled datatype's header row instead of swallowing it as a field (which also lost Type cell links).
- Dev commits: `1c6ad54d44`, `41dd52585a`, `ee31a3f2be`, `e0db8f94af`. Dev-side unit fixtures to mirror: `TitledDatatype.xlsx`, `TransientFields.xlsx`.
- JIRA: all Resolved. QA ticket: none — create one.
- Current coverage: datatype editing basics covered; **round-trip fidelity of titled/6-column/transient datatypes — not covered.**
- What to do: for each fixture shape (untitled, titled, 6-column with mandatory/description/example, transient field, reordered columns): open in the editor → edit one cell → save → reopen → assert ALL columns/markers/title/links intact (the strongest oracle is exporting the module and diffing, or re-reading every cell); 16428 guard: the first field row of a titled datatype is a field, the title is a title; Type cell links navigate.
- Definition of done: green headless; registered in `studio_rules_editor.xml`; QA ticket created and linked.

### 10. Files tab selection vs. tree reloads — EPBDS-16437 (Resolved; freshest fixes on main)
- What changed (functional view): the file selection kept in the URL is dropped when the reloaded tree no longer holds that file, and when another tab is opened — previously the pane showed "The resource is not found".
- Dev commits: `ec0156c44c`, `086db343f5` (both touch only `ProjectDetail.tsx`).
- JIRA: dev EPBDS-16437 Resolved. QA ticket: none — create one (small).
- Current coverage: Files tab CRUD covered; **this selection/URL state — not covered.**
- What to do: select a file (URL carries the selection) → delete that file (same session via Files toolbar, then variant: from a second session/REST) → tree reloads → no "resource is not found", selection cleared; select a file → switch to Overview/History and back → selection state is sane; deep-link a URL with a stale file selection → page loads without the error pane.
- Definition of done: green headless; registered in `studio_smoke.xml`; QA ticket created and linked.

### 11. In-editor text file editing — EPBDS-13084 (Closed)
- What changed (functional view): text files can be edited in place in the project workspace via a CodeMirror editor (`containers/projects/CodeEditor.tsx`).
- Dev commits: inside `a2f1c52cf2`.
- JIRA: dev EPBDS-13084 Closed. QA ticket: none — create one.
- Current coverage: **not covered** — no text/code editor page object exists; files can only be uploaded/replaced.
- What to do: build a `CodeEditorComponent`; primary: open a text file (e.g. rules.xml or a .txt), edit, save → content persists (verify via file download/REST read) and a revision is recorded with the correct author; negative: editing then navigating away prompts about unsaved changes (if spec'd — verify against the UI, else assert actual-and-file-a-bug); boundary: large file, file with non-ASCII content; regression: editing rules.xml through this editor must keep the project compilable and must respect the edit lock (EPBDS-16256 landed: files API takes the edit lock).
- Definition of done: green headless; registered in `studio_smoke.xml`; QA ticket created and linked.

### 12. Direct QA orders — small items already assigned (Open, dminchuk)
- **EPBDS-16264 → automate EPBDS-16210** (security token copy to clipboard failed): extend the PAT tests (`TestPersonalAccessTokenMultiUserUi` / `...AdUi`, EPBDS-16168) with a clipboard assertion after token creation (Playwright clipboard permissions in the docker context).
- **EPBDS-16272 → automate EPBDS-16224** ([Offer] Modules empty; dev fix `47c7a0b35d` keeps `<modules>` intact on Project Info and OpenAPI edits): note the ticket says "by scenario for AI" — confirm whether it targets this TAF or the MCP suite before implementing; the UI-side guard (edit Project Info → modules list unchanged) belongs here and partially exists (`TestUploadModulePreservesExistingUi` covers the adjacent 16227).
- Definition of done: each closed with a comment referencing the commit; tests registered in their home suites.

## Suite hygiene (do alongside, cheap)
- 5 classes are in NO suite and never run in CI: `TestReactCreateProjectSmoke` (6 tests), `TestReactSaveProjectSmoke`, `TestReactBranchesSmoke`, `TestReactMergeSmoke`, `TestReactMergeConflictSmoke`. Promote into `studio_smoke.xml`/`studio_git.xml` or delete — they are currently the only standalone create-from-zip/copy-project assertions.
- Dead legacy JSF page objects (post-EPBDS-9535): `ConfirmEraseDialogComponent`, `ConfirmUndeleteDialogComponent`, `RepositoryContent*`, `ElementsTabComponent`, `LeftRepositoryTreeComponent`, `RepositoryPage.createProjectFromWorkSpace/setShowDeletedProjects/unlockAllProjects` — schedule removal.
- `COVERAGE_GAP_ANALYSIS.md` is stale — replace or delete after this brief is worked.
- `TestACLManagePermission#testManagerCanAccessAdministration` is `enabled=false` because the product feature (Manager grants Administration) is not implemented — keep tracking, do not delete.

## Explicitly out of scope (checked, skipped)
- EPBDS-16194 (REST raw-table cell styles) → API-only; owned by MCP pair (open QAA EPBDS-16223).
- EPBDS-16305 (startup/readiness health checks) → API-only, no UI surface.
- EPBDS-16310 (Maven archetype), EPBDS-16342 (DEMO start script), EPBDS-16436 (docs site), EPBDS-16333/16287/16265/16208 (docs) → not UI-testable product behavior.
- EPBDS-16429 (StaticResourcesServlet NPE without trailing slash) → API-level; one-line ITEST in dev repo is the right home.
- EPBDS-16375 (cross-branch index startup performance) → environment-scale performance, not functional UI automation.
- EPBDS-16409 (DEMO mapped-folder 30-min cache) → DEMO-distribution specific.
- EPBDS-16276 → Closed as Cannot Reproduce, no commits.
- EPBDS-16366/16367 + EPBDS-16251 (workspace upgrade/migration on disk) → upgrade-path scenarios need old-version containers; valuable but a separate initiative (per memory, re-check of 16366/16367 on a published image is already pending).
- EPBDS-12429 (Branch/Project actions on list view) → status Clarification, no commits — re-check later.
- EPBDS-16380 (arbitrary home branch for non-default-branch projects) → In Progress, no commits — re-check when closed.
- EPBDS-16227/16229/16275/16327/16357 → already covered by existing tests (`TestUploadModulePreservesExistingUi`, `TestProjectDeleteUnsavedEditUi`, `TestMigratedMethodFilterReloadUi`, `TestProjectOverviewEditKeepsModulesUi` (needs rework, see item 4), `TestCopyTableVersioningUi`).
- EPBDS-15637/15636 (Remove Local Projects / Erase) → landed under EPBDS-15385/16253; permanent-delete already covered by `TestPermanentDeleteAfterRefreshUi`; the erase/undelete UI no longer exists — only the dead-code cleanup above remains.
- Old wizard sub-tasks (EPBDS-1566, 1749, 2474, 3794, 3795, 3796, 6903, 6912, 5576, 12337, 12020, 12170, 11434, 14469, 14474, 14794) → historical tickets swept into the fixVersion; functionally absorbed by items 3 and the lifecycle coverage.
