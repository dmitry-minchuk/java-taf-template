package domain.ui.webstudio.pages.wizard;

import configuration.core.ui.PlaywrightBasePage;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;

public class PlaywrightInstallWizardStep2Page extends PlaywrightBasePage {

    private PlaywrightWebElement nextBtn;

    public PlaywrightInstallWizardStep2Page() {
        super("/faces/pages/modules/install/step2.xhtml");
        initializeElements();
    }

    private void initializeElements() {
        nextBtn = new PlaywrightWebElement(page, "//input[@value='Next']", "Next Button");
    }

    public PlaywrightInstallWizardStep3Page clickNext() {
        nextBtn.click();
        return new PlaywrightInstallWizardStep3Page();
    }
}