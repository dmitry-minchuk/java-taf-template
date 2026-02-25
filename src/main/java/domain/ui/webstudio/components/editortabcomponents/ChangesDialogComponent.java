package domain.ui.webstudio.components.editortabcomponents;

import com.microsoft.playwright.Page;
import domain.ui.webstudio.components.BaseComponent;
import domain.ui.webstudio.components.repositorytabcomponents.CompareDialogComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import helpers.utils.WaitUtil;

import java.util.List;

public class ChangesDialogComponent extends BaseComponent {

    private WebElement changesSection;
    private WebElement compareCheckboxTemplate;
    private WebElement restoreBtn;
    private WebElement compareBtn;
    private WebElement noChangesMsg;
    private WebElement changesTitle;
    private List<WebElement> changeRowCheckboxes;

    public ChangesDialogComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public ChangesDialogComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        changesSection = createScopedElement("xpath=.//section[@id='changes']", "changesSection");
        compareCheckboxTemplate = new WebElement(page, "xpath=(//section[@id='changes']//div[contains(@class,'row')]//input)[%s]", "compareCheckboxTemplate");
        restoreBtn = new WebElement(page, "xpath=//input[@value='Restore']", "restoreBtn");
        compareBtn = new WebElement(page, "xpath=//div[@id='content']//input[contains(@id, 'compareBtn')]", "compareBtn");
        noChangesMsg = new WebElement(page, "xpath=//span[@class='problem-info noChanges']", "noChangesMsg");
        changesTitle = new WebElement(page, "xpath=//span[@id='localChanges']", "changesTitle");
        changeRowCheckboxes = createElementList("xpath=//section[@id='changes']//div[contains(@class,'row')]//input[@type='checkbox']", "changeRowCheckboxes");
    }

    public void setCompareCheckbox(int index, boolean value) {
        WebElement checkbox = compareCheckboxTemplate.format(String.valueOf(index));
        if (checkbox.isChecked() != value) {
            checkbox.click();
            WaitUtil.sleep(100, "Waiting after checkbox click");
        }
    }

    public boolean getCompareCheckboxValue(int index) {
        WebElement checkbox = compareCheckboxTemplate.format(String.valueOf(index));
        return checkbox.isChecked();
    }

    public void clickRestore() {
        restoreBtn.click();
        WaitUtil.sleep(500, "Waiting after Restore button click");
    }

    public CompareDialogComponent clickCompare() {
        Page comparePopup = page.waitForPopup(() -> {
            compareBtn.click();
        });
        comparePopup.waitForLoadState();
        return new CompareDialogComponent(comparePopup);
    }

    public String getNoChangesMessage() {
        return noChangesMsg.getText();
    }

    public String getChangesTitle() {
        return changesTitle.getText().trim();
    }

    public int getRowCount() {
        WaitUtil.sleep(500, "Waiting for change rows to load");
        return changeRowCheckboxes.size();
    }

    public void clickRestoreAtRow(int rowIndex) {
        setCompareCheckbox(rowIndex, true);
        clickRestore();
    }

    public boolean isDialogVisible() {
        return changesSection.isVisible();
    }
}
