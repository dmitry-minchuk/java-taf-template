package tests.ui.webservice.pages;

import com.microsoft.playwright.Page;
import configuration.core.ui.CorePage;
import configuration.core.ui.WebElement;
import lombok.Getter;

/**
 * Page Object for WebService/RuleService deployment and management UI
 */
@Getter
public class ServicePage extends CorePage {

    // Project list and management elements
    private WebElement projectElement;
    private WebElement downloadProjectButton;
    private WebElement deleteProjectButton;
    private WebElement deployButton;
    private WebElement fileInput;

    // Service information elements
    private WebElement manifestLink;
    private WebElement swaggerUILink;
    private WebElement expandServiceButton;
    private WebElement listMethods;
    private WebElement failedIcon;
    private WebElement failedMethods;
    private WebElement kafkaService;

    // Show all deployments checkbox
    private WebElement showAllDeploymentsCheckBox;

    // API selector
    private WebElement apiSelector;

    public ServicePage() {
        super();
        initializeElements();
    }

    public ServicePage(Page page) {
        super(page);
        initializeElements();
    }

    private void initializeElements() {
        // Deploy button
        deployButton = new WebElement(page, "xpath=//button[@class='deploy-button']", "Deploy Button");
        fileInput = new WebElement(page, "xpath=//input[@id='file-input']", "File Input");

        // Show all deployments checkbox
        showAllDeploymentsCheckBox = new WebElement(page, "xpath=//input[@id='showAll']", "Show All Deployments Checkbox");

        // API selector
        apiSelector = new WebElement(page, "xpath=//select[@id='select-api']", "API Selector");
    }

    /**
     * Get project element by name using dynamic xpath
     * @param projectName the name of the project
     * @return WebElement representing the project
     */
    public WebElement getProjectElement(String projectName) {
        String locator = String.format("xpath=//h3[a[text()='%s']]", projectName);
        return new WebElement(page, locator, "Project Element: " + projectName);
    }

    /**
     * Get download button for a specific project
     * @param projectName the name of the project
     * @return WebElement representing the download button
     */
    public WebElement getDownloadProjectButton(String projectName) {
        String locator = String.format("xpath=//h3[a[text()='%s']]/parent::*//button[@title='Download']", projectName);
        return new WebElement(page, locator, "Download Button for: " + projectName);
    }

    /**
     * Get delete button for a specific project
     * @param projectName the name of the project
     * @return WebElement representing the delete button
     */
    public WebElement getDeleteProjectButton(String projectName) {
        String locator = String.format("xpath=//h3[a[text()='%s']]/parent::*//button[@title='Delete']", projectName);
        return new WebElement(page, locator, "Delete Button for: " + projectName);
    }

    /**
     * Get service link within a project
     * @param projectName the project name
     * @param linkName the name of the link (e.g., "Swagger UI")
     * @return WebElement representing the service link
     */
    public WebElement getServiceLink(String projectName, String linkName) {
        String locator = String.format("xpath=//h3[a[text()='%s']]/parent::*//a[contains(text(), '%s')]", projectName, linkName);
        return new WebElement(page, locator, "Service Link: " + linkName);
    }

    /**
     * Get manifest link for a project
     * @param projectName the project name
     * @return WebElement representing the manifest link
     */
    public WebElement getManifestLink(String projectName) {
        String locator = String.format("xpath=//h3[a[text()='%s']]/parent::*//a[contains(text(), 'MANIFEST.MF')]", projectName);
        return new WebElement(page, locator, "Manifest Link for: " + projectName);
    }

    /**
     * Get expand service button for a project
     * @param serviceName the service name
     * @return WebElement representing the expand button
     */
    public WebElement getExpandServiceButton(String serviceName) {
        String locator = String.format("xpath=//h3[a[text()='%s']]/parent::div[contains(@class,'status-deployed')]/span", serviceName);
        return new WebElement(page, locator, "Expand Service Button for: " + serviceName);
    }

    /**
     * Get list of methods for a service
     * @param serviceName the service name
     * @return WebElement containing the methods list
     */
    public WebElement getListMethods(String serviceName) {
        String locator = String.format("xpath=//div[@data-service-name='%s']//ul", serviceName);
        return new WebElement(page, locator, "List Methods for: " + serviceName);
    }

    /**
     * Get failed icon for a project
     * @param projectName the project name
     * @return WebElement representing the failed icon
     */
    public WebElement getFailedIcon(String projectName) {
        String locator = String.format("xpath=//h3[a[text()='%s']]/parent::div[contains(@class,'status-failed')]/span", projectName);
        return new WebElement(page, locator, "Failed Icon for: " + projectName);
    }

    /**
     * Get failed methods div for a project
     * @param projectName the project name
     * @return WebElement containing the failed methods
     */
    public WebElement getFailedMethods(String projectName) {
        String locator = String.format("xpath=//h3[a[text()='%s']]/parent::*//div[@class='errors']", projectName);
        return new WebElement(page, locator, "Failed Methods for: " + projectName);
    }

    /**
     * Check if Kafka service exists for a project
     * @param projectName the project name
     * @return WebElement representing the Kafka service
     */
    public WebElement getKafkaService(String projectName) {
        String locator = String.format("xpath=//h3[a[text()='%s']]/parent::div[contains(@class,'KAFKA')]", projectName);
        return new WebElement(page, locator, "Kafka Service for: " + projectName);
    }

    /**
     * Download a specific project
     * @param projectName the name of the project to download
     */
    public void downloadProject(String projectName) {
        getDownloadProjectButton(projectName).click();
    }

    /**
     * Delete a specific project
     * @param projectName the name of the project to delete
     */
    public void deleteProject(String projectName) {
        getDeleteProjectButton(projectName).click();
    }

    /**
     * Deploy a project from a file path
     * @param filePath the absolute path to the file to deploy
     */
    public void deployProject(String filePath) {
        deployButton.waitForVisible();
        fileInput.fill(filePath);
    }
}
