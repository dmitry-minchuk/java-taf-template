package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class CopyProjectDialogComponent extends BaseComponent {

    private static final Logger LOGGER = LogManager.getLogger(CopyProjectDialogComponent.class);

    private WebElement dialogContainer;
    private WebElement newBranchNameField;
    private WebElement copyButton;
    private WebElement cancelButton;
    private WebElement currentProjectNameDisplay;
    private WebElement currentBranchDisplay;
    private WebElement newProjectNameField;
    private WebElement commentField;
    private WebElement copyOldVersionCheckbox;
    private WebElement revisionsCountField;
    private WebElement separateProjectCheckbox;
    private WebElement repositorySelect;
    private WebElement projectFolderField;
    private List<WebElement> errors;

    public CopyProjectDialogComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public CopyProjectDialogComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        dialogContainer = createScopedElement("xpath=//div[@id='modalCopyProject_container']", "dialogContainer");
        newBranchNameField = createScopedElement("xpath=.//input[@id='copyProjectForm:newBranchName']", "newBranchNameField");
        copyButton = createScopedElement("xpath=.//form[@id='copyProjectForm']//input[@value='Copy']", "copyButton");
        cancelButton = createScopedElement("xpath=.//form[@id='copyProjectForm']//input[@value='Cancel']", "cancelButton");
        currentProjectNameDisplay = createScopedElement("xpath=.//div[@id='copyProjectForm:currentProjectName']", "currentProjectNameDisplay");
        currentBranchDisplay = createScopedElement("xpath=.//span[@id='copyProjectForm:currentBranchName']", "currentBranchDisplay");
        newProjectNameField = createScopedElement("xpath=.//input[@id='copyProjectForm:newProjectName']", "newProjectNameField");
        commentField = createScopedElement("xpath=.//textarea[@id='copyProjectForm:comment']", "commentField");
        copyOldVersionCheckbox = createScopedElement("xpath=.//input[@id='copyProjectForm:copyOldRevisions']", "copyOldVersionCheckbox");
        revisionsCountField = createScopedElement("xpath=.//input[@name='copyProjectForm:revisionsCount']", "revisionsCountField");
        separateProjectCheckbox = createScopedElement("xpath=.//input[@id='copyProjectForm:separateProjectCheckbox']", "separateProjectCheckbox");
        repositorySelect = createScopedElement("xpath=.//select[@id='copyProjectForm:repository']", "repositorySelect");
        projectFolderField = createScopedElement("xpath=.//input[@id='copyProjectForm:projectFolder']", "projectFolderField");
        errors = createScopedElementList("xpath=.//span[@class='error']", "errors");
    }

    public CopyProjectDialogComponent setNewBranchName(String branchName) {
        LOGGER.info("Setting new branch name: {}", branchName);
        newBranchNameField.fill(branchName);
        WaitUtil.sleep(500, "Waiting for branch name to be entered");
        return this;
    }

    public String getNewBranchName() {
        return newBranchNameField.getAttribute("value");
    }

    public CopyProjectDialogComponent setNewProjectName(String projectName) {
        LOGGER.info("Setting new project name: {}", projectName);
        newProjectNameField.fill(projectName);
        return this;
    }

    public String getNewProjectName() {
        return newProjectNameField.getAttribute("value");
    }

    public CopyProjectDialogComponent setComment(String comment) {
        LOGGER.info("Setting copy comment");
        commentField.fill(comment);
        return this;
    }

    public String getComment() {
        return commentField.getAttribute("value");
    }

    public CopyProjectDialogComponent setCopyOldVersion(boolean enabled) {
        if (enabled) {
            copyOldVersionCheckbox.check();
        } else {
            copyOldVersionCheckbox.uncheck();
        }
        return this;
    }

    public CopyProjectDialogComponent setRevisionsCount(String count) {
        revisionsCountField.fill(count);
        return this;
    }

    public CopyProjectDialogComponent setSeparateProject(boolean enabled) {
        if (enabled) {
            separateProjectCheckbox.check();
        } else {
            separateProjectCheckbox.uncheck();
        }
        return this;
    }

    public CopyProjectDialogComponent selectRepository(String repositoryName) {
        repositorySelect.selectByVisibleText(repositoryName);
        return this;
    }

    public CopyProjectDialogComponent setProjectFolder(String folderPath) {
        projectFolderField.fill(folderPath);
        return this;
    }

    public String getCurrentProjectName() {
        return currentProjectNameDisplay.getText();
    }

    public String getCurrentBranch() {
        return currentBranchDisplay.getText();
    }

    public void clickCopyButton(boolean waitForDialogToClose) {
        LOGGER.info("Clicking Copy button");
        copyButton.click();
        if(waitForDialogToClose)
            waitForDialogToClose();
    }

    public void clickCopyButton() {
        clickCopyButton(true);
    }

    public void clickCancelButton() {
        LOGGER.info("Clicking Cancel button");
        cancelButton.click();
    }

    public boolean isDialogVisible() {
        return dialogContainer.isVisible(1000);
    }

    public void waitForDialogToAppear() {
        WaitUtil.waitForCondition(this::isDialogVisible, 5000, 100, "Waiting for Copy Project dialog to appear");
    }

    public void waitForDialogToClose() {
        WaitUtil.waitForCondition(() -> !isDialogVisible(), 5000, 100, "Waiting for Copy Project dialog to close");
    }

    public List<String> getErrors() {
        return errors.stream().map(WebElement::getText).toList();
    }
}
