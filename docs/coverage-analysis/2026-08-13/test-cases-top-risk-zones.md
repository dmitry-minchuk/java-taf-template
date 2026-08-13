# OpenL Studio 6.4.0 — test cases for the top-risk uncovered zones

Scope: java-taf-template (TestNG + Playwright). 28 cases across 7 zones — user-scenario oriented, each zone carries its negative checks, no case inflation. Assertions state the EXPECTED behavior from the tickets/spec; if actual differs, file a bug and keep the case as a known-failing regression linked to it (never weaken the assertion).

Legend: P = positive / primary flow, N = negative / error handling, B = boundary. Each case is one test method unless noted.

## Zone 1 — WebSocket live UI updates (EPBDS-16314; guards 16384, 16386, 16389, 16392)

Prerequisite (one-time infra): two live Playwright browser contexts against the same Studio container (current two-user tests are sequential). Suite: `studio_smoke.xml` or a new `studio_live_updates.xml`.

- **WS-1 (P). Another user's change appears without refresh.**
  User B uploads a module into project P and saves; user A keeps the Projects list and the P detail page open.
  Expected: A's list row and detail reflect the change within the push window (bounded wait, no reload calls in the test). After a manual reload the state is identical (push and read paths agree).
- **WS-2 (N). Own action is not echoed back.**
  User A saves a project and stays on the Projects screen.
  Expected: exactly one state transition on A's screen — no second loading overlay, no flicker of the just-saved row (own change ping is skipped via client id).
- **WS-3 (N, race). Change ping during reload never wedges the overlay.**
  User A triggers a list reload; user B commits a change while A's reload is in flight. Repeat 3×.
  Expected: the loading overlay always disappears; the list is interactive and shows B's change (guards EPBDS-16384).
- **WS-4 (P). Action feedback blocks the double-submit and unlocks on the rendered status.**
  User A clicks Open on a large project, immediately tries to click it again; then the same for Close.
  Expected: the control is disabled/busy until the page shows the new status; no second request is fired; buttons unlock only after the status is on screen (guards EPBDS-16389, EPBDS-16392).
- **WS-5 (P, integration). External git push reaches the UI.**
  Commit into the design repository bypassing Studio (direct push to the fixture git container) while A watches the Projects list.
  Expected: the list reflects the external change within the polling window (~10 s + margin) without any user action.

## Zone 2 — Interactive trace debugger (EPBDS-16195, 16292; guards 16291, 16344, 16406, 16407)

Suite: `studio_rules_editor.xml`. Reuse `TraceWindowComponent`; add breakpoint/watch support to the page object.

- **TR-1 (P). Business view is the default; Advanced reveals the debugger.**
  Launch trace for a test table via the launch modal.
  Expected: business tree opens (no debugger toolbar); switching Advanced on shows step controls, call stack, breakpoints and watches panels; decision tables are broken into condition rows and the fired rule + result are highlighted.
- **TR-2 (P). Breakpoint suspends, stack and watches are live, stepping completes.**
  Set a breakpoint on a table (include one name with an instance suffix — EPBDS-16406), add one valid watch, run the trace, step over once, resume.
  Expected: execution suspends at the breakpoint (including the instance-suffixed one); call stack and variables are populated; the watch shows its value; after resume the run completes and the result names the executed table, not a virtual suite (EPBDS-16407).
- **TR-3 (N). Breakpoint on a never-executed table does not suspend; invalid watch fails specifically.**
  Set a breakpoint on a table the input never reaches and add a syntactically invalid watch expression; run.
  Expected: the run completes without suspension; the invalid watch shows a specific error in its row — no crash, no generic failure of the whole trace.
- **TR-4 (N). A failing run surfaces the error on the failing step.**
  Trace a test that hits a runtime error.
  Expected: the business tree marks the failing step with the error, and the error text is reachable from the node (guards EPBDS-16344); the trace does not render as an empty/endless tree.
- **TR-5 (B). Large trace stays usable.**
  Trace a run producing hundreds of nodes.
  Expected: the tree renders via the single capped request (no lazy-page storm), expanding/collapsing works, closing the tab mid-run terminates the session cleanly and a re-launch works (guards EPBDS-16291).

