package domain.ui.webstudio.components.editortabcomponents;

import com.microsoft.playwright.Dialog;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.common.TableComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftProjectModuleSelectorComponent;
import domain.ui.webstudio.pages.BasePage;
import helpers.utils.WaitUtil;
import lombok.Getter;

import java.util.List;

public class EditorToolbarPanelComponent extends BaseComponent {

    // TOP LINE TOOLBAR
    private WebElement exportBtn;
    private WebElement saveBtn;
    private WebElement verifyBtn;
    private WebElement copyProjectBtn;
    private WebElement createTableBtn;
    private WebElement moreBtn;
    private WebElement syncBtn;
    @Getter
    private WebElement breadcrumbsAllProjects;
    private WebElement breadcrumbsProjectToggle;
    private WebElement breadcrumbsModuleBranch;
    private WebElement breadcrumbsModuleToggle;
    private WebElement breadcrumbsDropdownItemTemplate;
    private WebElement breadcrumbsCategoryLink;
    // Run Tests Menu elements
    private WebElement testPerPageDropdown;
    private WebElement failuresOnlyCheckbox;
    private WebElement compoundResultCheckbox;
    private WebElement runTestsBtn;
    @Getter
    private WebElement testDropdownBtn;
    private WebElement topPanelTestBtn;
    private WebElement topPanelRunTestBtn;
    // More Menu elements
    private WebElement changesBtn;
    private WebElement revisionsBtn;
    // Within Current Module Only checkboxes
    private WebElement withinCurrentModuleOnlyInputArgs;
    private WebElement withinCurrentModuleOnlyTestTables;
    private WebElement topPanelWithinCurrentModuleOnly;

    // SECOND LINE TOOLBAR
    private WebElement runBtn;
    private WebElement runDropdownBtn;
    private WebElement traceBtn;
    private WebElement benchmarkBtn;
    private WebElement benchmarkDropdownBtn;
    private WebElement targetTableLink;
    private WebElement availableTestRunsLink;
    private WebElement tableActionsTestBtn;
    private WebElement tableActionsTestDropdownBtn;
    @Getter
    private WebElement editTableBtn;
    private WebElement copyTableBtn;
    private WebElement removeBtn;
    private WebElement factorTextField;
    private WebElement traceDropdownBtn;
    // Run Menu elements
    private WebElement createItemBtn;
    private WebElement expandTypesBtn;
    private WebElement addElementToCollectionBtnTemplate;
    private WebElement runInsideDropdownBtn;
    private WebElement addedElementsExpanderTemplate;
    private WebElement selectTypeDropdown;
    // Trace Menu elements  
    private WebElement traceInsideMenuBtn;
    private WebElement traceIntoFileBtn;
    private WebElement factorTextFieldForTrace;
    private WebElement jsonRadioBtn;
    private WebElement jsonTextField;
    // Input parameter elements - from RunDropDown.java
    private WebElement inputTextFieldTemplate;
    private WebElement inputSelectFieldTemplate;

