package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import domain.ui.webstudio.components.common.TableComponent;

import java.util.List;

public class TagsPopupComponent extends BaseComponent {

    private TableComponent tagsTable;

    public TagsPopupComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public TagsPopupComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        tagsTable = createScopedComponent(TableComponent.class, "xpath=.//table[@class='formfields']", "tagsTable");
    }

    private int getTagTypeRowByName(String tagTypeName) {
        int rowCount = tagsTable.getRowsCount();
        for (int i = 1; i <= rowCount; i++) {
            String cellText = tagsTable.getCell(i, 1).getText().trim().replace(":", "");
            if (cellText.equals(tagTypeName))
                return i;
        }
        throw new RuntimeException("Tag type '" + tagTypeName + "' not found in tags popup");
    }

    public List<String> getAllTagTypeNames() {
        return tagsTable.getRows().stream()
                .map(row -> row.getCells().getFirst().getText().trim().replace(":", ""))
                .toList();
    }

    public List<String> getAvailableTagsForType(String tagTypeName) {
        int row = getTagTypeRowByName(tagTypeName);
        return tagsTable.getCell(row, 2).getLocator()
                .locator("xpath=.//ul[@class='es-list']/li")
                .allTextContents();
    }

    public TagsPopupComponent selectTagForType(String tagTypeName, String tagValue) {
        int row = getTagTypeRowByName(tagTypeName);
        tagsTable.getCell(row, 2).getLocator()
                .locator("xpath=.//input[contains(@class, 'editable-select')]")
                .click();
        tagsTable.getCell(row, 2).getLocator()
                .locator(String.format("xpath=.//ul[@class='es-list']/li[@value='%s']", tagValue))
                .click();
        return this;
    }

    public String getSelectedTagForType(String tagTypeName) {
        int row = getTagTypeRowByName(tagTypeName);
        return tagsTable.getCell(row, 2).getLocator()
                .locator("xpath=.//input[contains(@class, 'editable-select')]")
                .getAttribute("value");
    }
}
