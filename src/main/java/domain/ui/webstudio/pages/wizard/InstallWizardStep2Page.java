package domain.ui.webstudio.pages.wizard;

import domain.ui.BasePage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class InstallWizardStep2Page extends BasePage {

    @FindBy(xpath = "//input[@value='Next']")
    private WebElement nextBtn;

    public InstallWizardStep2Page() {
        super("/faces/pages/modules/install/step2.xhtml");
    }

    public InstallWizardStep3Page clickNext() {
        nextBtn.click();
        return new InstallWizardStep3Page();
    }
}
