package domain.ui.webstudio.pages.mainpages;

import configuration.core.ui.WebElement;
import domain.ui.webstudio.pages.BasePage;

// React project-detail view (/projects/<id>) introduced in build 032c60a664ce: Overview / Files /
// History / Branches / Publish tabs. Reached from the projects list via RepositoryPage.openProjectDetail.
public class ProjectDetailPage extends BasePage {

    // Tabs
    private WebElement filesTab;
    // Files tab: a file tree on the left, a preview panel with per-file actions on the right
    private WebElement fileNodeByName;      // format(fileName)
    private WebElement fileDeleteBtn;
    private WebElement fileDeleteConfirmOk;

    public ProjectDetailPage() {
        super();
        initializeComponents();
    }

    private void initializeComponents() {
        filesTab = new WebElement(page, "xpath=//div[@data-node-key='files']", "filesTab");
        fileNodeByName = new WebElement(page, "xpath=//div[@role='treeitem'][.//*[normalize-space()='%s']]", "fileTreeNode");
        fileDeleteBtn = new WebElement(page, "[data-testid=file-delete]", "fileDeleteBtn");
        fileDeleteConfirmOk = new WebElement(page, "xpath=//div[contains(@class,'ant-popover')]//button[.//span[normalize-space()='OK']]", "fileDeleteConfirmOk");
    }

    public ProjectDetailPage openFilesTab() {
        filesTab.click();
        waitUntilSpinnerLoaded();
        return this;
    }

    // Selecting a file reveals the preview panel with its delete action; deletion is confirmed via a popconfirm.
    public ProjectDetailPage deleteFile(String fileName) {
        fileNodeByName.format(fileName).click();
        fileDeleteBtn.click();
        fileDeleteConfirmOk.click();
        waitUntilSpinnerLoaded();
        return this;
    }

    public boolean isFilePresent(String fileName) {
        return fileNodeByName.format(fileName).isVisible(DEFAULT_TIMEOUT_MS);
    }

    public ProjectDetailPage reloadPage() {
        page.reload();
        waitUntilSpinnerLoaded();
        return this;
    }
}
