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

    @Getter // We want confirmationPopup available from BasePage and from BasePageComponent - but it's impossible at the same time with the same name in @FindBy
    protected ConfirmationPopupComponent confirmationPopup;

    protected BasePageComponent() {
        SmartPageFactory.initElements(driver, this);
        confirmationPopup = new ConfirmationPopupComponent();
        SmartPageFactory.initElements(driver, confirmationPopup);
    }

    public void init(WebDriver driver, By rootLocatorBy) {
        this.driver = driver;
        this.rootLocatorBy = rootLocatorBy;
        this.rootElement = null;
        SmartPageFactory.initElements(driver, this);
        confirmationPopup = new ConfirmationPopupComponent();
        SmartPageFactory.initElements(driver, confirmationPopup);
    }

    public boolean isPresent() {
        return WaitUtil.waitUntil(driver, ExpectedConditions.visibilityOfElementLocated(rootLocatorBy), timeoutInSeconds);
    }
}
