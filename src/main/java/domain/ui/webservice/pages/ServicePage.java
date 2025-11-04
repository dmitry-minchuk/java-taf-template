package domain.ui.webservice.pages;

import com.microsoft.playwright.Page;
import configuration.core.ui.CorePage;
import configuration.core.ui.WebElement;
import domain.ui.webstudio.pages.BasePage;
import lombok.Getter;

@Getter
public class ServicePage extends BasePage {
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

    public WebElement getProjectElement(String projectName) {
        String locator = String.format("xpath=//h3[a[text()='%s']]", projectName);
        return new WebElement(page, locator, "Project Element: " + projectName);
    }

    public WebElement getDownloadProjectButton(String projectName) {
        String locator = String.format("xpath=//h3[a[text()='%s']]/parent::*//button[@title='Download']", projectName);
        return new WebElement(page, locator, "Download Button for: " + projectName);
    }

    public WebElement getDeleteProjectButton(String projectName) {
        String locator = String.format("xpath=//h3[a[text()='%s']]/parent::*//button[@title='Delete']", projectName);
        return new WebElement(page, locator, "Delete Button for: " + projectName);
    }

    public WebElement getServiceLink(String projectName, String linkName) {
        String locator = String.format("xpath=//h3[a[text()='%s']]/parent::*//a[contains(text(), '%s')]", projectName, linkName);
        return new WebElement(page, locator, "Service Link: " + linkName);
    }

    public WebElement getManifestLink(String projectName) {
        String locator = String.format("xpath=//h3[a[text()='%s']]/parent::*//a[contains(text(), 'MANIFEST.MF')]", projectName);
        return new WebElement(page, locator, "Manifest Link for: " + projectName);
    }

    public WebElement getExpandServiceButton(String serviceName) {
        String locator = String.format("xpath=//h3[a[text()='%s']]/parent::div[contains(@class,'status-deployed')]/span", serviceName);
        return new WebElement(page, locator, "Expand Service Button for: " + serviceName);
    }

    public WebElement getListMethods(String serviceName) {
        String locator = String.format("xpath=//div[@data-service-name='%s']//ul", serviceName);
        return new WebElement(page, locator, "List Methods for: " + serviceName);
    }

    public WebElement getFailedIcon(String projectName) {
        String locator = String.format("xpath=//h3[a[text()='%s']]/parent::div[contains(@class,'status-failed')]/span", projectName);
        return new WebElement(page, locator, "Failed Icon for: " + projectName);
    }

    public WebElement getFailedMethods(String projectName) {
        String locator = String.format("xpath=//h3[a[text()='%s']]/parent::*//div[@class='errors']", projectName);
        return new WebElement(page, locator, "Failed Methods for: " + projectName);
    }

    public WebElement getKafkaService(String projectName) {
        String locator = String.format("xpath=//h3[a[text()='%s']]/parent::div[contains(@class,'KAFKA')]", projectName);
        return new WebElement(page, locator, "Kafka Service for: " + projectName);
    }

    public void downloadProject(String projectName) {
        getDownloadProjectButton(projectName).click();
    }

    public void deleteProject(String projectName) {
        getDeleteProjectButton(projectName).click();
    }

    public void deployProject(String filePath) {
        deployButton.waitForVisible();
        fileInput.fill(filePath);
    }
}