    public EditorToolbarPanelComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public EditorToolbarPanelComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        // TOP LINE TOOLBAR
        exportBtn = new WebElement(page, "xpath=//a[@id='exportProjectButton']", "exportBtn");
        verifyBtn = new WebElement(page,"//a[@id='verifyButton']", "verifyBtn");
        saveBtn = new WebElement(page,"//a[@id='saveProjectButton']", "saveBtn");
        copyProjectBtn = new WebElement(page, "xpath=//a[@id='copyProjectButton']", "copyProjectBtn");
        createTableBtn = new WebElement(page, "xpath=//a[@title='Create new table']", "createTableBtn");
        moreBtn = new WebElement(page, "xpath=//form[@id='headerForm']//span/*[contains(text(), 'More')]", "moreBtn");
        syncBtn = new WebElement(page, "xpath=//div//a[text()='Sync']", "syncBtn");
        breadcrumbsAllProjects = new WebElement(page, "//div[@class='breadcrumbs']/a[@href='/']", "breadcrumbsAllProjects");
        breadcrumbsProjectToggle = new WebElement(page, "xpath=//div[@class='breadcrumbs']/span[@id='breadcrumbs-project']/a", "breadcrumbsProjectToggle");
        breadcrumbsModuleBranch = new WebElement(page, "xpath=//div[@class='breadcrumbs']/span[@id='breadcrumbs-module']/a[starts-with(@title, 'Branch:')]", "breadcrumbsModuleBranch");
        breadcrumbsModuleToggle = new WebElement(page, "xpath=//div[@class='breadcrumbs']/span[@id='breadcrumbs-module']/a[not(contains(@title, 'Branch'))]", "breadcrumbsModuleToggle");
        breadcrumbsDropdownItemTemplate = new WebElement(page, "xpath=//span[@class='dropdown open']/ul[contains(@class, 'dropdown-menu')]//li//a[contains(text(), '%s')]", "breadcrumbsDropdownItemTemplate");
        breadcrumbsCategoryLink = new WebElement(page, "xpath=//div[@class='breadcrumbs']/a[@class='changes-listener-added']", "breadcrumbsCategoryLink");
        // Run Tests Menu elements initialization
        testDropdownBtn = new WebElement(page, "xpath=//a[@title='Run Tests']/following-sibling::span[1]", "testDropdownBtn");
        testPerPageDropdown = new WebElement(page, "xpath=//select[@name='pp']", "testPerPageDropdown");
        failuresOnlyCheckbox = new WebElement(page, "xpath=//input[@name='failuresOnly']", "failuresOnlyCheckbox");
        compoundResultCheckbox = new WebElement(page, "xpath=//input[@name='complexResult']", "compoundResultCheckbox");
        runTestsBtn = new WebElement(page, "xpath=//a[@class='button' and text()='Test']", "runTestsBtn");
        topPanelTestBtn = new WebElement(page, "xpath=//div[@id='testPanel']//a[@title='Run Tests']", "topPanelTestBtn");
        topPanelRunTestBtn = new WebElement(page, "xpath=//ul[@id='testSettings']//a[contains(@class,'button') and text()='Test']", "topPanelRunTestBtn");
        // More Menu elements initialization
        changesBtn = new WebElement(page, "xpath=//*[@id='topRevertLink']", "changesBtn");
        revisionsBtn = new WebElement(page, "xpath=//a[@title='Show project revisions']", "revisionsBtn");
        // Within Current Module Only checkboxes
        withinCurrentModuleOnlyInputArgs = new WebElement(page, "xpath=//input[@id='runTestModuleOnlyInputArgs']", "withinCurrentModuleOnlyInputArgs");
        withinCurrentModuleOnlyTestTables = new WebElement(page, "xpath=//input[@id='runTestModuleOnly']", "withinCurrentModuleOnlyTestTables");
        topPanelWithinCurrentModuleOnly = new WebElement(page, "xpath=//input[@id='testModuleOnlyField']", "topPanelWithinCurrentModuleOnly");
        // Target Table and Test Runs
        targetTableLink = new WebElement(page, "xpath=//section[@id='targetTablesSection']//a", "targetTableLink");
        availableTestRunsLink = new WebElement(page, "xpath=//section[@id='testsSection']", "availableTestRunsLink");
        tableActionsTestBtn = new WebElement(page, "xpath=//div[@id='tableToolbarPanel']//span[text()='Test']", "tableActionsTestBtn");
        tableActionsTestDropdownBtn = new WebElement(page, "xpath=//div[@id='tableToolbarPanel']//a[.//span[text()='Test']]//td[@class='arrow']", "tableActionsTestDropdownBtn");

