package domain.ui.webstudio.components.editortabcomponents;

import com.microsoft.playwright.Dialog;
import com.microsoft.playwright.Page;
import configuration.core.ui.PlaywrightBasePage;
import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;
import helpers.utils.WaitUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class PlaywrightTableToolbarPanelComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement runBtn;
    private PlaywrightWebElement traceBtn;
    private PlaywrightWebElement benchmarkBtn;
    private PlaywrightWebElement exportBtn;
    @Getter
    private PlaywrightWebElement editBtn;
    private PlaywrightWebElement copyBtn;
    private PlaywrightWebElement removeBtn;
    private PlaywrightWebElement factorTextField;
    private PlaywrightWebElement traceDropdownBtn;
    
    // Run Menu elements
    private PlaywrightWebElement createItemBtn;
    private PlaywrightWebElement expandTypesBtn;
    private PlaywrightWebElement addElementToCollectionBtnTemplate;
    private PlaywrightWebElement runInsideDropdownBtn;
    private PlaywrightWebElement addedElementsExpanderTemplate;
    private PlaywrightWebElement selectTypeDropdown;
    
    // Trace Menu elements  
    private PlaywrightWebElement traceInsideMenuBtn;
    private PlaywrightWebElement traceIntoFileBtn;
    private PlaywrightWebElement factorTextFieldForTrace;
    private PlaywrightWebElement jsonRadioBtn;
    private PlaywrightWebElement jsonTextField;
    
    // Input parameter elements - from RunDropDown.java
    private PlaywrightWebElement inputTextFieldTemplate;
    private PlaywrightWebElement inputSelectFieldTemplate;

    public PlaywrightTableToolbarPanelComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightTableToolbarPanelComponent(PlaywrightWebElement rootLocator) {
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
        
        // Dropdown/Form elements - page-level (appear outside toolbar after clicks)
        createItemBtn = new PlaywrightWebElement(page, "xpath=//a[@title='Create']", "createItemBtn");
        expandTypesBtn = new PlaywrightWebElement(page, "xpath=//table[@class='table']//span[contains(@class, 'rf-trn-hnd-colps') and contains(@class, 'rf-trn-hnd')]", "expandTypesBtn");
        addElementToCollectionBtnTemplate = new PlaywrightWebElement(page, "xpath=//span[contains(text(), '%s')]//a[@title='Add new element to collection']", "addElementToCollectionBtnTemplate");
        runInsideDropdownBtn = new PlaywrightWebElement(page, "xpath=//input[@id='inputArgsForm:runButton']", "runDropdownBtn");
        addedElementsExpanderTemplate = new PlaywrightWebElement(page, "xpath=//span[./span[contains(text(), '%s')]/a[@title='Add new element to collection']]/preceding-sibling::span", "addedElementsExpanderTemplate");
        selectTypeDropdown = new PlaywrightWebElement(page, "xpath=//div[contains(@id, 'input')]//select", "selectTypeDropdown");
        
        // Trace Menu elements - page-level (form elements)
        traceInsideMenuBtn = new PlaywrightWebElement(page, "xpath=//input[@id='inputArgsForm:traceButton']", "traceInsideMenuBtn");
        traceIntoFileBtn = new PlaywrightWebElement(page, "xpath=//input[@id='inputArgsForm:traceIntoFileButton']", "traceIntoFileBtn");
        factorTextField = new PlaywrightWebElement(page, "xpath=//div[contains(@id, 'input')]//input[@type='text']", "factorTextField");
        factorTextFieldForTrace = new PlaywrightWebElement(page, "xpath=//span[text()='factor = ']/input", "factorTextFieldForTrace");
        jsonRadioBtn = new PlaywrightWebElement(page, "xpath=//input[@type='radio' and@value='TEXT']", "jsonRadioBtn");
        jsonTextField = new PlaywrightWebElement(page, "xpath=//textarea[contains(@id, 'jsonInput')]", "jsonTextField");
        
        // Input parameter templates - page-level (form inputs)  
        inputTextFieldTemplate = new PlaywrightWebElement(page, "xpath=(//div[contains(@id, 'input')]//input[@type='text'])[%s]", "inputTextFieldTemplate");
        inputSelectFieldTemplate = new PlaywrightWebElement(page, "xpath=(//div[contains(@id, 'input')]//select)[%s]", "inputSelectFieldTemplate");
    }

    public IPlaywrightRunMenu clickRun() {
        runBtn.click();
        return new PlaywrightRunMenu();
    }

    public IPlaywrightTraceMenu clickTrace() {
        traceBtn.click();
        return new PlaywrightTraceMenu();
    }

    public void clickBenchmark() {
        benchmarkBtn.click();
    }

    public void clickExport() {
        exportBtn.click();
    }
    
    public PlaywrightCopyTableDialogComponent clickCopy() {
        copyBtn.click();
        return new PlaywrightCopyTableDialogComponent();
    }
    
    public void clickRemove() {
        removeBtn.click();
    }

    public PlaywrightTraceMenu setFactorTextField(String text) {
        factorTextField.fill(text);
        return new PlaywrightTraceMenu();
    }

    // Interface for Playwright Run Menu
    public interface IPlaywrightRunMenu {
        IPlaywrightRunMenu clickCreateItem();
        IPlaywrightRunMenu clickAddElementToCollectionBtn(String containsText);
        IPlaywrightRunMenu clickExpandCollection();
        IPlaywrightRunMenu clickRunInsideMenu();
        IPlaywrightRunMenu clickAddedElementsExpander(String containsText);
        java.util.List<String> getAliasDropdownValues();
        // Input parameter methods from RunDropDown.java
        IPlaywrightRunMenu setInputTextField(String index, String value);
        IPlaywrightRunMenu setInputSelectField(String index, String value);
    }

    // Implementation for Playwright Run Menu
    public class PlaywrightRunMenu implements IPlaywrightRunMenu {
        
        @Override
        public IPlaywrightRunMenu clickCreateItem() {
            createItemBtn.click();
            return this;
        }

        @Override
        public IPlaywrightRunMenu clickAddElementToCollectionBtn(String containsText) {
            addElementToCollectionBtnTemplate.format(containsText).click();
            return this;
        }

        @Override
        public IPlaywrightRunMenu clickExpandCollection() {
            expandTypesBtn.click();
            return this;
        }

        @Override
        public IPlaywrightRunMenu clickRunInsideMenu() {
            runInsideDropdownBtn.click();
            WaitUtil.sleep(250);
            return this;
        }

        @Override
        public IPlaywrightRunMenu clickAddedElementsExpander(String containsText) {
            addedElementsExpanderTemplate.format(containsText).click();
            return this;
        }

        @Override
        public java.util.List<String> getAliasDropdownValues() {
            java.util.List<String> values = new java.util.ArrayList<>();
            String baseSelector = selectTypeDropdown.getSelector();
            // Remove xpath= prefix if present and add //option
            String optionsSelector = baseSelector.startsWith("xpath=") ? 
                baseSelector.substring(6) + "//option" : 
                "xpath=" + baseSelector + "//option";
            var options = page.locator(optionsSelector);
            int count = options.count();
            for (int i = 0; i < count; i++) {
                String value = options.nth(i).getAttribute("value");
                values.add(value != null ? value : "");
            }
            return values;
        }

        @Override
        public IPlaywrightRunMenu setInputTextField(String index, String value) {
            inputTextFieldTemplate.format(index).fill(value);
            return this;
        }

        @Override
        public IPlaywrightRunMenu setInputSelectField(String index, String value) {
            inputSelectFieldTemplate.format(index).selectByVisibleText(value);
            return this;
        }
    }

    // Interface for Playwright Trace Menu  
    public interface IPlaywrightTraceMenu {
        IPlaywrightTraceMenu setFactorTextField(String text);
        IPlaywrightTraceMenu selectJSONTrace(String json);
        IPlaywrightTraceMenu clickTraceIntoFile();
        IPlaywrightTraceWindow clickTraceInsideMenu();
        java.util.List<String> getAliasDropdownValues();
    }

    // Implementation for Playwright Trace Menu
    public class PlaywrightTraceMenu implements IPlaywrightTraceMenu {
        
        @Override
        public IPlaywrightTraceMenu setFactorTextField(String text) {
            if (factorTextFieldForTrace.isVisible()) {
                factorTextFieldForTrace.fill(text);
            }
            return this;
        }

        @Override
        public IPlaywrightTraceMenu selectJSONTrace(String json) {
            jsonRadioBtn.click();
            jsonTextField.fill(json);
            return this;
        }

        @Override
        public IPlaywrightTraceMenu clickTraceIntoFile() {
            traceIntoFileBtn.click();
            return this;
        }

        @Override
        public IPlaywrightTraceWindow clickTraceInsideMenu() {
            // Wait for popup to open after click
            Page popup = page.waitForPopup(() -> {
                traceInsideMenuBtn.click();
            });

            // Wait for popup to load and trace tree to be ready
            popup.waitForLoadState();
            popup.waitForSelector("xpath=//div[@id='tree']", new Page.WaitForSelectorOptions().setTimeout(1000));
            return new PlaywrightTraceWindow(popup);
        }

        @Override
        public List<String> getAliasDropdownValues() {
            List<String> values = new java.util.ArrayList<>();
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
    public interface IPlaywrightTraceWindow {
        IPlaywrightTraceWindow expandItemInTree(int position);
        List<String> getVisibleItemsFromTree();
    }

    // Implementation for Playwright Trace Window
    public class PlaywrightTraceWindow extends PlaywrightBasePage implements IPlaywrightTraceWindow {
        private PlaywrightWebElement traceTree;
        private PlaywrightWebElement traceExpanderTemplate;
        private PlaywrightWebElement traceItemsTemplate;
        private Page tracePage;

        public PlaywrightTraceWindow() {
            this(PlaywrightDriverPool.getPage());
        }

        public PlaywrightTraceWindow(Page tracePage) {
            this.tracePage = tracePage;
            // Initialize trace window elements based on actual HTML structure
            traceTree = new PlaywrightWebElement(tracePage, "xpath=//div[@id='tree']", "traceTree");
            traceExpanderTemplate = new PlaywrightWebElement(tracePage, "xpath=(//span[@class='fancytree-expander'])[%d]", "traceExpanderTemplate");
            traceItemsTemplate = new PlaywrightWebElement(tracePage, "xpath=//span[@class='fancytree-title']", "traceItemsTemplate");
        }

        @Override
        public IPlaywrightTraceWindow expandItemInTree(int position) {
            PlaywrightWebElement item = traceExpanderTemplate.format(position + 1);
            item.click();
            return this;
        }

        @Override
        public List<String> getVisibleItemsFromTree() {
            List<String> items = new ArrayList<>();
            var locators = tracePage.locator("xpath=//span[@class='fancytree-title']");
            int count = locators.count();
            for (int i = 0; i < count; i++) {
                String text = locators.nth(i).textContent();
                if (text != null && !text.trim().isEmpty()) {
                    items.add(text.trim());
                }
            }
            return items;
        }
    }


    public void copyTableAsNew(String newName, String description) {
        PlaywrightCopyTableDialogComponent copyDialog = clickCopy();
        copyDialog.selectCopyAs("New Table").setName(newName);
        if (description != null && !description.isEmpty()) {
            copyDialog.setSaveTo(description);
        }
        copyDialog.clickCopy();
    }

    public void removeCurrentTable() {
        PlaywrightDriverPool.getPage().onDialog(Dialog::accept);
        clickRemove();
    }
}