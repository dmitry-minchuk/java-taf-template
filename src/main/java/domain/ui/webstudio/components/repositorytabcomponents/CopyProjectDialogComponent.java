package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * React "Copy project" dialog, opened from a project row's Copy action.
 *
 * <p>Studio 6.4.0 restored the legacy capabilities the first React cut had dropped: the dialog opens in
 * BRANCH mode (copy-project-branch) whenever the project's repository supports branching, and the
 * copy-project-as-new checkbox switches it to NEW-PROJECT mode (name / target repository / path /
 * comment), which also offers copying from an older revision.
 */
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
    private WebElement asNewProjectCheckbox;
    private WebElement branchField;
    private WebElement currentBranchLabel;
    private WebElement oldRevisionCheckbox;
    private WebElement revisionSelect;
    private WebElement revisionOption;
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
        asNewProjectCheckbox = new WebElement(page, "[data-testid=copy-project-as-new]", "copyProjectAsNew");
        branchField = new WebElement(page, "[data-testid=copy-project-branch]", "copyProjectBranch");
        currentBranchLabel = new WebElement(page, "[data-testid=copy-project-current-branch]", "copyProjectCurrentBranch");
        oldRevisionCheckbox = new WebElement(page, "[data-testid=copy-project-old-revision]", "copyProjectOldRevision");
        revisionSelect = new WebElement(page, "[data-testid=copy-project-revision]", "copyProjectRevision");
        revisionOption = new WebElement(page, "xpath=//div[contains(@class,'ant-select-item-option')][.//*[normalize-space(text())='%s'] or @title='%s']", "copyProjectRevisionOption");
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
        copyButton.waitForVisible(DEFAULT_TIMEOUT_MS);
        return this;
    }

    /**
     * Switches the dialog into NEW-PROJECT mode (name / target repository / path). On a branching
     * repository the dialog opens in branch mode, so copying into a new project must tick this first;
     * where the checkbox is absent (repository without branches) the dialog is already in that mode.
     */
    public CopyProjectDialogComponent setAsNewProject() {
        if (asNewProjectCheckbox.isVisible(DEFAULT_TIMEOUT_MS / 5) && !asNewProjectCheckbox.isChecked()) {
            asNewProjectCheckbox.click();
        }
        newProjectNameField.waitForVisible(DEFAULT_TIMEOUT_MS);
        return this;
    }

    /**
     * Copies the project into a NEW branch (branch mode — the dialog's default on a git repository).
     *
     * <p>The dialog suggests a branch name of its own ("&lt;project&gt;/&lt;user&gt;/&lt;date&gt;") once the
     * repository config arrives, and that late write also clears its "user edited this" flag — so typing
     * before the suggestion lands is silently undone. Wait for the suggestion, then replace it.
     */
    public CopyProjectDialogComponent setBranchName(String branchName) {
        branchField.waitForVisible(DEFAULT_TIMEOUT_MS);
        WaitUtil.waitForCondition(() -> !branchField.getCurrentInputValue().isBlank(),
                DEFAULT_TIMEOUT_MS, 200, "Waiting for the copy dialog's suggested branch name");
        branchField.fill(branchName);
        return this;
    }

    /** Copies from an earlier revision instead of the latest one (new-project mode only). */
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
        return branchField.getCurrentInputValue();
    }

    /** The branch the copied project currently sits on, as shown by the dialog. */
    public String getCurrentBranch() {
        return currentBranchLabel.waitForVisible(DEFAULT_TIMEOUT_MS).getText().trim();
    }

    // "Copy as a separate project" is expressed by the as-new checkbox in 6.4.0.
    public CopyProjectDialogComponent setSeparateProject(boolean enabled) {
        return enabled ? setAsNewProject() : this;
    }
}
