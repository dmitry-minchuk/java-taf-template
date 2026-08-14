package domain.ui.webstudio.pages.mainpages;

import configuration.core.ui.WebElement;
import domain.ui.webstudio.pages.BasePage;

import java.net.URI;
import java.util.List;

public class DeploymentWorkspacePage extends BasePage {

    private WebElement workspaceRoot;
    private WebElement title;
    private WebElement tabs;
    private WebElement projectsTable;
    private WebElement projectRowTemplate;
    private WebElement notFoundPage;
    private WebElement notFoundHomeButton;

    public DeploymentWorkspacePage() {
        super();
        initializeComponents();
    }

    private void initializeComponents() {
        workspaceRoot = new WebElement(page, "[data-testid=deployment-workspace]", "deploymentWorkspace");
        title = new WebElement(page, "[data-testid=deployment-title]", "deploymentTitle");
        tabs = new WebElement(page, "[data-testid=deployment-tabs]", "deploymentTabs");
        projectsTable = new WebElement(page, "[data-testid=deployment-projects-table]", "deploymentProjectsTable");
        projectRowTemplate = new WebElement(page,
                "xpath=//tr[@data-testid='deployment-project-row-%s']", "deploymentProjectRow");
        notFoundPage = new WebElement(page,
                "xpath=//main[contains(normalize-space(.),'404')][contains(normalize-space(.),'Page not found.')]",
                "notFoundPage");
        notFoundHomeButton = new WebElement(page,
                "xpath=//button[normalize-space()='Home'] | //a[normalize-space()='Home']", "notFoundHomeButton");
    }

    public DeploymentWorkspacePage openById(String deploymentId) {
        URI current = URI.create(page.url());
        page.navigate(current.getScheme() + "://" + current.getAuthority() + "/deployments/" + deploymentId);
        return this;
    }

    public boolean isNotFoundPageShown(int timeoutInMillis) {
        return notFoundPage.isVisible(timeoutInMillis);
    }

    public boolean isNotFoundHomeButtonShown(int timeoutInMillis) {
        return notFoundHomeButton.isVisible(timeoutInMillis);
    }

    public DeploymentWorkspacePage waitForLoaded() {
        workspaceRoot.waitForVisible(DEFAULT_TIMEOUT_MS);
        waitUntilSpinnerLoaded();
        return this;
    }

    public String getTitle() {
        return title.waitForVisible(DEFAULT_TIMEOUT_MS).getText().trim();
    }

    public List<String> getTabNames() {
        return tabs.waitForVisible(DEFAULT_TIMEOUT_MS).getLocator()
                .locator("xpath=.//div[@role='tab']").allInnerTexts().stream().map(String::trim).toList();
    }

    public boolean isProjectPresent(String projectName) {
        projectsTable.waitForVisible(DEFAULT_TIMEOUT_MS);
        return projectRowTemplate.format(projectName).isVisible(DEFAULT_TIMEOUT_MS / 2);
    }

    public int getProjectRowsCount() {
        projectsTable.waitForVisible(DEFAULT_TIMEOUT_MS);
        return page.locator("xpath=//tr[starts-with(@data-testid,'deployment-project-row-')]").count();
    }

    public List<String> getProjectRowCells(String projectName) {
        projectsTable.waitForVisible(DEFAULT_TIMEOUT_MS);
        return projectRowTemplate.format(projectName).waitForVisible(DEFAULT_TIMEOUT_MS).getLocator()
                .locator("xpath=./td").allInnerTexts().stream().map(String::trim).toList();
    }

}
