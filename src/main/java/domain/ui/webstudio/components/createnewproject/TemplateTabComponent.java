package domain.ui.webstudio.components.createnewproject;

import configuration.core.ui.BasePageComponent;
import configuration.core.ui.SmartWebElement;
import lombok.Getter;
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
        projectNameField.sendKeys(5, projectName);
        createProjectBtn.click();
    }

    @Getter
    public enum TemplateNames {
        EMPTY_PROJECT("Empty Project"),
        SAMPLE_PROJECT("Sample Project"),
        EXAMPLE_1("Example 1 - Bank Rating"),
        EXAMPLE_2("Example 2 - Corporate Rating"),
        EXAMPLE_3("Example 3 - Auto Policy Calculation"),
        TUTORIAL_1("Tutorial 1 - Introduction to Decision Tables"),
        TUTORIAL_2("Tutorial 2 - Introduction to Data Tables"),
        TUTORIAL_3("Tutorial 3 - More Advanced Decision and Data Tables"),
        TUTORIAL_4("Tutorial 4 - Introduction to Column Match Tables"),
        TUTORIAL_5("Tutorial 5 - Introduction to TBasic Tables"),
        TUTORIAL_6("Tutorial 6 - Introduction to Spreadsheet Tables"),
        TUTORIAL_7("Tutorial 7 - Introduction to Table Properties"),
        TUTORIAL_8("Tutorial 8 - Introduction to Smart Rules and Smart Lookup Tables");

        private final String value;

        TemplateNames(String value) {
            this.value = value;
        }
    }
}
