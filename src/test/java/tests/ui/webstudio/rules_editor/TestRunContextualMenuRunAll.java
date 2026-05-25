package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import com.microsoft.playwright.Page;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent.FilterOptions;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import helpers.utils.WaitUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers the EPBDS-14039 delta only ("Run All" checkbox + new labels + info-icon tooltip move)
 * out of the broader Run Contextual Menu spec described in EPBDS-15969.
 *
 * The full spec includes many sections that this class deliberately does NOT cover.
 * If/when those sections get TAF coverage, the new assertions MUST land in this class
 * (one home for the whole contextual menu), not in a separate test.
 *
 * Sections NOT yet covered here:
 *   A — toolbar entry points: Test/Run/Trace/Benchmark buttons rendering, arrow-on-hover,
 *       body-click semantics (Run body runs all, Test body skips validation, Trace body opens
 *       the menu, Benchmark body runs), single-instance #unitsMenu with .b-run/.b-test/.b-trace/.b-benchmark
 *       footer filtering.
 *   B.2–B.8 — checkbox list selection (≤20 cases):
 *       check/uncheck all toggle, header checkbox auto-desync on row uncheck, selected subset
 *       → request with comma-joined testRanges, "All" mode does NOT send testRanges param,
 *       swap range ↔ table via "Use the Range", selector shared across Test/Run/Run into File/
 *       Test into File/Trace/Trace into File/Benchmark, "Run Cases" header for xls.run.method tables.
 *   C.2–C.4, C.6–C.8 — range-input (>20 cases):
 *       range input pre-fills with the first test id, default execution uses pre-filled range,
 *       accepted range syntax (2-4,7,10-12 / id3-id7 / 1,3,5 / 1-100 / trailing comma),
 *       validation errors via isAnyTestSelected (`Wrong [..] ID in the Range of IDs`,
 *       `No tests selected`), range applies to all menu actions, performance smoke (regression
 *       cover for EPBDS-13816 — large table opens fast without 100-row DOM).
 *   D — "Within Current Module Only" checkbox (#runTestModuleOnly):
 *       default unchecked when project compiles cleanly, user toggle reaches the server
 *       (currentOpenedModule=true|false), confirm dialog when project is NOT compiled,
 *       confirm dialog when other modules have errors (tableHasProblems), flag applies
 *       to all menu actions.
 *   E — runCasesSettings panel (Test tables only, NOT for xls.run.method):
 *       panel renders for Test tables and is hidden for Run tables, "Failures Only" toggles
 *       the "Failures per test" dropdown visibility, settings flow through to the results page,
 *       panel state shared across actions in the same session.
 *   F — footer buttons behaviour:
 *       Run / Run into File / Test / Test into File honor selection (download via ws.nav.download),
 *       Trace / Trace into File (startTestTableTrace + download), Benchmark navigates to
 *       studio.url('test/benchmark'), empty selection blocks every action ("No tests selected").
 *   G — state preservation across menu reopens and context switches:
 *       reopening preserves checkbox/range state, switching action context keeps the same
 *       selection (single #testSelector shared via itemStatuses.switchTo), trace-separate
 *       selection baseline (EPBDS-11675), menu closes on outside click and on action invocation.
 *   H — Failures Only / Compound Result UX:
 *       settings-page default applied on first menu open, per-session overrides do not change
 *       admin defaults.
 *   I.1–I.4, I.6 — edge cases:
 *       special characters in test ids (escape regression for EPBDS-13846), concurrent edits
 *       on the same module while menu is open, boundary exactly at 20 cases (le 20 inclusive),
 *       test with zero test cases, console error scan (no jQuery deprecation warnings —
 *       regression cover for EPBDS-14407, no RichFaces AJAX errors).
 */
public class TestRunContextualMenuRunAll extends BaseTest {

    private static final String ZIP_NAME = "RunAllTestTable.zip";
    private static final String MODULE_NAME = "Rules";
    private static final String BIG_TEST = "doubleItTest";
    private static final String SMALL_TEST = "smallTest";

    @Test
    @TestCaseId("EPBDS-15969")
    @Description("ACL: 'Run All' checkbox in the Run contextual menu — autofills range, locks readonly, "
            + "auto-unticks on context switch (Test/Trace/Benchmark); absent for tables with ≤20 cases. "
            + "Covers EPBDS-14039 scenarios C.9–C.13 and the corollary in I.5.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testRunAllToggleAndContextReset() {
        // ============ Setup: import a project with a 25-case Test table (doubleItTest) and a 5-case (smallTest) ============
        String projectName = WorkflowService.loginCreateProjectFromZip(User.ADMIN, ZIP_NAME);

        EditorPage editorPage = new EditorPage();
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, MODULE_NAME);
        editorPage.getProblemsPanelComponent().waitForCompilationToComplete();

        // ============ STEP 1: Open the >20-case Test table ============
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(FilterOptions.BY_TYPE)
                .expandFolderInTree("Test")
                .selectItemInFolder("Test", BIG_TEST);

        Page page = LocalDriverPool.getPage();
        WaitUtil.waitForCondition(
                () -> Boolean.TRUE.equals(page.evaluate("() => document.getElementById('runAllTests') !== null")),
                10000, 250, "Waiting for '#runAllTests' to appear in the DOM for doubleItTest"
        );

        // ============ STEP 2: Verify static markup added by EPBDS-14039 ============
        @SuppressWarnings("unchecked")
        Map<String, Object> markup = (Map<String, Object>) page.evaluate(
                "() => {" +
                "  const runAll = document.getElementById('runAllTests');" +
                "  const parentCls = runAll ? runAll.parentElement.className : null;" +
                "  const totalLabel = Array.from(document.querySelectorAll('div'))" +
                "      .map(d => d.textContent.trim()).find(t => /^Total test cases:\\s*\\d+$/.test(t));" +
                "  const individualDisabledLabel = Array.from(document.querySelectorAll('div'))" +
                "      .map(d => d.textContent.trim())" +
                "      .find(t => t === 'Individual selection is disabled for tables with more than 20 test cases.');" +
                "  const infoIcon = document.querySelector('#testRangeSetting a.imageButton');" +
                "  const infoIconTitle = infoIcon ? infoIcon.title : null;" +
                "  const testTablePresent = !!document.getElementById('testTable');" +
                "  return {parentCls, totalLabel, individualDisabledLabel, infoIconTitle, testTablePresent};" +
                "}");

        assertThat(markup.get("parentCls"))
                .as("C.9 — #runAllTests must be wrapped in <div class='b-run'>")
                .isEqualTo("b-run");
        assertThat((String) markup.get("totalLabel"))
                .as("B.1/C.1 — 'Total test cases: N' label must be present (added by EPBDS-14039)")
                .isEqualTo("Total test cases: 25");
        assertThat((String) markup.get("individualDisabledLabel"))
                .as("C.1 — 'Individual selection is disabled...' label must be present in the >20 branch")
                .isEqualTo("Individual selection is disabled for tables with more than 20 test cases.");
        assertThat((String) markup.get("infoIconTitle"))
                .as("C.5 — Range tooltip must live on the info-icon a.imageButton inside #testRangeSetting")
                .isEqualTo("Define ranges like: 2-4,7,10-12 or id3-id7");
        assertThat(markup.get("testTablePresent"))
                .as("C.1 — #testTable must NOT be rendered for the >20 branch")
                .isEqualTo(false);

        // ============ STEP 3: Tick Run All → range autofills 'first - last', readonly=true, prev saved ============
        @SuppressWarnings("unchecked")
        Map<String, Object> afterTick = (Map<String, Object>) page.evaluate(
                "() => {" +
                "  const runAll = document.getElementById('runAllTests');" +
                "  const range = document.getElementById('testRanges');" +
                "  runAll.checked = true;" +
                "  toggleRunAllTests(runAll);" +
                "  return {value: range.value, readOnly: range.readOnly," +
                "          prev: $j('#testRanges').data('prevValue')};" +
                "}");

        assertThat((String) afterTick.get("value"))
                .as("C.10 — After ticking Run All, #testRanges must autofill 'first - last'")
                .isEqualTo("1 - 25");
        assertThat(afterTick.get("readOnly"))
                .as("C.10 — After ticking Run All, #testRanges must become readonly")
                .isEqualTo(true);
        assertThat((String) afterTick.get("prev"))
                .as("C.10 — Previous range value must be saved in jQuery data('prevValue')")
                .isEqualTo("1");

        // ============ STEP 4: Untick Run All → previous value restored, readonly cleared ============
        @SuppressWarnings("unchecked")
        Map<String, Object> afterUntick = (Map<String, Object>) page.evaluate(
                "() => {" +
                "  const runAll = document.getElementById('runAllTests');" +
                "  const range = document.getElementById('testRanges');" +
                "  runAll.checked = false;" +
                "  toggleRunAllTests(runAll);" +
                "  return {value: range.value, readOnly: range.readOnly};" +
                "}");

        assertThat((String) afterUntick.get("value"))
                .as("C.11 — Unticking Run All must restore the previous range value")
                .isEqualTo("1");
        assertThat(afterUntick.get("readOnly"))
                .as("C.11 — Unticking Run All must clear the readonly flag")
                .isEqualTo(false);

        // ============ STEP 5: Custom prev value → tick → switch to Test mode → auto-untick, prev restored ============
        @SuppressWarnings("unchecked")
        Map<String, Object> afterContextSwitch = (Map<String, Object>) page.evaluate(
                "() => {" +
                "  const runAll = document.getElementById('runAllTests');" +
                "  const range = document.getElementById('testRanges');" +
                "  range.value = '3-7';" +
                "  runAll.checked = true;" +
                "  toggleRunAllTests(runAll);" +
                "  const tickedRange = range.value;" +
                "  const tickedRO = range.readOnly;" +
                "  resetRunAllForNonRunMode('b-test');" +
                "  return {tickedRange, tickedRO, afterRange: range.value," +
                "          afterRO: range.readOnly, afterChecked: runAll.checked};" +
                "}");

        assertThat((String) afterContextSwitch.get("tickedRange"))
                .as("C.10 — Tick with custom prev value should still autofill first-to-last")
                .isEqualTo("1 - 25");
        assertThat(afterContextSwitch.get("tickedRO"))
                .as("C.10 — Tick should still set readonly even when prev value was custom")
                .isEqualTo(true);
        assertThat(afterContextSwitch.get("afterChecked"))
                .as("C.12 — Switching to non-Run context must auto-untick Run All")
                .isEqualTo(false);
        assertThat((String) afterContextSwitch.get("afterRange"))
                .as("C.12 — Context switch must restore the custom prev range value")
                .isEqualTo("3-7");
        assertThat(afterContextSwitch.get("afterRO"))
                .as("C.12 — Context switch must clear readonly")
                .isEqualTo(false);

        // ============ STEP 6: resetRunAllForNonRunMode is a no-op when Run All is already unchecked ============
        @SuppressWarnings("unchecked")
        Map<String, Object> afterNoopReset = (Map<String, Object>) page.evaluate(
                "() => {" +
                "  const runAll = document.getElementById('runAllTests');" +
                "  const range = document.getElementById('testRanges');" +
                "  resetRunAllForNonRunMode('b-trace');" +
                "  return {value: range.value, readOnly: range.readOnly, checked: runAll.checked};" +
                "}");

        assertThat(afterNoopReset.get("checked"))
                .as("C.13 — resetRunAllForNonRunMode must not change state when Run All is already unchecked")
                .isEqualTo(false);
        assertThat((String) afterNoopReset.get("value"))
                .as("C.13 — resetRunAllForNonRunMode must not touch the range value when Run All is unchecked")
                .isEqualTo("3-7");
        assertThat(afterNoopReset.get("readOnly"))
                .as("C.13 — resetRunAllForNonRunMode must not touch the readonly flag when Run All is unchecked")
                .isEqualTo(false);

        // ============ STEP 7: Navigate to the ≤20 table → #runAllTests must NOT be in the DOM ============
        editorPage.getEditorLeftRulesTreeComponent()
                .selectItemInFolder("Test", SMALL_TEST);

        WaitUtil.waitForCondition(
                () -> Boolean.TRUE.equals(page.evaluate("() => document.getElementById('testTable') !== null")),
                10000, 250, "Waiting for #testTable (≤20 branch) to appear for smallTest"
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> smallMarkup = (Map<String, Object>) page.evaluate(
                "() => {" +
                "  const runAll = document.getElementById('runAllTests');" +
                "  const testTable = document.getElementById('testTable');" +
                "  const totalLabel = Array.from(document.querySelectorAll('div'))" +
                "      .map(d => d.textContent.trim()).find(t => /^Total test cases:\\s*\\d+$/.test(t));" +
                "  return {runAllInDom: runAll !== null," +
                "          testTablePresent: testTable !== null, totalLabel};" +
                "}");

        assertThat(smallMarkup.get("runAllInDom"))
                .as("I.5 corollary — #runAllTests must be absent from the DOM in the ≤20 branch")
                .isEqualTo(false);
        assertThat(smallMarkup.get("testTablePresent"))
                .as("C.1 inverse — #testTable must be rendered for the ≤20 branch")
                .isEqualTo(true);
        assertThat((String) smallMarkup.get("totalLabel"))
                .as("B.1 — 'Total test cases: N' label must also be present in the ≤20 branch")
                .isEqualTo("Total test cases: 5");
    }
}
