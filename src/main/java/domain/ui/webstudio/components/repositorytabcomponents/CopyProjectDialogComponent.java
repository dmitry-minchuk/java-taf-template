package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

// React "Copy project" dialog (build 032c60a664ce+), opened from a project row's Copy action.
// Fields (verified live): copy-project-repository (ant-select), copy-project-name, copy-project-path,
// copy-project-comment, copy-project-submit. The legacy branch / separate-project / copy-old-revisions
// options were removed in React (their setters are kept as no-op shims so not-yet-migrated legacy tests
// still compile).
public class CopyProjectDialogComponent extends BaseComponent {

    private static final Logger LOGGER = LogManager.getLogger(CopyProjectDialogComponent.class);

    private static final String MODAL_ROOT =
            "//div[contains(@class,'ant-modal')][.//*[contains(@data-testid,'copy-project')]]";

    private WebElement newProjectNameField;
    private WebElement projectFolderField;
    private WebElement commentField;
    private WebElement copyButton;
    private WebElement cancelButton;
    private WebElement repositorySelect;
    private WebElement repositoryOption;
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
        newProjectNameField = new WebElement(page, "[data-testid=copy-project-name]", "copyProjectName");
        projectFolderField = new WebElement(page, "[data-testid=copy-project-path]", "copyProjectPath");
        commentField = new WebElement(page, "[data-testid=copy-project-comment]", "copyProjectComment");
        copyButton = new WebElement(page, "[data-testid=copy-project-submit]", "copyProjectSubmit");
        cancelButton = new WebElement(page, "xpath=" + MODAL_ROOT + "//div[contains(@class,'ant-modal-footer')]//button[not(contains(@class,'ant-btn-primary'))]", "copyProjectCancel");
        repositorySelect = new WebElement(page, "[data-testid=copy-project-repository]", "copyProjectRepository");
        repositoryOption = new WebElement(page, "xpath=//div[contains(@class,'ant-select-item-option')][.//*[normalize-space(text())='%s'] or @title='%s']", "copyProjectRepoOption");
        errors = createElementList("xpath=" + MODAL_ROOT + "//div[contains(@class,'ant-form-item-explain-error')] | " + MODAL_ROOT + "//div[contains(@class,'ant-alert-error')]", "copyProjectErrors");
    }

    public CopyProjectDialogComponent setNewProjectName(String projectName) {
        LOGGER.info("Setting new project name: {}", projectName);
        newProjectNameField.fill(projectName);
        return this;
    }

    public String getNewProjectName() {
        return newProjectNameField.getCurrentInputValue();
    }

    public CopyProjectDialogComponent setComment(String comment) {
        commentField.fill(comment);
        return this;
    }

    public CopyProjectDialogComponent selectRepository(String repositoryName) {
        LOGGER.info("Selecting copy target repository: {}", repositoryName);
        repositorySelect.click();
        repositoryOption.format(repositoryName, repositoryName).click();
        return this;
    }

    public CopyProjectDialogComponent setProjectFolder(String folderPath) {
        projectFolderField.clear();
        projectFolderField.fill(folderPath);
        return this;
    }

    public void clickCopyButton(boolean waitForDialogToClose) {
        LOGGER.info("Clicking Copy button");
        copyButton.click();
        if (waitForDialogToClose) {
            waitForDialogToClose();
        }
    }

    public void clickCopyButton() {
        clickCopyButton(true);
    }

    public void clickCancelButton() {
        cancelButton.click();
    }

    public CopyProjectDialogComponent waitForDialogToAppear() {
        newProjectNameField.waitForVisible(5000);
        return this;
    }

    public void waitForDialogToClose() {
        try {
            copyButton.waitForHidden(5000);
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

    // --- Legacy no-op/compat shims: branch / separate-project / copy-old-revisions were removed in the React
    // copy dialog. Kept so not-yet-migrated legacy tests still compile; they are no longer functional. ---
    public CopyProjectDialogComponent setSeparateProject(boolean enabled) {
        return this;
    }

    public CopyProjectDialogComponent setNewBranchName(String branchName) {
        return this;
    }

    public String getNewBranchName() {
        return "";
    }

    public String getCurrentBranch() {
        return "";
    }
}
