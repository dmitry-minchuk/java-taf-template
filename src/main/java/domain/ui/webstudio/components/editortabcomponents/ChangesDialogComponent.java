package domain.ui.webstudio.components.editortabcomponents;

import com.microsoft.playwright.Page;
import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import helpers.utils.WaitUtil;

import java.util.List;

public class ChangesDialogComponent extends BaseComponent {

    private WebElement changesSection;
    private WebElement compareCheckboxTemplate;
    private WebElement restoreRowLinkTemplate;
    private WebElement confirmRestoreBtn;
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
        compareCheckboxTemplate = new WebElement(page, "xpath=(//section[@id='changes']//div[contains(@class,'row') and not(contains(@class,'{'))]//input)[%s]", "compareCheckboxTemplate");
        restoreRowLinkTemplate = new WebElement(page, "xpath=(//section[@id='changes']//div[contains(@class,'row') and not(contains(@class,'{'))]//a[text()='Restore'])[%s]", "restoreRowLinkTemplate");
        confirmRestoreBtn = new WebElement(page, "xpath=//div[@id='confirmRestore_container']//input[@value='Restore']", "confirmRestoreBtn");
        compareBtn = new WebElement(page, "xpath=//div[@id='content']//input[contains(@id, 'compareBtn')]", "compareBtn");
        noChangesMsg = new WebElement(page, "xpath=//span[@class='problem-info noChanges']", "noChangesMsg");
        changesTitle = new WebElement(page, "xpath=//h1[@class='page-header']", "changesTitle");
        changeRowCheckboxes = createElementList("xpath=//section[@id='changes']//div[contains(@class,'row') and not(contains(@class,'{'))]//input[@type='checkbox']", "changeRowCheckboxes");
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

    public CompareLocalChangesDialogComponent clickCompare() {
        Page comparePopup = page.waitForPopup(() -> {
            compareBtn.click();
        });
        comparePopup.waitForLoadState();
        return new CompareLocalChangesDialogComponent(comparePopup);
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
        WebElement restoreLink = restoreRowLinkTemplate.format(String.valueOf(rowIndex));
        restoreLink.click();
        confirmRestoreBtn.click();
    }

    public boolean isDialogVisible() {
        return changesSection.isVisible();
    }
}
