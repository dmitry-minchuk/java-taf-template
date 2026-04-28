package domain.ui.webstudio.components.editortabcomponents;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.MouseButton;
import com.microsoft.playwright.options.SelectOption;
import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;

public class CreateTableDialogComponent extends BaseComponent {

    private WebElement tableTypeRadioTemplate;
    private WebElement nextButton;
    private WebElement technicalNameInput;
    private WebElement parameterTypeSelect;
    private WebElement parameterNameInput;
    private WebElement addParameterLink;
    private WebElement initialTableNameInput;
    private WebElement initialReturnValueTypeSelect;
    private WebElement parametersTableRows;
    private WebElement simpleRulesCellTemplate;
    private WebElement simpleRulesRowTemplate;
    private WebElement contextMenuItemTemplate;
    private WebElement saveButton;
    private WebElement categorySelection;

    public CreateTableDialogComponent(WebElement root) {
        super(root);
        initializeElements();
    }

    private void initializeElements() {
        tableTypeRadioTemplate = createScopedElement("xpath=.//label[normalize-space(.)='%s']/../input", "tableTypeRadioTemplate");
        nextButton = createScopedElement("xpath=.//input[@value='Next']", "nextButton");
        technicalNameInput = createScopedElement("xpath=.//input[contains(@id, ':technicalName')]", "technicalNameInput");
        parameterTypeSelect = createScopedElement("xpath=(.//span[contains(@id,'paramTable')]//tbody//tr)[1]//select", "parameterTypeSelect");
        parameterNameInput = createScopedElement("xpath=(.//span[contains(@id,'paramTable')]//tbody//tr)[1]//input[contains(@id,':pname')]", "parameterNameInput");
        addParameterLink = createScopedElement("xpath=.//a[@class='addButton']", "addParameterLink");
        initialTableNameInput = createScopedElement("xpath=.//h1[contains(text(), 'Enter the initial parameters')]/..//span[contains(text(), 'Table Name')]/../..//input", "initialTableNameInput");
        initialReturnValueTypeSelect = createScopedElement("xpath=.//h1[contains(text(), 'Enter the initial parameters')]/..//span[contains(text(), 'Return Value Type')]/../..//select", "initialReturnValueTypeSelect");
        parametersTableRows = createScopedElement("xpath=.//span[contains(@id, 'inputParamTable')]//tbody//tr", "parametersTableRows");
        simpleRulesCellTemplate = new WebElement(page, "xpath=(//form[@id='srtTableForm']//tbody//td)[%s]", "simpleRulesCell");
        simpleRulesRowTemplate = new WebElement(page, "xpath=(//form[@id='srtTableForm']//tbody//tr)[%s]", "simpleRulesRow");
        contextMenuItemTemplate = new WebElement(page, "xpath=//div[@id='divmenu']//a[contains(text(), '%s')]", "contextMenuItem");
        saveButton = createScopedElement("xpath=.//input[@value='Save']", "saveButton");
        categorySelection = createScopedElement("xpath=.//select[@id='sheet']", "categorySelection");
    }

    public CreateTableDialogComponent selectType(String type) {
        tableTypeRadioTemplate.format(type).click();
        return this;
    }

    public CreateTableDialogComponent clickNext() {
        nextButton.click();
        return this;
    }

    public CreateTableDialogComponent setTechnicalName(String name) {
        technicalNameInput.fill(name);
        nextButton.click();
        return this;
    }

    public CreateTableDialogComponent addParameter(String type, String name) {
        if (type != null && !type.isEmpty()) {
            parameterTypeSelect.selectByVisibleText(type);
        }
        parameterNameInput.fill(name);
        nextButton.click();
        return this;
    }

    public CreateTableDialogComponent setSimpleRulesInitialParameters(String tableName, String returnValueType) {
        initialTableNameInput.fill(tableName);
        if (returnValueType != null && !returnValueType.isEmpty()) {
            initialReturnValueTypeSelect.selectByVisibleText(returnValueType);
        }
        return this;
    }

    public CreateTableDialogComponent addSimpleRulesParameter(String type, boolean isArray, String name) {
        addParameterLink.click();
        WaitUtil.sleep(250, "Waiting for Simple Rules parameter row to be added");
        Locator row = firstEmptySimpleRulesParameterRow();
        if (type != null && !type.isEmpty()) {
            row.locator("select").selectOption(new SelectOption().setLabel(type));
        }
        Locator arrayCheckbox = row.locator("input[type='checkbox']");
        if (arrayCheckbox.isChecked() != isArray) {
            arrayCheckbox.click();
        }
        row.locator("xpath=.//input[contains(@id, ':pname')]").fill(name);
        return this;
    }

    private Locator firstEmptySimpleRulesParameterRow() {
        Locator rows = parametersTableRows.getLocator();
        int rowCount = rows.count();
        for (int i = 0; i < rowCount; i++) {
            Locator row = rows.nth(i);
            if (row.locator("xpath=.//input[contains(@id, ':pname')]").inputValue().isBlank()) {
                return row;
            }
        }
        return rows.last();
    }

    public CreateTableDialogComponent addSimpleRule(String column, String rule, int cellIndex) {
        page.locator("xpath=//td[text()='" + column + "']").first()
                .click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));
        WaitUtil.sleep(1000, "Waiting for Simple Rules context menu");
        contextMenuItemTemplate.format("Add Rule").click();
        return setSimpleRule(rule, cellIndex);
    }

    public CreateTableDialogComponent setSimpleRule(String rule, int cellIndex) {
        simpleRulesCellTemplate.format(String.valueOf(cellIndex)).click();
        page.locator("xpath=//form[@id='srtTableForm']//div//input").fill(rule);
        return this;
    }

    public CreateTableDialogComponent deleteSimpleRuleRow(int rowIndex) {
        simpleRulesRowTemplate.format(String.valueOf(rowIndex)).getLocator()
                .click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));
        WaitUtil.sleep(1000, "Waiting for Simple Rules context menu");
        contextMenuItemTemplate.format("Delete Row").click();
        return this;
    }

    public CreateTableDialogComponent setCategorySelection(String category) {
        categorySelection.waitForVisible();
        categorySelection.selectByVisibleText(category);
        return this;
    }

    public void save() {
        saveButton.click();
    }
}
