package domain.ui.webstudio.components.editortabcomponents;

import com.microsoft.playwright.Page;
import configuration.core.ui.WebElement;
import configuration.driver.DriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangesDialogComponent extends BaseComponent {

    private static final String VIEW = "//*[@data-testid='local-changes-view']";
    private static final String ROW = VIEW + "//table//tbody//tr";
    private static final String RESTORE_MODAL = "//div[contains(@class,'ant-modal') and .//div[contains(@class,'ant-modal-title') and normalize-space()='Confirm Restore']]";
    private static final Pattern COUNT = Pattern.compile("(\\d+)\\s+change");

    private WebElement view;
    private WebElement title;
    private WebElement countLabel;
    private WebElement noChangesMsg;
    private WebElement loadFailedAlert;
    private WebElement spinner;
    private WebElement compareBtn;
    private WebElement rowCheckboxTemplate;
    private WebElement rowRestoreLinkTemplate;
    private WebElement rowModifiedOnTemplate;
    private WebElement currentRowMarkerTemplate;
    private WebElement confirmRestoreBtn;
    private WebElement restoreSucceededNotice;
    private WebElement restoreFailedNotice;
    private List<WebElement> rows;

    public ChangesDialogComponent() {
        super(DriverPool.getPage());
        initializeElements();
    }

    public ChangesDialogComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        view = new WebElement(page, "xpath=" + VIEW, "localChangesView");
        title = new WebElement(page, "xpath=" + VIEW + "//h1", "changesTitle");
        countLabel = new WebElement(page, "xpath=" + VIEW + "//*[@data-testid='local-changes-count']", "changesCount");
        noChangesMsg = new WebElement(page, "xpath=" + VIEW + "//div[contains(@class,'ant-empty-description')]", "noChangesMsg");
        loadFailedAlert = new WebElement(page, "xpath=" + VIEW + "//div[contains(@class,'ant-alert-error')]", "changesLoadFailed");
        spinner = new WebElement(page, "xpath=" + VIEW + "//div[contains(@class,'ant-spin-spinning')]", "changesSpinner");
        compareBtn = new WebElement(page, "xpath=" + VIEW + "//button[@data-testid='compare-local-history']", "compareBtn");
        rowCheckboxTemplate = new WebElement(page, "xpath=(" + ROW + ")[%s]//input[@type='checkbox']", "compareCheckboxTemplate");
        rowRestoreLinkTemplate = new WebElement(page, "xpath=(" + ROW + ")[%s]//a[normalize-space()='Restore']", "restoreRowLinkTemplate");
        rowModifiedOnTemplate = new WebElement(page, "xpath=(" + ROW + ")[%s]/td[2]", "rowModifiedOn");
        currentRowMarkerTemplate = new WebElement(page, "xpath=(" + ROW + ")[%s]//span[normalize-space()='Current']", "currentRowMarker");
        confirmRestoreBtn = new WebElement(page, "xpath=" + RESTORE_MODAL + "//div[contains(@class,'ant-modal-footer')]//button[contains(@class,'ant-btn-primary')]", "confirmRestoreBtn");
        restoreSucceededNotice = new WebElement(page, "xpath=//div[contains(@class,'ant-notification-notice')][contains(normalize-space(.),'Restoring changes was successful!')]", "restoreSucceededNotice");
        restoreFailedNotice = new WebElement(page, "xpath=//div[contains(@class,'ant-notification-notice')][contains(normalize-space(.),'Restoring changes failed!')]", "restoreFailedNotice");
        rows = createElementList("xpath=" + ROW, "changeRows");
    }

    public ChangesDialogComponent waitForLoaded() {
        view.waitForVisible(DEFAULT_TIMEOUT_MS);
        WaitUtil.waitForCondition(() -> !spinner.exists(), DEFAULT_TIMEOUT_MS, 200, "Waiting for the local history to load");
        WaitUtil.waitForCondition(() -> !rows.isEmpty() || noChangesMsg.exists() || loadFailedAlert.exists(),
                DEFAULT_TIMEOUT_MS, 200, "Waiting for the local history rows or the empty state");
        if (loadFailedAlert.exists()) {
            throw new IllegalStateException("Local Changes failed to load: " + loadFailedAlert.getText());
        }
        return this;
    }

    public boolean isDialogVisible() {
        return view.isVisible();
    }

    public String getChangesTitle() {
        return title.waitForVisible(DEFAULT_TIMEOUT_MS).getText().trim();
    }

    public int getChangesCount() {
        String label = countLabel.waitForVisible(DEFAULT_TIMEOUT_MS).getText().trim();
        Matcher matcher = COUNT.matcher(label);
        if (!matcher.find()) {
            throw new IllegalStateException("Unexpected local changes count label: '" + label + "'");
        }
        return Integer.parseInt(matcher.group(1));
    }

    public String getNoChangesMessage() {
        return noChangesMsg.waitForVisible(DEFAULT_TIMEOUT_MS).getText().trim();
    }

    public int getRowCount() {
        waitForLoaded();
        return rows.size();
    }

    public boolean isRowCurrent(int index) {
        return currentRowMarkerTemplate.format(String.valueOf(index)).exists();
    }

    public String getRowModifiedOn(int index) {
        return rowModifiedOnTemplate.format(String.valueOf(index)).getText().trim();
    }

    public void setCompareCheckbox(int index, boolean value) {
        WebElement checkbox = rowCheckboxTemplate.format(String.valueOf(index));
        if (!checkbox.exists()) {
            page.reload();
            waitUntilSpinnerLoaded();
            waitForLoaded();
            checkbox.waitForVisible(DEFAULT_TIMEOUT_MS);
        }
        if (checkbox.isChecked() != value) {
            checkbox.click();
            WaitUtil.waitForCondition(() -> checkbox.isChecked() == value, DEFAULT_TIMEOUT_MS / 2, 100,
                    "Waiting for the compare checkbox at row " + index + " to become " + value);
        }
    }

    public boolean getCompareCheckboxValue(int index) {
        return rowCheckboxTemplate.format(String.valueOf(index)).waitForVisible(DEFAULT_TIMEOUT_MS).isChecked();
    }

    public boolean isCompareEnabled() {
        return compareBtn.waitForVisible(DEFAULT_TIMEOUT_MS).isEnabled();
    }

    public CompareLocalChangesDialogComponent clickCompare() {
        WaitUtil.waitForCondition(this::isCompareEnabled, DEFAULT_TIMEOUT_MS / 2, 100,
                "Waiting for the Compare button to accept the two selected versions");
        Page comparePopup = page.waitForPopup(() -> compareBtn.click());
        comparePopup.waitForLoadState();
        return new CompareLocalChangesDialogComponent(comparePopup);
    }

    public void clickRestoreAtRow(int rowIndex) {
        rowRestoreLinkTemplate.format(String.valueOf(rowIndex)).waitForVisible(DEFAULT_TIMEOUT_MS).click();
        confirmRestoreBtn.waitForVisible(DEFAULT_TIMEOUT_MS).click();
        WaitUtil.waitForCondition(() -> restoreSucceededNotice.exists() || restoreFailedNotice.exists(),
                DEFAULT_TIMEOUT_MS * 3, 200, "Waiting for the restore outcome notification");
        if (restoreFailedNotice.exists()) {
            throw new IllegalStateException("Restore failed: " + restoreFailedNotice.getText());
        }
        confirmRestoreBtn.waitForHidden(DEFAULT_TIMEOUT_MS);
        waitUntilSpinnerLoaded();
    }
}
