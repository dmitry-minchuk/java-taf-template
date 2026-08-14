# Coverage plan: the React Deployments tab (EPBDS-16307, EPBDS-16403)

Target: OpenL Studio 6.4.0 React Deployments feature — the list screen `/deployments` and the detail screen
`/deployments/:deploymentId`. Automation repo: java-taf-template (TestNG + Playwright).
Revision 2 — rewritten after the plan review; every change from that review is folded in.

Sources of truth:
- **Live UI**, `ghcr.io/openl-tablets/webstudio:6.4.0-71e2caea5c3b` with a JDBC (PostgreSQL) production
  repository. Facts marked "verified live" were observed in a browser on 2026-08-13/14.
- **Product code** on `openl-tablets` `origin/main`: `containers/DeploymentsHome.tsx`,
  `containers/DeploymentWorkspace.tsx`, `containers/DeployModal.tsx`, `containers/Header.tsx`,
  `services/deployments.ts`, `services/projectId.ts`, `DeploymentsController`, `DeploymentServiceImpl`,
  `DeploymentManager`, `ProjectIdModel`, `locales/repository.en.ts`.

## 1. Verified product contract

1. **The tab is read-only.** `/deployments` and `/deployments/:id` offer no deploy, redeploy, delete, compare or
   export. The user can: pick a repository, search (and clear it), open a deployment, page through the list and
   change the page size; the detail screen has its own project pagination held in component state, not the URL.
   Writing happens from the Projects side through the Deploy modal.
2. **The nav tab is gated on a SUCCESSFUL non-empty `GET /web/production-repos`.** A failed probe shows the tab;
   a successful answer is cached for the page session, so the tab does not appear or disappear without a reload.
   With no deployment repository the tab is absent but `/deployments` stays routable by URL.
3. **Deploying replaces the deployment content.** A deploy pushes the chosen project **plus its resolved
   dependencies**; every project previously in that deployment and not in the new set is removed. *(verified live:
   deploying B into a deployment holding A left only B)*
4. **Two different write paths share the Deploy modal.** Typing a NEW name sends `POST /web/deployments`
   (create); selecting an EXISTING name from the dropdown sends `POST /web/deployments/{id}` — the redeploy-by-id
   path that EPBDS-16403 fixed. A test that types an existing name is exercising create, not redeploy.
5. **Ids are URL-safe Base64** of `<repositoryId>:<deploymentName>` (`+`→`-`, `/`→`_`; padding appears only when
   the byte length is not a multiple of three, so never assert on `=`). Malformed and unknown ids both answer
   HTTP 404, and because the deployments services do not suppress error pages the user gets the **global 404
   page**. *(verified live)*
6. **List state lives in the URL**: `?repo`, `?q`, `?page`, `?size`; the search is client-side substring,
   case-insensitive, no debounce; `size` accepts any positive integer; an unknown `?repo` silently falls back to
   the first repository. The summary counts the **unfiltered** deployments of the selected repository and reads
   `0 deployments` while loading. *(code + verified live)*
7. **A search whose result set is smaller than the current page clamps to the last available page** rather than
   showing an empty list, and the pagination control disappears while the filtered set fits one page.
   *(verified live: `?size=1&page=2&q=Bravo` displayed BravoDeployment)*
8. **Row order is server-side**: folders first, then case-sensitive `String.compareTo` — uppercase before
   lowercase. Fixture names must therefore be same-case ASCII when a test depends on which row is on page 1.

## 2. Fixture strategy

