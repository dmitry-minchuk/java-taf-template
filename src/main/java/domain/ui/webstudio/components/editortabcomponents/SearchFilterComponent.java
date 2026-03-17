package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SearchFilterComponent extends BaseComponent {

    private WebElement searchName;
    private WebElement openSearchDropdown;
    private WebElement selectScope;
    private WebElement selectType;
    private WebElement outsideSelectTableType;
    private WebElement searchBtn;
    private WebElement closeSearchBtn;
    private WebElement headerContains;
    private WebElement tableProperties;
    private WebElement addPropertyBtn;
    private WebElement searchResultCounter;
    private WebElement selectTableTypeCheckboxTemplate;
    private WebElement propertyValueTextTemplate;
    private WebElement viewTableTemplate;
    private List<WebElement> tableNameCells;
    private List<WebElement> scopeOptions;

    public SearchFilterComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public SearchFilterComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        searchName = new WebElement(page, "xpath=//input[@id='searchQuery']", "searchName");
        openSearchDropdown = new WebElement(page, "xpath=//span[@id='searchInput']//a", "openSearchDropdown");
        selectScope = new WebElement(page, "xpath=//select[@id='searchScopeSelection']", "selectScope");
        selectType = new WebElement(page, "xpath=//input[@id='multiselect-select']", "selectType");
        outsideSelectTableType = new WebElement(page, "xpath=//div[@id='advancedSearch']//label[text()='Table Type']", "outsideSelectTableType");
        searchBtn = new WebElement(page, "xpath=//div[@id='advancedSearch']//input[@value='Search']", "searchBtn");
        closeSearchBtn = new WebElement(page, "xpath=//span[contains(@class,'jquery-popup-close-icon')]", "closeSearchBtn");
        headerContains = new WebElement(page, "xpath=//input[@id='tableHeader']", "headerContains");
        tableProperties = new WebElement(page, "xpath=//select[@id='propertyItems']", "tableProperties");
        addPropertyBtn = new WebElement(page, "xpath=//div[@id='advancedSearch']//input[@value='Add']", "addPropertyBtn");
        searchResultCounter = new WebElement(page, "xpath=//h1[@class='page-header']", "searchResultCounter");
        selectTableTypeCheckboxTemplate = new WebElement(page, "xpath=//div[@class='jquery-multiselect-popup jquery-popup']//div[@class='jquery-multiselect-popup-data']//input[contains(@value, '%s')]", "selectTableTypeCheckbox");
        propertyValueTextTemplate = new WebElement(page, "xpath=//div[@id='advancedSearch']//td[text()='%s']/..//input[@type='text']", "propertyValueText");
        viewTableTemplate = new WebElement(page, "xpath=//div[@id='searchResults']//table//tr//td[contains(text(), '%s')]/../../../../../..//a[text()='View Table']", "viewTable");
        tableNameCells = createElementList("xpath=//table[@class='te_table']//tr[1]/td[1]", "tableNameCells");
        scopeOptions = createElementList("xpath=//select[@id='searchScopeSelection']/option", "scopeOptions");
    }

    public SearchFilterComponent typeSearchAndEnter(String text) {
        WaitUtil.sleep(1000, "Waiting before type search");
        searchName.fillSequentially(text);
        WaitUtil.sleep(1000, "Waiting after type search");
        searchName.press("Enter");
        return this;
    }

    public SearchFilterComponent setSearchName(String text) {
        searchName.fill(text);
        return this;
    }

    public SearchFilterComponent openAdvancedSearch() {
        openSearchDropdown.click();
        return this;
    }

    public SearchFilterComponent setScope(String scopeValue) {
        selectScope.selectByVisibleText(scopeValue);
        return this;
    }

    public SearchFilterComponent setHeaderContains(String value) {
        headerContains.fill(value);
        return this;
    }

    public SearchFilterComponent searchByTableType(String... types) {
        selectType.click();
        for (String type : types) {
            WebElement checkbox = selectTableTypeCheckboxTemplate.format(type);
            if (!checkbox.isChecked()) {
                checkbox.click();
            }
        }
        outsideSelectTableType.click();
        return this;
    }

    public SearchFilterComponent searchByProperty(String propertyName, String propertyValue) {
        tableProperties.selectByVisibleText(propertyName);
        addPropertyBtn.click();
        propertyValueTextTemplate.format(propertyName).fill(propertyValue);
        return this;
    }

    public SearchFilterComponent performSearch() {
        searchBtn.click();
        closeSearchBtn.click();
        return this;
    }

    public SearchFilterComponent waitForSearchResult() {
        WaitUtil.waitForCondition(
                () -> searchResultCounter.getText().contains("found") || searchResultCounter.getText().contains("No results"),
                10000, 250, "Waiting for search results to appear"
        );
        return this;
    }

    public String getResultCounterText() {
        return searchResultCounter.getText().trim();
    }

    public boolean isTableFound(String tableName) {
        return getTableNamesInSearchResults().contains(tableName);
    }

    public SearchFilterComponent clickViewTable(String tableName) {
        viewTableTemplate.format(tableName).getLocator().first().click();
        return this;
    }

    public List<String> getTableNamesInSearchResults() {
        return tableNameCells.stream().map(e -> {
            String tableHeaderFull = e.getText().replace("\n", " ").trim();
            // Table name is the last word before the opening parenthesis
            int parenIndex = tableHeaderFull.indexOf("(");
            if (parenIndex > 0) {
                String beforeParen = tableHeaderFull.substring(0, parenIndex).trim();
                String[] parts = beforeParen.split("\\s+");
                return parts[parts.length - 1].replaceAll("[^a-zA-Z0-9]", "");
            }
            // No parenthesis — standalone name (e.g. Datatype tables)
            String[] parts = tableHeaderFull.split("\\s+");
            return parts[parts.length - 1].replaceAll("[^a-zA-Z0-9]", "");
        }).collect(Collectors.toList());
    }

    public List<String> getScopeOptions() {
        return scopeOptions.stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }
}
