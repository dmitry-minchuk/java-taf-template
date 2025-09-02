package domain.ui.webstudio.pages.mainpages;

import configuration.core.ui.PlaywrightWebElement;
import configuration.core.ui.PlaywrightTableComponent;
import domain.ui.webstudio.components.PlaywrightTabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.PlaywrightAddModuleComponent;
import domain.ui.webstudio.components.editortabcomponents.PlaywrightEditTablePanelComponent;
import domain.ui.webstudio.components.editortabcomponents.PlaywrightEditorMainContentProblemsPanelComponent;
import domain.ui.webstudio.components.editortabcomponents.PlaywrightProblemsPanelComponent;
import domain.ui.webstudio.components.editortabcomponents.PlaywrightProjectDetailsComponent;
import domain.ui.webstudio.components.editortabcomponents.PlaywrightRightTableDetailsComponent;
import domain.ui.webstudio.components.editortabcomponents.PlaywrightTableToolbarPanelComponent;
import domain.ui.webstudio.components.editortabcomponents.PlaywrightTestResultValidationComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.PlaywrightLeftProjectModuleSelectorComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.PlaywrightLeftRulesTreeComponent;
import lombok.Getter;

@Getter
public class PlaywrightEditorPage extends PlaywrightProxyMainPage {

    private PlaywrightLeftProjectModuleSelectorComponent leftProjectModuleSelectorComponent;
    private PlaywrightLeftRulesTreeComponent leftRulesTreeComponent;
    private PlaywrightRightTableDetailsComponent rightTableDetailsComponent;
    private PlaywrightTabSwitcherComponent tabSwitcherComponent;
    private PlaywrightTableComponent centerTable;
    private PlaywrightProblemsPanelComponent problemsPanelComponent;
    private PlaywrightProjectDetailsComponent projectDetailsComponent;
    private PlaywrightAddModuleComponent addModulePopupComponent;
    private PlaywrightTableToolbarPanelComponent tableToolbarPanelComponent;
    private PlaywrightEditTablePanelComponent editTablePanelComponent;
    private PlaywrightTestResultValidationComponent testResultValidationComponent;
    private PlaywrightEditorMainContentProblemsPanelComponent editorMainContentProblemsPanelComponent;

    public PlaywrightEditorPage() {
        super("/");
        initializeComponents();
    }

    private void initializeComponents() {
        leftProjectModuleSelectorComponent = createScopedComponent(PlaywrightLeftProjectModuleSelectorComponent.class, "xpath=//div[@id='projects']", "leftProjectModuleSelectorComponent");
        leftRulesTreeComponent = createScopedComponent(PlaywrightLeftRulesTreeComponent.class, "xpath=//div[@id='left']", "leftRulesTreeComponent");
        rightTableDetailsComponent = createScopedComponent(PlaywrightRightTableDetailsComponent.class, "xpath=//div[@id='right']", "rightTableDetailsComponent");
        tabSwitcherComponent = createScopedComponent(PlaywrightTabSwitcherComponent.class, "xpath=//ul[@role='menu' and contains(@class,'ant-menu-horizontal')]", "tabSwitcherComponent");
        centerTable = createScopedComponent(PlaywrightTableComponent.class, "xpath=//table[@class='te_table']", "centerTable");
        tableToolbarPanelComponent = createScopedComponent(PlaywrightTableToolbarPanelComponent.class, "xpath=//div[@id='tableToolbarPanel']", "tableToolbarPanelComponent");
        testResultValidationComponent = createScopedComponent(PlaywrightTestResultValidationComponent.class, "xpath=//div[@id='content' and contains(@class,'ui-layout-center')]", "testResultValidationComponent");
        problemsPanelComponent = createScopedComponent(PlaywrightProblemsPanelComponent.class, "xpath=//div[@id='bottom']", "problemsPanelComponent");
        projectDetailsComponent = createScopedComponent(PlaywrightProjectDetailsComponent.class, "xpath=//div[@class='page']", "projectDetailsComponent");
        addModulePopupComponent = createScopedComponent(PlaywrightAddModuleComponent.class, "xpath=//div[@id='editModulePopup_container']", "addModulePopupComponent");
        editTablePanelComponent = createScopedComponent(PlaywrightEditTablePanelComponent.class, "xpath=//div[@class='te_toolbar']", "editTablePanelComponent");
        editorMainContentProblemsPanelComponent = createScopedComponent(PlaywrightEditorMainContentProblemsPanelComponent.class, "xpath=//div[@id='content']", "editorMainContentProblemsPanelComponent");
    }

    public PlaywrightTableComponent getCenterTable() {
        centerTable.isPresent();
        WaitUtil.sleep(500);
        return centerTable;
    }
}