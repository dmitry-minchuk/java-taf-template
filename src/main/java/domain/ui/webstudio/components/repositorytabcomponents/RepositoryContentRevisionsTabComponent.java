package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import domain.ui.webstudio.components.common.TableComponent;

public class RepositoryContentRevisionsTabComponent extends BaseComponent {

    private TableComponent revisionsTable;

    public RepositoryContentRevisionsTabComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public RepositoryContentRevisionsTabComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        revisionsTable = createScopedComponent(TableComponent.class, "xpath=.//table[contains(@class, 'rf-dt')]", "revisionsTable");
    }

    public int getRevisionsCount() {
        return revisionsTable.getRowsCount();
    }

    public String getRevisionDescription(int rowIndex) {
        return revisionsTable.getRow(rowIndex).getValue().getFirst();
    }

    public TableComponent.PlaywrightTableRowComponent selectRevision(int rowIndex) {
        return revisionsTable.getRow(rowIndex);
    }
}
