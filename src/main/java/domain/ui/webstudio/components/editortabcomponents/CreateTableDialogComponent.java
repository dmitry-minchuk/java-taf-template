package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.DriverPool;
import configuration.driver.ExecutionMode;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;

public class CreateTableDialogComponent extends BaseComponent {

    private static final String MODAL =
            "//div[contains(@class,'ant-modal')][.//div[contains(@class,'ant-modal-title')][contains(normalize-space(.),'Create Table')]]";
    private static final int DATATYPE_TYPE_COLUMN = 0;
    private static final int DATATYPE_NAME_COLUMN = 1;
    private static final int MAX_SKELETON_ROWS = 30;
    private static final int SETTLE_MS = 400;

    private WebElement modal;
    private WebElement typeSelect;
    private WebElement nameInput;
    private WebElement moduleInput;
    private WebElement sheetInput;
    private WebElement resultTypeInput;
    private WebElement headerPreview;
    private WebElement createButton;
    private WebElement argumentTypeTemplate;
    private WebElement argumentNameTemplate;
    private WebElement argumentRowTemplate;
    private WebElement insertArgumentTemplate;
    private WebElement cellTemplate;
    private WebElement rowTemplate;
    private int writtenParameterRows;
    private int writtenArgumentRows;

    public CreateTableDialogComponent(WebElement root) {
        super(root);
        initializeElements();
    }

    public CreateTableDialogComponent() {
        super(DriverPool.getPage());
        initializeElements();
    }

    private void initializeElements() {
        modal = new WebElement(page, "xpath=" + MODAL, "createTableModal");
        typeSelect = new WebElement(page, "css=[data-testid=create-table-type]", "createTableType");
        nameInput = new WebElement(page,
                "css=[data-testid=create-table-name] input, input[data-testid=create-table-name]", "createTableName");
        moduleInput = new WebElement(page,
                "css=[data-testid=create-table-module] input, input[data-testid=create-table-module]", "createTableModule");
        sheetInput = new WebElement(page,
                "css=[data-testid=create-table-sheet] input, input[data-testid=create-table-sheet]", "createTableSheet");
        resultTypeInput = new WebElement(page,
                "css=[data-testid=create-table-result-type] input, input[data-testid=create-table-result-type]",
                "createTableResultType");
        headerPreview = new WebElement(page, "css=[data-testid=create-table-header]", "createTableHeader");
        createButton = new WebElement(page,
                "xpath=" + MODAL + "//div[contains(@class,'ant-modal-footer')]//button[.//span[normalize-space()='Create']]",
                "createTableSubmit");
        argumentTypeTemplate = new WebElement(page,
                "css=[data-testid=create-table-argument-type-%1$s] input, input[data-testid=create-table-argument-type-%1$s]",
                "createTableArgumentType");
        argumentNameTemplate = new WebElement(page,
                "css=[data-testid=create-table-argument-name-%1$s] input, input[data-testid=create-table-argument-name-%1$s]",
                "createTableArgumentName");
        argumentRowTemplate = new WebElement(page,
                "css=[data-testid=create-table-argument-row-%s]", "createTableArgumentRow");
        insertArgumentTemplate = new WebElement(page,
                "css=[data-testid=create-table-argument-row-%s] button[aria-label='Insert Argument']",
                "createTableInsertArgument");
        cellTemplate = new WebElement(page,
                "css=[data-testid=create-table-cell-%1$s-%2$s] input, input[data-testid=create-table-cell-%1$s-%2$s]",
                "createTableCell");
        rowTemplate = new WebElement(page,
                "css=[data-testid=create-table-cell-%s-0] input, input[data-testid=create-table-cell-%s-0]",
                "createTableRow");
    }

    public CreateTableDialogComponent waitForDialogToAppear() {
        createButton.waitForVisible(DEFAULT_TIMEOUT_MS);
        return this;
    }

    public CreateTableDialogComponent selectType(String type) {
        waitForDialogToAppear();
        writtenParameterRows = 0;
        writtenArgumentRows = 0;
        String option = type.replaceAll("(?i)\\s+table$", "").trim();
        typeSelect.waitForVisible(DEFAULT_TIMEOUT_MS).click();
        openDropdownOption(option).waitForVisible(DEFAULT_TIMEOUT_MS).click(DEFAULT_TIMEOUT_MS / 2);
        return this;
    }

    public CreateTableDialogComponent clickNext() {
        return waitForDialogToAppear();
    }