| Need | How |
|---|---|
| Deployment repositories | `DeployInfrastructureService.builder().withPostgres().withSecondProductionRepository()`, started from `BaseTest.startAuxiliaryContainers()`. The second repository is **opt-in**: this service is shared with the ACL and deploy-message suites, which drive the repository dropdown by index, so adding a repository unconditionally would have changed their fixture. With the flag the fixture declares `production` ("Deployment") and `production-second` ("Deployment Two") against two schemas of the same PostgreSQL container; without it the configuration is unchanged from before. |
| Second repository plumbing | Both schemas must be created explicitly — OpenL never issues `CREATE SCHEMA`, and PostgreSQL refuses `CREATE TABLE` when `currentSchema` names a schema that does not exist. The factory key is `repository.<id>.$ref` (single `$`); `production` survives a wrong key because it has built-in defaults, `production-second` does not and is silently skipped. |
| Container params | `DEPLOY_STUDIO_PARAMS` is byte-identical to `DEFAULT_STUDIO_PARAMS`; the deploy repositories come entirely from the copied `.properties`. |
| No deployment repository | plain `DEFAULT_STUDIO_PARAMS` with no auxiliary containers, in its own class so the fixture files are not copied. |
| Deployments | created through the Deploy modal (the real user path). |
| Deterministic paging | `?size=1` with two deployments instead of creating 26. |
| Dependency-free projects | the built-in `Sample Project` template, so a deployment holds exactly one project. |

Each `@Test` method gets its own Studio container (BaseTest starts one per method), so fixed names are safe and
a fresh deployment repository is guaranteed. The cost driver is the number of methods, which is why the cases
below are merged into journeys.

## 3. Test classes and cases (6 methods)

### `TestDeploymentsEmptyStateUi` — no deployment repository (1 method)

- **DEP-E1 (N). Without a deployment repository the feature is closed off, and says so.**
  Log in, then open `/deployments` by URL.
  Expected, in this order (the first assertion proves `/production-repos` has answered, so the header assertion
  cannot pass prematurely): the content pane shows `deployments-empty-repositories` and the rail shows
  `deployments-no-repositories`, both reading `No deployment repositories`; the header lists `Projects` but not
  `Deployments`; and on a clean, unmodified project the row's overflow menu lists `Export` but not `Deploy`.

### `TestDeploymentsTabUi` — the main journey with two repositories (1 method)

- **DEP-J1 (P). Empty repository → deploy → list → open → detail → deep link → redeploy by id.**
  1. Before any deploy: the Deployments tab shows `deployments-empty` (`No deployments in this repository`) and
     the summary reads `0 deployments`; both repositories are listed in the rail.
  2. Deploy a project (non-ASCII name, so the ids need URL-safe encoding) into repository `production` under a
     new deployment name.
  3. The list shows exactly that one row, the summary reads `1 deployment`.
  4. Opening the row navigates to `/deployments/<id>`. The deployment name is chosen so that its **standard**
     Base64 contains a `+`, and the test asserts that up front — otherwise the alphabet assertion would be
     vacuous, as it was in the first implementation. The id must then contain `-` or `_`, must contain neither
     `+` nor `/`, and must decode to `production:<deploymentName>`.
  5. The detail screen shows the deployment name as its title, a single `Projects` tab, and one project row
     carrying the project name, a revision and the deploying user. Revision and user are compared against the
     `—` placeholder the UI renders when the server drops the audit fields, not merely against blank.
  6. Reloading that deep link renders the same title (the browser-side half of EPBDS-16402/16403).
  7. Redeploy: from the project row, open the Deploy modal, **select the existing deployment name from the
     dropdown** and deploy, wrapped in a Playwright `waitForResponse` on `POST /web/deployments/<id>`. That
     response must be `204` (the controller method is declared `void`), the list must still hold exactly one
     row, and the project row's revision must differ from the one captured in step 5.

### `TestDeploymentRepositoryIsolationUi` — two repositories, one deployment name (1 method)

- **DEP-J5 (P). The same deployment name in both repositories stays isolated.**
  Deploy one project into `production` and into `production-second`, both under the identical deployment name.
  Expected: each deploy reports success; each repository lists exactly that one row; the rail switch puts
  `?repo=production-second` in the URL; and the id opened from each repository decodes to **its own** repository
  prefix. Opening the second repository's deployment must show the project inside it.

  This case exists because a broken deployment repository is otherwise indistinguishable from an empty one:
  `DeploymentServiceImpl` swallows per-repository failures and answers with an empty list, so "the second
  repository is empty" passes just as well when the repository cannot be instantiated at all. Verified to fail
  on a broken second repository — it caught two real fixture defects (a missing Postgres schema, and the factory
  key written as `$$ref` instead of `$ref`).

