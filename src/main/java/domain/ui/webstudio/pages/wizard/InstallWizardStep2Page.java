package domain.ui.webstudio.pages.wizard;

import configuration.core.ui.SmartWebElement;
import configuration.core.ui.BasePage;
import org.openqa.selenium.support.FindBy;

public class InstallWizardStep2Page extends BasePage {

    @FindBy(xpath = "//input[@value='Next']")
    private SmartWebElement nextBtn;

    public InstallWizardStep2Page() {
        super("/faces/pages/modules/install/step2.xhtml");
    }

    public InstallWizardStep3Page clickNext() {
        nextBtn.click();
        return new InstallWizardStep3Page();
    }
}
