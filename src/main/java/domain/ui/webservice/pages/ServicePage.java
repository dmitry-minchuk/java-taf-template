package domain.ui.webservice.pages;

import com.microsoft.playwright.Page;
import configuration.core.ui.CorePage;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.pages.BasePage;
import lombok.Getter;

@Getter
public class ServicePage extends BasePage {
    // Parameterized elements with %s placeholders
    private WebElement projectElement;
    private WebElement downloadProjectButton;
    private WebElement deleteProjectButton;
    private WebElement manifestLink;
    private WebElement expandServiceButton;
    private WebElement listMethods;
    private WebElement failedIcon;
    private WebElement failedMethods;
    private WebElement kafkaService;
    private WebElement serviceLink;

    // Static elements
    private WebElement deployButton;
    private WebElement fileInput;
    private WebElement showAllDeploymentsCheckBox;
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
        // Parameterized elements
        projectElement = new WebElement(page, "xpath=//h3[a[text()='%s']]", "Project Element");
        downloadProjectButton = new WebElement(page, "xpath=//h3[a[text()='%s']]/parent::*//button[@title='Download']", "Download Button");
        deleteProjectButton = new WebElement(page, "xpath=//h3[a[text()='%s']]/parent::*//button[@title='Delete']", "Delete Button");
        manifestLink = new WebElement(page, "xpath=//h3[a[text()='%s']]/parent::*//a[contains(text(), 'MANIFEST.MF')]", "Manifest Link");
        expandServiceButton = new WebElement(page, "xpath=//h3[a[text()='%s']]/parent::div[contains(@class,'status-deployed')]/span", "Expand Service Button");
        listMethods = new WebElement(page, "xpath=//div[@data-service-name='%s']//ul", "List Methods");
        failedIcon = new WebElement(page, "xpath=//h3[a[text()='%s']]/parent::div[contains(@class,'status-failed')]/span", "Failed Icon");
        failedMethods = new WebElement(page, "xpath=//h3[a[text()='%s']]/parent::*//div[@class='errors']", "Failed Methods");
        kafkaService = new WebElement(page, "xpath=//h3[a[text()='%s']]/parent::div[contains(@class,'KAFKA')]", "Kafka Service");
        serviceLink = new WebElement(page, "xpath=//h3[a[text()='%s']]/parent::*//a[contains(text(), '%s')]", "Service Link");

        // Static elements
        deployButton = new WebElement(page, "xpath=//button[@class='deploy-button']", "Deploy Button");
        fileInput = new WebElement(page, "xpath=//input[@id='file-input']", "File Input");
        showAllDeploymentsCheckBox = new WebElement(page, "xpath=//input[@id='showAll']", "Show All Deployments Checkbox");
        apiSelector = new WebElement(page, "xpath=//select[@id='select-api']", "API Selector");
    }

    public void open() {
        this.page.navigate(LocalDriverPool.getAppUrl());
    }

    public WebElement getProjectElement(String projectName) {
        return projectElement.format(projectName);
    }

    public WebElement getDownloadProjectButton(String projectName) {
        return downloadProjectButton.format(projectName);
    }

    public WebElement getDeleteProjectButton(String projectName) {
        return deleteProjectButton.format(projectName);
    }

    public WebElement getServiceLink(String projectName, String linkName) {
        return serviceLink.format(projectName, linkName);
    }

    public WebElement getManifestLink(String projectName) {
        return manifestLink.format(projectName);
    }

    public WebElement getExpandServiceButton(String serviceName) {
        return expandServiceButton.format(serviceName);
    }

    public WebElement getListMethods(String serviceName) {
        return listMethods.format(serviceName);
    }

    public WebElement getFailedIcon(String projectName) {
        return failedIcon.format(projectName);
    }

    public WebElement getFailedMethods(String projectName) {
        return failedMethods.format(projectName);
    }

    public WebElement getKafkaService(String projectName) {
        return kafkaService.format(projectName);
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
