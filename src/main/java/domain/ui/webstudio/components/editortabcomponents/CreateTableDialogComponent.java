package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.BaseComponent;

public class CreateTableDialogComponent extends BaseComponent {

    private WebElement tableTypeRadioTemplate;
    private WebElement nextButton;
    private WebElement technicalNameInput;
    private WebElement parameterTypeSelect;
    private WebElement parameterNameInput;
    private WebElement saveButton;

    public CreateTableDialogComponent(WebElement root) {
        super(root);
        initializeElements();
    }

    private void initializeElements() {
        tableTypeRadioTemplate = createScopedElement("xpath=.//label[normalize-space(.)='%s']/../input", "tableTypeRadioTemplate");
        nextButton = createScopedElement("xpath=.//input[@value='Next']", "nextButton");
        technicalNameInput = createScopedElement("xpath=.//input[contains(@id, ':technicalName')]", "technicalNameInput");
        parameterTypeSelect = createScopedElement("xpath=(.//span[contains(@id,'paramTable')]//tbody//tr)[1]//select", "parameterTypeSelect");
        parameterNameInput = createScopedElement("xpath=(.//span[contains(@id,'paramTable')]//tbody//tr)[1]//input[contains(@id,':pname')]", "parameterNameInput");
        saveButton = createScopedElement("xpath=.//input[@value='Save']", "saveButton");
    }

    public CreateTableDialogComponent selectType(String type) {
        tableTypeRadioTemplate.format(type).click();
        return this;
    }

    public CreateTableDialogComponent clickNext() {
        nextButton.click();
        return this;
    }

    public CreateTableDialogComponent setTechnicalName(String name) {
        technicalNameInput.fill(name);
        nextButton.click();
        return this;
    }

    public CreateTableDialogComponent addParameter(String type, String name) {
        parameterTypeSelect.selectByVisibleText(type);
        parameterNameInput.fill(name);
        nextButton.click();
        return this;
    }

    public void save() {
        saveButton.click();
    }
}
