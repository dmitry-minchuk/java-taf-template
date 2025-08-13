package configuration.core.ui;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class PlaywrightTableComponent {
    private final Page page;
    private final String selector;

    public PlaywrightTableComponent(Page page, String selector) {
        this.page = page;
        this.selector = selector;
    }

    public String getCellText(int rowIndex, int columnIndex) {
        // XPath to find cell: table//tr[not(@class='hidden')][rowIndex+1]//td[columnIndex+1] or //th[columnIndex+1]
        String cellSelector = selector + String.format("//tr[not(@class='hidden')][%d]//*[self::td or self::th][%d]", 
                                                       rowIndex + 1, columnIndex + 1);
        Locator cellLocator = page.locator("xpath=" + cellSelector);
        return cellLocator.textContent().trim();
    }

    public void clickCell(int rowIndex, int columnIndex) {
        String cellSelector = selector + String.format("//tr[not(@class='hidden')][%d]//*[self::td or self::th][%d]", 
                                                       rowIndex + 1, columnIndex + 1);
        Locator cellLocator = page.locator("xpath=" + cellSelector);
        cellLocator.click();
    }

    public boolean isPresent() {
        return page.locator("xpath=" + selector).isVisible();
    }

    public PlaywrightWebElement getCell(int rowIndex, int columnIndex) {
        String cellSelector = selector + String.format("//tr[not(@class='hidden')][%d]//*[self::td or self::th][%d]",
                rowIndex + 1, columnIndex + 1);
        return new PlaywrightWebElement(page, "xpath=" + cellSelector);
    }

    public void doubleClickAndPasteTextToCell(int rowIndex, int columnIndex, String text, boolean pressEnter) {
        PlaywrightWebElement cell = getCell(rowIndex, columnIndex);
        cell.getLocator().dblclick();

        Locator inputLocator = page.locator("xpath=//*[@id='_t_te_editorWrapper']");
        inputLocator.press("Control+A");
        inputLocator.press("Delete");
        inputLocator.fill(text);
        if (pressEnter) {
            inputLocator.press("Enter");
        }
    }

    public int getRowCount() {
        String rowSelector = selector + "//tr[not(@class='hidden')]";
        return page.locator("xpath=" + rowSelector).count();
    }
}