## Zone 3 — Create Table / Copy Table React modals (EPBDS-16313; guards 16355, 16357, 16359, 16388, 16412/16354, 16417, 16418)

Suite: `studio_rules_editor.xml`. Extend `CreateTableDialogComponent` / `CopyTableDialogComponent`.

- **CT-1 (P, parameterized). Every table type is creatable through the modal.**
  Create one Decision (rules), one Data, one Test and one Spreadsheet table via the modal (Datatype and Simple Rules are already covered); include one transposed variant; for the Decision table set Result type = Integer (guards EPBDS-16417); save the project and reopen.
  Expected: each table compiles, appears in the module, survives the save/reopen round-trip; the Data table has no extra "Name" column (guards EPBDS-16418).
- **CT-2 (N/B). Name and title validation.**
  Try to create with an empty technical name, a name with forbidden characters, and titles of exactly 31 and 32+ symbols.
  Expected: empty/forbidden names are rejected with the specific message and Create stays disabled; 32+ titles succeed with the mirrored sheet name clipped to Excel's 31-char limit without a duplicate-sheet clash (guards EPBDS-16355).
- **CT-3 (P/B). Numeric range condition survives as a range.**
  In a Decision table typed Integer, enter the condition `18-30`; also one single value and one open-ended range.
  Expected: the created cell holds the range OpenL matches by (`18-30`), not a mangled number like `181` (guards EPBDS-16359); rule evaluation honors the bounds.
- **CT-4 (P). Copy preserves the table style — direct QA order EPBDS-16412.**
  Copy a table that carries non-default styling; open both copies.
  Expected: the copy renders with the original style (guards EPBDS-16354). If the defect still reproduces, keep known-failing linked to 16354.
- **CT-5 (N, known-failing until EPBDS-16388 is fixed). The modal keeps the wizard's guarantees.**
  Copy a table as a new version twice with the same version value; inspect the created version's properties.
  Expected: the duplicate version is rejected with a specific message, and the copy carries the author stamp. Both assertions are the EXPECTED spec — keep the test red and linked to EPBDS-16388 while it is open.

## Zone 4 — Migrate rules.xml / rules-deploy.xml (EPBDS-16327; guards 16363, 16364, 16365, 16408)

Suite: `studio_smoke.xml`. Fixtures: zip projects with legacy descriptors (root-level workbooks; .xls+.xlsm+.xlsx mix; a module method-filter; a formatting-only-diff rules.xml).

- **MG-1 (P). Legacy project migrates cleanly and is audited.**
  Open a legacy-descriptor project → Overview offers Migrate → confirm.
  Expected: workbooks land under `rules/`, the module list is identical before/after, the project compiles, History records the migration revision with the correct author; after migration Edit is offered instead of Migrate.
- **MG-2 (N). Migrate is refused when it would widen the module set.**
  Project with an undeclared root workbook → attempt Migrate.
  Expected: the server refuses with the specific module-widening message; `rules.xml` is byte-identical to before; project state is intact (guards EPBDS-16363).
- **MG-3 (P). Mixed formats and method filters survive.**
  Legacy project with `.xls`, `.xlsm`, `.xlsx` modules and one module method-filter → Migrate.
  Expected: ALL modules are present and compile afterwards (guards EPBDS-16364); the method filter still hides what it hid before — assert the exposed method set before/after (guards EPBDS-16365; if the filter cannot become a glob, the EXPECTED behavior is refusal, not silent dropping).
- **MG-4 (N). No false offer, no hidden offer.**
  Project whose rules.xml differs from canonical only in formatting; and a genuinely legacy one.
  Expected: the formatting-only project gets Edit (no Migrate offer); the legacy one gets Migrate (guards EPBDS-16408).

## Zone 5 — Delete Branch dialog (EPBDS-16378; guards 16255, 16381)

Suite: `studio_git.xml`. REST oracle: `ProjectBranchesMethod`.

- **DB-1 (P). Branch deletion end-to-end.**
  Create branch B from master, delete it via the row action → confirmation dialog.
  Expected: the dialog names the branch; after confirm the branch is gone from the switcher, the merge-target list and Git (REST oracle); the project itself is untouched.
- **DB-2 (N). Deletion is not offered where it cannot succeed.**
  Inspect the branch actions for the default branch and for a protected branch.
  Expected: no Delete action for either (guards EPBDS-16378); protected branch keeps its marker.
