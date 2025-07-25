package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.BasePageComponent;
import configuration.core.ui.SmartWebElement;
import configuration.core.ui.TableComponent;
import helpers.utils.WaitUtil;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RepositoryContentTabPropertiesComponent extends BasePageComponent {

    @FindBy(xpath = ".//table[@class='formfields']")
    private TableComponent propertiesTable;

    @FindBy(xpath = ".//span[text()='%s']")
    private SmartWebElement authorElement;

    @FindBy(xpath = ".//div[@class='rf-tab']//table[@class='formfields']//span[@class='expandable']")
    private SmartWebElement expandableRevision;

    @FindBy(xpath = ".//h3[contains(text(), 'Tags')]/..//td[contains(text(), '%s')]/..//span")
    private SmartWebElement tagSelectOpen;

    @FindBy(xpath = ".//span[@class='error']")
    private SmartWebElement tagError;

    @FindBy(xpath = ".//h3[contains(text(), 'Tags')]")
    private SmartWebElement tagsSection;

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

    public static String[] properties = {"Effective Date",
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
        WaitUtil.waitUntil(getDriver(), org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(propertiesTable.getRootLocatorBy()), timeoutInSeconds);
        // PLAYWRIGHT MIGRATION: Removed WaitUtil.sleep() - element visibility wait is sufficient
        return propertiesTable.getCell(findRowByText(name.text), 1).getText();
    }

    public String getAuthorEmail(String author) {
        return authorElement.format(author).getAttribute("title");
    }

    public void expandRevisionID() {
        int revisionRow = findRowByText(Property.REVISION.text);
        SmartWebElement revisionCell = propertiesTable.getCell(revisionRow, 1);
        SmartWebElement expandableSpan = new SmartWebElement(getDriver(), 
            By.xpath(".//span[@class='expandable']"), propertiesTable.getRootLocatorBy());
        expandableSpan.click();
    }

    public boolean isRevisionExpandable() {
        return expandableRevision.isDisplayed(1);
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

    public List<String> getTagsValues(String mainTagName) {
        SmartWebElement elementTagSelectOpen = tagSelectOpen.format(mainTagName);
        elementTagSelectOpen.click();
        
        List<WebElement> tags = getDriver().findElements(By.xpath(String.format(
            ".//h3[contains(text(), 'Tags')]/..//td[contains(text(), '%s')]/..//span" +
            "[contains(@class, 'tag-type')]//li", mainTagName)));
        
        List<String> tagsValues = new ArrayList<>();
        tags.forEach(tag -> tagsValues.add(tag.getText()));
        return tagsValues;
    }

    public String getProjectTag(String mainTagName) {
        SmartWebElement tagInput = new SmartWebElement(getDriver(), 
            By.xpath(String.format(".//h3[contains(text(), 'Tags')]/..//td[contains(text(), '%s')]/..//" +
                "span//input[contains(@id, 'tag-name')]", mainTagName)), getRootLocatorBy());
        return tagInput.getAttribute("value");
    }

    public void selectProjectTag(String mainTagName, String tag) {
        SmartWebElement elementTagSelectOpen = tagSelectOpen.format(mainTagName);
        elementTagSelectOpen.click();
        
        SmartWebElement tagOption = new SmartWebElement(getDriver(),
            By.xpath(String.format(".//h3[contains(text(), 'Tags')]/..//td[contains(text(), '%s')]/..//span" +
                "[contains(@class, 'tag-type')]//li[@value='%s']", mainTagName, tag)), getRootLocatorBy());
        tagOption.click();
    }

    public String getTagError() {
        return tagError.getText();
    }

    public boolean isTagsSectionPresent() {
        return tagsSection.isDisplayed(1);
    }

    // Helper method to find row index by text content
    private int findRowByText(String text) {
        int rowCount = propertiesTable.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            String cellText = propertiesTable.getCell(i, 0).getText();
            if (cellText.equals(text)) {
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