package domain.ui.webstudio.components.projectdetail;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.BaseComponent;

import java.util.ArrayList;
import java.util.List;

public class ProjectOverviewTabComponent extends BaseComponent {

    private static final String DEFAULT_BRANCH_TAG = "Default";
    private static final int PROBE_MS = DEFAULT_TIMEOUT_MS / 5;

    private final WebElement branchLabel;
    private final WebElement branchSwitcherTrigger;
    private final WebElement branchMenuItem;
    private final WebElement modifiedValue;
    private final WebElement modifiedDate;
    private final WebElement tagValueForType;
    private final WebElement overviewRight;
    private final WebElement moduleRows;
    private final WebElement editBtn;
    private final WebElement saveBtn;
    private final WebElement descriptionInput;
    private final WebElement migrateBtn;
    private final WebElement migrateConfirmOkBtn;
    private final WebElement descriptorActionsMarker;
    private final WebElement matchedModuleToggles;
    private final WebElement matchedModuleRows;

    public ProjectOverviewTabComponent(Page page) {
        this(new WebElement(page, "[data-testid=overview-panel]", "overviewPanel"));
    }

    public ProjectOverviewTabComponent(WebElement rootLocator) {
        super(rootLocator);
        branchLabel = createScopedElement("[data-testid=overview-branch]", "branchLabel");
        branchSwitcherTrigger = createScopedElement("[data-testid=overview-branch-trigger]", "branchSwitcherTrigger");
        branchMenuItem = new WebElement(page, "xpath=//div[contains(@class,'ant-dropdown')][not(contains(@class,'ant-dropdown-hidden'))]//li[contains(@class,'ant-dropdown-menu-item')][@data-menu-id='rc-menu-uuid-%s' or .//*[normalize-space()='%s']]", "branchMenuItem");
        modifiedValue = createScopedElement("xpath=.//*[@data-testid='overview-right']//div[./span[normalize-space()='Modified']]/following-sibling::div[1]", "modifiedValue");
        modifiedDate = createScopedElement("xpath=.//*[@data-testid='overview-right']//div[./span[normalize-space()='Modified']]/following-sibling::div[1]/div[last()]", "modifiedDate");
        tagValueForType = createScopedElement("xpath=.//*[@data-testid='project-tags']/span[normalize-space()='%s']/following-sibling::span[1]", "tagValueForType");
        overviewRight = createScopedElement("[data-testid=overview-right]", "overviewRight");
        moduleRows = createScopedElement(
                "xpath=.//li[starts-with(@data-testid,'module-')]"
                        + "[not(starts-with(@data-testid,'module-filter-'))]"
                        + "[not(starts-with(@data-testid,'module-matched-'))]"
                        + "[not(starts-with(@data-testid,'module-unmatched-'))]",
                "overviewModuleRows");
        editBtn = createScopedElement("[data-testid=overview-edit]", "overviewEditBtn");
        saveBtn = createScopedElement("[data-testid=overview-save]", "overviewSaveBtn");
        descriptionInput = createScopedElement("[data-testid=edit-description]", "overviewDescriptionInput");
        migrateBtn = createScopedElement("[data-testid=overview-migrate]", "overviewMigrateBtn");
        migrateConfirmOkBtn = new WebElement(page,
                "xpath=//div[contains(@class,'ant-modal-confirm')]//div[contains(@class,'ant-modal-confirm-btns')]//button[contains(@class,'ant-btn-primary')]",
                "migrateConfirmOkBtn");
        descriptorActionsMarker = createScopedElement(
                "[data-testid=overview-edit], [data-testid=overview-migrate]", "descriptorActionsMarker");
        matchedModuleToggles = createScopedElement(
                "button[data-testid^='module-matched-']:not([data-testid^='module-matched-item-'])",
                "matchedModuleToggles");
        matchedModuleRows = createScopedElement("li[data-testid^='module-matched-item-']", "matchedModuleRows");
    }

