package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;

public class DeployModalComponent extends BaseComponent {

    private static final String MODAL_BASE = "//div[contains(@class,'ant-modal-container') and .//form[@id='deploy_form']]";

    private WebElement modal;
    private WebElement repositorySelect;
    private WebElement deploymentNameSelect;
    private WebElement deploymentNameSearchInput;
    private WebElement commentTextarea;
    private WebElement deployButton;
    private WebElement cancelButton;
    private WebElement successNotification;
    private WebElement errorMessage;
    private WebElement firstDropdownOption;
    private WebElement dropdownOption;

    public DeployModalComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public DeployModalComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        modal = new WebElement(page, "xpath=" + MODAL_BASE, "deployModal");
        repositorySelect = new WebElement(page, "xpath=" + MODAL_BASE + "//div[contains(@class,'ant-select') and .//input[@id='deploy_form_repository']]//div[contains(@class,'ant-select-content')]", "repositorySelect");
        deploymentNameSelect = new WebElement(page, "xpath=" + MODAL_BASE + "//div[contains(@class,'ant-select') and .//input[@id='deploy_form_deploymentName']]//div[contains(@class,'ant-select-content')]", "deploymentNameSelect");
        deploymentNameSearchInput = new WebElement(page, "xpath=" + MODAL_BASE + "//input[@id='deploy_form_deploymentName']", "deploymentNameSearchInput");
        commentTextarea = new WebElement(page, "xpath=" + MODAL_BASE + "//textarea[@id='deploy_form_comment']", "commentTextarea");
        deployButton = new WebElement(page, "xpath=" + MODAL_BASE + "//div[contains(@class,'ant-modal-footer')]//button[contains(@class,'ant-btn-primary')]", "deployButton");
        cancelButton = new WebElement(page, "xpath=" + MODAL_BASE + "//div[contains(@class,'ant-modal-footer')]//button[not(contains(@class,'ant-btn-primary'))]", "cancelButton");
        // Ant Design dropdown options render at body level
        firstDropdownOption = new WebElement(page, "xpath=//div[contains(@class,'ant-select-item') and contains(@class,'ant-select-item-option') and @title]", "firstDropdownOption");
        dropdownOption = new WebElement(page, "xpath=//div[contains(@class,'ant-select-item') and contains(@class,'ant-select-item-option') and @title='%s']", "dropdownOption");
        successNotification = new WebElement(page, "xpath=//div[contains(@class,'ant-notification')]//div[contains(@class,'ant-notification-notice-success')]", "successNotification");
        errorMessage = new WebElement(page, "xpath=" + MODAL_BASE + "//div[contains(@class,'ant-form-item-explain-error')]", "errorMessage");
    }

    public boolean isModalVisible() {
        return modal.isVisible(2000);
    }

    public DeployModalComponent waitForModal() {
        boolean modalAppeared = WaitUtil.waitForCondition(() -> isModalVisible(), DEFAULT_TIMEOUT_MS, 500, "Waiting for deploy modal to appear");
        if (!modalAppeared) {
            throw new RuntimeException("Deploy modal did not appear within timeout");
        }
        return this;
    }

    public DeployModalComponent selectRepository(String repositoryName) {
        repositorySelect.click();
        if (repositoryName != null) {
            dropdownOption.format(repositoryName).click();
        } else {
            firstDropdownOption.click();
        }
        return this;
    }

    public DeployModalComponent fillDeploymentName(String name) {
        deploymentNameSelect.click();
        deploymentNameSearchInput.fillSequentially(name).press("Tab");
        // Click outside to trigger onBlur which sets the new deployment name
        //commentTextarea.click();
        return this;
    }

    public DeployModalComponent fillComment(String comment) {
        commentTextarea.fillSequentially(comment);
        return this;
    }

    public void clickDeploy() {
        deployButton.click();
    }

    public void clickCancel() {
        cancelButton.click();
    }

    public void deployWithAllFields(String repository, String deploymentName, String comment) {
        waitForModal();
        selectRepository(repository);
        fillDeploymentName(deploymentName);
        fillComment(comment);
        clickDeploy();
    }

    public void deployWithoutMandatoryFields() {
        waitForModal();
        clickDeploy();
    }

    public boolean isSuccessNotificationVisible() {
        return successNotification.isVisible(5000);
    }

    public boolean isErrorMessageDisplayed() {
        return errorMessage.isVisible(1000);
    }

    public String getErrorMessage() {
        if (isErrorMessageDisplayed()) {
            return errorMessage.getText();
        }
        return "";
    }

    public boolean isDeployButtonEnabled() {
        return deployButton.isEnabled();
    }
}