### `TestDeploymentsListUi` — search and paging over two deployments (1 method)

- **DEP-J2 (P/N/B). Search hit, miss, clear; `?size=1` paging; clamping.**
  Deploy two projects under two same-case ASCII deployment names (`AlphaDeployment`, `BravoDeployment`).
  Expected: a fragment of one name leaves exactly that row; a garbage query shows `deployments-no-match` with
  `No deployments match your search`; `Clear search` restores both rows; the summary reads `2 deployments`
  throughout, including while filtered; `/deployments?size=1` renders the pagination control and one row per
  page, with different rows on pages 1 and 2; searching from page 2 for the deployment that lives on page 1
  displays that deployment instead of an empty page.

### `TestDeploymentWorkspaceUi` — unknown id (1 method)

- **DEP-J3 (N). A well-formed but unknown deployment id is the global 404 page.**
  Navigate to `/deployments/<valid-alphabet id that decodes to an unknown deployment>`.
  Expected: the global `404` / `Page not found.` screen with its `Home` button (the page carries no testid, so
  assert the text); navigating back to `/deployments` restores a working list. Dev-side ITEST covers a
  *malformed* id only, so this case is new.

### `TestDeploymentReplacesContentUi` — destructive replacement (1 method)

- **DEP-J4 (N, state). Deploying another project into an existing deployment replaces its content.**
  Deploy dependency-free project A into deployment D, then deploy dependency-free project B into the same D by
  selecting D from the dropdown.
  Expected: D holds exactly one project row, it names B, and A is gone.

## 4. Page objects

- `DeploymentsHomePage`: rail (`deployment-repository-<id>`, selection), summary (waits for the list to leave
  its loading state), search + `Clear search`, `deployments-no-match` / `deployments-empty` /
  `deployments-empty-repositories` / `deployments-no-repositories`, pagination, row listing and open,
  `open()` / `openWithQuery()`.
- `DeploymentWorkspacePage`: title, tabs, project rows with their cells, `openById()` and the 404 page with its
  `Home` button, so no test builds locators of its own.
- `DeployModalComponent`: added `selectExistingDeploymentName` and `deployToExistingDeployment` so the
  redeploy-by-id path is reachable.
- `TabSwitcherComponent`: added `getVisibleTabNames()` and `isTabOfferedWithin(name, timeout)`, so asserting
  that a tab is **absent** polls for it instead of reading the header once, before the async probe that adds
  the `Deployments` item has resolved.

## 5. Explicitly out of scope

- `deployments-error`, `deployments-repositories-error`, `deployment-missing` — unreachable through the product:
  the server swallows repository failures and a genuine 403/404/500 is intercepted into a full-page error.
  Needs request interception; covered by vitest.
- `projectDeployed` refresh — the listener lives on a screen that is unmounted while the Deploy modal is open,
  so it is unreachable from the UI. Covered by vitest.
- The repository→deployments cache not refetching on switch-back — real and untested, but observing it needs an
  out-of-band deployment; it is stale data rather than a screen state. Raise with the dev team, keep as vitest.
- Tooltip/ellipsis on long names, responsive column hiding (768/992 px) — CSS-only; the DOM text is intact, so
  no honest assertion is cheap.
- Main-branch-only repositories, broken deployment repository, the ACL deploy matrix — covered by dev-side
  ITEST (`task_EPBDS-16307-deploy-main-branch-only`, `task_EPBDS-16307-broken-deploy-repo`, `EPBDS-12973`).
- The Publish tab of a project (`publish-deployment-*`) — the other view of this data; one assertion block is
  worth adding later, but it is a Projects-screen surface, not the Deployments tab.

## 6. Product questions raised by this analysis

- `DeploymentManager.deploy` appends `_V<version>` to the requested name whenever a deployed project's
  `rules-deploy.xml` carries a version. Redeploying by id into `…:Foo_V2` therefore appears to create
  `Foo_V2_V2` instead of updating in place. Nothing covers this at any level — worth a question to the dev team
  and an ITEST rather than a UI test.
- The detail screen labels its column `Revision in Design Repository` but renders the artefact version from the
  **production** repository.
