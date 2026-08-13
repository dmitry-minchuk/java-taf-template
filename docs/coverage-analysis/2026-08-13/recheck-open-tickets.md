# OpenL 6.4.0 — unfinished dev tickets: re-check after they close

Analysis date: 2026-08-13. Purpose of this file: these tickets are NOT included in the automation brief as work items because their code is not final — automating their behavior now would pin down in tests something that is still going to change. Come back when the statuses become Resolved/Closed.

## EPBDS-16380 — arbitrary "home" branch for projects outside the default branch

- Status as of 2026-08-13: **In Progress**, no commits in the 6.3.0..main window.
- Essence: a project living outside the default branch gets an arbitrary "home" branch — the newest tip of any branch wins, even over protected branches, and the choice shifts between Studio restarts.
- Why it matters: a direct consequence of the cross-branch index (EPBDS-8537) — the same mechanism the already-closed 16378/16381/16410/16411 hang off. Our protected-branch tests (the EPBDS-15960 cluster) implicitly depend on which branch Studio considers "home".
- What to check after it closes:
  1. The rule that picks the home branch (expected: default > protected > a deterministic order, NOT commit freshness).
  2. Stability of the choice across container restarts (two restarts — the same branch).
  3. A regression for protected branches: a project that exists only in a protected branch does not "move" after a push to an unrelated branch.
  4. Whether `TestGitSwitch*`/`TestProtectedBranch*` still pass (they create projects in non-default branches).
- Where it goes: `studio_git.xml`, next to the delete-branch cases (see test-cases-top-risk-zones.md, zone 5).

## EPBDS-12429 — Branch and Project Actions in the Repository on the Project List View

- Status as of 2026-08-13: **Clarification** (In Progress category), no commits, the ticket is several years old.
- Essence: an improvement — surface branch and project actions directly on the projects list.
- Current reality: in 6.4.0 the React list ALREADY carries row actions (Open/Close/Copy/Delete Branch + the overflow menu: Save/Sync/Deploy/Compare/Export/Delete) — a large part of the ticket was effectively delivered by the EPBDS-16307/9535 cluster without referencing this key.
- What to check after it closes:
  1. What is left of the original request after the Clarification (the ticket may be closed as Duplicate/Done on the strength of 16307).
  2. If new code appears — diff it against the current row-action model; our tests already rely on `ProjectsTableComponent.clickRowAction`/`getProjectActionLabels`, and any action reshuffle will hit them.
- Where it goes: row-action coverage already exists (`TestRepositoryTableActions`, the ACL cluster); most likely no new cases — only page-object updates.

## Other unfinished release tickets (a short checklist)

| Ticket | Status | Essence | Note |
|---|---|---|---|
| EPBDS-16388 | Open | The Copy modal dropped the wizard's guarantees (author stamp, version uniqueness) | Guarding known-failing scenarios already exist in the test cases — flip them to normal green on closure. |
| EPBDS-16227 | Integration | Modules disappear when adding an Excel module | The UI guard is covered (`TestUploadModulePreservesExistingUi`); on closure check whether the fix grew wider. |
| EPBDS-16275 | In Testing | Endless loop after a filter migration | Covered by `TestMigratedMethodFilterReloadUi`; on closure update the RP marking if it was failing. |
| EPBDS-16430 | In Testing | Studio hangs after renaming a module that has no name in rules.xml | A regression-case candidate for the Migrate zone after closure. |
| EPBDS-16432 | In Testing | 404 on Revisions after a project rename | Candidate: fold the rename into the end-to-end Base64/rename case (zone 6). |
| EPBDS-16433 | In Testing | No validation of Properties processor / version pattern on Overview | Candidate: a negative case for Overview (zone 4 grows). |
