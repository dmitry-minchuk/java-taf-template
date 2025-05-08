package configuration.core.ui;

import helpers.utils.WaitUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

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
        WebElement tableElement = getRootElement() != null ? getRootElement() : getDriver().findElement(getRootLocatorBy());
        List<WebElement> rows = WaitUtil.waitForElementsList(tableElement, rowLocator, timeoutInSeconds);
        if (rowIndex >= rows.size()) {
            throw new IndexOutOfBoundsException("Row index " + rowIndex + " is out of bounds. Table has " + rows.size() + " rows.");
        }
        WebElement row = rows.get(rowIndex);
        List<WebElement> cells = WaitUtil.waitForElementsList(row, cellLocator, timeoutInSeconds);
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

    public int getRowCount() {
        WebElement tableElement = getRootElement() != null ? getRootElement() : getDriver().findElement(getRootLocatorBy());
        return WaitUtil.waitForElementsList(tableElement, rowLocator, timeoutInSeconds).size();
    }
}

