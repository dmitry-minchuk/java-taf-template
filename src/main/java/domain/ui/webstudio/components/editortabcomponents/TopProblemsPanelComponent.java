package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;

public class TopProblemsPanelComponent extends BaseComponent {

    private WebElement allErrorsBlock;

    public TopProblemsPanelComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public TopProblemsPanelComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        allErrorsBlock = new WebElement(page, "xpath=//div[@id='problemsPanel']", "allErrorsBlock");
    }

    public String getText() {
        if (allErrorsBlock.isVisible(2000)) {
            return allErrorsBlock.getText().trim();
        }
        return "";
    }

    public boolean isVisible() {
        return allErrorsBlock.isVisible(2000);
    }

    public boolean isAbsent() {
        return !allErrorsBlock.isVisible(1000);
    }

    public boolean containsError(String errorText) {
        if (!allErrorsBlock.isVisible(2000)) {
            return false;
        }
        return allErrorsBlock.getText().contains(errorText);
    }
}
