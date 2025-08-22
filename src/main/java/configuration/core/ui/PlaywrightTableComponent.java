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
        String cellSelector;
        if (selector.startsWith("xpath=")) {
            cellSelector = selector.substring(6) + String.format("//tr[not(@class='hidden')][%d]//*[self::td or self::th][%d]", 
                                                               rowIndex + 1, columnIndex + 1);
            return page.locator("xpath=" + cellSelector).textContent().trim();
        } else {
            return page.locator(selector)
                      .locator(String.format("tr:not([class*='hidden']):nth-child(%d)", rowIndex + 1))
                      .locator(String.format("td:nth-child(%d), th:nth-child(%d)", columnIndex + 1, columnIndex + 1))
                      .textContent().trim();
        }
    }

    public void clickCell(int rowIndex, int columnIndex) {
        if (selector.startsWith("xpath=")) {
            String cellSelector = selector.substring(6) + String.format("//tr[not(@class='hidden')][%d]//*[self::td or self::th][%d]", 
                                                           rowIndex + 1, columnIndex + 1);
            page.locator("xpath=" + cellSelector).click();
        } else {
            page.locator(selector)
                .locator(String.format("tr:not([class*='hidden']):nth-child(%d)", rowIndex + 1))
                .locator(String.format("td:nth-child(%d), th:nth-child(%d)", columnIndex + 1, columnIndex + 1))
                .click();
        }
    }

    public boolean isPresent() {
        return page.locator(selector).isVisible();
    }

    public PlaywrightWebElement getCell(int rowIndex, int columnIndex) {
        if (selector.startsWith("xpath=")) {
            String cellSelector = selector.substring(6) + String.format("//tr[not(@class='hidden')][%d]//*[self::td or self::th][%d]",
                    rowIndex + 1, columnIndex + 1);
            return new PlaywrightWebElement(page, "xpath=" + cellSelector);
        } else {
            String cellSelector = selector + String.format(" tr:not([class*='hidden']):nth-child(%d) td:nth-child(%d), ", 
                    rowIndex + 1, columnIndex + 1) +
                    selector + String.format(" tr:not([class*='hidden']):nth-child(%d) th:nth-child(%d)",
                    rowIndex + 1, columnIndex + 1);
            return new PlaywrightWebElement(page, cellSelector);
        }
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
        // Use proper XPath locator for counting rows
        if (selector.startsWith("xpath=")) {
            String rowSelector = selector.substring(6) + "//tr[not(@class='hidden')]";
            return page.locator("xpath=" + rowSelector).count();
        } else {
            return page.locator(selector).locator("tr:not([class*='hidden'])").count();
        }
    }

    public int getRowsCount() {
        return getRowCount();
    }

    public void doubleClickCell(int rowIndex, int columnIndex) {
        if (selector.startsWith("xpath=")) {
            String cellSelector = selector.substring(6) + String.format("//tr[not(@class='hidden')][%d]//*[self::td or self::th][%d]", 
                                                           rowIndex + 1, columnIndex + 1);
            page.locator("xpath=" + cellSelector).dblclick();
        } else {
            page.locator(selector)
                .locator(String.format("tr:not([class*='hidden']):nth-child(%d)", rowIndex + 1))
                .locator(String.format("td:nth-child(%d), th:nth-child(%d)", columnIndex + 1, columnIndex + 1))
                .dblclick();
        }
    }

    public PlaywrightTableRow getRow(int rowIndex) {
        return new PlaywrightTableRow(this, rowIndex);
    }

    // Inner class for table row operations
    public static class PlaywrightTableRow {
        private final PlaywrightTableComponent table;
        private final int rowIndex;

        public PlaywrightTableRow(PlaywrightTableComponent table, int rowIndex) {
            this.table = table;
            this.rowIndex = rowIndex;
        }

        public java.util.List<String> getValue() {
            java.util.List<String> values = new java.util.ArrayList<>();
            
            if (table.selector.startsWith("xpath=")) {
                String rowSelector = table.selector.substring(6) + String.format("//tr[not(@class='hidden')][%d]", rowIndex);
                Locator rowLocator = table.page.locator("xpath=" + rowSelector);
                
                // Get all cells in this row
                Locator cellsLocator = rowLocator.locator("xpath=.//*[self::td or self::th]");
                int cellCount = cellsLocator.count();
                
                for (int i = 0; i < cellCount; i++) {
                    String cellText = cellsLocator.nth(i).textContent();
                    values.add(cellText != null ? cellText.trim() : "");
                }
            } else {
                Locator rowLocator = table.page.locator(table.selector)
                    .locator(String.format("tr:not([class*='hidden']):nth-child(%d)", rowIndex));
                
                // Get all cells in this row
                Locator cellsLocator = rowLocator.locator("td, th");
                int cellCount = cellsLocator.count();
                
                for (int i = 0; i < cellCount; i++) {
                    String cellText = cellsLocator.nth(i).textContent();
                    values.add(cellText != null ? cellText.trim() : "");
                }
            }
            
            return values;
        }
    }
}