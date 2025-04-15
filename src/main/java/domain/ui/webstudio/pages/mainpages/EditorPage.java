package domain.ui.webstudio.pages.mainpages;

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

    public EditorPage() {
        super("/");
    }
}
