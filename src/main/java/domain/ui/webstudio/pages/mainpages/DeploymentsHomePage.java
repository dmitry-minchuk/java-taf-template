package domain.ui.webstudio.pages.mainpages;

import configuration.core.ui.WebElement;
import domain.ui.webstudio.pages.BasePage;
import helpers.utils.WaitUtil;

import java.net.URI;
import java.util.List;

public class DeploymentsHomePage extends BasePage {

    private static final int FILTER_SETTLE_MS = 600;

    private WebElement homeRoot;
    private WebElement emptyRepositoriesPlaceholder;
    private WebElement noRepositoriesRailPlaceholder;
    private WebElement summary;
    private WebElement searchInput;
    private WebElement deploymentsTable;
    private WebElement deploymentOpenTemplate;
    private WebElement noMatchPlaceholder;
    private WebElement clearSearchBtn;
    private WebElement emptyPlaceholder;
    private WebElement pagination;
    private WebElement repositoryRailItemTemplate;

    public DeploymentsHomePage() {
        super();
        initializeComponents();
    }

    private void initializeComponents() {
        homeRoot = new WebElement(page, "[data-testid=deployments-home]", "deploymentsHome");
        emptyRepositoriesPlaceholder = new WebElement(page,
                "[data-testid=deployments-empty-repositories]", "deploymentsEmptyRepositories");
        noRepositoriesRailPlaceholder = new WebElement(page,
                "[data-testid=deployments-no-repositories]", "deploymentsNoRepositories");
        summary = new WebElement(page, "[data-testid=deployments-summary]", "deploymentsSummary");
        searchInput = new WebElement(page,
                "css=[data-testid=deployments-search] input, input[data-testid=deployments-search]",
                "deploymentsSearch");
        deploymentsTable = new WebElement(page, "[data-testid=deployments-table]", "deploymentsTable");
        deploymentOpenTemplate = new WebElement(page,
                "xpath=//button[starts-with(@data-testid,'deployment-open-')][normalize-space()='%s']",
                "deploymentOpen");
        noMatchPlaceholder = new WebElement(page, "[data-testid=deployments-no-match]", "deploymentsNoMatch");
        clearSearchBtn = new WebElement(page, "xpath=//button[normalize-space()='Clear search']", "clearSearchBtn");
        emptyPlaceholder = new WebElement(page, "[data-testid=deployments-empty]", "deploymentsEmpty");
        pagination = new WebElement(page, "[data-testid=deployments-pagination]", "deploymentsPagination");
        repositoryRailItemTemplate = new WebElement(page,
                "xpath=//button[@data-testid='deployment-repository-%s']", "deploymentRepositoryRailItem");
    }

    public DeploymentsHomePage open() {
        navigateToDeployments("");
        return waitForLoaded();
    }

    public DeploymentsHomePage openWithQuery(String query) {
        navigateToDeployments(query);
        return waitForLoaded();
    }

    private void navigateToDeployments(String query) {
        URI current = URI.create(page.url());
        page.navigate(current.getScheme() + "://" + current.getAuthority() + "/deployments" + query);
    }

    public DeploymentsHomePage waitForLoaded() {
        homeRoot.waitForVisible(DEFAULT_TIMEOUT_MS);
        waitUntilSpinnerLoaded();
        return this;
    }

    public boolean isEmptyRepositoriesShown(int timeoutInMillis) {
        return emptyRepositoriesPlaceholder.isVisible(timeoutInMillis);
    }

    public boolean isNoRepositoriesRailShown(int timeoutInMillis) {
        return noRepositoriesRailPlaceholder.isVisible(timeoutInMillis);
    }

    public boolean isRepositoryListed(String repositoryId) {
        return repositoryRailItemTemplate.format(repositoryId).isVisible(DEFAULT_TIMEOUT_MS / 2);
    }

    public DeploymentsHomePage selectRepository(String repositoryId) {
        repositoryRailItemTemplate.format(repositoryId).waitForVisible(DEFAULT_TIMEOUT_MS).click();
        waitUntilSpinnerLoaded();
        return this;
    }

    public String getSummary() {
        waitForListSettled();
        return summary.waitForVisible(DEFAULT_TIMEOUT_MS).getText().trim();
    }

    private void waitForListSettled() {
        WaitUtil.waitForCondition(
                () -> deploymentsTable.isVisible(1000) || emptyPlaceholder.isVisible(500)
                        || noMatchPlaceholder.isVisible(500) || emptyRepositoriesPlaceholder.isVisible(500),
                DEFAULT_TIMEOUT_MS, 300, "Waiting for the deployments list to leave its loading state");
    }

    public List<String> getVisibleDeploymentNames() {
        deploymentsTable.waitForVisible(DEFAULT_TIMEOUT_MS);
        return page.locator("xpath=//button[starts-with(@data-testid,'deployment-open-')]")
                .allInnerTexts().stream().map(String::trim).toList();
    }

    public DeploymentsHomePage search(String query) {
        searchInput.waitForVisible(DEFAULT_TIMEOUT_MS);
        searchInput.fill(query);
        WaitUtil.sleep(FILTER_SETTLE_MS, "Letting the client-side deployments filter apply");
        return this;
    }

    public DeploymentsHomePage clickClearSearch() {
        clearSearchBtn.waitForVisible(DEFAULT_TIMEOUT_MS).click();
        WaitUtil.sleep(FILTER_SETTLE_MS, "Letting the deployments list restore after clearing the search");
        return this;
    }

    public boolean isNoMatchShown(int timeoutInMillis) {
        return noMatchPlaceholder.isVisible(timeoutInMillis);
    }

    public String getNoMatchText() {
        return noMatchPlaceholder.waitForVisible(DEFAULT_TIMEOUT_MS).getText().trim();
    }

    public boolean isEmptyShown(int timeoutInMillis) {
        return emptyPlaceholder.isVisible(timeoutInMillis);
    }

    public boolean isPaginationShown(int timeoutInMillis) {
        return pagination.isVisible(timeoutInMillis);
    }

    public DeploymentWorkspacePage openDeployment(String deploymentName) {
        deploymentOpenTemplate.format(deploymentName).waitForVisible(DEFAULT_TIMEOUT_MS).click();
        return new DeploymentWorkspacePage().waitForLoaded();
    }
}
