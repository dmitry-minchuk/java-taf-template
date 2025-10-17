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

    public void setTestThreadCount(int count) {
        testThreadCountField.fill(String.valueOf(count));
    }

    public int getTestThreadCount() {
        String value = testThreadCountField.getAttribute("value");
        return value != null && !value.isEmpty() ? Integer.parseInt(value) : 1;
    }

    public void setProjectHistoryCount(int count) {
        projectHistoryCountField.fill(String.valueOf(count));
    }

    public int getProjectHistoryCount() {
        String value = projectHistoryCountField.getAttribute("value");
        return value != null && !value.isEmpty() ? Integer.parseInt(value) : 10;
    }

    public void clearAllHistory() {
        clearAllHistoryBtn.click();
    }

    // New methods for test validation and error handling

    public void setTestThreadCount(String value) {
        testThreadCountField.fill(value);
    }

    public boolean isDispatchingValidationEnabled() {
        return dispatchingValidationCheckbox.isChecked();
    }

    public boolean isVerifyOnEditEnabled() {
        return verifyOnEditCheckbox.isChecked();
    }

    public String getErrorMessage() {
        if (errorMessage.isVisible()) {
            return errorMessage.getText();
        }
        return "";
    }

    public void clickApplyButton() {
        applyButton.click();
        // Wait for potential alert and handle it
        try {
            page.waitForCondition(() -> {
                if (page.locator("role=dialog").isVisible()) {
                    return true;
                }
                return false;
            }, new com.microsoft.playwright.Page.WaitForConditionOptions().setTimeout(2000));

            // If alert appears, accept it
            page.locator("role=dialog >> button:has-text('OK'), button:has-text('Yes')").click();
        } catch (Exception e) {
            // No alert appeared, continue
        }
    }

    public void applySettingsAndRelogin(User user) {
        clickApplyButton();
        WaitUtil.waitForCondition(() -> page.url().contains("/login"), DEFAULT_TIMEOUT_MS, 250, "Waiting for login page to be ready");
        new LoginPage().login(UserService.getUser(user));
    }
}