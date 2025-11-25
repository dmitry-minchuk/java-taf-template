package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import domain.ui.webstudio.components.common.TableComponent;
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
}
