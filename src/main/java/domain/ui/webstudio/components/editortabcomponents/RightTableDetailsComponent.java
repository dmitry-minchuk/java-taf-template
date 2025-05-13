package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.SmartWebElement;
import configuration.core.ui.BasePageComponent;
import lombok.Getter;
import org.openqa.selenium.support.FindBy;

public class RightTableDetailsComponent extends BasePageComponent {

    @FindBy(xpath = ".//a[@id='addPropBtn']")
    private SmartWebElement addPropertyLink;

    @FindBy(xpath = ".//div[@id='addPropsPanel']//select")
    private SmartWebElement propertyTypeSelector;

    @FindBy(xpath = ".//div[@id='addPropsPanel']//input[@value='Add']")
    private SmartWebElement addBtn;

    @FindBy(xpath = ".//div[@id='addPropsPanel']//a[text()='Cancel']")
    private SmartWebElement cancelBtn;

    @FindBy(xpath = ".//td[@class='propName' and contains(text(),'%s')]/following-sibling::td[@class='propData']/span/input")
    private SmartWebElement propertyInputTextField;

    @FindBy(xpath = ".//td[@class='propName' and contains(text(),'%s')]/following-sibling::td[@class='propData']/span[1][contains(text(),'%s')]")
    private SmartWebElement propertyContent;

    @Getter
    @FindBy(xpath = ".//input[@id='savePropsButton']")
    private SmartWebElement saveBtn;

    public RightTableDetailsComponent() {
    }

    public void addProperty(String propertyName) {
        addPropertyLink.click();
        propertyTypeSelector.selectByVisibleText(propertyName);
        addBtn.click();
    }

    public void setProperty(String propertyName, String propertyValue) {
        propertyInputTextField.format(propertyName).sendKeys(propertyValue);
    }

    public boolean isPropertySet(String propertyName, String propertyValue) {
        return propertyContent.format(propertyName, propertyValue).isDisplayed();
    }

    @Getter
    public enum DropdownOptions {
        DESCRIPTION("Description"),
        CATEGORY("Category"),
        TAGS("Tags");

        private String value;

        DropdownOptions(String value) {
            this.value = value;
        }
    }

}
