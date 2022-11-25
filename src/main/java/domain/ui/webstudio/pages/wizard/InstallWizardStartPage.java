package domain.ui.webstudio.pages.wizard;

import domain.ui.BasePage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class InstallWizardStartPage extends BasePage {

    @FindBy(xpath = "//input[@value='Start']")
    private WebElement startBtn;

    public InstallWizardStartPage() {
        super("/faces/pages/modules/install/index.xhtml");
    }

    public InstallWizardStep1Page clickStartBtn() {
        startBtn.click();
        return new InstallWizardStep1Page();
    }
}
