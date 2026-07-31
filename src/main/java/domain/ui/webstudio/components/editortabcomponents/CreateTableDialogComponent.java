package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;

/**
 * The React "Create Table" modal, which EPBDS-16313 put in place of the JSF wizard.
 *
 * <p>The wizard walked through a step per question; the modal asks everything on one page and previews the
 * table being built. The step-by-step methods are kept so the tests written against the wizard keep reading
 * the same way - {@link #clickNext()} has nothing left to do here.
 */
public class CreateTableDialogComponent extends BaseComponent {

    private static final String MODAL =
            "//div[contains(@class,'ant-modal')][.//div[contains(@class,'ant-modal-title')][contains(normalize-space(.),'Create Table')]]";
    // Columns of the Datatype skeleton: Type, Name, Default Value, Mandatory, Description, Examples.
    private static final int DATATYPE_TYPE_COLUMN = 0;
    private static final int DATATYPE_NAME_COLUMN = 1;
    private static final int MAX_SKELETON_ROWS = 30;
    // Long enough for the value the modal suggests itself to arrive and overwrite what was typed.
    private static final int SETTLE_MS = 400;

    private WebElement modal;
    private WebElement typeSelect;
    private WebElement nameInput;
    private WebElement moduleInput;
    private WebElement sheetInput;
    private WebElement resultTypeInput;
    private WebElement headerPreview;
    private WebElement createButton;
    // Formatted with the argument's row index, starting at 0.
    private WebElement argumentTypeTemplate;
    private WebElement argumentNameTemplate;
    private WebElement argumentRowTemplate;
    private WebElement insertArgumentTemplate;
    // Formatted with the cell's row and column index, both starting at 0.
    private WebElement cellTemplate;
    private WebElement rowTemplate;
    // Rows already written by this dialog, counted from the type selection so a reopened dialog starts over.
    private int writtenParameterRows;
    private int writtenArgumentRows;

    public CreateTableDialogComponent(WebElement root) {
        super(root);
        initializeElements();
    }

