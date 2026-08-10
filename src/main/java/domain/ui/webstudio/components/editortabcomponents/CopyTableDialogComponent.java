package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.DriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;
import lombok.Getter;

/**
 * The React "Copy table" modal, which EPBDS-16313 put in place of the JSF copy wizard.
 *
 * <p>The modal asks for the copy's name, where it goes (module and sheet) and a list of properties. The
 * legacy "Copy as" selector is gone: a plain copy, a new version and a new business dimension version are all
 * a name plus the property that carries the version or the dimension, so the old three-way choice is
 * expressed by setting those properties.
 */
@Getter
public class CopyTableDialogComponent extends BaseComponent {

    private static final String MODAL =
            "//div[contains(@class,'ant-modal')][.//div[contains(@class,'ant-modal-title')][contains(normalize-space(.),'Copy table')]]";
    private static final int MAX_PROPERTY_ROWS = 10;

    private WebElement modal;
    private WebElement dialogTitle;
    private WebElement nameTextBox;
    private WebElement moduleComboBox;
    private WebElement sheetComboBox;
    private WebElement copyButton;
    // Property rows, formatted with the row index starting at 0.
    private WebElement propertyNameTemplate;
    private WebElement propertyValueTemplate;
    private WebElement insertPropertyTemplate;

    public CopyTableDialogComponent() {
        super(DriverPool.getPage());
        initializeElements();
    }

