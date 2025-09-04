package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.CoreComponent;
import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.TableComponent;
import configuration.driver.LocalDriverPool;
import helpers.utils.WaitUtil;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import static org.assertj.core.api.Assertions.assertThat;

public class RepositoryContentTabPropertiesComponent extends CoreComponent {

    private static final Logger LOGGER = LogManager.getLogger(RepositoryContentTabPropertiesComponent.class);

    private TableComponent propertiesTable;
    private WebElement authorElementTemplate;
    private WebElement expandableRevision;
    private WebElement tagSelectOpenTemplate;
    private WebElement tagError;
    private WebElement tagsSection;

    public RepositoryContentTabPropertiesComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public RepositoryContentTabPropertiesComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        propertiesTable = createScopedComponent(TableComponent.class, "xpath=.//table[@class='formfields']", "propertiesTable");
        authorElementTemplate = createScopedElement("xpath=.//span[text()='%s']", "authorElementTemplate");
        expandableRevision = createScopedElement("xpath=.//div[@class='rf-tab']//table[@class='formfields']//span[@class='expandable']", "expandableRevision");
        tagSelectOpenTemplate = createScopedElement("xpath=.//h3[contains(text(), 'Tags')]/..//td[contains(text(), '%s')]/..//span", "tagSelectOpenTemplate");
        tagError = createScopedElement("xpath=.//span[@class='error']", "tagError");
        tagsSection = createScopedElement("xpath=.//h3[contains(text(), 'Tags')]", "tagsSection");
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

    public static String[] properties = {
            "Effective Date",
            "Expiration Date",
            "Start Request Date",
            "End Request Date",
            "Canada Region",
            "Canada Province",
            "Countries",
            "Region",
            "Currency",
            "Language",
            "LOB",
            "Origin",
            "US Region",
            "US States",
    };

    public String getProperty(Property name) {
        propertiesTable.isVisible();
        return propertiesTable.getCell(findRowByText(name.text), 2).getText().trim().replaceAll("\n", "");
    }

    public String getAuthorEmail(String author) {
        return authorElementTemplate.format(author).getAttribute("title").trim().replaceAll("\n", "");
    }

    public void expandRevisionID() {
        int revisionRow = findRowByText(Property.REVISION.text);
        WebElement revisionCell = propertiesTable.getCell(revisionRow, 1);
        WebElement expandableSpan = new WebElement(page,
            "xpath=.//span[@class='expandable']", "expandableSpan");
        expandableSpan.click();
    }

    public boolean isRevisionExpandable() {
        return expandableRevision.isVisible(100);
    }

    public boolean isPropertyPresent(String nameProperty) {
        try {
            findRowByText(nameProperty);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void checkPropertyPresent(String nameProperty, boolean isExpected) {
        assertThat(isPropertyPresent(nameProperty))
            .as("Property '" + nameProperty + "' presence:")
            .isEqualTo(isExpected);
    }

    public void checkProperty(Property name, String value) {
        if (!value.isEmpty()) {
            if (value.toLowerCase().equals("today")) {
                // Note: Extensions.getCurrentDate would need to be migrated or replaced
                // value = Extensions.getCurrentDate("MM/dd/yyyy");
            }
            assertThat(getProperty(name)).as("Property '" + name + "':").isEqualTo(value);
        }
    }

    public void checkMainProperties(HashMap<String, String> properties) {
        Arrays.asList("Name:", "Status:", "Modified By:").forEach(property -> {
            int row = findRowByText(property);
            String actualValue = propertiesTable.getCell(row, 1).getText();
            assertThat(actualValue).as("Property '" + property + "'").isEqualTo(properties.get(property));
        });
    }

    public String getTagError() {
        return tagError.getText();
    }

    public boolean isTagsSectionPresent() {
        return tagsSection.isVisible(100);
    }

    // Helper method to find row index by text content (returns 1-based index)
    private int findRowByText(String text) {
        WaitUtil.sleep(500);
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
        return getProperty(Property.REVISION);
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
}