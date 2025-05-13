package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.BasePageComponent;
import configuration.core.ui.SmartWebElement;
import lombok.Getter;
import org.openqa.selenium.support.FindBy;

import java.util.List;

@Getter
public class AddModuleComponent extends BasePageComponent {

    @FindBy(xpath = ".//input[@id='moduleName']")
    private SmartWebElement moduleNameField;

    @FindBy(xpath = ".//input[@id='modulePath']")
    private SmartWebElement modulePathField;

    @FindBy(xpath = ".//input[@value='Save']")
    private SmartWebElement moduleSaveBtn;

    @FindBy(css = "input[value='Save']")
    private List<SmartWebElement> moduleSaveBtns;

    @FindBy(xpath = ".//input[@value='Cancel']")
    private SmartWebElement moduleCancelBtn;

    @FindBy(xpath = ".//table[@class='properties properties-form wide']//tr[.//span[contains(text(), '%s')]]")
    private SmartWebElement commonProperty;


    public AddModuleComponent() {
    }

    public void fillForm(String moduleName, String modulePath) {
        moduleNameField.sendKeys(moduleName);
        modulePathField.sendKeys(modulePath);
        moduleSaveBtn.click();
    }

    public boolean isSpecificPropertyShown(String text) {
        return commonProperty.format(text).isDisplayed();
    }

}
