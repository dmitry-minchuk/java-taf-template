# Debugging a failed test with an AI assistant

Every test that did not pass has one JSON file in this directory, named `<TestClass>.<testMethod>.json`,
and `index.json` lists all tests of the run. A bundle is self-contained: hand it to the assistant together
with the files it points to (paths are relative to the report root).

What a bundle holds:

- `test` — class, method, `@TestCaseId`, description, suite, shard and a link to the test source at the exact commit.
- `run` — application build, workflow run URL, tests repository, branch and commit.
- `result` — status, timestamps, duration, exception type, message and full stack trace.
- `steps.lines` — the complete framework log of this test only: container start, every page action, wait,
  REST call and assertion, with millisecond timestamps. Read it top to bottom to see what the test did
  before it failed; the last lines show the failing step.
- `applicationLog` — every WARN, ERROR and exception (with the following stack lines) from the
  OpenL Studio or Rule Services container that served this test, plus the path to the full log.
- `trace.path` — the Playwright trace: DOM snapshots before and after every action (the page at every step,
  including the failure), network and console. Open it with `npx playwright show-trace trace.zip` or drop it
  onto https://trace.playwright.dev.
- `attachments` — failure screenshot and downloaded files; the failure video is not copied into the report,
  it stays in the rp-export artifact and in ReportPortal.

Suggested prompt:

    Here is the debug bundle of a failed UI test (JSON) and its Playwright trace. Determine whether the
    failure is a product defect, a test defect or an infrastructure problem. Use `steps.lines` for the
    test's actions, `result.stackTrace` for the failing assertion, `applicationLog.warningsAndErrors`
    for server-side errors around the failure time, and the trace for the page state. Quote the exact
    log lines you base the verdict on.
