package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.BasePageComponent;
import configuration.core.ui.SmartWebElement;
import lombok.Getter;
import org.openqa.selenium.support.FindBy;

@Getter
public class TableToolbarPanelComponent extends BasePageComponent {

    @FindBy(xpath = ".//a[.//div[text()='Edit']]")
    private SmartWebElement editBtn;

    @FindBy(xpath = ".//a[.//div[text()='Open']]")
    private SmartWebElement openBtn;

    @FindBy(xpath = ".//a[.//div[text()='Copy']]")
    private SmartWebElement copyBtn;

    @FindBy(xpath = ".//a[.//div[text()='Remove']]")
    private SmartWebElement removeBtn;

    @FindBy(xpath = ".//a[.//div[text()='Run']]")
    private SmartWebElement runBtn;

    @FindBy(xpath = ".//a[.//div[text()='Trace']]")
    private SmartWebElement traceBtn;

    @FindBy(xpath = ".//a[.//div[text()='Create Test']]")
    private SmartWebElement createTestBtn;

    // Run Dropdown elements

    @FindBy(xpath = ".//input[@id='inputArgsForm:runButton']")
    private SmartWebElement runDropdownBtn;

    @FindBy(xpath = ".//span[contains(text(), '%s')]/a[@title='Add new element to collection']")
    private SmartWebElement addElementToCollectionBtn;

    @FindBy(xpath = ".//span[./span[contains(text(), '%s')]/a[@title='Add new element to collection']]/preceding-sibling::span")
    private SmartWebElement addedElementsExpander;

    // Trace Dropdown elements

    @FindBy(xpath = ".//input[@id='inputArgsForm:traceButton']")
    private SmartWebElement traceDropdownBtn;

    public TableToolbarPanelComponent() {
    }



}
