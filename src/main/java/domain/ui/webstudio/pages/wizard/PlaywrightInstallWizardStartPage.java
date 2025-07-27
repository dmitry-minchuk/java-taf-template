package domain.ui.webstudio.pages.wizard;

import configuration.core.ui.PlaywrightBasePage;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;

public class PlaywrightInstallWizardStartPage extends PlaywrightBasePage {

    private PlaywrightWebElement startBtn;

    public PlaywrightInstallWizardStartPage() {
        super("/faces/pages/modules/install/index.xhtml");
        initializeElements();
    }

    private void initializeElements() {
        startBtn = new PlaywrightWebElement(page, "//input[@value='Start']", "Start Button");
    }

    public PlaywrightInstallWizardStep1Page clickStartBtn() {
        startBtn.click();
        return new PlaywrightInstallWizardStep1Page();
    }
}