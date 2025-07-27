package domain.ui.webstudio.pages.mainpages;

import configuration.core.ui.PlaywrightWebElement;
import configuration.core.ui.PlaywrightTableComponent;
import domain.ui.webstudio.components.PlaywrightTabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.PlaywrightRightTableDetailsComponent;
import domain.ui.webstudio.components.editortabcomponents.PlaywrightTableToolbarPanelComponent;
import domain.ui.webstudio.components.editortabcomponents.PlaywrightTestResultValidationComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.PlaywrightLeftProjectModuleSelectorComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.PlaywrightLeftRulesTreeComponent;
import lombok.Getter;

/**
 * Playwright version of EditorPage - Main editor interface page
 * Contains all editor components: left project selector, rules tree, right table details
 * Matches original EditorPage architecture with Playwright-native components
 */
@Getter
public class PlaywrightEditorPage extends PlaywrightProxyMainPage {

    private PlaywrightLeftProjectModuleSelectorComponent leftProjectModuleSelectorComponent;
    private PlaywrightLeftRulesTreeComponent leftRulesTreeComponent;
    private PlaywrightRightTableDetailsComponent rightTableDetailsComponent;
    private PlaywrightTabSwitcherComponent tabSwitcherComponent;
    @Getter
    private PlaywrightTableComponent centerTable;
    private PlaywrightTableToolbarPanelComponent tableToolbarPanelComponent;
    private PlaywrightTestResultValidationComponent testResultValidationComponent;

    public PlaywrightEditorPage() {
        super("/");
        initializeComponents();
    }

    private void initializeComponents() {
        // Left project module selector: "//div[@id='projects']"
        PlaywrightWebElement projectsLocator = new PlaywrightWebElement(page, "xpath=//div[@id='projects']", "leftProjectModuleSelectorComponent");
        leftProjectModuleSelectorComponent = new PlaywrightLeftProjectModuleSelectorComponent(projectsLocator);
        
        // Left rules tree: "//div[@id='left']"
        PlaywrightWebElement leftLocator = new PlaywrightWebElement(page, "xpath=//div[@id='left']", "leftRulesTreeComponent");
        leftRulesTreeComponent = new PlaywrightLeftRulesTreeComponent(leftLocator);
        
        // Right table details: "//div[@id='right']"
        PlaywrightWebElement rightLocator = new PlaywrightWebElement(page, "xpath=//div[@id='right']", "rightTableDetailsComponent");
        rightTableDetailsComponent = new PlaywrightRightTableDetailsComponent(rightLocator);
            
        // Tab switcher component for EDITOR/REPOSITORY navigation
        PlaywrightWebElement tabLocator = new PlaywrightWebElement(page, "xpath=//ul[contains(@class,'nav-tabs')]", "tabSwitcherComponent");
        tabSwitcherComponent = new PlaywrightTabSwitcherComponent(tabLocator);
        
        // Center table component: "//table[@class='te_table']"
        centerTable = new PlaywrightTableComponent(page, "xpath=//table[@class='te_table']");
        
        // Table toolbar panel: "//div[@id='tableToolbarPanel']"
        PlaywrightWebElement toolbarLocator = new PlaywrightWebElement(page, "xpath=//div[@id='tableToolbarPanel']", "tableToolbarPanelComponent");
        tableToolbarPanelComponent = new PlaywrightTableToolbarPanelComponent(toolbarLocator);
        
        // Test result validation: "//div[@id='content' and contains(@class,'ui-layout-center')]"
        PlaywrightWebElement testResultLocator = new PlaywrightWebElement(page, "xpath=//div[@id='content' and contains(@class,'ui-layout-center')]", "testResultValidationComponent");
        testResultValidationComponent = new PlaywrightTestResultValidationComponent(testResultLocator);
    }

}