package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.BasePageComponent;
import configuration.core.ui.SmartWebElement;
import helpers.utils.WindowSwitcher;
import lombok.Getter;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.stream.Collectors;

public class TableToolbarPanelComponent extends BasePageComponent {

    @Getter
    @FindBy(xpath = ".//a[.//div[text()='Edit']]")
    private SmartWebElement editBtn;

    @Getter
    @FindBy(xpath = ".//a[.//div[text()='Open']]")
    private SmartWebElement openBtn;

    @Getter
    @FindBy(xpath = ".//a[.//div[text()='Copy']]")
    private SmartWebElement copyBtn;

    @Getter
    @FindBy(xpath = ".//a[.//div[text()='Remove']]")
    private SmartWebElement removeBtn;

    @FindBy(xpath = ".//a[.//div[text()='Run']]")
    private SmartWebElement runBtn;

    @FindBy(xpath = ".//a[.//div[text()='Trace']]")
    private SmartWebElement traceBtn;

    @FindBy(xpath = ".//a[.//div[text()='Create Test']]")
    private SmartWebElement createTestBtn;

    // Edit Panel elements

    // RunMenu Dropdown elements

    @FindBy(xpath = ".//input[@id='inputArgsForm:runButton']")
    private SmartWebElement runDropdownBtn;

    @FindBy(xpath = ".//span[contains(text(), '%s')]/a[@title='Add new element to collection']")
    private SmartWebElement addElementToCollectionBtn;

    @FindBy(xpath = ".//span[./span[contains(text(), '%s')]/a[@title='Add new element to collection']]/preceding-sibling::span")
    private SmartWebElement addedElementsExpander;

    // Trace Dropdown and opened window elements

    @FindBy(xpath = ".//input[@id='inputArgsForm:traceButton']")
    private SmartWebElement traceDropdownBtn;

    @FindBy(xpath = ".//input[@id='inputArgsForm:traceIntoFileButton']")
    private SmartWebElement traceIntoFileDropdownBtn;

    @FindBy(xpath = ".//span[text()='factor = ']/input")
    private List<SmartWebElement> factorTextFields;

    @FindBy(xpath = ".//input[@type='radio' and @name='inputArgsForm:inputTestCaseType' and following-sibling::label[contains(text(),'JSON')]]")
    private SmartWebElement jsonRadioBtn;

    @FindBy(xpath = ".//textarea[@name='inputArgsForm:jsonInput']")
    private SmartWebElement jsonTextField;

    @FindBy(xpath = "//span[contains(@class, 'fancytree-exp-c')]/span[@class='fancytree-expander']")
    private SmartWebElement traceWindowLeftPanelExpander;

    @FindBy(xpath = "//span[text()='factor = ']/input")
    private List<SmartWebElement> traceWindowExpanderList;

    @FindBy(xpath = "//li//span[@class='fancytree-title']\"")
    private List<SmartWebElement> traceWindowMthodList;

    public TableToolbarPanelComponent() {
    }

    public IRunMenu clickRun() {
        runBtn.click();
        return new RunMenu();
    }

    public IRunMenu getRunMenu() {
        return new RunMenu();
    }

    public ITraceMenu clickTrace() {
        traceBtn.click();
        return new TraceMenu();
    }

    public ITraceMenu getTraceMenu() {
        return new TraceMenu();
    }

    public ITraceWindow getTraceWindow() {
        return new TraceWindow();
    }

    protected class RunMenu implements IRunMenu{
        public RunMenu clickRunInsideMenu() {
            runDropdownBtn.click();
            return this;
        }

        public RunMenu clickAddElementToCollectionBtn(String containsText) {
            addElementToCollectionBtn.format(containsText).click();
            return this;
        }

        public RunMenu clickAddedElementsExpander(String containsText) {
            addedElementsExpander.format(containsText).click();
            return this;
        }
    }

    public interface IRunMenu {
        IRunMenu clickRunInsideMenu();
        IRunMenu clickAddElementToCollectionBtn(String containsText);
        IRunMenu clickAddedElementsExpander(String containsText);
    }

    protected class TraceMenu implements ITraceMenu {
        public TraceMenu setFactorTextField(String text) {
            SmartWebElement factorTextField = factorTextFields.stream()
                    .filter(e -> e.isDisplayed(1))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No factorTextField found"));
            factorTextField.sendKeys(text);
            return this;
        }

        public TraceWindow clickTraceInsideMenu() {
            traceDropdownBtn.click();
            WindowSwitcher.switchToWindow("Trace");
            WindowSwitcher.maximizeWindow("Trace");
            return new TraceWindow();
        }

        public TraceMenu clickTraceIntoFile() {
            traceIntoFileDropdownBtn.click();
            return this;
        }

        public TraceMenu selectJSONTrace(String json) {
            jsonRadioBtn.click();
            jsonTextField.sendKeys(1, json);
            return this;
        }
    }

    public interface ITraceMenu {
        ITraceMenu setFactorTextField(String text);
        ITraceMenu selectJSONTrace(String json);
        ITraceMenu clickTraceIntoFile();
        ITraceWindow clickTraceInsideMenu();
    }

    public class TraceWindow implements ITraceWindow {
        public TraceWindow expandItemInTree(int position) {
            traceWindowExpanderList.get(position).click();
            return this;
        }

        public List<String> getVisibleItemsFromTree() {
            return traceWindowMthodList.stream()
                    .map(SmartWebElement::getText)
                    .filter(s -> s != null && !s.trim().isEmpty())
                    .collect(Collectors.toList());
        }
    }

    public interface ITraceWindow {
        ITraceWindow expandItemInTree(int position);
        List<String> getVisibleItemsFromTree();
    }

}
