package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;

import java.util.List;

public class MissingTagsPopupComponent extends BaseComponent {

    private List<WebElement> warningList;
    private WebElement continueBtn;
    private WebElement cancelBtn;

    public MissingTagsPopupComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public MissingTagsPopupComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        warningList = createScopedElementList("xpath=.//span[@class='error']", "warningList");
        continueBtn = createScopedElement("xpath=.//input[@value='Continue']", "continueBtn");
        cancelBtn = createScopedElement("xpath=.//input[@value='Cancel']", "cancelBtn");
    }

    public List<String> getAllWarnings() {
        return warningList.stream()
                .map(row -> row.getText().trim())
                .toList();
    }

    public void clickContinue() {
        continueBtn.click();
    }

    public void clickCancel() {
        cancelBtn.click();
    }
}
