package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.BasePageComponent;
import configuration.core.ui.SmartWebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

public class ProjectDetailsComponent extends BasePageComponent {

    @FindBy(xpath = ".//h3/span[text()='Modules']")
    private SmartWebElement modulesHeaderElement;

    @FindBy(xpath = ".//h3/span[text()='Modules']/following-sibling::a[@title='Add Module']")
    private SmartWebElement addModuleBtn;

    public ProjectDetailsComponent() {
    }

    public void openAddModulePopup() {
        new Actions(getDriver())
                .moveToElement(modulesHeaderElement.getUnwrappedElement())
                .click(addModuleBtn.getUnwrappedElement())
                .perform();
    }

}
