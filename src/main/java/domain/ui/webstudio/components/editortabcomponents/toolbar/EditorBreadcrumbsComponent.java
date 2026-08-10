package domain.ui.webstudio.components.editortabcomponents.toolbar;

import com.microsoft.playwright.Dialog;
import com.microsoft.playwright.Page;
import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;

/**
 * The breadcrumb strip of the editor's top toolbar: Projects / project / branch / module. All elements are
 * scoped under the breadcrumbs container of the header form; the dropdowns render inside their breadcrumb
 * spans, so they are in scope too (verified against the live 6.4.0 DOM).
 */
public class EditorBreadcrumbsComponent extends BaseComponent {

    @lombok.Getter
    private final WebElement allProjectsLink;
    private final WebElement projectToggle;
    private final WebElement moduleBranch;
    private final WebElement moduleToggle;
    private final WebElement dropdownItemTemplate;
    private final WebElement categoryLink;

    public EditorBreadcrumbsComponent(Page page) {
        this(new WebElement(page, "xpath=//form[@id='headerForm']//div[@class='breadcrumbs']", "editorBreadcrumbs"));
    }

    public EditorBreadcrumbsComponent(WebElement rootLocator) {
        super(rootLocator);
        allProjectsLink = createScopedElement("xpath=./a[@href='/']", "breadcrumbsAllProjects");
        projectToggle = createScopedElement("xpath=./span[@id='breadcrumbs-project']/a", "breadcrumbsProjectToggle");
        // The branch and the module share the breadcrumbs-module id; only the branch link carries a Branch: title.
        moduleBranch = createScopedElement("xpath=./span[@id='breadcrumbs-module']/a[starts-with(@title, 'Branch:')]", "breadcrumbsModuleBranch");
        moduleToggle = createScopedElement("xpath=./span[@id='breadcrumbs-module']/a[not(contains(@title, 'Branch'))]", "breadcrumbsModuleToggle");
        dropdownItemTemplate = createScopedElement(
                "xpath=.//span[contains(@class,'dropdown') and contains(@class,'open')]/ul[contains(@class, 'dropdown-menu')]//li//a[contains(text(), '%s')]",
                "breadcrumbsDropdownItem");
        categoryLink = createScopedElement("xpath=./a[@class='changes-listener-added']", "breadcrumbsCategoryLink");
    }

    public void navigateToProjectsList() {
        if (allProjectsLink.isVisible(1000)) {
            LOGGER.info("Navigating back to projects list via breadcrumb...");
            allProjectsLink.click();
            WaitUtil.sleep(500, "Waiting for projects list to load");
        }
    }

    public void navigateToProjectRoot(String projectName) {
        WaitUtil.retryOnException(() -> {
            // The breadcrumb re-renders during the post-save recompile; click it resiliently.
            projectToggle.clickWhenSettled();
            dropdownItemTemplate.format(projectName).waitForVisible(3000);
            dropdownItemTemplate.format(projectName).click();
            return true;
        }, 15000, 500, "Trying to navigate to project root " + projectName);
        WaitUtil.sleep(500, "Waiting for project view to load");
    }

    public void switchBranch(String branchName) {
        // Switching a branch on a project with uncommitted changes raises a native confirm ("...current
        // changes will be lost..."); Playwright auto-dismisses unhandled dialogs, which would cancel the
        // switch, so accept it for the duration of the switch.
        java.util.function.Consumer<Dialog> acceptChanges = Dialog::accept;
        page.onDialog(acceptChanges);
        try {
            WaitUtil.retryOnException(() -> {
                moduleBranch.click(1000);
                dropdownItemTemplate.format(branchName).click(1000);
                waitUntilSpinnerLoaded();
                if (!getCurrentBranch().trim().equals(branchName)) {
                    throw new RuntimeException("Branch did not switch to " + branchName + ", current: " + getCurrentBranch().trim());
                }
                return true;
            }, 10000, 500, "Switching branch to " + branchName);
        } finally {
            page.offDialog(acceptChanges);
        }
    }

    public void selectBranchInDropdown(String branchName) {
        java.util.function.Consumer<Dialog> acceptChanges = Dialog::accept;
        page.onDialog(acceptChanges);
        try {
            moduleBranch.click();
            dropdownItemTemplate.format(branchName).waitForVisible();
            dropdownItemTemplate.format(branchName).click();
        } finally {
            page.offDialog(acceptChanges);
        }
    }

    public String getCurrentBranch() {
        return moduleBranch.getText();
    }

    public void selectModuleInDropdown(String moduleName) {
        WaitUtil.retryOnException(() -> {
            moduleToggle.click();
            dropdownItemTemplate.format(moduleName).waitForVisible();
            dropdownItemTemplate.format(moduleName).click();
            return true;
        }, 5000, 500, "Selecting module " + moduleName + " from breadcrumb");
    }

    public void selectProjectInDropdown(String projectName) {
        WaitUtil.retryOnException(() -> {
            projectToggle.click();
            dropdownItemTemplate.format(projectName).waitForVisible();
            dropdownItemTemplate.format(projectName).click();
            return true;
        }, 5000, 500, "Selecting project " + projectName + " from breadcrumb");
        WaitUtil.sleep(500, "Waiting for project view to load");
    }

    public String getProjectName() {
        return projectToggle.isVisible(1000) ? projectToggle.getText().trim() : "";
    }

    public String getProjectName(int timeoutInMillis) {
        return projectToggle.isVisible(timeoutInMillis) ? projectToggle.getText().trim() : "";
    }

    public String getModuleName() {
        return moduleToggle.isVisible(1000) ? moduleToggle.getText().trim() : "";
    }

    public String getModuleName(int timeoutInMillis) {
        return moduleToggle.isVisible(timeoutInMillis) ? moduleToggle.getText().trim() : "";
    }

    public void clickCategory() {
        categoryLink.click();
    }

    public void checkBreadcrumbs(String category, String project, String module) {
        if (!category.isEmpty()) {
            WaitUtil.waitForCondition(() -> categoryLink.isVisible(500)
                            && categoryLink.getText().contains(category),
                    5000, 250, "Waiting for breadcrumb category to be: " + category);
        }
        if (!project.isEmpty()) {
            WaitUtil.waitForCondition(() -> projectToggle.isVisible(500)
                            && projectToggle.getText().equals(project),
                    5000, 250, "Waiting for breadcrumb project to be: " + project);
        }
        if (!module.isEmpty()) {
            WaitUtil.waitForCondition(() -> moduleToggle.isVisible(500)
                            && moduleToggle.getText().equals(module),
                    5000, 250, "Waiting for breadcrumb module to be: " + module);
        }
    }
}