    public CopyTableDialogComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        modal = new WebElement(page, "xpath=" + MODAL, "copyTableModal");
        dialogTitle = new WebElement(page, "xpath=" + MODAL + "//div[contains(@class,'ant-modal-title')]", "copyTableTitle");
        // antd wraps these fields, so a testid can sit on the wrapper - reach the inner control either way.
        nameTextBox = new WebElement(page,
                "css=[data-testid=copy-table-name] input, input[data-testid=copy-table-name]", "copyTableName");
        moduleComboBox = new WebElement(page, "css=[data-testid=copy-table-module]", "copyTableModule");
        sheetComboBox = new WebElement(page, "css=[data-testid=copy-table-sheet]", "copyTableSheet");
        copyButton = new WebElement(page,
                "xpath=" + MODAL + "//div[contains(@class,'ant-modal-footer')]//button[.//span[normalize-space()='Copy']]",
                "copyTableSubmit");
        propertyNameTemplate = new WebElement(page,
                "css=[data-testid=copy-table-property-name-%1$s] input, input[data-testid=copy-table-property-name-%1$s]",
                "copyTablePropertyName");
        propertyValueTemplate = new WebElement(page,
                "css=[data-testid=copy-table-property-value-%1$s] input, input[data-testid=copy-table-property-value-%1$s]",
                "copyTablePropertyValue");
        // The row controls carry their label in aria-label, not title.
        insertPropertyTemplate = new WebElement(page,
                "css=[data-testid=copy-table-property-row-%s] button[aria-label='Insert property above']",
                "copyTableInsertProperty");
    }

    public CopyTableDialogComponent waitForDialogToAppear() {
        copyButton.waitForVisible(DEFAULT_TIMEOUT_MS);
        return this;
    }

    /**
     * Kept for callers written against the legacy wizard: the modal has no "Copy as" selector any more, so
     * what is filled in afterwards decides whether this is a plain copy, a version or a dimension.
     */
    public CopyTableDialogComponent selectCopyAs(String value) {
        return waitForDialogToAppear();
    }

    public CopyTableDialogComponent setName(String name) {
        nameTextBox.waitForVisible(DEFAULT_TIMEOUT_MS);
        // The modal pre-fills the source table's name and does it asynchronously, so a single fill can be
        // overwritten (or joined) by that value - retype until the field holds only what was asked for.
        boolean accepted = WaitUtil.waitForCondition(() -> {
            retype(nameTextBox, name);
            return name.equals(nameTextBox.getCurrentInputValue());
        }, DEFAULT_TIMEOUT_MS, 300, "Waiting for the copy dialog to hold only the requested table name");
        if (!accepted) {
            throw new IllegalStateException("The copy dialog kept the name '" + nameTextBox.getCurrentInputValue()
                    + "' instead of '" + name + "'");
        }
        return this;
    }

    /** A new version is a copy carrying the "version" property. */
    public CopyTableDialogComponent setVersion(String version) {
        return setProperty("version", version);
    }

    /** The sheet the copy lands on; the legacy dialog called this "Save to". */
    public CopyTableDialogComponent setSaveTo(String sheetName) {
        if (sheetName != null && !sheetName.isEmpty()) {
            selectFromDropdown(sheetComboBox, sheetName);
        }
        return this;
    }

    public CopyTableDialogComponent setModule(String moduleName) {
        selectFromDropdown(moduleComboBox, moduleName);
        return this;
    }

    public CopyTableDialogComponent setTextProperty(String propertyLabel, String value) {
        return setProperty(propertyLabel, value);
    }

    public CopyTableDialogComponent setSelectProperty(String propertyLabel, String value) {
        return setProperty(propertyLabel, value);
    }

    /** Puts a property into the first free row, adding a row when every one already carries a property. */
    public CopyTableDialogComponent setProperty(String propertyLabel, String value) {
        waitForDialogToAppear();
        String row = String.valueOf(firstFreePropertyRow());
        WebElement name = propertyNameTemplate.format(row);
        name.waitForVisible(DEFAULT_TIMEOUT_MS);
        WaitUtil.waitForCondition(() -> {
            retype(name, propertyLabel);
            return propertyLabel.equals(name.getCurrentInputValue());
        }, DEFAULT_TIMEOUT_MS, 300, "Waiting for the property name to hold the requested value");
        // The name is a suggest input: committing it lets the value editor pick up the property's type.
        name.press("Enter");
        WebElement propertyValue = propertyValueTemplate.format(row);
        propertyValue.waitForVisible(DEFAULT_TIMEOUT_MS);
        if (isListBacked(row)) {
            // A property whose type lists its values (a business dimension, for one) is picked, never typed:
            // typing only moves the list to the first value starting with that letter.
            pickPropertyValue(row, propertyValue, value);
        } else {
            WaitUtil.waitForCondition(() -> {
                retype(propertyValue, value);
                return value.equals(propertyValue.getCurrentInputValue());
            }, DEFAULT_TIMEOUT_MS, 300, "Waiting for the property value to hold the requested value");
        }
        return this;
    }

    /** A property backed by a list of its own values: antd renders it as a select, not as a plain input. */
    private boolean isListBacked(String row) {
        return new WebElement(page, "css=[data-testid=copy-table-property-value-" + row + "].ant-select",
                "copyTablePropertySelect").exists();
    }

    private void pickPropertyValue(String row, WebElement field, String value) {
        WebElement option = new WebElement(page,
                "css=.ant-select-dropdown:not(.ant-select-dropdown-hidden) .ant-select-item-option[title='"
                        + value + "']", "copyTablePropertyOption");
        field.click();
        // The list can be long (every country, for one), so it is filtered down to the wanted value first.
        field.fillSequentially(value);
        if (!WaitUtil.waitForCondition(option::exists, DEFAULT_TIMEOUT_MS / 2, 200,
                "Waiting for the property to offer '" + value + "'")) {
            field.click();
            if (!WaitUtil.waitForCondition(option::exists, DEFAULT_TIMEOUT_MS / 2, 200,
                    "Waiting for the property to offer '" + value + "'")) {
                throw new IllegalStateException("The copy dialog offers no '" + value + "' for this property");
            }
        }
        option.click(DEFAULT_TIMEOUT_MS / 2);
        // A multi-value property shows each picked value as its own tag.
        WebElement selected = new WebElement(page,
                "css=[data-testid=copy-table-property-value-" + row + "] .ant-select-selection-item[title='"
                        + value + "']", "copyTablePropertySelection");
        if (!WaitUtil.waitForCondition(selected::exists, DEFAULT_TIMEOUT_MS / 2, 200,
                "Waiting for the property to hold '" + value + "'")) {
            throw new IllegalStateException("The copy dialog did not take '" + value + "' for this property");
        }
    }

    /**
     * Clears the field and types the text key by key.
     *
     * <p>The modal keeps its own copy of these values in React state and builds the copy's header from that,
     * not from the DOM: a single set-value leaves the state on the suggested name, so the copy comes out under
     * the source table's name. Typing sends the events the modal listens to.
     */
    private void retype(WebElement field, String text) {
        field.clearByKeyCombination();
        field.fillSequentially(text);
    }

    private int firstFreePropertyRow() {
        for (int row = 0; row < MAX_PROPERTY_ROWS; row++) {
            WebElement name = propertyNameTemplate.format(String.valueOf(row));
            if (!name.exists()) {
                if (row > 0) {
                    insertPropertyTemplate.format(String.valueOf(row - 1)).click();
                }
                return row;
            }
            if (name.getCurrentInputValue().isBlank()) {
                return row;
            }
        }
        throw new IllegalStateException("The copy dialog offers no free property row");
    }

    private void selectFromDropdown(WebElement select, String value) {
        select.waitForVisible(DEFAULT_TIMEOUT_MS).click();
        new WebElement(page, "xpath=//div[contains(@class,'ant-select-item-option')][@title='" + value + "']",
                "dropdownOption").waitForVisible(DEFAULT_TIMEOUT_MS).click();
    }

    public boolean isCopyButtonEnabled() {
        return copyButton.isEnabled();
    }

    public void clickCopy() {
        copyButton.waitForVisible(DEFAULT_TIMEOUT_MS).click();
        copyButton.waitForHidden(DEFAULT_TIMEOUT_MS);
        waitUntilSpinnerLoaded();
    }

    public String getName() {
        return nameTextBox.getCurrentInputValue();
    }

    public String getVersion() {
        WebElement firstValue = propertyValueTemplate.format("0");
        return firstValue.exists() ? firstValue.getCurrentInputValue() : "";
    }

    public boolean isDialogVisible() {
        return modal.isVisible(DEFAULT_TIMEOUT_MS / 2);
    }

    public CopyTableDialogComponent waitForDialogToClose() {
        WaitUtil.waitForCondition(() -> !modal.isVisible(500), DEFAULT_TIMEOUT_MS, 250,
                "Waiting for the copy table dialog to close");
        return this;
    }
}
