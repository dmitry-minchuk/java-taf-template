package domain.ui.webstudio.pages.mainpages;

import configuration.core.ui.PlaywrightTableComponent;
import domain.ui.webstudio.components.editortabcomponents.*;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.PlaywrightLeftProjectModuleSelectorComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.PlaywrightLeftRulesTreeComponent;
import lombok.Getter;
import org.openqa.selenium.support.FindBy;

/**
 * Playwright version of EditorPage - Main editor interface page
 * Uses exact same architecture as Selenium version with @FindBy annotations
 */
public class PlaywrightEditorPage extends PlaywrightProxyMainPage {

    @Getter
    @FindBy(xpath = "//div[@id='projects']")
    private PlaywrightLeftProjectModuleSelectorComponent leftProjectModuleSelectorComponent;

    @Getter
    @FindBy(xpath = "//div[@id='left']")
    private PlaywrightLeftRulesTreeComponent leftRulesTreeComponent;

    @Getter
    @FindBy(xpath = "//div[@id='right']")
    private PlaywrightRightTableDetailsComponent rightTableDetailsComponent;

    @FindBy(xpath = "//table[@class='te_table']")
    private PlaywrightTableComponent centerTable;

    @Getter
    @FindBy(xpath = "//div[@id='bottom']")
    private PlaywrightProblemsPanelComponent problemsPanelComponent;

    @Getter
    @FindBy(xpath = "//div[@class='page']")
    private PlaywrightProjectDetailsComponent projectDetailsComponent;

    @Getter
    @FindBy(xpath = "//div[@id='editModulePopup_container']")
    private PlaywrightAddModuleComponent addModulePopupComponent;

    @Getter
    @FindBy(xpath = "//div[@id='tableToolbarPanel']")
    private PlaywrightTableToolbarPanelComponent tableToolbarPanelComponent;

    @Getter
    @FindBy(xpath = "//div[@class='te_toolbar']")
    private PlaywrightEditTablePanelComponent editTablePanelComponent;

    @Getter
    @FindBy(xpath = "//div[@id='content' and contains(@class,'ui-layout-center')]")
    private PlaywrightTestResultValidationComponent testResultValidationComponent;

    @Getter
    @FindBy(xpath = "//div[@id='content']")
    private PlaywrightEditorMainContentProblemsPanelComponent editorMainContentProblemsPanelComponent;

    public PlaywrightEditorPage() {
        super("/");
    }

    /**
     * Get the center table component
     * @return PlaywrightTableComponent for table operations
     */
    public PlaywrightTableComponent getCenterTable() {
        centerTable.isPresent();
        return centerTable;
    }
}