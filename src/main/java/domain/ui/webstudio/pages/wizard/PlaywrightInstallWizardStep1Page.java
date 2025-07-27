package domain.ui.webstudio.pages.wizard;

import configuration.core.ui.PlaywrightBasePage;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;

public class PlaywrightInstallWizardStep1Page extends PlaywrightBasePage {

    private PlaywrightWebElement workingDirectoryPathTextField;
    private PlaywrightWebElement nextBtn;

    public PlaywrightInstallWizardStep1Page() {
        super("/faces/pages/modules/install/step1.xhtml");
        initializeElements();
    }

    private void initializeElements() {
        workingDirectoryPathTextField = new PlaywrightWebElement(page, "//input[@type='text']", "Working Directory Path Text Field");
        nextBtn = new PlaywrightWebElement(page, "//input[@value='Next']", "Next Button");
    }

    public PlaywrightInstallWizardStep2Page fillWorkingDirPathAndClickNext(String path) {
        if (path != null) {
            workingDirectoryPathTextField.clear();
            workingDirectoryPathTextField.fill(path);
        }
        nextBtn.click();
        return new PlaywrightInstallWizardStep2Page();
    }

    public PlaywrightInstallWizardStep2Page fillWorkingDirPathAndClickNext() {
        return fillWorkingDirPathAndClickNext(null);
    }
}