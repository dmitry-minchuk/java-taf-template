package domain.ui.webstudio.components.editortabcomponents;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.MouseButton;
import com.microsoft.playwright.options.SelectOption;
import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;

public class CreateTableDialogComponent extends BaseComponent {

    // The construct-a-table wizard is a handful of steps; Save lives on the last one.
    private static final int MAX_WIZARD_STEPS = 4;

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
        // The wizard is JSF and re-renders each step on its own, so click once the page has settled.
        nextButton.clickWhenSettled();
        waitUntilSpinnerLoaded();
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
        contextMenuItemTemplate.format("Add Rule").waitForVisible(DEFAULT_TIMEOUT_MS).click();
        return setSimpleRule(rule, cellIndex);
    }

    /**
     * Types a value into the rules grid cell at the given position. Adding a rule grows the grid a moment
     * later, so the cell is waited for — otherwise the value lands in whatever cell is there at the time.
     */
    public CreateTableDialogComponent setSimpleRule(String rule, int cellIndex) {
        WebElement cell = simpleRulesCellTemplate.format(String.valueOf(cellIndex));
        cell.waitForVisible(DEFAULT_TIMEOUT_MS).click();
        Locator cellInput = page.locator("xpath=//form[@id='srtTableForm']//div//input");
        cellInput.first().waitFor();
        cellInput.fill(rule);
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

    /**
     * Saves the table. The wizard advances a step at a time and the grid step can still be settling when
     * Next is pressed, so if Save is not on screen yet the wizard is advanced once more.
     */
    public void save() {
        // The wizard advances a step at a time, and a step can still be settling when Next is pressed, so
        // keep advancing until the last step (the one with Save) is reached.
        for (int step = 1; step <= MAX_WIZARD_STEPS && !saveButton.isVisible(DEFAULT_TIMEOUT_MS / 5); step++) {
            clickNext();
        }
        saveButton.waitForVisible(DEFAULT_TIMEOUT_MS).click();
    }
}
