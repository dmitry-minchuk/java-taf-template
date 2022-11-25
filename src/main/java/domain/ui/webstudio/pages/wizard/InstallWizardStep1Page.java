package domain.ui.webstudio.pages.wizard;

import domain.ui.BasePage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class InstallWizardStep1Page extends BasePage {

    @FindBy(xpath = "//input[@type='text']")
    private WebElement workingDirectoryPathTextField;

    @FindBy(xpath = "//input[@value='Next']")
    private WebElement nextBtn;

    public InstallWizardStep1Page() {
        super("/faces/pages/modules/install/step1.xhtml");
    }

    public InstallWizardStep2Page fillWorkingDirPathAndClickNext(String path) {
        if (path != null) {
            workingDirectoryPathTextField.clear();
            workingDirectoryPathTextField.sendKeys(path);
        }
        nextBtn.click();
        return new InstallWizardStep2Page();
    }

    public InstallWizardStep2Page fillWorkingDirPathAndClickNext() {
        return fillWorkingDirPathAndClickNext(null);
    }

}
