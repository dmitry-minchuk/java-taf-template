package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;
import lombok.Getter;
import java.util.List;
import java.util.ArrayList;

@Getter

public class PlaywrightTableToolbarPanelComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement runBtn;
    private PlaywrightWebElement traceBtn;
    private PlaywrightWebElement benchmarkBtn;
    private PlaywrightWebElement exportBtn;
    private PlaywrightWebElement editBtn;
    private PlaywrightWebElement copyBtn;
    private PlaywrightWebElement removeBtn;
    private PlaywrightWebElement factorTextField;
    private PlaywrightWebElement traceDropdownBtn;
    
    // Run Menu elements
    private PlaywrightWebElement createItemBtn;
    private PlaywrightWebElement expandTypesBtn;
    private PlaywrightWebElement addElementToCollectionBtnTemplate;
    private PlaywrightWebElement runDropdownBtn;
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
        // Toolbar elements - scoped to toolbar container
        runBtn = createScopedElement("xpath=.//img[contains(@src, 'run')]", "runBtn");
        traceBtn = createScopedElement("xpath=.//img[contains(@src, 'trace')]", "traceBtn");
        benchmarkBtn = createScopedElement("xpath=.//span[contains(text(), 'Benchmark')]", "benchmarkBtn");
        exportBtn = createScopedElement("xpath=.//a[@class='toolbarButton' and @title='Export the table']", "exportBtn");
        editBtn = createScopedElement("xpath=.//a[@class='toolbarButton' and @title='Edit the table']", "editBtn");
        copyBtn = createScopedElement("xpath=.//a[@class='toolbarButton' and @title='Copy the table']", "copyBtn");
        removeBtn = createScopedElement("xpath=.//a[@class='toolbarButton' and @title='Remove the table']", "removeBtn");
        traceDropdownBtn = createScopedElement("xpath=.//a[@id='traceLink']//td[@class='arrow']", "traceDropdownBtn");
        
        // Dropdown/Form elements - page-level (appear outside toolbar after clicks)
        createItemBtn = new PlaywrightWebElement(page, "xpath=//a[@title='Create']", "createItemBtn");
        expandTypesBtn = new PlaywrightWebElement(page, "xpath=//table[@class='table']//span[contains(@class, 'rf-trn-hnd-colps') and contains(@class, 'rf-trn-hnd')]", "expandTypesBtn");
        addElementToCollectionBtnTemplate = new PlaywrightWebElement(page, "xpath=//span[contains(text(), '%s')]//a[@title='Add new element to collection']", "addElementToCollectionBtnTemplate");
        runDropdownBtn = new PlaywrightWebElement(page, "xpath=//input[@id='inputArgsForm:runButton']", "runDropdownBtn");
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
    
    public void clickCopy() {
        copyBtn.click();
    }
    
    public void clickRemove() {
        removeBtn.click();
    }

    public boolean isRunButtonEnabled() {
        return runBtn.isEnabled();
    }

    public boolean isTraceButtonEnabled() {
        return traceBtn.isEnabled();
    }

    public boolean isBenchmarkButtonEnabled() {
        return benchmarkBtn.isEnabled();
    }

    public boolean isExportButtonEnabled() {
        return exportBtn.isEnabled();
    }

    public PlaywrightTraceMenu setFactorTextField(String text) {
        factorTextField.fill(text);
        return new PlaywrightTraceMenu();
    }

    public PlaywrightTraceWindow clickTraceInsideMenu() {
        traceDropdownBtn.click();
        // Wait for trace window to open - in Playwright we don't need window switching like Selenium
        page.waitForSelector("xpath=.//div[contains(@class,'trace-window') or contains(@title,'Trace')]", 
                            new com.microsoft.playwright.Page.WaitForSelectorOptions().setTimeout(5000));
        return new PlaywrightTraceWindow();
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
            runDropdownBtn.click();
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
            traceInsideMenuBtn.click();
            // Wait for trace window to open - in Playwright we don't need window switching like Selenium
            page.waitForSelector("xpath=.//div[contains(@class,'trace-window') or contains(@title,'Trace')]", 
                                new com.microsoft.playwright.Page.WaitForSelectorOptions().setTimeout(5000));
            return new PlaywrightTraceWindow();
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
    }

    // Interface for Playwright Trace Window
    public interface IPlaywrightTraceWindow {
        IPlaywrightTraceWindow expandItemInTree(int position);
        java.util.List<String> getVisibleItemsFromTree();
    }

    // Implementation for Playwright Trace Window
    public class PlaywrightTraceWindow implements IPlaywrightTraceWindow {
        private PlaywrightWebElement traceTree;
        private PlaywrightWebElement traceExpanderTemplate;
        private PlaywrightWebElement traceItemsTemplate;

        public PlaywrightTraceWindow() {
            // Initialize trace window elements
            traceTree = new PlaywrightWebElement(PlaywrightDriverPool.getPage(), 
                "xpath=.//div[contains(@class,'trace-tree') or contains(@id,'traceTree')]", "traceTree");
            traceExpanderTemplate = new PlaywrightWebElement(PlaywrightDriverPool.getPage(),
                "xpath=(.//span[contains(@class, 'fancytree-exp-c')]/span[@class='fancytree-expander'])[%d]", "traceExpanderTemplate");
            traceItemsTemplate = new PlaywrightWebElement(PlaywrightDriverPool.getPage(),
                "xpath=.//li//span[@class='fancytree-title']", "traceItemsTemplate");
        }

        @Override
        public IPlaywrightTraceWindow expandItemInTree(int position) {
            PlaywrightWebElement item = traceExpanderTemplate.format(position + 1);
            item.click();
            return this;
        }

        @Override
        public java.util.List<String> getVisibleItemsFromTree() {
            java.util.List<String> items = new java.util.ArrayList<>();
            var page = PlaywrightDriverPool.getPage();
            var locators = page.locator("xpath=.//li//span[@class='fancytree-title']");
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
}