        // SECOND LINE TOOLBAR
        // Toolbar elements - scoped to toolbar container
        runBtn = createScopedElement("xpath=.//img[contains(@src, 'run')]", "runBtn");
        runDropdownBtn = new WebElement(page, "xpath=//div[@id='tableToolbarPanel']//a[@id='runLink']//td[@class='arrow']", "runDropdownBtn");
        traceBtn = createScopedElement("xpath=.//img[contains(@src, 'trace')]", "traceBtn");
        benchmarkBtn = createScopedElement("xpath=.//span[contains(text(), 'Benchmark')]", "benchmarkBtn");
        benchmarkDropdownBtn = new WebElement(page, "xpath=//div[@id='tableToolbarPanel']//a[@id='benchmarkLink']//td[@class='arrow']", "benchmarkDropdownBtn");
        editTableBtn = createScopedElement("xpath=.//a[@class='toolbarButton' and ./img[contains(@src,'editTable')]]", "editBtn");
        copyTableBtn = createScopedElement("xpath=.//a[@class='toolbarButton' and ./img[contains(@src,'copyTable')]]", "copyBtn");
        removeBtn = createScopedElement("xpath=.//a[@class='toolbarButton' and ./span[@class='delete-icon']]", "removeBtn");
        traceDropdownBtn = createScopedElement("xpath=.//a[@id='traceLink']//td[@class='arrow']", "traceDropdownBtn");
        // Dropdown/Form elements - page-level (appear outside toolbar after clicks)
        createItemBtn = new WebElement(page, "xpath=//a[@title='Create']", "createItemBtn");
        expandTypesBtn = new WebElement(page, "xpath=//table[@class='table']//span[contains(@class, 'rf-trn-hnd-colps') and contains(@class, 'rf-trn-hnd')]", "expandTypesBtn");
        addElementToCollectionBtnTemplate = new WebElement(page, "xpath=//span[contains(text(), '%s')]//a[@title='Add new element to collection']", "addElementToCollectionBtnTemplate");
        runInsideDropdownBtn = new WebElement(page, "xpath=//input[@id='inputArgsForm:runButton']", "runDropdownBtn");
        addedElementsExpanderTemplate = new WebElement(page, "xpath=//span[./span[contains(text(), '%s')]/a[@title='Add new element to collection']]/preceding-sibling::span", "addedElementsExpanderTemplate");
        selectTypeDropdown = new WebElement(page, "xpath=//div[contains(@id, 'input')]//select", "selectTypeDropdown");
        // Trace Menu elements - page-level (form elements)
        traceInsideMenuBtn = new WebElement(page, "xpath=//input[@id='inputArgsForm:traceButton']", "traceInsideMenuBtn");
        traceIntoFileBtn = new WebElement(page, "xpath=//input[@id='inputArgsForm:traceIntoFileButton']", "traceIntoFileBtn");
        factorTextField = new WebElement(page, "xpath=//div[contains(@id, 'input')]//input[@type='text']", "factorTextField");
        factorTextFieldForTrace = new WebElement(page, "xpath=//span[text()='factor = ']/input", "factorTextFieldForTrace");
        jsonRadioBtn = new WebElement(page, "xpath=//input[@type='radio' and@value='TEXT']", "jsonRadioBtn");
        jsonTextField = new WebElement(page, "xpath=//textarea[contains(@id, 'jsonInput')]", "jsonTextField");
        // Input parameter templates - page-level (form inputs)  
        inputTextFieldTemplate = new WebElement(page, "xpath=(//div[contains(@id, 'input')]//input[@type='text'])[%s]", "inputTextFieldTemplate");
        inputSelectFieldTemplate = new WebElement(page, "xpath=(//div[contains(@id, 'input')]//select)[%s]", "inputSelectFieldTemplate");
    }

    public void clickVerify() {
        verifyBtn.click();
    }

    public boolean isVerifyButtonPresent() {
        return verifyBtn.isVisible(2000);
    }

    public void clickCopyProjectBtn() {
        copyProjectBtn.click();
    }

    public void clickCreateTable() {
        createTableBtn.click();
    }

    public void clickSave() {
        saveBtn.click();
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

    public void navigateToProjectsInBreadcrumbs() {
        if (breadcrumbsAllProjects.isVisible(1000)) {
            LOGGER.info("Navigating back to projects list via breadcrumb...");
            breadcrumbsAllProjects.click();
            WaitUtil.sleep(500, "Waiting for projects list to load");
        }
    }

    public void navigateToProjectRoot(String projectName) {
        WaitUtil.retryOnException(() -> {
            breadcrumbsProjectToggle.click();
            WaitUtil.sleep(250, "Waiting for project dropdown to open");
            breadcrumbsDropdownItemTemplate.format(projectName).click(100);
            return true;
        }, 5000, 500, "Trying to navigate to project root" + projectName);
        WaitUtil.sleep(500, "Waiting for project view to load");
    }

    public void switchBranch(String branchName) {
        breadcrumbsModuleBranch.click();
        breadcrumbsDropdownItemTemplate.format(branchName).waitForVisible();
        breadcrumbsDropdownItemTemplate.format(branchName).click();
        WaitUtil.sleep(1000, "Waiting for branch switch to complete");
    }

    public String getCurrentBranch() {
        return breadcrumbsModuleBranch.getText();
    }

    public IRunMenu clickRun() {
        runBtn.waitForVisible();
        runBtn.click();
        return new RunMenu();
    }

    public ITraceMenu clickTrace() {
        traceBtn.click();
        return new TraceMenu();
    }

    public ITraceWindow clickTraceExpectTraceWindow() {
        // Wait for popup to open after click
        Page popup = page.waitForPopup(() -> {
            traceBtn.click();
        });

        // Wait for popup to load and trace tree to be ready
        popup.waitForLoadState();
        popup.waitForSelector("xpath=//div[@id='tree']", new Page.WaitForSelectorOptions().setTimeout(1000));
        return new TraceWindow(popup);
    }

    public void clickBenchmark() {
        benchmarkBtn.click();
    }

    public void clickExport() {
        exportBtn.click();
        WaitUtil.sleep(500, "Waiting for export dialog to open");
    }
    
    public CopyTableDialogComponent clickCopy() {
        copyTableBtn.click();
        return new CopyTableDialogComponent();
    }
    
    public void clickRemove() {
        removeBtn.click();
        WaitUtil.sleep(100, "Waiting for table removal action to complete");
    }

    public TraceMenu setFactorTextField(String text) {
        factorTextField.fill(text);
        return new TraceMenu();
    }

    public IRunTestsMenu clickTestDropdown() {
        testDropdownBtn.click();
        return new RunTestsMenu();
    }

    public IMoreMenu clickMore() {
        WaitUtil.sleep(1000, "Waiting before clicking More dropdown");
        moreBtn.click();
        WaitUtil.sleep(500, "Waiting for More dropdown to open");
        return new MoreMenu();
    }

    // Interface for Playwright Run Menu
    public interface IRunMenu {
        IRunMenu clickCreateItem();
        IRunMenu clickAddElementToCollectionBtn(String containsText);
        IRunMenu clickExpandCollection();
        IRunMenu clickRunInsideMenu();
        IRunMenu clickAddedElementsExpander(String containsText);
        List<String> getAliasDropdownValues();
        // Input parameter methods from RunDropDown.java
        IRunMenu setInputTextField(String index, String value);
        IRunMenu setInputSelectField(String index, String value);
    }

    // Interface for Run Tests Menu
    public interface IRunTestsMenu {
        IRunTestsMenu setTestPerPage(String testsPerPage);
        IRunTestsMenu setFailuresOnly(boolean failuresOnly);
        IRunTestsMenu setCompoundResult(boolean compoundResult);
        void runTests();
        String getTestPerPage();
        boolean isFailuresOnlyChecked();
        boolean isCompoundResultChecked();
    }

    // Interface for More Menu
    public interface IMoreMenu {
        ChangesDialogComponent clickChanges();
        void clickRevisions();
    }

    // Implementation for Playwright Run Menu
    public class RunMenu implements IRunMenu {
        
        @Override
        public IRunMenu clickCreateItem() {
            createItemBtn.click();
            return this;
        }

        @Override
        public IRunMenu clickAddElementToCollectionBtn(String containsText) {
            addElementToCollectionBtnTemplate.format(containsText).click();
            return this;
        }

        @Override
        public IRunMenu clickExpandCollection() {
            expandTypesBtn.click();
            return this;
        }

        @Override
        public IRunMenu clickRunInsideMenu() {
            runInsideDropdownBtn.click();
            WaitUtil.sleep(250, "Waiting for run menu action to complete");
            return this;
        }

        @Override
        public IRunMenu clickAddedElementsExpander(String containsText) {
            addedElementsExpanderTemplate.format(containsText).click();
            return this;
        }

        @Override
        public List<String> getAliasDropdownValues() {
            return selectTypeDropdown.getSelectValues();
        }

        @Override
        public IRunMenu setInputTextField(String index, String value) {
            inputTextFieldTemplate.format(index).fill(value);
            return this;
        }

        @Override
        public IRunMenu setInputSelectField(String index, String value) {
            inputSelectFieldTemplate.format(index).selectByVisibleText(value);
            return this;
        }
    }

    // Implementation for Run Tests Menu
    public class RunTestsMenu implements IRunTestsMenu {

        @Override
        public IRunTestsMenu setTestPerPage(String testsPerPage) {
            if (testsPerPage != null && !testsPerPage.isEmpty() && !testsPerPage.equals("empty")) {
                testPerPageDropdown.selectByVisibleText(testsPerPage);
            }
            return this;
        }

        @Override
        public IRunTestsMenu setFailuresOnly(boolean failuresOnly) {
            if (failuresOnly != failuresOnlyCheckbox.isChecked()) {
                failuresOnlyCheckbox.click();
            }
            return this;
        }

        @Override
        public IRunTestsMenu setCompoundResult(boolean compoundResult) {
            if (compoundResult != compoundResultCheckbox.isChecked()) {
                compoundResultCheckbox.click();
            }
            return this;
        }

        @Override
        public void runTests() {
            runTestsBtn.click();
        }

        @Override
        public String getTestPerPage() {
            return testPerPageDropdown.getLocator().inputValue();
        }

        @Override
        public boolean isFailuresOnlyChecked() {
            return failuresOnlyCheckbox.isChecked();
        }

        @Override
        public boolean isCompoundResultChecked() {
            return compoundResultCheckbox.isChecked();
        }
    }

    // Implementation for More Menu
    public class MoreMenu implements IMoreMenu {

        @Override
        public ChangesDialogComponent clickChanges() {
            changesBtn.click();
            WaitUtil.sleep(1500, "Waiting for Changes dialog to open");
            return new ChangesDialogComponent();
        }

        @Override
        public void clickRevisions() {
            revisionsBtn.click();
            WaitUtil.sleep(500, "Waiting for Revisions dialog to open");
        }
    }

    // Interface for Playwright Trace Menu  
    public interface ITraceMenu {
        ITraceMenu setFactorTextField(String text);
        ITraceMenu selectJSONTrace(String json);
        ITraceMenu clickTraceIntoFile();
        ITraceWindow clickTraceInsideMenu();
        ITraceWindow clickTraceInsideMenu(boolean isPopupExpected);
        List<String> getAliasDropdownValues();
    }

    // Implementation for Playwright Trace Menu
    public class TraceMenu implements ITraceMenu {

        @Override
        public ITraceMenu setFactorTextField(String text) {
            if (factorTextFieldForTrace.isVisible()) {
                factorTextFieldForTrace.fill(text);
            }
            return this;
        }

        @Override
        public ITraceMenu selectJSONTrace(String json) {
            jsonRadioBtn.click();
            jsonTextField.fill(json);
            return this;
        }

        @Override
        public ITraceMenu clickTraceIntoFile() {
            traceIntoFileBtn.click();
            return this;
        }

        @Override
        public ITraceWindow clickTraceInsideMenu() {
            return clickTraceInsideMenu(true);
        }

        @Override
        public ITraceWindow clickTraceInsideMenu(boolean isPopupExpected) {
            if (isPopupExpected) {
                Page popup = page.waitForPopup(() -> {
                    traceInsideMenuBtn.waitForVisible();
                    traceInsideMenuBtn.click();
                });
                popup.waitForLoadState();
                popup.waitForSelector("xpath=//div[@id='tree']", new Page.WaitForSelectorOptions().setTimeout(1000));
                return new TraceWindow(popup);
            } else {
                traceInsideMenuBtn.waitForVisible();
                traceInsideMenuBtn.click();
                return null;
            }
        }

        @Override
        public List<String> getAliasDropdownValues() {
            return selectTypeDropdown.getSelectValues();
        }
    }

    // Interface for Playwright Trace Window
    public interface ITraceWindow {
        ITraceWindow expandItemInTree(int position);
        List<String> getVisibleItemsFromTree();
        TableComponent getCenterTable();
        void close();
    }

    // Implementation for Playwright Trace Window
    public class TraceWindow extends BasePage implements ITraceWindow {
        private WebElement traceExpanderTemplate;
        private List<WebElement> visibleItemsFromTree;
        private TableComponent centerTable;

        public TraceWindow(Page tracePage) {
            super(tracePage);
            // Initialize trace window elements based on actual HTML structure
            traceExpanderTemplate = new WebElement(tracePage, "xpath=(//span[@class='fancytree-expander'])[%d]", "traceExpanderTemplate");
            visibleItemsFromTree = createElementList("xpath=//span[@class='fancytree-title']", "visibleItemsFromTree");
            centerTable = createScopedComponent(TableComponent.class, "xpath=//table[@class='te_table']", "centerTable");
        }

        @Override
        public ITraceWindow expandItemInTree(int position) {
            WebElement item = traceExpanderTemplate.format(position + 1);
            item.click();
            WaitUtil.sleep(100, "Waiting for trace tree item " + position + " to expand");
            return this;
        }

        @Override
        public List<String> getVisibleItemsFromTree() {
            WaitUtil.sleep(500, "Waiting for trace tree items to be fully rendered");
            return visibleItemsFromTree.stream().map(i -> i.getText().trim()).toList();
        }

        @Override
        public TableComponent getCenterTable() {
            return centerTable;
        }

        @Override
        public void close() {
            if (getPage() != null && !getPage().isClosed()) {
                LOGGER.debug("Closing trace popup window");
                getPage().close();
            }
        }
    }


    // ========== Breadcrumb Module Navigation ==========

    public void selectBreadcrumbModule(String projectName, String moduleName) {
        String actualProject = breadcrumbsProjectToggle.isVisible(1000) ? breadcrumbsProjectToggle.getText() : "";
        String actualModule = breadcrumbsModuleToggle.isVisible(1000) ? breadcrumbsModuleToggle.getText() : "";

        if (actualProject.equals(projectName) && !actualModule.equals(moduleName)) {
            WaitUtil.retryOnException(() -> {
                breadcrumbsModuleToggle.click();
                breadcrumbsDropdownItemTemplate.format(moduleName).waitForVisible();
                breadcrumbsDropdownItemTemplate.format(moduleName).click(100);
                return true;
            }, 5000, 500, "Selecting module " + moduleName + " from breadcrumb");
        } else if (!actualProject.equals(projectName)) {
            navigateToProjectRoot(projectName);
            new EditorLeftProjectModuleSelectorComponent(new WebElement(page, "xpath=//div[@id='projects']")).selectModule(projectName, moduleName);
        }
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    public void selectProjectBreadcrumbs(String projectName) {
        WaitUtil.retryOnException(() -> {
            breadcrumbsProjectToggle.click();
            WaitUtil.sleep(250, "Waiting for project dropdown to open");
            breadcrumbsDropdownItemTemplate.format(projectName).click(100);
            return true;
        }, 5000, 500, "Selecting project " + projectName + " from breadcrumb");
        WaitUtil.sleep(500, "Waiting for project view to load");
    }

    public String getBreadcrumbsProjectName() {
        return breadcrumbsProjectToggle.isVisible(1000) ? breadcrumbsProjectToggle.getText() : "";
    }

    public String getBreadcrumbsModuleName() {
        return breadcrumbsModuleToggle.isVisible(1000) ? breadcrumbsModuleToggle.getText() : "";
    }

    public void clickBreadcrumbsCategory() {
        breadcrumbsCategoryLink.click();
    }

    public void checkBreadcrumbs(String category, String project, String module) {
        if (!category.isEmpty()) {
            WaitUtil.waitForCondition(() -> breadcrumbsCategoryLink.isVisible(500)
                            && breadcrumbsCategoryLink.getText().contains(category),
                    5000, 250, "Waiting for breadcrumb category to be: " + category);
        }
        if (!project.isEmpty()) {
            WaitUtil.waitForCondition(() -> breadcrumbsProjectToggle.isVisible(500)
                            && breadcrumbsProjectToggle.getText().equals(project),
                    5000, 250, "Waiting for breadcrumb project to be: " + project);
        }
        if (!module.isEmpty()) {
            WaitUtil.waitForCondition(() -> breadcrumbsModuleToggle.isVisible(500)
                            && breadcrumbsModuleToggle.getText().equals(module),
                    5000, 250, "Waiting for breadcrumb module to be: " + module);
        }
    }

    // ========== Top Panel Test Button ==========

    public String getTestButtonText() {
        if (topPanelTestBtn.isVisible(2000)) {
            return topPanelTestBtn.getText().trim();
        }
        return "";
    }

    public boolean isTestButtonVisible() {
        return topPanelTestBtn.isVisible(2000);
    }

    public void clickTopPanelTestButton() {
        topPanelTestBtn.click();
    }

    public void runAllTests() {
        topPanelTestBtn.waitForVisible();
        topPanelTestBtn.click();
    }

    public void clickTopPanelTestDropdown() {
        testDropdownBtn.click();
    }

    public void clickTopPanelRunTestBtn() {
        topPanelRunTestBtn.waitForVisible();
        topPanelRunTestBtn.click();
    }

    // ========== Within Current Module Only ==========

    public boolean isWithinCurrentModuleOnlyInputArgsChecked() {
        return withinCurrentModuleOnlyInputArgs.isChecked();
    }

    public boolean isWithinCurrentModuleOnlyInputArgsEnabled() {
        return withinCurrentModuleOnlyInputArgs.isEnabled();
    }

    public boolean isWithinCurrentModuleOnlyTestTablesChecked() {
        return withinCurrentModuleOnlyTestTables.isChecked();
    }

    public boolean isWithinCurrentModuleOnlyTestTablesEnabled() {
        return withinCurrentModuleOnlyTestTables.isEnabled();
    }

    public boolean isTopPanelWithinCurrentModuleOnlyChecked() {
        return topPanelWithinCurrentModuleOnly.isChecked();
    }

    public boolean isTopPanelWithinCurrentModuleOnlyEnabled() {
        return topPanelWithinCurrentModuleOnly.isEnabled();
    }

    public void setTopPanelWithinCurrentModuleOnly(boolean value) {
        topPanelWithinCurrentModuleOnly.waitForVisible();
        if (value != topPanelWithinCurrentModuleOnly.isChecked()) {
            topPanelWithinCurrentModuleOnly.click();
        }
    }

    // ========== Run/Trace/Benchmark Dropdown Arrows ==========

    public void clickRunDropdown() {
        runBtn.waitForVisible();
        runBtn.hover();
        runDropdownBtn.click();
    }

    public void clickBenchmarkDropdown() {
        benchmarkBtn.waitForVisible();
        benchmarkBtn.hover();
        benchmarkDropdownBtn.click();
    }

    // ========== Run/Trace/Benchmark Visibility ==========

    public boolean isRunButtonVisible() {
        return runBtn.isVisible(1000);
    }

    public boolean isTraceButtonVisible() {
        return traceBtn.isVisible(1000);
    }

    public boolean isBenchmarkButtonVisible() {
        return benchmarkBtn.isVisible(1000);
    }

    // ========== Target Table and Test Runs ==========

    public String getTargetTableText() {
        if (targetTableLink.isVisible(2000)) {
            return targetTableLink.getText().trim();
        }
        return "";
    }

    public boolean isTargetTableVisible() {
        return targetTableLink.isVisible(2000);
    }

    public void clickTargetTable() {
        targetTableLink.click();
        WaitUtil.sleep(500, "Waiting for target table navigation");
    }

    public String getAvailableTestRunsLinkText() {
        if (availableTestRunsLink.isVisible(2000)) {
            return availableTestRunsLink.getText().trim();
        }
        return "";
    }

    public boolean isAvailableTestRunsLinkVisible() {
        return availableTestRunsLink.isVisible(1000);
    }

    // ========== Table Actions Test Button ==========

    public void clickTableActionsTestBtn() {
        tableActionsTestBtn.click();
    }

    public void clickTableActionsTestDropdown() {
        tableActionsTestBtn.waitForVisible();
        tableActionsTestBtn.hover();
        tableActionsTestDropdownBtn.click();
    }

    public void copyTableAsNew(String newName, String description) {
        CopyTableDialogComponent copyDialog = clickCopy();
        copyDialog.selectCopyAs("New Table").setName(newName);
        if (description != null && !description.isEmpty()) {
            copyDialog.setSaveTo(description);
        }
        copyDialog.clickCopy();
    }

    public void removeCurrentTable() {
        LocalDriverPool.getPage().onDialog(Dialog::accept);
        clickRemove();
    }
}