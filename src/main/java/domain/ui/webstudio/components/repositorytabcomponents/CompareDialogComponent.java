package domain.ui.webstudio.components.repositorytabcomponents;

import com.microsoft.playwright.Page;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;

import java.util.List;

public class CompareDialogComponent extends BaseComponent {

    private WebElement leftModulesSelect;
    private WebElement rightModulesSelect;
    private WebElement closeBtn;
    private Page comparePopup;

    public CompareDialogComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public CompareDialogComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    public CompareDialogComponent(Page comparePopup) {
        super(comparePopup);
        this.comparePopup = comparePopup;
        initializeElements();
    }

    private void initializeElements() {
        leftModulesSelect = new WebElement(getPage(), "xpath=(//select[contains(@name, 'compareForm')])[1]", "leftModulesSelect");
        rightModulesSelect = new WebElement(getPage(), "xpath=(//select[contains(@name, 'compareForm')])[4]", "rightModulesSelect");
        closeBtn = new WebElement(getPage(), "xpath=//input[@value='Close']", "closeBtn");
    }

    public CompareDialogComponent waitForDialogToAppear() {
        WaitUtil.sleep(1500, "Waiting for Compare dialog to appear");
        leftModulesSelect.waitForVisible(5000);
        return this;
    }

    public List<String> getLeftModulesList() {
        return leftModulesSelect.getSelectVisibleTextValues();
    }

    public List<String> getRightModulesList() {
        return rightModulesSelect.getSelectVisibleTextValues();
    }

    public void close() {
        if (comparePopup != null && !comparePopup.isClosed()) {
            comparePopup.close();
        }
    }
}
