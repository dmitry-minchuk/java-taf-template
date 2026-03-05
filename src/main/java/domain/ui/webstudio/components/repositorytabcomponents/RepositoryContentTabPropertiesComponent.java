package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import domain.ui.webstudio.components.common.TableComponent;
import helpers.utils.WaitUtil;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class RepositoryContentTabPropertiesComponent extends BaseComponent {

    private static final Logger LOGGER = LogManager.getLogger(RepositoryContentTabPropertiesComponent.class);

    private TableComponent propertiesTable;
    private TableComponent tagsTable;
    private WebElement branchSelect;
    private WebElement branchSelectError;

    public RepositoryContentTabPropertiesComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public RepositoryContentTabPropertiesComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        propertiesTable = createScopedComponent(TableComponent.class, "xpath=.//span[@id='propertiesContent']//table[@class='formfields']", "propertiesTable");
        tagsTable = createScopedComponent(TableComponent.class, "xpath=.//span[@id='editProjectTagsPanel']//table[@class='formfields']", "tagsTable");
        branchSelect = createScopedElement("xpath=.//select[@id='branchSelector']", "branchSelect");
        branchSelectError = createScopedElement("xpath=.//span[@id='branchSelector_error']", "branchSelectError");
    }

    @Getter
    public enum Property {
        NAME("Name:"),
        REVISION("Revision ID:"),
        STATUS("Status:"),
        CREATED_AT("Created At:"),
        CREATED_BY("Created By:"),
        MODIFIED_AT("Modified At:"),
        MODIFIED_BY("Modified By:"),
        PATH("Path:"),
        REPOSITORY("Repository:");

        private final String text;

        Property(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public String getProperty(Property name) {
        propertiesTable.isVisible();
        return propertiesTable.getCell(findRowByText(name.text), 2).getText().trim().replaceAll("\n", "");
    }

    // Helper method to find row index by text content (returns 1-based index)
    private int findRowByText(String text) {
        WaitUtil.sleep(500, "Waiting for properties table to fully render before searching for row");
        int rowCount = propertiesTable.getRowsCount();
        for (int i = 1; i <= rowCount; i++) {
            String cellText = propertiesTable.getCell(i, 1).getText().trim().replaceAll("\n", "");
            if (cellText.equals(text)) {
                LOGGER.info("Found row N{} with text: {}", i, cellText);
                return i;
            }
        }
        throw new RuntimeException("Row with text '" + text + "' not found in properties table");
    }

    // Convenience methods for common properties
    public String getName() {
        return getProperty(Property.NAME);
    }

    public String getRevision() {
        int row = findRowByText(Property.REVISION.getText());
        return propertiesTable.getCell(row, 2).getLocator()
                .locator("xpath=.//span[@class='expandable']")
                .textContent()
                .trim();
    }

    public boolean isRevisionExpandable() {
        int row = findRowByText(Property.REVISION.getText());
        return propertiesTable.getCell(row, 2).getLocator()
                .locator("xpath=.//span[@class='expandable']")
                .count() > 0;
    }

    public String getStatus() {
        return getProperty(Property.STATUS);
    }

    public String getCreatedAt() {
        return getProperty(Property.CREATED_AT);
    }

    public String getCreatedBy() {
        return getProperty(Property.CREATED_BY);
    }

    public String getModifiedAt() {
        return getProperty(Property.MODIFIED_AT);
    }

    public String getModifiedBy() {
        return getProperty(Property.MODIFIED_BY);
    }

    public String getPath() {
        return getProperty(Property.PATH);
    }

    public String getRepository() {
        return getProperty(Property.REPOSITORY);
    }

    public int getTagTypeRowByName(String tagTypeName) {
        int rowCount = tagsTable.getRowsCount();
        for (int i = 1; i <= rowCount; i++) {
            String cellText = tagsTable.getCell(i, 1).getText().trim().replace(":", "");
            if (cellText.equals(tagTypeName))
                return i;
        }
        throw new RuntimeException("Tag type '" + tagTypeName + "' not found in tags table");
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

    public RepositoryContentTabPropertiesComponent selectTagForType(String tagTypeName, String tagValue) {
        int row = getTagTypeRowByName(tagTypeName);
        tagsTable.getCell(row, 2).getLocator()
                .locator("xpath=.//input[@class='editable-select es-input']")
                .click();
        tagsTable.getCell(row, 2).getLocator()
                .locator(String.format("xpath=.//ul[@class='es-list']/li[@value='%s']", tagValue))
                .click();
        return this;
    }

    public String getSelectedTagForType(String tagTypeName) {
        int row = getTagTypeRowByName(tagTypeName);
        return tagsTable.getCell(row, 2).getLocator()
                .locator("xpath=.//input[contains(@class,'editable-select')]/following-sibling::ul/li[@selected='selected']")
                .getAttribute("value");
    }

    public RepositoryContentTabPropertiesComponent selectBranch(String branchName) {
        branchSelect.selectByVisibleText(branchName);
        return this;
    }

    public List<String> getSelectOprions() {
        return branchSelect.getSelectValues();
    }

    public String getSelectedBranch() {
        return branchSelect.getAttribute("title");
    }
}