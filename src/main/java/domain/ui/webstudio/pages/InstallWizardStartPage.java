package domain.ui.webstudio.pages;

import domain.ui.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class InstallWizardStartPage extends BasePage {

    @FindBy(xpath = "//input[@value='Start']")
    private WebElement startBtn;

    public InstallWizardStartPage(WebDriver driver) {
        super(driver, "/faces/pages/modules/install/index.xhtml");
    }

    public InstallWizardStep1Page clickStartBtn() {
        startBtn.click();
        return new InstallWizardStep1Page(driver);
    }
}
