package domain.ui.webstudio.components.editortabcomponents.leftmenu;

import configuration.core.ui.SmartWebElement;
import configuration.core.ui.BasePageComponent;
import org.openqa.selenium.support.FindBy;

public class LeftProjectModuleSelectorComponent extends BasePageComponent {

    @FindBy(xpath = ".//li/a[@class='projectName' and text()='%s']")
    private SmartWebElement projectNameLink;

    @FindBy(xpath = ".//li/a[text()='%s']/following-sibling::ul/li/a[text()='%s']")
    private SmartWebElement projectModuleLink;

    public LeftProjectModuleSelectorComponent() {
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
