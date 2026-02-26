package domain.ui.webstudio.components.editortabcomponents;

import com.microsoft.playwright.Dialog;
import com.microsoft.playwright.Page;
import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.common.TableComponent;
import domain.ui.webstudio.pages.BasePage;
import helpers.utils.WaitUtil;
import lombok.Getter;
import org.testcontainers.containers.wait.strategy.Wait;

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
    private WebElement breadcrumbsDropdownItemTemplate;
    // Run Tests Menu elements
    @Getter
    private WebElement testDropdownBtn;
    private WebElement testPerPageDropdown;
    private WebElement failuresOnlyCheckbox;
    private WebElement compoundResultCheckbox;
    private WebElement runTestsBtn;
    // More Menu elements
    private WebElement changesBtn;
    private WebElement revisionsBtn;

    // SECOND LINE TOOLBAR
    private WebElement runBtn;
    private WebElement traceBtn;
    private WebElement benchmarkBtn;
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
        breadcrumbsDropdownItemTemplate = new WebElement(page, "xpath=//span[@class='dropdown open']/ul[contains(@class, 'dropdown-menu')]//li//a[contains(text(), '%s')]", "breadcrumbsDropdownItemTemplate");
        // Run Tests Menu elements initialization
        testDropdownBtn = new WebElement(page, "xpath=//a[@title='Run Tests']/following-sibling::span[1]", "testDropdownBtn");
        testPerPageDropdown = new WebElement(page, "xpath=//select[@name='pp']", "testPerPageDropdown");
        failuresOnlyCheckbox = new WebElement(page, "xpath=//input[@name='failuresOnly']", "failuresOnlyCheckbox");
        compoundResultCheckbox = new WebElement(page, "xpath=//input[@name='complexResult']", "compoundResultCheckbox");
        runTestsBtn = new WebElement(page, "xpath=//a[@class='button' and text()='Test']", "runTestsBtn");
        // More Menu elements initialization
        changesBtn = new WebElement(page, "xpath=//*[@id='topRevertLink']", "changesBtn");
        revisionsBtn = new WebElement(page, "xpath=//a[@title='Show project revisions']", "revisionsBtn");

        // SECOND LINE TOOLBAR
        // Toolbar elements - scoped to toolbar container
        runBtn = createScopedElement("xpath=.//img[contains(@src, 'run')]", "runBtn");
        traceBtn = createScopedElement("xpath=.//img[contains(@src, 'trace')]", "traceBtn");
        benchmarkBtn = createScopedElement("xpath=.//span[contains(text(), 'Benchmark')]", "benchmarkBtn");
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
        WaitUtil.sleep(250, "Waiting for branch dropdown to open");
        breadcrumbsDropdownItemTemplate.format(branchName).click();
        WaitUtil.sleep(1000, "Waiting for branch switch to complete");
    }

    public String getCurrentBranch() {
        return breadcrumbsModuleBranch.getText();
    }

    public IRunMenu clickRun() {
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
            // Wait for popup to open after click
            Page popup = page.waitForPopup(() -> {
                traceInsideMenuBtn.click();
            });

            // Wait for popup to load and trace tree to be ready
            popup.waitForLoadState();
            popup.waitForSelector("xpath=//div[@id='tree']", new Page.WaitForSelectorOptions().setTimeout(1000));
            return new TraceWindow(popup);
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