# OpenL 6.4.0 — mismatches between the JIRA release scope and the actual commits

Analysis date: 2026-08-13. Window: tag `6.3.0` → `origin/main` (509 commits).
Purpose of this file: come back later and (a) reconcile the JIRA fixVersion with the code that actually shipped, (b) make sure the user-visible changes made it into the release QA scope.

## Part 1. Release tickets with NO commits in the window (9)

| Ticket | What it is | Explanation / what to do on return |
|---|---|---|
| EPBDS-15637 | Remove Local Projects (New Feature, Closed) | The code landed under EPBDS-15385 and EPBDS-16253. No action needed — coverage is described in the brief (permanent delete is covered, the LOCAL conversion is gone). |
| EPBDS-15636 | Erase Project (Improvement, Closed) | The Erase concept was removed from the product (no "erase" in `studio-ui`). Check: the ticket is closed as Fixed — in reality the feature was retired, not implemented; the resolution wording can mislead. |
| EPBDS-15806 | Admin UI – search on Users/Groups (Closed/Completed) | No commits under this key; the search was actually added by commit `e678142276` under EPBDS-16214. The open QAA EPBDS-16233 references 15806 — reference both keys when automating. |
| EPBDS-12429 | Branch/Project Actions on the Project List View | Status Clarification, no code. See `recheck-open-tickets.md`. |
| EPBDS-16270 | Project with the name of a deleted project can't be created (Closed) | No commit with this key. Likely fixed as part of permanent delete (EPBDS-15385) or the cross-branch index (EPBDS-8537). On return: check the JIRA resolution and add a regression scenario "create a project named after a previously deleted one" — it is cheap and missing from the TAF. |
| EPBDS-16276 | HTTP 400 on an invalid password (Closed) | Resolution Cannot Reproduce — no action needed. |
| EPBDS-16354 | "Copy" table loses the style (Resolved/Fixed) | No dedicated commit; likely absorbed by the server-side copy-by-id (`00a6697448`, EPBDS-16358). There is a direct QA order EPBDS-16412 — before automating, verify in JIRA which commit closed it. |
| EPBDS-16380 | Arbitrary "home" branch for projects outside the default branch | In Progress, no code. See `recheck-open-tickets.md`. |
| EPBDS-16388 | The Copy modal dropped the wizard's guarantees | Open, no code — a live defect. The test cases include guarding scenarios (known-failing until fixed). |

## Part 2. Keys WITH commits but NOT in the "OpenL 6.4.0" fixVersion (20)

### User-visible — must enter the release QA scope (the important part)

| Key | Commits | What changed | What to do on return |
|---|---|---|---|
| EPBDS-9535 | 34 | The JSF→React migration epic: the legacy Repository tab was deleted (`a7a5715b65`), the "import from repository" flow removed (`7a3526e1ca`), the React repository UI added. | Check the epic's fixVersion; make sure the release notes record the REMOVALS of functionality. Regression scope: everything that lived on the legacy Repository tab. |
| EPBDS-15385 | 3 | Project deletion became PERMANENT (no archiving); file/folder deletion moved to React + REST. | Already covered by `TestPermanentDeleteAfterRefreshUi`. Check the fixVersion. |
| EPBDS-16253 | 2 | Projects no longer convert to LOCAL when the repository link is lost; workspace copies of unidentifiable projects are evicted. | The "repository unreachable → the project does NOT become LOCAL" scenario is not automated — a backlog candidate. Related to the old bugs 11434/14469/14794 closed in this release. |
| EPBDS-16251 | 6 | Per-project `.studioProps` replaced by a workspace metainfo registry; local edit history relocated out of project folders. | Upgrade risk: pairs with EPBDS-16366/16367 (workspace loss on upgrade). Needs a separate initiative with an old-version container — see the brief, "out of scope". |
| EPBDS-15136 | 1 | Manual project import removed (superseded by automatic detection). | Make sure the release notes record it; there must be no tests for the removed feature (verified — none). |
| EPBDS-16225 | 1 | The legacy JSF home page replaced by the React Help component. | Cheap smoke: `/help` renders, links are alive. Missing from the TAF. |

### Infrastructure / housekeeping (for reference)

| Key | Commits | What it is |
|---|---|---|
| EPBDS-16262 | 13 | CI restructuring: parallel quick build, split Keycloak SSO suites, **`itest.webstudio` retired** in favour of `ITEST/itest.studio/{acl,demo,disabled-settings,dtr,multi,repos,simple,sso,users}`. Affects WHERE the dev side adds integration tests. |
| EPBDS-16290 | 6 | Integration tests consolidated into the openl-packaging / openl-multiproject / openl-positive / openl-codegen reactors. |
| EPBDS-16260 | 5 | log4j→slf4j, the Jetty log directory, the ruleservice log file name. |
| EPBDS-16309 | 2 | Removed the obsolete Studio AI/gRPC search integration. |
| EPBDS-6912 | 2 | Typed/transposed table creation — functionally part of the EPBDS-16313 cluster (React modals), committed together. |
| EPBDS-16170 | 1 | A **revert** of "Make JAR repository project search location configurable" — on return, confirm the setting is really gone from the UI/docs. |
| EPBDS-15494 | 1 | Atomic folder uploads + the REPLACE conflict policy (together with EPBDS-6786). |
| EPBDS-14474 | 1 | The workspace copy closes when read access is revoked (an old bug, the code is in this window). |
| EPBDS-5576 | 1 | JSF page toasts unified with Ant Design notifications. |
| EPBDS-16342 | 1 | DEMO launcher: spaces in the path. |
| EPBDS-16333, 16287, 16265, 16208 | 1 each | Documentation only (release notes, getting started, trace guide). |

## Recommendation on return

1. In JIRA: add the "OpenL 6.4.0" fixVersion to EPBDS-9535, 15385, 16253, 16251, 15136, 16225 (or record why they sit outside the release).
2. In the TAF: the real new backlog candidates from this file — the "unreachable git → the project does not become LOCAL" scenario (16253), a `/help` smoke (16225), the "create a project named after a deleted one" regression (16270).
3. The upgrade scope (16251 + 16366/16367) is a separate initiative: it needs a container with the old version and a pre-populated workspace.
