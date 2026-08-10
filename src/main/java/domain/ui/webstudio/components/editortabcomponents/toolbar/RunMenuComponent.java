package domain.ui.webstudio.components.editortabcomponents.toolbar;

import com.microsoft.playwright.Page;
import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;

import java.util.List;

/**
 * The Run dropdown of the table toolbar: the input-parameters form and the launch button. Scoped under
 * {@code form#inputArgsForm} (verified against the live 6.4.0 DOM — the menu content renders inside
 * the form).
 */
public class RunMenuComponent extends BaseComponent implements IRunMenu {

    private final WebElement createItemBtn;
    private final WebElement expandTypesBtn;
    private final WebElement addElementToCollectionBtnTemplate;
    private final WebElement runInsideDropdownBtn;
    private final WebElement addedElementsExpanderTemplate;
    private final WebElement selectTypeDropdown;
    private final WebElement inputTextFieldTemplate;
    private final WebElement inputSelectFieldTemplate;

    public RunMenuComponent(Page page) {
        this(new WebElement(page, "xpath=//form[@id='inputArgsForm']", "inputArgsForm"));
    }

    public RunMenuComponent(WebElement rootLocator) {
        super(rootLocator);
        createItemBtn = createScopedElement("xpath=.//a[@title='Create']", "createItemBtn");
        expandTypesBtn = createScopedElement("xpath=.//table[@class='table']//span[contains(@class, 'rf-trn-hnd-colps') and contains(@class, 'rf-trn-hnd')]", "expandTypesBtn");
        addElementToCollectionBtnTemplate = createScopedElement("xpath=.//span[contains(text(), '%s')]//a[@title='Add new element to collection']", "addElementToCollectionBtn");
        runInsideDropdownBtn = createScopedElement("xpath=.//input[@id='inputArgsForm:runButton']", "runInsideDropdownBtn");
        addedElementsExpanderTemplate = createScopedElement("xpath=.//span[./span[contains(text(), '%s')]/a[@title='Add new element to collection']]/preceding-sibling::span", "addedElementsExpander");
        selectTypeDropdown = createScopedElement("xpath=.//div[contains(@id, 'input')]//select", "selectTypeDropdown");
        inputTextFieldTemplate = createScopedElement("xpath=(.//div[contains(@id, 'input')]//input[@type='text'])[%s]", "inputTextField");
        inputSelectFieldTemplate = createScopedElement("xpath=(.//div[contains(@id, 'input')]//select)[%s]", "inputSelectField");
    }

    @Override
    public IRunMenu clickCreateItem() {
        createItemBtn.click();
        return this;
    }

    @Override
    public IRunMenu clickAddElementToCollectionBtn(String containsText) {
        addElementToCollectionBtnTemplate.format(containsText).click();
        return this;
    }

    @Override
    public IRunMenu clickExpandCollection() {
        expandTypesBtn.click();
        return this;
    }

    @Override
    public IRunMenu clickRunInsideMenu() {
        runInsideDropdownBtn.click();
        WaitUtil.sleep(250, "Waiting for run menu action to complete");
        return this;
    }

    @Override
    public IRunMenu clickAddedElementsExpander(String containsText) {
        addedElementsExpanderTemplate.format(containsText).click();
        return this;
    }

    @Override
    public List<String> getAliasDropdownValues() {
        return selectTypeDropdown.getSelectValues();
    }

    @Override
    public IRunMenu setInputTextField(String index, String value) {
        inputTextFieldTemplate.format(index).fill(value);
        return this;
    }

    @Override
    public IRunMenu setInputSelectField(String index, String value) {
        inputSelectFieldTemplate.format(index).selectByVisibleText(value);
        return this;
    }
}
