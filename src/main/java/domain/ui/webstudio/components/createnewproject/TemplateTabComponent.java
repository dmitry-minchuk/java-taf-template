package domain.ui.webstudio.components.createnewproject;

import configuration.core.ui.BasePageComponent;
import configuration.core.ui.SmartWebElement;
import org.openqa.selenium.support.FindBy;

public class TemplateTabComponent extends BasePageComponent {

    @FindBy(xpath = ".//table[@id='projectTemplates']//tr[.//span[contains(text(), '%s')]]")
    private SmartWebElement projectTemplate;

    @FindBy(xpath = ".//input[@id='createProjectFormTempl:projectName']")
    private SmartWebElement projectNameField;

    @FindBy(id = "createProjectFormTempl:sbtTemplatesBtn")
    private SmartWebElement createProjectBtn;

    @FindBy(xpath = ".//input[@value='Cancel']")
    private SmartWebElement cancelBtn;

    public TemplateTabComponent() {
    }

    public void createProjectFromTemplate(String projectName, String templateName) {
        projectTemplate.format(templateName).click();
        projectNameField.sendKeys(projectName);
        createProjectBtn.click();
    }
}
