package configuration.core.ui;

import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import helpers.utils.WaitUtil;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

public abstract class BasePageComponent {

    protected final int timeoutInSeconds = Integer.parseInt(ProjectConfiguration.getProperty(PropertyNameSpace.WEB_ELEMENT_EXPLICIT_WAIT));
    @Getter
    private By rootLocatorBy;
    @Getter
    private WebElement rootElement;
    @Getter
    private WebDriver driver;

    @Getter
    @FindBy(xpath = "//div[contains(@class,'ant-modal-confirm')]")
    protected ConfirmationPopupComponent confirmationPopup;

    protected BasePageComponent() {
        SmartPageFactory.initElements(driver, this);
    }

    public void init(WebDriver driver, By rootLocatorBy) {
        this.driver = driver;
        this.rootLocatorBy = rootLocatorBy;
        this.rootElement = null;
        SmartPageFactory.initElements(driver, this);
    }

    public boolean isPresent() {
        return WaitUtil.waitUntil(driver, ExpectedConditions.visibilityOfElementLocated(rootLocatorBy), timeoutInSeconds);
    }
}
