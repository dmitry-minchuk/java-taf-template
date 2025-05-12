package domain.ui.webstudio.pages.mainpages;

import configuration.core.ui.SmartWebElement;
import configuration.core.ui.TableComponent;
import domain.ui.webstudio.components.editortabcomponents.ProblemsPanelComponent;
import domain.ui.webstudio.components.editortabcomponents.RightTableDetailsComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.LeftProjectModuleSeleclorComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.LeftRulesTreeComponent;
import lombok.Getter;
import org.openqa.selenium.support.FindBy;

public class EditorPage extends ProxyMainPage {

    @Getter
    @FindBy(xpath = "//div[@id='projects']")
    private LeftProjectModuleSeleclorComponent leftProjectModuleSeleclorComponent;

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

    public TableComponent getCenterTable() {
        centerTable.isPresent();
        return centerTable;
    }

    public EditorPage() {
        super("/");
    }
}
