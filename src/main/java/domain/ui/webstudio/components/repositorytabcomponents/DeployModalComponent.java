package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;

public class DeployModalComponent extends BaseComponent {

    private WebElement modal;
    private WebElement repositoryDropdown;
    private WebElement deploymentNameInput;
    private WebElement commentTextarea;
    private WebElement deployButton;
    private WebElement cancelButton;
    private WebElement errorMessage;

    public DeployModalComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public DeployModalComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        // Modal container
        modal = createScopedElement("xpath=.//div[@id='modalDeploy_container' or contains(@class,'modal-deploy')]", "deployModal");

        // Form elements - using typical JSF form naming patterns
        repositoryDropdown = createScopedElement("xpath=.//select[@id='deployForm:repository' or contains(@id,'repository')]", "repositoryDropdown");
        deploymentNameInput = createScopedElement("xpath=.//input[@id='deployForm:deploymentName' or contains(@id,'deploymentName')]", "deploymentNameInput");
        commentTextarea = createScopedElement("xpath=.//textarea[@id='deployForm:comment' or contains(@id,'comment')]", "commentTextarea");

        // Action buttons
        deployButton = createScopedElement("xpath=.//input[@value='Deploy' and @type='submit']", "deployButton");
        cancelButton = createScopedElement("xpath=.//input[@value='Cancel' and @type='button']", "cancelButton");

        // Error message
        errorMessage = createScopedElement("xpath=.//span[contains(@class,'error') or contains(@class,'rf-msg')]", "errorMessage");
    }

    public boolean isModalVisible() {
        return modal.isVisible(2000);
    }

    public DeployModalComponent waitForModal() {
        boolean modalAppeared = WaitUtil.waitForCondition(() -> isModalVisible(), DEFAULT_TIMEOUT_MS, 500);
        if (!modalAppeared) {
            throw new RuntimeException("Deploy modal did not appear within timeout");
        }
        return this;
    }

    public DeployModalComponent selectRepository(String repositoryName) {
        repositoryDropdown.selectByVisibleText(repositoryName);
        return this;
    }

    public DeployModalComponent fillDeploymentName(String name) {
        deploymentNameInput.fillSequentially(name);
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