    public CreateTableDialogComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    private void initializeElements() {
        modal = new WebElement(page, "xpath=" + MODAL, "createTableModal");
        // antd puts the testid on the wrapper of some controls, so reach the inner control either way.
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
                "css=[data-testid=create-table-argument-row-%s] button[title='Insert Argument']",
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

    /**
     * Picks the table type. The wizard listed types as "Datatype Table"; the modal drops the "Table" suffix, so
     * either wording is accepted here.
     */
    public CreateTableDialogComponent selectType(String type) {
        waitForDialogToAppear();
        writtenParameterRows = 0;
        writtenArgumentRows = 0;
        String option = type.replaceAll("(?i)\\s+table$", "").trim();
        typeSelect.waitForVisible(DEFAULT_TIMEOUT_MS).click();
        openDropdownOption(option).waitForVisible(DEFAULT_TIMEOUT_MS).click();
        return this;
    }

    /** The modal asks everything at once, so there is no step to advance to. */
    public CreateTableDialogComponent clickNext() {
        return waitForDialogToAppear();
    }

    public CreateTableDialogComponent setTechnicalName(String name) {
        nameInput.waitForVisible(DEFAULT_TIMEOUT_MS);
        retype(nameInput, name);
        return this;
    }

    /**
     * Adds a field to the table being built: the wizard asked for a type and a name per field, and the modal
     * takes the same pair in the first free row of the skeleton it previews.
     */
    public CreateTableDialogComponent addParameter(String type, String name) {
        // The modal opens with a field of its own ("String field1"), so the first parameter overwrites that row
        // instead of looking for a free one - otherwise the table is created with a field nobody asked for.
        int row = writtenParameterRows++;
        if (type != null && !type.isEmpty()) {
            growSkeletonTo(row);
            WebElement typeCell = cellTemplate.format(String.valueOf(row), String.valueOf(DATATYPE_TYPE_COLUMN));
            typeCell.waitForVisible(DEFAULT_TIMEOUT_MS);
            retypeSuggest(typeCell, type);
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

    /**
     * Declares one argument of the table's signature. The modal has no "array" checkbox any more - an array is
     * the type with brackets, which is how it reaches the header either way.
     */
    public CreateTableDialogComponent addSimpleRulesParameter(String type, boolean isArray, String name) {
        int row = argumentRow(writtenArgumentRows++);
        String declaredType = isArray && type != null && !type.endsWith("[]") ? type + "[]" : type;
        if (declaredType != null && !declaredType.isEmpty()) {
            retypeSuggest(argumentTypeTemplate.format(String.valueOf(row)), declaredType);
        }
        retype(argumentNameTemplate.format(String.valueOf(row)), name);
        return this;
    }

    /** Writes into the skeleton cell at the given zero-based position, growing the grid when the row is missing. */
    public CreateTableDialogComponent setCell(int row, int column, String value) {
        growSkeletonTo(row);
        WebElement cell = cellTemplate.format(String.valueOf(row), String.valueOf(column));
        cell.waitForVisible(DEFAULT_TIMEOUT_MS);
        retype(cell, value);
        return this;
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

    /** Adds rows below the last one until the requested row exists. */
    private void growSkeletonTo(int row) {
        for (int current = lastSkeletonRow(); current < row; current++) {
            insertRowBelow(current);
        }
    }

    /** Fills a whole skeleton row left to right, adding the row when the grid is shorter than that. */
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
                        + "//button[@title='" + title + "']",
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

    /** The argument row at that index, added below the last one when the modal does not offer it yet. */
    private int argumentRow(int row) {
        WebElement name = argumentNameTemplate.format(String.valueOf(row));
        if (!name.exists() && row > 0) {
            insertArgumentTemplate.format(String.valueOf(row - 1)).click();
            name.waitForVisible(DEFAULT_TIMEOUT_MS);
        }
        return row;
    }

    /**
     * Writes into a plain text field, retyping until it holds only the requested text: the modal fills a field
     * it can name itself (a field's name, for one) a moment after it is opened, and that value joins whatever
     * was typed before it arrived.
     */
    private void retype(WebElement field, String text) {
        boolean accepted = WaitUtil.waitForCondition(() -> {
            field.fill(text);
            // The suggested value can land after the field was read, so the value is checked once it has settled.
            WaitUtil.sleep(SETTLE_MS, "Letting the create table dialog fill in what it names itself");
            return text.equals(field.getCurrentInputValue());
        }, DEFAULT_TIMEOUT_MS, 300, "Waiting for the create table field to hold '" + text + "'");
        if (!accepted) {
            throw new IllegalStateException("The create table dialog kept '" + field.getCurrentInputValue()
                    + "' instead of '" + text + "'");
        }
    }

    /**
     * Writes into a suggest field. Setting the value outright never reaches the list's own state, so the text is
     * typed key by key and then committed by picking the matching option - leaving the field on an uncommitted
     * value drops it back to the type the modal suggests.
     */
    private void retypeSuggest(WebElement field, String text) {
        boolean accepted = WaitUtil.waitForCondition(() -> {
            clearWithoutLeaving(field);
            field.fillSequentially(text);
            WebElement option = openDropdownOption(text);
            if (option.exists()) {
                option.click();
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

    /**
     * The requested option of the dropdown that is open. Every suggest field keeps its own dropdown in the page,
     * so a match has to be looked for in the one on screen.
     */
    private WebElement openDropdownOption(String text) {
        return new WebElement(page,
                "css=.ant-select-dropdown:not(.ant-select-dropdown-hidden) .ant-select-item-option[title='" + text + "']",
                "createTableDropdownOption");
    }

    /** Empties a field while keeping the focus in it: a suggest field resets itself when it loses focus empty. */
    private void clearWithoutLeaving(WebElement field) {
        field.click();
        field.press(System.getProperty("os.name").toLowerCase().contains("mac") ? "Meta+a" : "Control+a");
        field.press("Backspace");
    }
}
