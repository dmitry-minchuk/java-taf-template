package domain.ui.webstudio.pages;

import domain.ui.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class InstallWizardStep1Page extends BasePage {

    @FindBy(xpath = "//input[@type='text']")
    private WebElement workingDirectoryPathTextField;

    @FindBy(xpath = "//input[@value='Next']")
    private WebElement nextBtn;

    public InstallWizardStep1Page(WebDriver driver) {
        super(driver, "/faces/pages/modules/install/step1.xhtml");
    }

    public InstallWizardStep2Page fillWorkingDirPathAndClickNext(String path) {
        if (path != null) {
            workingDirectoryPathTextField.clear();
            workingDirectoryPathTextField.sendKeys(path);
        }
        nextBtn.click();
        return new InstallWizardStep2Page(driver);
    }

    public InstallWizardStep2Page fillWorkingDirPathAndClickNext() {
        return fillWorkingDirPathAndClickNext(null);
    }

}
