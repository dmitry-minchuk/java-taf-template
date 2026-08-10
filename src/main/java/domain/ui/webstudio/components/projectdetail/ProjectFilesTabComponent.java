package domain.ui.webstudio.components.projectdetail;

import com.microsoft.playwright.Page;
import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.BaseComponent;

/**
 * The Files tab of the React project-detail view: the file tree with its Add menu and the per-file
 * actions. Tree and buttons are scoped under {@code [data-testid=project-detail]} (verified against the
 * live 6.4.0 DOM); the menus and the upload/update/folder dialogs are antd portals rendered at body level,
 * so those are located at page level on purpose. Callers open the tab first — see
 * {@code ProjectDetailPage.openFilesTab()}.
 */
public class ProjectFilesTabComponent extends BaseComponent {

    // Reading a file's content before its dialog opens can take a while on a large project.
    private static final int FILE_DIALOG_TIMEOUT_MS = DEFAULT_TIMEOUT_MS * 3;

    private final WebElement fileNodeByName;
    private final WebElement addBtn;
    private final WebElement searchField;
    private final WebElement fileActionsBtn;
    // antd portals — rendered at body level, outside the detail subtree
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
        // The Add menu is only rendered for a user who may change the files, so the tab is confirmed by the
        // file search box instead — it is always there.
        searchField = createScopedElement("[data-testid=files-search]", "filesSearchField");
        fileActionsBtn = createScopedElement("[data-testid=file-actions]", "fileActionsBtn");
        // antd binds the menu handler to the <li>, so click that rather than the labelled <span> inside it.
        addMenuItem = new WebElement(page, "xpath=//div[contains(@class,'ant-dropdown')][not(contains(@class,'ant-dropdown-hidden'))]//li[contains(@class,'ant-dropdown-menu-item')][.//span[@data-testid='%s']]", "filesAddMenuItem");
        fileActionsMenuItem = new WebElement(page, "xpath=//div[contains(@class,'ant-dropdown')][not(contains(@class,'ant-dropdown-hidden'))]//li[contains(@class,'ant-dropdown-menu-item')][normalize-space()='%s']", "fileActionsMenuItem");
        fileDeleteSubmitBtn = new WebElement(page, "[data-testid=file-delete-submit]", "fileDeleteSubmitBtn");
        // As with the upload dragger, antd puts the testid on the file input itself.
        updateFileInput = new WebElement(page, "input[data-testid=update-file-dragger]", "updateFileInput");
        updateFileSubmitBtn = new WebElement(page, "[data-testid=update-file-submit]", "updateFileSubmitBtn");
        updateFileNameWarning = new WebElement(page, "[data-testid=update-file-name-warning]", "updateFileNameWarning");
        // antd Upload.Dragger forwards unknown props to the file input itself, so the testid lands there.
        uploadInput = new WebElement(page, "input[data-testid=files-upload-dragger]", "filesUploadInput");
        uploadNameField = new WebElement(page, "[data-testid=files-upload-name]", "filesUploadNameField");
        // The path field is an AutoComplete wrapper, so type into its inner input.
        uploadPathField = new WebElement(page, "[data-testid=files-upload-path] input", "filesUploadPathField");
        uploadSubmitBtn = new WebElement(page, "[data-testid=files-upload-submit]", "filesUploadSubmitBtn");
        // files-folder-path is an antd AutoComplete wrapper (a DIV); the typeable field is its inner input.
        folderPathInput = new WebElement(page, "[data-testid=files-folder-path] input", "folderPathInput");
        folderSubmitBtn = new WebElement(page, "[data-testid=files-folder-submit]", "folderSubmitBtn");
    }

    /** The search box that confirms the Files tab is really open (the Add button is permission-gated). */
    public boolean isOpen(int timeoutInMillis) {
        return searchField.isVisible(timeoutInMillis);
    }

    public void waitForOpen(int timeoutInMillis) {
        searchField.waitForVisible(timeoutInMillis);
    }

    /**
     * Deletes a file: select it in the tree, then Delete from the preview pane's actions menu and confirm.
     * The tree node goes away asynchronously, so wait it out — otherwise a following isFilePresent check
     * races the removal and still sees the node.
     */
    public void deleteFile(String fileName) {
        fileNodeByName.format(fileName).click();
        fileActionsBtn.waitForVisible(DEFAULT_TIMEOUT_MS).click();
        fileActionsMenuItem.format("Delete").click();
        fileDeleteSubmitBtn.click();
        waitUntilSpinnerLoaded();
        fileNodeByName.format(fileName).waitForHidden(DEFAULT_TIMEOUT_MS);
    }

    // Folders and files are both tree nodes, so this also answers "is this folder present?".
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

    /**
     * Whether the tab offers its Add menu (New folder / New text file / Upload). It is only rendered for a
     * user who may change the project's files, so this is how "can this user edit here?" is checked.
     */
    public boolean isAddMenuAvailable() {
        return addBtn.isVisible(DEFAULT_TIMEOUT_MS);
    }

    /** Opens the Update dialog for a file and picks the replacement, leaving the dialog open. */
    public void pickUpdateFile(String fileName, String newFilePath) {
        fileNodeByName.format(fileName).click();
        // Selecting a file opens its preview pane, where the actions menu lives.
        fileActionsBtn.waitForVisible(DEFAULT_TIMEOUT_MS).click();
        fileActionsMenuItem.format("Update").click();
        // The dragger's file input is hidden, so wait on the dialog's own button and feed the input directly.
        updateFileSubmitBtn.waitForVisible(FILE_DIALOG_TIMEOUT_MS);
        updateFileInput.setInputFiles(newFilePath);
    }

    /** Confirms the Update dialog opened by {@link #pickUpdateFile}. */
    public void confirmUpdateFile() {
        updateFileSubmitBtn.click();
        waitUntilSpinnerLoaded();
    }

    /** Whether the update dialog warns that the chosen file has a different name. */
    public boolean isUpdateFileNameWarningShown() {
        return updateFileNameWarning.isVisible(DEFAULT_TIMEOUT_MS / 2);
    }

    // Creates a folder (or path/to/folder) in the project.
    public void createFolder(String folderPath) {
        openAddMenuItem("files-new-folder");
        folderPathInput.fill(folderPath);
        folderSubmitBtn.click();
        waitUntilSpinnerLoaded();
    }

    // The Files tab gathers New folder / New text file / Upload behind a single Add menu. That menu is an
    // antd Dropdown with the default HOVER trigger, so clicking the button toggles it shut again — hover it.
    private void openAddMenuItem(String itemTestId) {
        addBtn.hover();
        addMenuItem.format(itemTestId).click();
    }
}
