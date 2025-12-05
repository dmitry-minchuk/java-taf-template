package domain.ui.webstudio.components.editortabcomponents;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import helpers.utils.WaitUtil;

public class ChangesDialogComponent extends BaseComponent {

    private WebElement changesSection;
    private WebElement compareCheckboxTemplate;
    private WebElement restoreBtn;
    private WebElement compareBtn;
    private WebElement noChangesMsg;

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

    public void clickCompare() {
        compareBtn.click();
        WaitUtil.sleep(500, "Waiting after Compare button click");
    }

    public String getNoChangesMessage() {
        return noChangesMsg.getText();
    }

    public boolean isDialogVisible() {
        return changesSection.isVisible();
    }
}
