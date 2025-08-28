package configuration.core.ui;

import com.microsoft.playwright.Locator;
import configuration.driver.PlaywrightDriverPool;
import helpers.utils.WaitUtil;

import java.util.ArrayList;
import java.util.List;

public class PlaywrightTableComponent extends PlaywrightBasePageComponent {

    public PlaywrightTableComponent() {
        super(PlaywrightDriverPool.getPage());
    }
    
    public PlaywrightTableComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
    }

    public String getCellText(int rowIndex, int columnIndex) {
        // Try OpenL-specific selector first (1-based indexing)
        String openLSelector = String.format("xpath=//td[@id='t_te_c-%d:%d']", rowIndex, columnIndex);
        if (page.locator(openLSelector).count() > 0) {
            return page.locator(openLSelector).textContent().trim();
        }
        
        // Fallback to standard HTML table logic (1-based CSS selectors)
        String standardSelector = String.format("xpath=.//tr[%d]/td[%d]", rowIndex, columnIndex);
        return rootLocator.getLocator().locator(standardSelector).textContent().trim();
    }
    
    public String getCellContent(int rowIndex, int columnIndex) {
        // Alias for getCellText to match original API
        return getCellText(rowIndex, columnIndex);
    }

    public void clickCell(int rowIndex, int columnIndex) {
        getCell(rowIndex, columnIndex).click();
    }

    public boolean isPresent() {
        return rootLocator.isVisible();
    }

    public PlaywrightWebElement getCell(int rowIndex, int columnIndex) {
        // Try OpenL-specific selector first (1-based indexing)
        String openLSelector = String.format("xpath=//td[@id='t_te_c-%d:%d']", rowIndex, columnIndex);
        if (page.locator(openLSelector).count() > 0) {
            return new PlaywrightWebElement(page, openLSelector);
        }
        
        // Fallback to standard HTML table logic (1-based CSS selectors)
        String standardSelector = String.format("xpath=.//tr[%d]/td[%d]", rowIndex, columnIndex);
        return new PlaywrightWebElement(rootLocator, standardSelector);
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
        return rootLocator.getLocator().locator("xpath=.//tr[not(@class='hidden')]").count();
    }

    public int getRowsCount() {
        return getRowCount();
    }

    public void doubleClickCell(int rowIndex, int columnIndex) {
        getCell(rowIndex, columnIndex).getLocator().dblclick();
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
            
            // Try OpenL-specific selector first (1-based indexing)
            String openLRowSelector = String.format("//td[starts-with(@id, 't_te_c-%d:')]", rowIndex);
            Locator cellsLocator = table.page.locator("xpath=" + openLRowSelector);
            
            if (cellsLocator.count() > 0) {
                // OpenL table logic
                int cellCount = cellsLocator.count();
                for (int i = 0; i < cellCount; i++) {
                    String cellText = cellsLocator.nth(i).textContent();
                    values.add(cellText != null ? cellText.trim() : "");
                }
            } else {
                // Standard HTML table logic (1-based CSS selectors)
                String standardRowSelector = String.format("xpath=.//tr[%d]/td", rowIndex);
                cellsLocator = table.rootLocator.getLocator().locator(standardRowSelector);
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