    public CreateTableDialogComponent setTechnicalName(String name) {
        nameInput.waitForVisible(DEFAULT_TIMEOUT_MS);
        retype(nameInput, name);
        return this;
    }

    public CreateTableDialogComponent addParameter(String type, String name) {
        int row = writtenParameterRows++;
        if (type != null && !type.isEmpty()) {
            setCell(row, DATATYPE_TYPE_COLUMN, type);
        }
        setCell(row, DATATYPE_NAME_COLUMN, name);
        return this;
    }

    public CreateTableDialogComponent setSimpleRulesInitialParameters(String tableName, String returnValueType) {
        setTechnicalName(tableName);
        if (returnValueType != null && !returnValueType.isEmpty()) {
            resultTypeInput.waitForVisible(DEFAULT_TIMEOUT_MS);
            retypeSuggest(resultTypeInput, returnValueType);
        }
        return this;
    }

    public CreateTableDialogComponent addSimpleRulesParameter(String type, boolean isArray, String name) {
        int row = argumentRow(writtenArgumentRows++);
        String declaredType = isArray && type != null && !type.endsWith("[]") ? type + "[]" : type;
        if (declaredType != null && !declaredType.isEmpty()) {
            retypeSuggest(argumentTypeTemplate.format(String.valueOf(row)), declaredType);
        }
        retype(argumentNameTemplate.format(String.valueOf(row)), name);
        return this;
    }

    public CreateTableDialogComponent setCell(int row, int column, String value) {
        growSkeletonTo(row);
        WebElement cell = cellTemplate.format(String.valueOf(row), String.valueOf(column));
        cell.waitForVisible(DEFAULT_TIMEOUT_MS);
        if (cell.getAttribute("readonly") != null) {
            pickFromList(row, column, cell, value);
        } else if (isSuggest(row, column)) {
            retypeSuggest(cell, value);
        } else {
            retype(cell, value);
        }
        return this;
    }

    private void pickFromList(int row, int column, WebElement cell, String value) {
        WebElement option = openDropdownOption(value);
        cell.click();
        if (!WaitUtil.waitForCondition(option::exists, DEFAULT_TIMEOUT_MS / 2, 200,
                "Waiting for the create table cell to offer '" + value + "'")) {
            cell.click();
            if (!WaitUtil.waitForCondition(option::exists, DEFAULT_TIMEOUT_MS / 2, 200,
                    "Waiting for the create table cell to offer '" + value + "'")) {
                throw new IllegalStateException("The create table dialog offers no '" + value + "' for this cell");
            }
        }
        option.click(DEFAULT_TIMEOUT_MS / 2);
        WebElement selected = new WebElement(page,
                "css=[data-testid=create-table-cell-" + row + "-" + column + "] .ant-select-content,"
                        + " [data-testid=create-table-cell-" + row + "-" + column + "] .ant-select-selection-item",
                "createTableCellSelection");
        boolean set = WaitUtil.waitForCondition(() -> selected.exists() && value.equals(selected.getText(false)),
                DEFAULT_TIMEOUT_MS / 2, 200, "Waiting for the create table cell to hold '" + value + "'");
        if (!set) {
            throw new IllegalStateException("The create table cell kept '"
                    + (selected.exists() ? selected.getText(false) : "") + "' instead of '" + value + "'");
        }
    }

    private boolean isSuggest(int row, int column) {
        return new WebElement(page,
                "css=[data-testid=create-table-cell-" + row + "-" + column + "].ant-select",
                "createTableSuggestCell").exists();
    }

    public String getCellValue(int row, int column) {
        return cellTemplate.format(String.valueOf(row), String.valueOf(column)).getCurrentInputValue();
    }

    public String getGeneratedHeader() {
        return headerPreview.waitForVisible(DEFAULT_TIMEOUT_MS).getText();
    }

    public CreateTableDialogComponent setCategorySelection(String category) {
        if (category != null && !category.isEmpty()) {
            sheetInput.waitForVisible(DEFAULT_TIMEOUT_MS);
            retypeSuggest(sheetInput, category);
        }
        return this;
    }

    public CreateTableDialogComponent setModule(String moduleName) {
        if (moduleName != null && !moduleName.isEmpty()) {
            moduleInput.waitForVisible(DEFAULT_TIMEOUT_MS);
            retypeSuggest(moduleInput, moduleName);
        }
        return this;
    }

    public boolean isCreateButtonEnabled() {
        return createButton.isEnabled();
    }

