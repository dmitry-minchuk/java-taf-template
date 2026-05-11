package domain.ui.webstudio.components.repositorytabcomponents;

import com.microsoft.playwright.Dialog;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class CopyProjectDialogComponent extends BaseComponent {

    private static final Logger LOGGER = LogManager.getLogger(CopyProjectDialogComponent.class);

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
    private String expectedNewBranchName;

    public CopyProjectDialogComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public CopyProjectDialogComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
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
        expectedNewBranchName = branchName;
        fillNewBranchName(branchName);
        return this;
    }

    public String getNewBranchName() {
        return newBranchNameField.getCurrentInputValue();
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
        // The repository select fires JSF AJAX (mojarra.ab) on change which re-renders
        // the newProject table including projectFolder. Use waitForResponse to ensure
        // the AJAX completes before we attempt to fill the path field.
        repositorySelect.getPage().waitForResponse(
                response -> response.url().contains("index.xhtml"),
                () -> repositorySelect.selectByVisibleText(repositoryName)
        );
        return this;
    }

    public CopyProjectDialogComponent setProjectFolder(String folderPath) {
        projectFolderField.clear();
        projectFolderField.fillSequentially(folderPath);
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
        ensureExpectedNewBranchName();
        LocalDriverPool.getPage().onceDialog(Dialog::accept);
        copyButton.click();
        if (waitForDialogToClose) {
            waitForDialogToClose();
        }
    }

    public void clickCopyButton() {
        copyButton.press("Tab");
        clickCopyButton(true);
    }

    public void clickCancelButton() {
        LOGGER.info("Clicking Cancel button");
        cancelButton.click();
    }

    public CopyProjectDialogComponent waitForDialogToAppear() {
        getRootLocator().waitForVisible(5000);
        return this;
    }

    public void waitForDialogToClose() {
        try {
            getRootLocator().waitForHidden(5000);
        } catch (Exception e) {
            List<String> visibleErrors = getErrors();
            if (!visibleErrors.isEmpty()) {
                throw new RuntimeException("Copy dialog did not close. Errors: " + visibleErrors, e);
            }
            throw e;
        }
    }

    public List<String> getErrors() {
        return errors.stream().map(WebElement::getText).toList();
    }

    public List<String> waitForErrors(int timeoutMs) {
        WaitUtil.waitForCondition(() -> !errors.isEmpty(), timeoutMs, 100, "Waiting for copy dialog errors to appear");
        return getErrors();
    }

    private void fillNewBranchName(String branchName) {
        boolean branchNameSet = WaitUtil.retryAction(() -> {
            WaitUtil.sleep(1000, "Extra wait for fillNewBranchName() method (before filling new branch name)");
            newBranchNameField.waitForVisible();
            newBranchNameField.clear();
            newBranchNameField.fillSequentially(branchName);
            WaitUtil.sleep(1000, "Extra wait for fillNewBranchName() method (after filling new branch name)");
            String actualBranchName = getNewBranchName();
            if (!branchName.equals(actualBranchName)) {
                throw new RuntimeException(String.format(
                        "Expected new branch name '%s', but field value is '%s'",
                        branchName,
                        actualBranchName
                ));
            }
        }, 5000, 200, "Setting and verifying new branch name");

        if (!branchNameSet) {
            throw new RuntimeException("Failed to set new branch name to '" + branchName + "'");
        }
    }

    private void ensureExpectedNewBranchName() {
        if (expectedNewBranchName == null) {
            return;
        }

        if (!expectedNewBranchName.equals(getNewBranchName())) {
            fillNewBranchName(expectedNewBranchName);
        }
    }
}
