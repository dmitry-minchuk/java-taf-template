package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ElementsTabComponent extends BaseComponent {

    private static final Logger LOGGER = LogManager.getLogger(ElementsTabComponent.class);

    private WebElement elementsTable;
    private WebElement elementsContainer;

    public ElementsTabComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public ElementsTabComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        elementsContainer = createScopedElement("xpath=//div[@id='elements']", "elementsContainer");
        elementsTable = createScopedElement("xpath=.//table", "elementsTable");
    }

    public boolean isElementPresent(String elementName) {
        try {
            WebElement elementCell = createScopedElement(
                String.format("xpath=.//div[@id='elements']//table//td[contains(text(), '%s')]", elementName),
                "elementCell"
            );
            boolean isPresent = elementCell.isVisible(1000);
            LOGGER.info("Element '{}' presence check: {}", elementName, isPresent);
            return isPresent;
        } catch (Exception e) {
            LOGGER.warn("Element '{}' not found or error occurred: {}", elementName, e.getMessage());
            return false;
        }
    }

    public boolean verifyElementPresent(String elementName) {
        return isElementPresent(elementName);
    }

    public void deleteElement(String elementName) {
        try {
            WebElement deleteLink = createScopedElement(
                String.format("xpath=.//div[@id='elements']//table//td[contains(text(), '%s')]//..//a[@title='Delete']", elementName),
                "deleteLink"
            );
            deleteLink.click();
            LOGGER.info("Clicked delete for element: {}", elementName);
        } catch (Exception e) {
            LOGGER.error("Error deleting element '{}': {}", elementName, e);
            throw new RuntimeException("Failed to delete element: " + elementName, e);
        }
    }

    public boolean isTableVisible() {
        try {
            return elementsTable.isVisible(1000);
        } catch (Exception e) {
            return false;
        }
    }
}
