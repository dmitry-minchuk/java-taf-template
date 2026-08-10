package domain.ui.webstudio.components.editortabcomponents;

import com.microsoft.playwright.Dialog;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import configuration.core.ui.WebElement;
import configuration.driver.DriverPool;
import domain.ui.webstudio.components.BaseComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftProjectModuleSelectorComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.components.editortabcomponents.toolbar.EditorBreadcrumbsComponent;
import domain.ui.webstudio.components.editortabcomponents.toolbar.IMoreMenu;
import domain.ui.webstudio.components.editortabcomponents.toolbar.IRunMenu;
import domain.ui.webstudio.components.editortabcomponents.toolbar.IRunTestsMenu;
import domain.ui.webstudio.components.editortabcomponents.toolbar.ITraceMenu;
import domain.ui.webstudio.components.editortabcomponents.toolbar.ITraceWindow;
import domain.ui.webstudio.components.editortabcomponents.toolbar.MoreMenuComponent;
import domain.ui.webstudio.components.editortabcomponents.toolbar.RunTestsMenuComponent;
import domain.ui.webstudio.components.editortabcomponents.toolbar.TableToolbarComponent;
import domain.ui.webstudio.components.editortabcomponents.toolbar.TraceMenuComponent;
import helpers.utils.WaitUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * The editor's toolbar, composed of scoped sub-components (see the {@code toolbar} package):
 * <ul>
 *   <li>{@link EditorBreadcrumbsComponent} — the Projects / project / branch / module breadcrumb strip;</li>
 *   <li>{@link RunTestsMenuComponent} — the top-panel Test button with its settings dropdown;</li>
 *   <li>{@link MoreMenuComponent} — the top-panel More dropdown;</li>
 *   <li>{@link TableToolbarComponent} — the second-line table toolbar (Run/Trace/Benchmark, table actions).</li>
 * </ul>
 * The public API is kept flat here so existing tests keep working; new code may also use the
 * sub-component getters directly.
 */
public class EditorToolbarPanelComponent extends BaseComponent {

    // TOP LINE TOOLBAR — plain buttons that belong to no dropdown
    private WebElement exportBtn;
    private WebElement saveBtn;
    private WebElement verifyBtn;
    private WebElement copyProjectBtn;
    private WebElement createTableBtn;
    private WebElement refreshProjectBtn;
    private WebElement syncBtn;
    private WebElement allTopToolbarLinks;
    // Trace factor input in the launcher form (kept page-level: the menu exists in the DOM only while open)
    private WebElement factorTextField;

    // Scoped sub-components
    @Getter
    private EditorBreadcrumbsComponent breadcrumbs;
    @Getter
    private RunTestsMenuComponent runTestsMenu;
    @Getter
    private TableToolbarComponent tableToolbar;

    public EditorToolbarPanelComponent() {
        super(DriverPool.getPage());
        initializeElements();
    }

    public EditorToolbarPanelComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        exportBtn = new WebElement(page, "xpath=//a[@id='exportProjectButton']", "exportBtn");
        verifyBtn = new WebElement(page, "//a[@id='verifyButton']", "verifyBtn");
        saveBtn = new WebElement(page, "//a[@id='saveProjectButton']", "saveBtn");
        refreshProjectBtn = new WebElement(page, "xpath=//a[@id='refreshBtn']", "refreshProjectBtn");
        copyProjectBtn = new WebElement(page, "xpath=//a[@id='copyProjectButton']", "copyProjectBtn");
        createTableBtn = new WebElement(page, "xpath=//a[@title='Create new table']", "createTableBtn");
        syncBtn = new WebElement(page, "xpath=//div//a[text()='Sync']", "syncBtn");
        allTopToolbarLinks = new WebElement(page, "xpath=//form[@id='headerForm']//a", "allTopToolbarLinks");
        factorTextField = new WebElement(page, "xpath=//div[contains(@id, 'input')]//input[@type='text']", "factorTextField");

