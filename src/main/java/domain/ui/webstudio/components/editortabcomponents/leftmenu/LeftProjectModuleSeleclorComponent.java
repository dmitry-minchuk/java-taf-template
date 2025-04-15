package domain.ui.webstudio.components.editortabcomponents.leftmenu;

import configuration.core.SmartWebElement;
import domain.ui.BasePageComponent;
import org.openqa.selenium.support.FindBy;

public class LeftProjectModuleSeleclorComponent extends BasePageComponent {

    @FindBy(xpath = ".//li/a[@class='projectName' and text()='%s']")
    private SmartWebElement projectNameLink;

    @FindBy(xpath = ".//li/a[text()='%s']/following-sibling::ul/li/a[text()='%s']")
    private SmartWebElement projectModuleLink;

    public LeftProjectModuleSeleclorComponent() {
    }

    public void selectProject(String projectName) {
        projectNameLink.format(projectName).click();
        // Opens ProjectDetailsComponent
    }

    public void selectModule(String projectName, String projectModuleName) {
        projectModuleLink.format(projectName, projectModuleName).click();
        // Opens ProjectModuleDetailsComponent
    }
}
