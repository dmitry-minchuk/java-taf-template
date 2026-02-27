package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;

import java.util.List;

public class TopProblemsPanelComponent extends BaseComponent {

    private List<WebElement> errorItems;

    public TopProblemsPanelComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public TopProblemsPanelComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        errorItems = createScopedElementList("xpath=.//div[@class='messages']//li[contains(@class,'error')]/span", "errorItems");
    }

    public String getText() {
        if (errorItems.isEmpty()) {
            return "";
        }
        return errorItems.getFirst().getText().trim();
    }

    public boolean isVisible() {
        return !errorItems.isEmpty();
    }

    public boolean isAbsent() {
        return errorItems.isEmpty();
    }

    public boolean containsError(String errorText) {
        return errorItems.stream().anyMatch(item -> item.getText().contains(errorText));
    }

    public List<String> getAllErrors() {
        return errorItems.stream().map(item -> item.getText().trim()).toList();
    }
}
