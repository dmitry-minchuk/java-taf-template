package domain.ui.webstudio.components.admincomponents;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.pages.mainpages.LoginPage;
import helpers.service.UserService;
import com.microsoft.playwright.options.WaitForSelectorState;
import helpers.utils.WaitUtil;

public class SystemSettingsPageComponent extends BaseComponent {

    private WebElement dispatchingValidationCheckbox;
    private WebElement verifyOnEditCheckbox;
    private WebElement testThreadCountField;
    private WebElement projectHistoryCountField;
    private WebElement clearAllHistoryBtn;
    private WebElement applyButton;
    private WebElement errorMessage;

    public SystemSettingsPageComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public SystemSettingsPageComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        dispatchingValidationCheckbox = createScopedElement("#dispatchingValidationEnabled", "dispatchingValidationCheckbox");
        verifyOnEditCheckbox = createScopedElement("#autoCompile", "verifyOnEditCheckbox");
        testThreadCountField = createScopedElement("#testRunThreadCount", "testThreadCountField");
        projectHistoryCountField = createScopedElement("#projectHistoryCount", "projectHistoryCountField");
        clearAllHistoryBtn = createScopedElement("xpath=.//button[./span[text()='Clear All History']]", "clearAllHistoryBtn");
        applyButton = createScopedElement("xpath=.//button[./span[text()='Apply Changes'] or ./span[text()='Apply']]", "applyButton");
        errorMessage = createScopedElement("xpath=.//div[contains(@class, 'ant-form-item-explain-error')]", "errorMessage");
    }

    public void setDispatchingValidation(boolean enable) {
        if (enable != dispatchingValidationCheckbox.isChecked()) {
            dispatchingValidationCheckbox.click();
        }
    }

    public void setVerifyOnEdit(boolean enable) {
        if (enable != verifyOnEditCheckbox.isChecked()) {
            verifyOnEditCheckbox.click();
        }
    }

    public void setTestThreadCount(String value) {
        testThreadCountField.fill(value);
    }

    public String getErrorMessage() {
        if (errorMessage.isVisible()) {
            return errorMessage.getText();
        }
        return "";
    }

    public void clickApplyButton() {
        applyButton.click();
        getModalOkBtn().click();
    }

    public void applySettingsAndRelogin(User user) {
        clickApplyButton();
        new LoginPage().login(UserService.getUser(user));
    }
}