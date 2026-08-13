package domain.ui.webstudio.components.projectdetail;

import com.microsoft.playwright.Page;
import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.BaseComponent;

public class ProjectFilesTabComponent extends BaseComponent {

    private static final int FILE_DIALOG_TIMEOUT_MS = DEFAULT_TIMEOUT_MS * 3;

    private final WebElement fileNodeByName;
    private final WebElement addBtn;
    private final WebElement searchField;
    private final WebElement fileActionsBtn;
    private final WebElement addMenuItem;
    private final WebElement fileActionsMenuItem;
    private final WebElement fileDeleteSubmitBtn;
    private final WebElement updateFileInput;
    private final WebElement updateFileSubmitBtn;
    private final WebElement updateFileNameWarning;
    private final WebElement uploadInput;
    private final WebElement uploadNameField;
    private final WebElement uploadPathField;
    private final WebElement uploadSubmitBtn;
    private final WebElement folderPathInput;
    private final WebElement folderSubmitBtn;

    public ProjectFilesTabComponent(Page page) {
        this(new WebElement(page, "[data-testid=project-detail]", "projectDetail"));
    }

    public ProjectFilesTabComponent(WebElement rootLocator) {
        super(rootLocator);
        fileNodeByName = createScopedElement("xpath=.//div[@role='treeitem'][.//*[normalize-space()='%s']]", "fileTreeNode");
        addBtn = createScopedElement("[data-testid=files-add]", "filesAddBtn");
        searchField = createScopedElement("[data-testid=files-search]", "filesSearchField");
        fileActionsBtn = createScopedElement("[data-testid=file-actions]", "fileActionsBtn");
        addMenuItem = new WebElement(page, "xpath=//div[contains(@class,'ant-dropdown')][not(contains(@class,'ant-dropdown-hidden'))]//li[contains(@class,'ant-dropdown-menu-item')][.//span[@data-testid='%s']]", "filesAddMenuItem");
        fileActionsMenuItem = new WebElement(page, "xpath=//div[contains(@class,'ant-dropdown')][not(contains(@class,'ant-dropdown-hidden'))]//li[contains(@class,'ant-dropdown-menu-item')][normalize-space()='%s']", "fileActionsMenuItem");
        fileDeleteSubmitBtn = new WebElement(page, "[data-testid=file-delete-submit]", "fileDeleteSubmitBtn");
        updateFileInput = new WebElement(page, "input[data-testid=update-file-dragger]", "updateFileInput");
        updateFileSubmitBtn = new WebElement(page, "[data-testid=update-file-submit]", "updateFileSubmitBtn");
        updateFileNameWarning = new WebElement(page, "[data-testid=update-file-name-warning]", "updateFileNameWarning");
        uploadInput = new WebElement(page, "input[data-testid=files-upload-dragger]", "filesUploadInput");
        uploadNameField = new WebElement(page, "[data-testid=files-upload-name]", "filesUploadNameField");
        uploadPathField = new WebElement(page, "[data-testid=files-upload-path] input", "filesUploadPathField");
        uploadSubmitBtn = new WebElement(page, "[data-testid=files-upload-submit]", "filesUploadSubmitBtn");
        folderPathInput = new WebElement(page, "[data-testid=files-folder-path] input", "folderPathInput");
        folderSubmitBtn = new WebElement(page, "[data-testid=files-folder-submit]", "folderSubmitBtn");
    }

    public boolean isOpen(int timeoutInMillis) {
        return searchField.isVisible(timeoutInMillis);
    }

    public void waitForOpen(int timeoutInMillis) {
        searchField.waitForVisible(timeoutInMillis);
    }

    public void deleteFile(String fileName) {
        fileNodeByName.format(fileName).click();
        fileActionsBtn.waitForVisible(DEFAULT_TIMEOUT_MS).click();
        fileActionsMenuItem.format("Delete").click();
        fileDeleteSubmitBtn.click();
        waitUntilSpinnerLoaded();
        fileNodeByName.format(fileName).waitForHidden(DEFAULT_TIMEOUT_MS);
    }

    public void selectFile(String fileName) {
        fileNodeByName.format(fileName).waitForVisible(DEFAULT_TIMEOUT_MS).click();
    }

    public boolean isResourceNotFoundShown() {
        return page.locator("xpath=//*[not(*)][contains(normalize-space(.),'The resource is not found')]").count() > 0;
    }

    public boolean isNodePresent(String nodeName) {
        return fileNodeByName.format(nodeName).isVisible(DEFAULT_TIMEOUT_MS);
    }

    public void uploadFileAs(String filePath, String targetName, String targetFolder) {
        openAddMenuItem("files-upload");
        uploadInput.setInputFiles(filePath);
        if (targetName != null && !targetName.isEmpty()) {
            uploadNameField.waitForVisible(DEFAULT_TIMEOUT_MS).fill(targetName);
        }
        if (targetFolder != null && !targetFolder.isEmpty()) {
            uploadPathField.waitForVisible(DEFAULT_TIMEOUT_MS).fill(targetFolder);
        }
        uploadSubmitBtn.click();
        waitUntilSpinnerLoaded();
    }

    public boolean isAddMenuAvailable() {
        return addBtn.isVisible(DEFAULT_TIMEOUT_MS);
    }

    public void pickUpdateFile(String fileName, String newFilePath) {
        fileNodeByName.format(fileName).click();
        fileActionsBtn.waitForVisible(DEFAULT_TIMEOUT_MS).click();
        fileActionsMenuItem.format("Update").click();
        updateFileSubmitBtn.waitForVisible(FILE_DIALOG_TIMEOUT_MS);
        updateFileInput.setInputFiles(newFilePath);
    }

    public void confirmUpdateFile() {
        updateFileSubmitBtn.click();
        waitUntilSpinnerLoaded();
    }

    public boolean isUpdateFileNameWarningShown() {
        return updateFileNameWarning.isVisible(DEFAULT_TIMEOUT_MS / 2);
    }

    public void createFolder(String folderPath) {
        openAddMenuItem("files-new-folder");
        folderPathInput.fill(folderPath);
        folderSubmitBtn.click();
        waitUntilSpinnerLoaded();
    }

    private void openAddMenuItem(String itemTestId) {
        addBtn.hover();
        addMenuItem.format(itemTestId).click();
    }
}