    public boolean isDialogVisible() {
        return modal.isVisible(DEFAULT_TIMEOUT_MS / 2);
    }

    public void save() {
        createButton.waitForVisible(DEFAULT_TIMEOUT_MS).click();
        createButton.waitForHidden(DEFAULT_TIMEOUT_MS);
        waitUntilSpinnerLoaded();
    }

    private void growSkeletonTo(int row) {
        for (int current = lastSkeletonRow(); current < row; current++) {
            insertRowBelow(current);
        }
    }

    public CreateTableDialogComponent setRow(int row, String... values) {
        for (int column = 0; column < values.length; column++) {
            if (values[column] != null && !values[column].isEmpty()) {
                setCell(row, column, values[column]);
            }
        }
        return this;
    }

    public CreateTableDialogComponent deleteRow(int row) {
        rowAction(row, "Delete Row").click();
        WaitUtil.waitForCondition(() -> lastSkeletonRow() < row, DEFAULT_TIMEOUT_MS, 250,
                "Waiting for the skeleton row to be deleted");
        return this;
    }

    public CreateTableDialogComponent insertRowBelow(int row) {
        int expected = lastSkeletonRow() + 1;
        rowAction(row, "Insert Row Below").click();
        WaitUtil.waitForCondition(() -> lastSkeletonRow() >= expected, DEFAULT_TIMEOUT_MS, 250,
                "Waiting for the skeleton to grow a row");
        return this;
    }

    private WebElement rowAction(int row, String title) {
        return new WebElement(page,
                "xpath=" + MODAL + "//tr[.//*[@data-testid='create-table-cell-" + row + "-0']]"
                        + "//button[@aria-label='" + title + "']",
                "createTableRowAction").waitForVisible(DEFAULT_TIMEOUT_MS);
    }

    private int lastSkeletonRow() {
        int last = -1;
        for (int row = 0; row < MAX_SKELETON_ROWS; row++) {
            if (!rowTemplate.format(String.valueOf(row), String.valueOf(row)).exists()) {
                break;
            }
            last = row;
        }
        return last;
    }

    private int argumentRow(int row) {
        WebElement name = argumentNameTemplate.format(String.valueOf(row));
        if (!name.exists() && row > 0) {
            insertArgumentTemplate.format(String.valueOf(row - 1)).click();
            name.waitForVisible(DEFAULT_TIMEOUT_MS);
        }
        return row;
    }

    private void retype(WebElement field, String text) {
        boolean accepted = WaitUtil.waitForCondition(() -> {
            field.fill(text);
            WaitUtil.sleep(SETTLE_MS, "Letting the create table dialog fill in what it names itself");
            return text.equals(field.getCurrentInputValue());
        }, DEFAULT_TIMEOUT_MS, 300, "Waiting for the create table field to hold '" + text + "'");
        if (!accepted) {
            throw new IllegalStateException("The create table dialog kept '" + field.getCurrentInputValue()
                    + "' instead of '" + text + "'");
        }
    }

    private void retypeSuggest(WebElement field, String text) {
        boolean accepted = WaitUtil.waitForCondition(() -> {
            clearWithoutLeaving(field);
            field.fillSequentially(text);
            WebElement option = openDropdownOption(text);
            if (option.exists()) {
                option.click(DEFAULT_TIMEOUT_MS / 2);
            } else {
                field.press("Enter");
            }
            WaitUtil.sleep(SETTLE_MS, "Letting the create table dialog settle on the picked value");
            return text.equals(field.getCurrentInputValue());
        }, DEFAULT_TIMEOUT_MS, 300, "Waiting for the create table suggest field to hold '" + text + "'");
        if (!accepted) {
            throw new IllegalStateException("The create table dialog kept '" + field.getCurrentInputValue()
                    + "' instead of '" + text + "'");
        }
    }

    private WebElement openDropdownOption(String text) {
        return new WebElement(page,
                "css=.ant-select-dropdown:not(.ant-select-dropdown-hidden) .ant-select-item-option[title='" + text + "']",
                "createTableDropdownOption");
    }

    private void clearWithoutLeaving(WebElement field) {
        field.click();
        boolean dockerMode = ExecutionMode.current() == ExecutionMode.PLAYWRIGHT_DOCKER;
        boolean macHost = System.getProperty("os.name").toLowerCase().contains("mac");
        field.press(!dockerMode && macHost ? "Meta+a" : "Control+a");
        field.press("Backspace");
    }
}