- **DB-3 (P/N). Deleting the last branch that holds a project warns explicitly.**
  Project created only in branch X → delete X.
  Expected: the dialog warns the project will be deleted with the branch and requires the explicit acknowledgement; Cancel leaves everything intact (negative half); Confirm removes branch and project from the list (positive half).
- **DB-4 (N, two users). A branch locked by another user's edit is not deletable.**
  User B edits project P on branch X; user A attempts to delete X.
  Expected: specific rejection message naming the lock; the branch survives; B's editing session is unaffected (guards EPBDS-16255).

## Zone 6 — URL-safe Base64 ids and non-ASCII names (EPBDS-16402, 16403; guard 16381)

Suite: `studio_smoke.xml`. Precompute names whose Base64(repo+path) contains `+` and `/`.

- **B64-1 (P, end-to-end). Special-encoding project survives the full lifecycle.**
  With a name forcing `+`/`/` in the id: create → open → Files tab → History tab → editor round-trip (legacy bridge) → deploy → **redeploy into the SAME deployment configuration** → delete branch dialog → delete.
  Expected: every screen and request succeeds — no HTTP 400/404 anywhere (guards EPBDS-16402, EPBDS-16403, EPBDS-16381).
- **B64-2 (P). Non-ASCII name works everywhere, including deep links.**
  Create a project named in Cyrillic (e.g. `Проект-Тест`); walk the same screens; paste the `/projects/{id}` URL into a fresh session.
  Expected: all screens render the name correctly; the deep link opens the project (no 400); History and Deploy show the correct name.
- **B64-3 (N/B). Name validation boundaries.**
  Try names with forbidden characters, a leading/trailing space, and one at the maximum allowed length.
  Expected: forbidden and space-padded names are rejected with the specific validation message (nothing is created — verify via REST); the max-length name is created and usable.

## Zone 7 — Remaining gaps: Deployments tab, datatype round-trip, Files-tab selection, admin search/marker

- **DEP-1 (P/N). Deployments tab reflects deploy and redeploy.**
  Deploy project P; open `/deployments`; open the deployment; redeploy P with a change; also check the tab with no deploy repository configured (negative).
  Expected: the deployment appears with correct name/status and content; redeploy updates the same entry (no duplicate); without a deploy repo the tab renders its empty state, not an error. Covers the EPBDS-16307 Deployments slice.
- **DT-1 (P, parameterized). Datatype editor round-trip fidelity.**
  For fixtures: titled datatype, 6-column (mandatory/description/example), transient (`~`) field, reordered columns — open in the editor, edit one cell, save, reopen.
  Expected: title stays a title (first field row is a field), all six columns survive, the transient marker survives regardless of column order, Type cell links navigate (guards EPBDS-16425, 16426, 16428).
- **FT-1 (P/N). Files tab drops a stale selection instead of erroring.**
  Select a file (URL carries the selection); delete that file (same session; variant: from a second session); also deep-link a URL pointing at a no-longer-existing file (negative).
  Expected: after the tree reloads the pane never shows "The resource is not found" — the selection is cleared and the page stays usable (guards EPBDS-16437).
- **ADM-1 (P/N). Admin search and online marker — direct QA orders EPBDS-16233, EPBDS-16234.**
  On Users and Groups tabs: search by username/display name/email fragment; search garbage (negative); clear the filter. Separately: user B holds an active session — admin checks the Last Login online badge in multi mode (and AD mode under PLAYWRIGHT_DOCKER); B logs out.
  Expected: matches filter correctly, garbage shows the empty state, clearing restores the full list, search cooperates with pagination; the online badge shows for B's active session and disappears after logout (guards EPBDS-16216).

## Case budget

| Zone | Cases | Of them negative/boundary |
|---|---|---|
| 1. WebSocket | 5 | 2 |
| 2. Trace debugger | 5 | 2 |
| 3. Table modals | 5 | 2 (+1 known-failing) |
| 4. Migrate | 4 | 2 |
| 5. Delete Branch | 4 | 2 |
| 6. Base64 / non-ASCII | 3 | 1 |
| 7. Remaining gaps | 4 | 3 mixed P/N |
| **Total** | **28** | **~12** |
