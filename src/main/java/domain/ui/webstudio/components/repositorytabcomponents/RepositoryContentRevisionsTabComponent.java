package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class RepositoryContentRevisionsTabComponent extends BaseComponent {

    private static final Logger LOGGER = LogManager.getLogger(RepositoryContentRevisionsTabComponent.class);

    private List<WebElement> visibleRows;

    public RepositoryContentRevisionsTabComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public RepositoryContentRevisionsTabComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        visibleRows = createScopedElementList("xpath=.//div[@class='table-row' and not(@style='display: none')]", "visibleRows");
    }

    public int getRevisionsCount() {
        WaitUtil.waitForCondition(() -> !visibleRows.isEmpty(), 1000, 100, "Waiting for visibleRows to load.");
        int count = visibleRows.size();
        LOGGER.info("Found {} visible revision rows", count);
        return count;
    }

    public String getRevisionDescription(int rowIndex) {
        if (rowIndex < 1) {
            throw new IllegalArgumentException("Row index must be >= 1, got: " + rowIndex);
        }
        String comment = visibleRows.get(rowIndex - 1).getLocator().locator("xpath=.//div[@attr='comment']").textContent().trim();
        LOGGER.info("Revision {} description: '{}'", rowIndex, comment);
        return comment;
    }

    public String getRevisionModifiedBy(int rowIndex) {
        if (rowIndex < 1) {
            throw new IllegalArgumentException("Row index must be >= 1, got: " + rowIndex);
        }
        return visibleRows.get(rowIndex - 1).getLocator().locator("xpath=.//div[@attr='modified-by']").textContent().trim();
    }

    public String getRevisionModifiedAt(int rowIndex) {
        if (rowIndex < 1) {
            throw new IllegalArgumentException("Row index must be >= 1, got: " + rowIndex);
        }
        return visibleRows.get(rowIndex - 1).getLocator().locator("xpath=.//div[@attr='modified-at']").textContent().trim();
    }

    public String getRevisionId(int rowIndex) {
        if (rowIndex < 1) {
            throw new IllegalArgumentException("Row index must be >= 1, got: " + rowIndex);
        }
        return visibleRows.get(rowIndex - 1).getLocator().locator("xpath=.//div[@attr='revision-id']").textContent().trim();
    }
}
