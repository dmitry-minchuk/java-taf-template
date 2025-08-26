package configuration.core.ui;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import helpers.utils.WaitUtil;

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
    
    public String getCellContent(int rowIndex, int columnIndex) {
        // Alias for getCellText to match original API
        return getCellText(rowIndex, columnIndex);
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
        // Double-click on the cell to open editor
        doubleClickCell(rowIndex, columnIndex);
        
        // Find and interact with the table editor
        Locator inputLocator = page.locator("xpath=//*[@id='_t_te_editorWrapper']");
        inputLocator.press("Control+A");
        inputLocator.press("Delete");
        inputLocator.fill(text);
        inputLocator.press("Enter");
        WaitUtil.sleep(250);
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
            
            // Try OpenL-specific selector first (for OpenL tables with t_te_c-row:col IDs)
            String rowXPath = String.format("//td[starts-with(@id, 't_te_c-%d:')]", rowIndex);
            Locator cellsLocator = table.page.locator("xpath=" + rowXPath);
            int cellCount = cellsLocator.count();
            
            // Fallback to regular HTML table selector if no OpenL cells found
            if (cellCount == 0) {
                // For regular HTML tables like TestResultPage (table[@class='table'])
                String tableSelector = table.selector.startsWith("xpath=") ? 
                    table.selector.substring(6) : table.selector;
                String regularRowXPath = String.format("%s//tr[%d]/td", tableSelector, rowIndex);
                cellsLocator = table.page.locator("xpath=" + regularRowXPath);
                cellCount = cellsLocator.count();
            }
            
            for (int i = 0; i < cellCount; i++) {
                String cellText = cellsLocator.nth(i).textContent();
                values.add(cellText != null ? cellText.trim() : "");
            }
            
            return values;
        }
    }
}