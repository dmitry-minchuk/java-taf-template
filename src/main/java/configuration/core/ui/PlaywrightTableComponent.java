package configuration.core.ui;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.ArrayList;
import java.util.List;

public class PlaywrightTableComponent {
    private final Page page;
    private final String selector;

    public PlaywrightTableComponent(Page page, String selector) {
        this.page = page;
        this.selector = selector;
    }

    public String getCellText(int rowIndex, int columnIndex) {
        // Use OpenL-specific cell ID selector
        // OpenL tables use cell IDs in format: t_te_c-row:col
        String cellIdSelector = String.format("//td[@id='t_te_c-%d:%d']", rowIndex, columnIndex);
        return page.locator("xpath=" + cellIdSelector).textContent().trim();
    }

    public void clickCell(int rowIndex, int columnIndex) {
        // Use OpenL-specific cell ID selector
        String cellIdSelector = String.format("//td[@id='t_te_c-%d:%d']", rowIndex, columnIndex);
        page.locator("xpath=" + cellIdSelector).click();
    }

    public boolean isPresent() {
        return page.locator(selector).isVisible();
    }

    public PlaywrightWebElement getCell(int rowIndex, int columnIndex) {
        // Use OpenL-specific cell ID selector
        String cellIdSelector = String.format("//td[@id='t_te_c-%d:%d']", rowIndex, columnIndex);
        return new PlaywrightWebElement(page, "xpath=" + cellIdSelector);
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
        // Use OpenL-specific cell ID selector
        String cellIdSelector = String.format("//td[@id='t_te_c-%d:%d']", rowIndex, columnIndex);
        page.locator("xpath=" + cellIdSelector).dblclick();
    }

    public PlaywrightTableRow getRow(int rowIndex) {
        return new PlaywrightTableRow(this, rowIndex);
    }

    public void editCell(int rowIndex, int columnIndex, String text) {
        // Double click on the cell to open editor
        doubleClickCell(rowIndex, columnIndex);
        
        // Find and interact with the table editor
        Locator inputLocator = page.locator("xpath=//*[@id='_t_te_editorWrapper']");
        inputLocator.press("Control+A");
        inputLocator.press("Delete");
        inputLocator.fill(text);
        inputLocator.press("Enter");
    }

    // Inner class for table row operations
    public static class PlaywrightTableRow {
        private final PlaywrightTableComponent table;
        private final int rowIndex;

        public PlaywrightTableRow(PlaywrightTableComponent table, int rowIndex) {
            this.table = table;
            this.rowIndex = rowIndex;
        }

        public List<String> getValue() {
            List<String> values = new ArrayList<>();
            
            // Use OpenL-specific row selector to find all cells in this row
            // OpenL tables use cell IDs in format: t_te_c-row:col
            String rowXPath = String.format("//td[starts-with(@id, 't_te_c-%d:')]", rowIndex);
            Locator cellsLocator = table.page.locator("xpath=" + rowXPath);
            int cellCount = cellsLocator.count();
            
            for (int i = 0; i < cellCount; i++) {
                String cellText = cellsLocator.nth(i).textContent();
                values.add(cellText != null ? cellText.trim() : "");
            }
            
            return values;
        }
    }
}