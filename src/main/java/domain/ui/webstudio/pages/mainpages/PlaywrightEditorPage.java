package domain.ui.webstudio.pages.mainpages;

import org.openqa.selenium.support.FindBy;

/**
 * Playwright version of EditorPage - Main editor interface page
 * Uses exact same architecture as Selenium version with @FindBy annotations
 */
public class PlaywrightEditorPage extends PlaywrightProxyMainPage {

    public PlaywrightEditorPage() {
        super("/");
    }
}