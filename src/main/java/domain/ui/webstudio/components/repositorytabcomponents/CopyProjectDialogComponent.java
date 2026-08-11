package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.DriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class CopyProjectDialogComponent extends BaseComponent {

    private static final Logger LOGGER = LogManager.getLogger(CopyProjectDialogComponent.class);

    private static final String MODAL_ROOT =
            "//div[contains(@class,'ant-modal')][.//*[contains(@data-testid,'copy-project')]]";
    private static final String VISIBLE_DROPDOWN =
            "//div[contains(@class,'ant-select-dropdown') and not(contains(@class,'ant-select-dropdown-hidden'))]";
    private static final String OPTION_BY_TEXT =
            "//div[contains(@class,'ant-select-item-option')][.//*[normalize-space(text())='%s'] or @title='%s']";
    private static final int SELECTION_ATTEMPTS = 3;
    private static final int SELECTION_CONFIRM_MS = DEFAULT_TIMEOUT_MS / 5;

    private WebElement newProjectNameField;
    private WebElement projectFolderField;
    private WebElement commentField;
    private WebElement copyButton;
    private WebElement cancelButton;
    private WebElement repositorySelect;
    private WebElement repositoryOption;
    private WebElement asNewProjectCheckbox;
    private WebElement branchField;
    private WebElement currentBranchLabel;
    private WebElement oldRevisionCheckbox;
    private WebElement revisionSelect;
    private WebElement revisionOption;
    private List<WebElement> errors;

    public CopyProjectDialogComponent() {
        super(DriverPool.getPage());
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
        repositoryOption = new WebElement(page, "xpath=" + VISIBLE_DROPDOWN + OPTION_BY_TEXT, "copyProjectRepoOption");
        asNewProjectCheckbox = new WebElement(page, "[data-testid=copy-project-as-new]", "copyProjectAsNew");
        branchField = new WebElement(page, "[data-testid=copy-project-branch]", "copyProjectBranch");
        currentBranchLabel = new WebElement(page, "[data-testid=copy-project-current-branch]", "copyProjectCurrentBranch");
        oldRevisionCheckbox = new WebElement(page, "[data-testid=copy-project-old-revision]", "copyProjectOldRevision");
        revisionSelect = new WebElement(page, "[data-testid=copy-project-revision]", "copyProjectRevision");
        revisionOption = new WebElement(page, "xpath=" + VISIBLE_DROPDOWN + OPTION_BY_TEXT, "copyProjectRevisionOption");
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
        for (int attempt = 1; attempt <= SELECTION_ATTEMPTS; attempt++) {
            repositorySelect.click();
            repositoryOption.format(repositoryName, repositoryName).click();
            boolean selected = WaitUtil.waitForCondition(
                    () -> repositoryName.equals(repositorySelect.getText().trim()),
                    SELECTION_CONFIRM_MS, 200,
                    "Waiting for the copy dialog to show " + repositoryName + " as the target repository");
            if (selected) {
                return this;
            }
            LOGGER.warn("Target repository is still '{}' after clicking '{}' (attempt {}/{}), retrying",
                    repositorySelect.getText().trim(), repositoryName, attempt, SELECTION_ATTEMPTS);
        }
        throw new IllegalStateException("The copy dialog did not accept '" + repositoryName
                + "' as the target repository; it shows '" + repositorySelect.getText().trim() + "'");
    }

    public String getSelectedRepository() {
        return repositorySelect.getText().trim();
    }

    public CopyProjectDialogComponent setProjectFolder(String folderPath) {
        projectFolderField.waitForVisible(DEFAULT_TIMEOUT_MS);
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
        copyButton.waitForVisible(DEFAULT_TIMEOUT_MS);
        return this;
    }

    public CopyProjectDialogComponent setAsNewProject() {
        if (asNewProjectCheckbox.isVisible(DEFAULT_TIMEOUT_MS / 5) && !asNewProjectCheckbox.isChecked()) {
            asNewProjectCheckbox.click();
        }
        newProjectNameField.waitForVisible(DEFAULT_TIMEOUT_MS);
        return this;
    }

    public CopyProjectDialogComponent setBranchName(String branchName) {
        branchField.waitForVisible(DEFAULT_TIMEOUT_MS);
        WaitUtil.waitForCondition(() -> !branchField.getCurrentInputValue().isBlank(),
                DEFAULT_TIMEOUT_MS, 200, "Waiting for the copy dialog's suggested branch name");
        branchField.fill(branchName);
        return this;
    }

    public CopyProjectDialogComponent setOldRevision(String revisionLabel) {
        if (!oldRevisionCheckbox.isChecked()) {
            oldRevisionCheckbox.click();
        }
        revisionSelect.waitForVisible(DEFAULT_TIMEOUT_MS).click();
        revisionOption.format(revisionLabel, revisionLabel).click();
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

    public CopyProjectDialogComponent setNewBranchName(String branchName) {
        return setBranchName(branchName);
    }

    public String getNewBranchName() {
        branchField.waitForVisible(DEFAULT_TIMEOUT_MS);
        WaitUtil.waitForCondition(() -> !branchField.getCurrentInputValue().isBlank(),
                DEFAULT_TIMEOUT_MS, 200, "Waiting for the suggested branch name");
        return branchField.getCurrentInputValue();
    }

    public String getCurrentBranch() {
        return currentBranchLabel.waitForVisible(DEFAULT_TIMEOUT_MS).getText().trim();
    }

    public CopyProjectDialogComponent setSeparateProject(boolean enabled) {
        return enabled ? setAsNewProject() : this;
    }
}