    public String getCurrentBranch() {
        String label = branchLabel.getText().trim();
        if (label.endsWith(DEFAULT_BRANCH_TAG)) {
            label = label.substring(0, label.length() - DEFAULT_BRANCH_TAG.length());
        }
        return label.trim();
    }

    public void switchBranch(String branchName) {
        branchSwitcherTrigger.click();
        branchMenuItem.format(branchName, branchName).click();
        waitUntilSpinnerLoaded();
    }

    public boolean isBranchPresent(String branchName) {
        if (branchName.equals(getCurrentBranch())) {
            return true;
        }
        branchSwitcherTrigger.click();
        boolean present = branchMenuItem.format(branchName, branchName).isVisible(PROBE_MS);
        page.keyboard().press("Escape");
        return present;
    }

    public String getTagValueForType(String tagType) {
        return tagValueForType.format(tagType).getText().trim();
    }

    public String extractField(String label, String nextLabel) {
        String blob = overviewRight.getText();
        int start = blob.indexOf(label);
        if (start < 0) {
            return "";
        }
        start += label.length();
        int end = blob.indexOf(nextLabel, start);
        return (end < 0 ? blob.substring(start) : blob.substring(start, end)).trim();
    }

    public List<String> getModuleNames() {
        descriptorActionsMarker.waitForVisible(DEFAULT_TIMEOUT_MS);
        Locator rows = moduleRows.getLocator();
        List<String> names = new ArrayList<>();
        for (int i = 0; i < rows.count(); i++) {
            names.add(rows.nth(i).innerText().trim().split("\n")[0].trim());
        }
        return names;
    }

    public List<String> getMatchedModuleNames() {
        descriptorActionsMarker.waitForVisible(DEFAULT_TIMEOUT_MS);
        Locator toggles = matchedModuleToggles.getLocator();
        for (int i = 0; i < toggles.count(); i++) {
            Locator toggle = toggles.nth(i);
            if ("false".equals(toggle.getAttribute("aria-expanded"))) {
                toggle.click();
            }
        }
        Locator rows = matchedModuleRows.getLocator();
        List<String> names = new ArrayList<>();
        for (int i = 0; i < rows.count(); i++) {
            names.add(rows.nth(i).innerText().trim().split("\n")[0].trim());
        }
        return names;
    }

    public void editAndSave() {
        editBtn.waitForVisible(DEFAULT_TIMEOUT_MS).click();
        saveBtn.waitForVisible(DEFAULT_TIMEOUT_MS).click();
        waitUntilSpinnerLoaded();
    }

    public void editDescriptionAndSave(String description) {
        editBtn.waitForVisible(DEFAULT_TIMEOUT_MS).click();
        descriptionInput.waitForVisible(DEFAULT_TIMEOUT_MS).fill(description);
        saveBtn.waitForVisible(DEFAULT_TIMEOUT_MS).click();
        waitUntilSpinnerLoaded();
    }

    public void migrateAndWaitUntilEditable() {
        migrateBtn.waitForVisible(DEFAULT_TIMEOUT_MS).click();
        migrateConfirmOkBtn.waitForVisible(DEFAULT_TIMEOUT_MS).click();
        editBtn.waitForVisible(DEFAULT_TIMEOUT_MS);
        waitUntilSpinnerLoaded();
    }

    public boolean isMigrateOffered() {
        return migrateBtn.isVisible(DEFAULT_TIMEOUT_MS / 2);
    }

    public boolean isEditOffered() {
        return editBtn.isVisible(DEFAULT_TIMEOUT_MS / 2);
    }

    public boolean isMigrateEnabled() {
        migrateBtn.waitForVisible(DEFAULT_TIMEOUT_MS);
        return migrateBtn.isEnabled();
    }

    public String getModifiedBy() {
        String whole = modifiedValue.getText().trim();
        String date = modifiedDate.getText().trim();
        return whole.endsWith(date) ? whole.substring(0, whole.length() - date.length()).trim() : whole;
    }

    public String getModifiedAt() {
        return modifiedDate.getText().trim();
    }
}
