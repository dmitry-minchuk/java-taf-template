package domain.ui.webstudio.pages.mainpages;

import configuration.core.ui.TableComponent;
import domain.ui.webstudio.components.editortabcomponents.*;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.LeftProjectModuleSelectorComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.LeftRulesTreeComponent;
import lombok.Getter;
import org.openqa.selenium.support.FindBy;

public class EditorPage extends ProxyMainPage {

    @Getter
    @FindBy(xpath = "//div[@id='projects']")
    private LeftProjectModuleSelectorComponent leftProjectModuleSelectorComponent;

    @Getter
    @FindBy(xpath = "//div[@id='left']")
    private LeftRulesTreeComponent leftRulesTreeComponent;

    @Getter
    @FindBy(xpath = "//div[@id='right']")
    private RightTableDetailsComponent rightTableDetailsComponent;

    @FindBy(xpath = "//table[@class='te_table']")
    private TableComponent centerTable;

    @Getter
    @FindBy(xpath = "//div[@id='bottom']")
    private ProblemsPanelComponent problemsPanelComponent;

    @Getter
    @FindBy(xpath = "//div[@class='page']")
    private ProjectDetailsComponent projectDetailsComponent;

    @Getter
    @FindBy(xpath = "//div[@id='editModulePopup_container']")
    private AddModuleComponent addModulePopupComponent;

    @Getter
    @FindBy(xpath = "//div[@id='tableToolbarPanel']")
    private TableToolbarPanelComponent tableToolbarPanelComponent;

    @Getter
    @FindBy(xpath = "//div[@class='te_toolbar']")
    private EditTablePanelComponent editTablePanelComponent;

    public EditorPage() {
        super("/");
    }

    public TableComponent getCenterTable() {
        centerTable.isPresent();
        return centerTable;
    }


}