        breadcrumbs = new EditorBreadcrumbsComponent(page);
        runTestsMenu = new RunTestsMenuComponent(page);
        // EditorPage passes div#tableToolbarPanel as this component's root — reuse it for the table toolbar.
        tableToolbar = hasRootLocator() ? new TableToolbarComponent(getRootLocator()) : new TableToolbarComponent(page);
    }

    // ========== Top line toolbar ==========

    public void clickVerify() {
        verifyBtn.click();
    }

    public boolean isVerifyButtonPresent() {
        return verifyBtn.isVisible(2000);
    }

    public void clickCopyProjectBtn() {
        copyProjectBtn.click();
    }

    public boolean isCopyProjectBtnVisible() {
        return copyProjectBtn.isVisible(DEFAULT_TIMEOUT_MS / 2);
    }

    public void clickCreateTable() {
        createTableBtn.click();
    }

    public void clickSave() {
        // A real click is what reliably opens the save dialog; dispatching the event is only the fallback for
        // when the recompile keeps re-rendering this JSF toolbar button (see WebElement.clickWhenSettled).
        try {
            saveBtn.click();
        } catch (RuntimeException stillReRendering) {
            saveBtn.clickWhenSettled();
        }
    }

    /** The toolbar's Refresh, which reloads the project the editor is showing. */
    public void clickProjectRefresh() {
        refreshProjectBtn.waitForVisible(DEFAULT_TIMEOUT_MS).click();
    }

    public void clickSync() {
        syncBtn.click();
        WaitUtil.sleep(500, "Waiting for Sync dialog to open");
    }

    public boolean isSyncButtonVisible() {
        return syncBtn.isVisible(1000);
    }

    public String getSyncButtonTitle() {
        return syncBtn.getAttribute("title");
    }

    public void clickExport() {
        // Like Save, this toolbar button sits in the JSF layer that re-renders on its own while the project
        // recompiles, so a strict click can thrash — click it once the page has settled.
        exportBtn.clickWhenSettled();
        new ExportProjectDialogComponent().waitForDialogToAppear();
    }

    public List<String> getAllVisibleTopToolbarActions() {
        List<String> actions = new ArrayList<>();
        com.microsoft.playwright.Locator links = allTopToolbarLinks.getLocator();
        for (int i = 0; i < links.count(); i++) {
            com.microsoft.playwright.Locator link = links.nth(i);
            if (link.isVisible()) {
                String text = link.textContent().trim();
                if (!text.isEmpty()) {
                    actions.add(text);
                }
                String title = link.getAttribute("title");
                if (title != null && !title.isEmpty()) {
                    actions.add(title);
                }
            }
        }
        return actions;
    }

    // ========== Breadcrumbs (delegated) ==========

    public void navigateToProjectsInBreadcrumbs() {
        breadcrumbs.navigateToProjectsList();
    }

    public WebElement getBreadcrumbsAllProjects() {
        return breadcrumbs.getAllProjectsLink();
    }

    public void navigateToProjectRoot(String projectName) {
        breadcrumbs.navigateToProjectRoot(projectName);
    }

    public void switchBranch(String branchName) {
        breadcrumbs.switchBranch(branchName);
    }

    public void selectBranchInDropdown(String branchName) {
        breadcrumbs.selectBranchInDropdown(branchName);
    }

    public String getCurrentBranch() {
        return breadcrumbs.getCurrentBranch();
    }

    public void selectBreadcrumbModule(String projectName, String moduleName) {
        waitUntilSpinnerLoaded();
        String actualProject = breadcrumbs.getProjectName(5000);
        String actualModule = breadcrumbs.getModuleName(5000);

        if (actualProject.equals(projectName) && !actualModule.equals(moduleName)) {
            breadcrumbs.selectModuleInDropdown(moduleName);
        } else if (!actualProject.equals(projectName)) {
            navigateToProjectRoot(projectName);
            new EditorLeftProjectModuleSelectorComponent(new WebElement(page, "xpath=//div[@id='projects']")).selectModule(projectName, moduleName);
        }
        WaitUtil.waitForCondition(
                () -> moduleName.equals(getBreadcrumbsModuleName().trim()),
                10000,
                250,
                "Waiting for breadcrumb module to become " + moduleName);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        waitUntilSpinnerLoaded();
        new ProblemsPanelComponent(new WebElement(page, "xpath=//div[@id='bottom']"))
                .waitForCompilationToComplete(60000, 250);
        new EditorLeftRulesTreeComponent(new WebElement(page, "xpath=//div[@id='left']"))
                .waitForTreeFoldersToLoad();
    }

    public void selectProjectBreadcrumbs(String projectName) {
        breadcrumbs.selectProjectInDropdown(projectName);
    }

    public String getBreadcrumbsProjectName() {
        return breadcrumbs.getProjectName();
    }

    public String getBreadcrumbsModuleName() {
        return breadcrumbs.getModuleName();
    }

    public void clickBreadcrumbsCategory() {
        breadcrumbs.clickCategory();
    }

    public void checkBreadcrumbs(String category, String project, String module) {
        breadcrumbs.checkBreadcrumbs(category, project, module);
    }

    // ========== Run / Trace / Benchmark (delegated to the table toolbar) ==========

    public IRunMenu clickRun() {
        return tableToolbar.clickRun();
    }

    public ITraceMenu clickTrace() {
        return tableToolbar.clickTrace();
    }

    public ITraceWindow clickTraceExpectTraceWindow() {
        return tableToolbar.clickTraceExpectTraceWindow();
    }

    public void clickBenchmark() {
        tableToolbar.clickBenchmark();
    }

    public void clickRunDropdown() {
        tableToolbar.clickRunDropdown();
    }

    public void clickBenchmarkDropdown() {
        tableToolbar.clickBenchmarkDropdown();
    }

    public boolean isRunButtonVisible() {
        return tableToolbar.isRunButtonVisible();
    }

    public boolean isTraceButtonVisible() {
        return tableToolbar.isTraceButtonVisible();
    }

    public boolean isBenchmarkButtonVisible() {
        return tableToolbar.isBenchmarkButtonVisible();
    }

    public TraceMenuComponent setFactorTextField(String text) {
        factorTextField.fill(text);
        return new TraceMenuComponent(page);
    }

    // ========== Table actions (delegated to the table toolbar) ==========

    public WebElement getEditTableBtn() {
        return tableToolbar.getEditTableBtn();
    }

    public CopyTableDialogComponent clickCopy() {
        tableToolbar.getCopyTableBtn().click();
        return new CopyTableDialogComponent();
    }

    public void clickRemove() {
        tableToolbar.getRemoveBtn().click();
        WaitUtil.sleep(100, "Waiting for table removal action to complete");
    }

    public void copyTableAsNew(String newName, String description) {
        CopyTableDialogComponent copyDialog = clickCopy();
        copyDialog.selectCopyAs("New Table").setName(newName);
        if (description != null && !description.isEmpty()) {
            copyDialog.setSaveTo(description);
        }
        copyDialog.clickCopy();
    }

    public void copyTableAsNewVersion(String version) {
        CopyTableDialogComponent copyDialog = clickCopy();
        copyDialog.selectCopyAs("New Version").setVersion(version);
        copyDialog.clickCopy();
    }

    public void copyTableAsBusinessDimension(String propertyLabel, String propertyValue) {
        CopyTableDialogComponent copyDialog = clickCopy();
        copyDialog.selectCopyAs("New Business Dimension Version").setProperty(propertyLabel, propertyValue).clickCopy();
    }

    public void removeCurrentTable() {
        DriverPool.getPage().onDialog(Dialog::accept);
        clickRemove();
    }

    public void createDefaultTestTable() {
        // Create Test now opens the React Create Table modal with the tested table already filled in, so the
        // default test table is one press of Create away.
        tableToolbar.getCreateTestBtn().click();
        new CreateTableDialogComponent().waitForDialogToAppear().save();
        WaitUtil.sleep(500, "Waiting for created test table to open");
    }

    public void clickTableActionsTestBtn() {
        tableToolbar.clickTableActionsTestBtn();
    }

    public void clickTableActionsTestDropdown() {
        tableToolbar.clickTableActionsTestDropdown();
    }

    // ========== Target table and available test runs (delegated) ==========

    public String getTargetTableText() {
        return tableToolbar.getTargetTableText();
    }

    public boolean isTargetTableVisible() {
        return tableToolbar.isTargetTableVisible();
    }

    public void clickTargetTable() {
        tableToolbar.clickTargetTable();
    }

    public String getAvailableTestRunsLinkText() {
        return tableToolbar.getAvailableTestRunsLinkText();
    }

    public boolean isAvailableTestRunsLinkVisible() {
        return tableToolbar.isAvailableTestRunsLinkVisible();
    }

    public String getAvailableTestRunsInlineLinkText() {
        return tableToolbar.getAvailableTestRunsInlineLinkText();
    }

    public void clickAvailableTestRunsInlineLink() {
        tableToolbar.clickAvailableTestRunsInlineLink();
    }

    public boolean isAvailableTestRunsExpandLinkVisible() {
        return tableToolbar.isAvailableTestRunsExpandLinkVisible();
    }

    public void clickAvailableTestRunsExpandLink() {
        tableToolbar.clickAvailableTestRunsExpandLink();
    }

    public String getAvailableTestRunsPopupText() {
        return tableToolbar.getAvailableTestRunsPopupText();
    }

    // ========== Test menu (delegated) ==========

    public WebElement getTestDropdownBtn() {
        // Kept for tests that click the dropdown toggle directly.
        return new WebElement(page, "xpath=//a[@title='Run Tests']/following-sibling::span[1]", "testDropdownBtn");
    }

    public IRunTestsMenu clickTestDropdown() {
        runTestsMenu.openDropdown();
        return runTestsMenu;
    }

    public String getTestButtonText() {
        return runTestsMenu.getTestButtonText();
    }

    public boolean isTestButtonVisible() {
        return runTestsMenu.isTestButtonVisible();
    }

    public void clickTopPanelTestButton() {
        runTestsMenu.clickTestButton();
    }

    public void runAllTests() {
        runTestsMenu.runAllTests();
    }

    public void clickTopPanelTestDropdown() {
        runTestsMenu.openDropdownAndWaitForSettings();
    }

    public void clickTopPanelRunTestBtn() {
        runTestsMenu.clickRunTestsButton();
    }

    // ========== Within Current Module Only (delegated) ==========

    public boolean isWithinCurrentModuleOnlyInputArgsChecked() {
        return tableToolbar.isWithinCurrentModuleOnlyInputArgsChecked();
    }

    public boolean isWithinCurrentModuleOnlyInputArgsEnabled() {
        return tableToolbar.isWithinCurrentModuleOnlyInputArgsEnabled();
    }

    public boolean isWithinCurrentModuleOnlyTestTablesChecked() {
        return tableToolbar.isWithinCurrentModuleOnlyTestTablesChecked();
    }

    public boolean isWithinCurrentModuleOnlyTestTablesEnabled() {
        return tableToolbar.isWithinCurrentModuleOnlyTestTablesEnabled();
    }

    public boolean isTopPanelWithinCurrentModuleOnlyChecked() {
        return runTestsMenu.isWithinCurrentModuleOnlyChecked();
    }

    public boolean isTopPanelWithinCurrentModuleOnlyEnabled() {
        return runTestsMenu.isWithinCurrentModuleOnlyEnabled();
    }

    public void setTopPanelWithinCurrentModuleOnly(boolean value) {
        runTestsMenu.setWithinCurrentModuleOnly(value);
    }

    // ========== More menu (delegated) ==========

    public IMoreMenu clickMore() {
        return new MoreMenuComponent(page).open();
    }

    public List<String> getMoreMenuItems() {
        MoreMenuComponent moreMenu = new MoreMenuComponent(page);
        moreMenu.open();
        return moreMenu.getMenuItems();
    }
}
