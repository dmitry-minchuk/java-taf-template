package domain.ui.webstudio.components.editortabcomponents;

import com.microsoft.playwright.Dialog;
import com.microsoft.playwright.Page;
import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.pages.BasePage;
import helpers.utils.WaitUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TableToolbarPanelComponent extends BaseComponent {

    private WebElement runBtn;
    private WebElement traceBtn;
    private WebElement benchmarkBtn;
    private WebElement exportBtn;
    @Getter
    private WebElement editBtn;
    private WebElement copyBtn;
    private WebElement removeBtn;
    private WebElement factorTextField;
    private WebElement traceDropdownBtn;

    // Run Tests Menu elements
    private WebElement testDropdownBtn;
    private WebElement testPerPageDropdown;
    private WebElement failuresOnlyCheckbox;
    private WebElement compoundResultCheckbox;
    private WebElement runTestsBtn;

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

    public TableToolbarPanelComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public TableToolbarPanelComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        // Top line toolbar
        exportBtn = createScopedElement("xpath=.//a[@class='toolbarButton' and @title='Export the table']", "exportBtn");

        // Toolbar elements - scoped to toolbar container
        runBtn = createScopedElement("xpath=.//img[contains(@src, 'run')]", "runBtn");
        traceBtn = createScopedElement("xpath=.//img[contains(@src, 'trace')]", "traceBtn");
        benchmarkBtn = createScopedElement("xpath=.//span[contains(text(), 'Benchmark')]", "benchmarkBtn");
        editBtn = createScopedElement("xpath=.//a[@class='toolbarButton' and ./img[contains(@src,'editTable')]]", "editBtn");
        copyBtn = createScopedElement("xpath=.//a[@class='toolbarButton' and ./img[contains(@src,'copyTable')]]", "copyBtn");
        removeBtn = createScopedElement("xpath=.//a[@class='toolbarButton' and ./span[@class='delete-icon']]", "removeBtn");
        traceDropdownBtn = createScopedElement("xpath=.//a[@id='traceLink']//td[@class='arrow']", "traceDropdownBtn");

        // Run Tests Menu elements initialization
        testDropdownBtn = new WebElement(page, "xpath=//a[@id='testButton']//td[@class='arrow']", "testDropdownBtn");
        testPerPageDropdown = new WebElement(page, "xpath=//select[@name='testPerPage']", "testPerPageDropdown");
        failuresOnlyCheckbox = new WebElement(page, "xpath=//input[@name='failuresOnly']", "failuresOnlyCheckbox");
        compoundResultCheckbox = new WebElement(page, "xpath=//input[@name='compoundResult']", "compoundResultCheckbox");
        runTestsBtn = new WebElement(page, "xpath=//input[@id='runTestButton']", "runTestsBtn");
        
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

    public IRunMenu clickRun() {
        runBtn.click();
        return new RunMenu();
    }

    public ITraceMenu clickTrace() {
        traceBtn.click();
        return new TraceMenu();
    }

    public void clickBenchmark() {
        benchmarkBtn.click();
    }

    public void clickExport() {
        exportBtn.click();
    }
    
    public CopyTableDialogComponent clickCopy() {
        copyBtn.click();
        return new CopyTableDialogComponent();
    }
    
    public void clickRemove() {
        removeBtn.click();
        WaitUtil.sleep(100);
    }

    public TraceMenu setFactorTextField(String text) {
        factorTextField.fill(text);
        return new TraceMenu();
    }

    public IRunTestsMenu clickTestDropdown() {
        testDropdownBtn.click();
        return new RunTestsMenu();
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
            WaitUtil.sleep(250);
            return this;
        }

        @Override
        public IRunMenu clickAddedElementsExpander(String containsText) {
            addedElementsExpanderTemplate.format(containsText).click();
            return this;
        }

        // TODO: refactor this!
        @Override
        public List<String> getAliasDropdownValues() {
            List<String> values = new ArrayList<>();
            String baseSelector = selectTypeDropdown.getSelector();
            // Remove xpath= prefix if present and add //option
            String optionsSelector = baseSelector.startsWith("xpath=") ? baseSelector.substring(6) + "//option" : "xpath=" + baseSelector + "//option";
            var options = page.locator(optionsSelector);
            int count = options.count();
            for (int i = 0; i < count; i++) {
                String value = options.nth(i).getAttribute("value");
                values.add(value != null ? value : "");
            }
            return values;
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

        // TODO: refactor this!
        @Override
        public List<String> getAliasDropdownValues() {
            List<String> values = new ArrayList<>();
            String baseSelector = selectTypeDropdown.getSelector();
            // Remove xpath= prefix if present and add //option
            String optionsSelector = baseSelector.startsWith("xpath=") ? baseSelector.substring(6) + "//option" : "xpath=" + baseSelector + "//option";
            var options = page.locator(optionsSelector);
            int count = options.count();
            for (int i = 0; i < count; i++) {
                String value = options.nth(i).getAttribute("value");
                values.add(value != null ? value : "");
            }
            return values;
        }
    }

    // Interface for Playwright Trace Window
    public interface ITraceWindow {
        ITraceWindow expandItemInTree(int position);
        List<String> getVisibleItemsFromTree();
    }

    // Implementation for Playwright Trace Window
    public class TraceWindow extends BasePage implements ITraceWindow {
        private WebElement traceExpanderTemplate;
        private List<WebElement> visibleItemsFromTree;

        public TraceWindow(Page tracePage) {
            super(tracePage);
            // Initialize trace window elements based on actual HTML structure
            traceExpanderTemplate = new WebElement(tracePage, "xpath=(//span[@class='fancytree-expander'])[%d]", "traceExpanderTemplate");
            visibleItemsFromTree = createElementList("xpath=//span[@class='fancytree-title']", "visibleItemsFromTree");
        }

        @Override
        public ITraceWindow expandItemInTree(int position) {
            WebElement item = traceExpanderTemplate.format(position + 1);
            item.click();
            WaitUtil.sleep(100); // Waiting for expander to get expanded
            return this;
        }

        @Override
        public List<String> getVisibleItemsFromTree() {
            return visibleItemsFromTree.stream().map(i -> i.getText().trim()).toList();
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