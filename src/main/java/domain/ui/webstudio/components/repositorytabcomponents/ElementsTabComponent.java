package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import domain.ui.webstudio.components.common.TableComponent;
import helpers.utils.WaitUtil;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
public class ElementsTabComponent extends BaseComponent {

    private static final Logger LOGGER = LogManager.getLogger(ElementsTabComponent.class);

    private TableComponent elementsTable;

    public ElementsTabComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public ElementsTabComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        elementsTable = createScopedComponent(TableComponent.class, "xpath=.//table", "elementsTable");
    }

    public void deleteElement(String name) {
        elementsTable.getRows().stream()
                .filter(row -> row.getValue().size() > 1)
                .filter(row -> row.getValue().get(1).equals(name))
                .findFirst()
                .ifPresent(row -> {
                    page.onDialog(dialog -> dialog.accept());
                    row.getCells().get(2).getLocator().locator("a.delete-icon").click();
                });
        WaitUtil.sleep(500, "Waiting for delete action to process");
    }

    public boolean isElementPresent(String name) {
        return elementsTable.getRows().stream()
                .filter(row -> row.getValue().size() > 1)
                .anyMatch(row -> row.getValue().get(1).equals(name));
    }
}
