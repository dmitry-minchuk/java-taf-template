package configuration.core.ui;

import helpers.utils.WaitUtil;
import helpers.utils.PlaywrightExpectUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import configuration.driver.PlaywrightDriverPool;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;

import java.util.List;

public class TableComponent extends BasePageComponent {

    private final By rowLocator = By.xpath(".//tr[not(@class='hidden')]");
    private final By cellLocator = By.xpath(".//td | .//th");

    public TableComponent() {}

    @Override
    public void init(WebDriver driver, By rootLocatorBy) {
        super.init(driver, rootLocatorBy);
    }

    public SmartWebElement getCell(int rowIndex, int columnIndex) {
        WebElement tableElement;
        if(getRootElement() != null)
            tableElement = getRootElement();
        else {
            // PLAYWRIGHT MIGRATION: Use appropriate wait strategy based on mode
            if (PlaywrightDriverPool.isInitialized()) {
                Page page = PlaywrightDriverPool.getPage();
                String selector = convertByToSelector(getRootLocatorBy());
                PlaywrightExpectUtil.expectVisible(page, selector, timeoutInSeconds * 1000);
            } else {
                WaitUtil.waitUntil(getDriver(), ExpectedConditions.visibilityOfElementLocated(getRootLocatorBy()), timeoutInSeconds);
            }
            tableElement = getDriver().findElement(getRootLocatorBy());
        }
        
        List<WebElement> rows;
        if (PlaywrightDriverPool.isInitialized()) {
            // PLAYWRIGHT MIGRATION: Use Playwright element waiting
            rows = getDriver().findElements(rowLocator);
        } else {
            rows = WaitUtil.waitForElementsList(tableElement, rowLocator, timeoutInSeconds);
        }
        if (rowIndex >= rows.size()) {
            throw new IndexOutOfBoundsException("Row index " + rowIndex + " is out of bounds. Table has " + rows.size() + " rows.");
        }
        WebElement row = rows.get(rowIndex);
        List<WebElement> cells;
        if (PlaywrightDriverPool.isInitialized()) {
            // PLAYWRIGHT MIGRATION: Use Playwright element finding
            cells = row.findElements(cellLocator);
        } else {
            cells = WaitUtil.waitForElementsList(row, cellLocator, timeoutInSeconds);
        }
        if (columnIndex >= cells.size()) {
            throw new IndexOutOfBoundsException("Column index " + columnIndex + " is out of bounds. Row has " + cells.size() + " columns.");
        }
        By cellBy = By.xpath(".//tr[not(@class='hidden')][" + (rowIndex + 1) + "]/*[self::td or self::th][" + (columnIndex + 1) + "]");
        return new SmartWebElement(getDriver(), cellBy, getRootLocatorBy());
    }

    public String getCellText(int rowIndex, int columnIndex) {
        return getCell(rowIndex, columnIndex).getText();
    }

    public void clickCell(int rowIndex, int columnIndex) {
        getCell(rowIndex, columnIndex).click();
    }

    public void doubleClickAndPasteTextToCell(int rowIndex, int columnIndex, String text) {
        doubleClickAndPasteTextToCell(rowIndex, columnIndex, text, true);
    }

    public void doubleClickAndPasteTextToCell(int rowIndex, int columnIndex, String text, boolean pressEnter) {
        SmartWebElement cell = getCell(rowIndex, columnIndex);
        WebElement element = cell.getUnwrappedElement();

        new Actions(getDriver())
                .moveToElement(element)
                .doubleClick()
                .perform();

        By inputLocator = By.xpath("//*[@id='_t_te_editorWrapper']");
        SmartWebElement input = new SmartWebElement(getDriver(), inputLocator);
        WebElement unwrappedInput = input.getUnwrappedElement();

        unwrappedInput.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        unwrappedInput.sendKeys(Keys.DELETE);
        unwrappedInput.sendKeys(text);
        if (pressEnter)
            unwrappedInput.sendKeys(Keys.ENTER);
    }

    public int getRowCount() {
        WebElement tableElement = getRootElement() != null ? getRootElement() : getDriver().findElement(getRootLocatorBy());
        if (PlaywrightDriverPool.isInitialized()) {
            // PLAYWRIGHT MIGRATION: Use Playwright element counting
            return tableElement.findElements(rowLocator).size();
        } else {
            return WaitUtil.waitForElementsList(tableElement, rowLocator, timeoutInSeconds).size();
        }
    }
    
    /**
     * PLAYWRIGHT MIGRATION: Convert Selenium By locator to CSS selector
     */
    private String convertByToSelector(By locator) {
        String locatorString = locator.toString();
        if (locatorString.startsWith("By.xpath:")) {
            // For now, return a generic selector - proper XPath conversion would be complex
            return "[data-testid], table, .ant-table";
        } else if (locatorString.startsWith("By.id:")) {
            return "#" + locatorString.substring("By.id: ".length());
        } else if (locatorString.startsWith("By.className:")) {
            return "." + locatorString.substring("By.className: ".length());
        } else if (locatorString.startsWith("By.cssSelector:")) {
            return locatorString.substring("By.cssSelector: ".length());
        }
        return "[data-testid], table, .ant-table"; // Fallback
    }
}

