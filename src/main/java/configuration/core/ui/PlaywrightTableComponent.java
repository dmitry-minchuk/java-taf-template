package configuration.core.ui;

import com.microsoft.playwright.Locator;
import configuration.driver.PlaywrightDriverPool;
import helpers.utils.WaitUtil;

import java.util.ArrayList;
import java.util.List;

public class PlaywrightTableComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement openLSelectorTemplate;
    private PlaywrightWebElement standardSelectorTemplate;
    private PlaywrightWebElement inputLocator;

    public PlaywrightTableComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightTableComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        openLSelectorTemplate = createScopedElement("xpath=.//td[@id='t_te_c-%d:%d']", "openLSelector");
        standardSelectorTemplate = createScopedElement("xpath=.//tr[%d]/td[%d]", "standardSelector");
        inputLocator = new PlaywrightWebElement(page, "xpath=//*[@id='_t_te_editorWrapper']", "inputLocator");
    }

    public void clickCell(int rowIndex, int columnIndex) {
        getCell(rowIndex, columnIndex).click();
    }

    public PlaywrightWebElement getCell(int rowIndex, int columnIndex) {
        PlaywrightWebElement openLSelector = openLSelectorTemplate.format(rowIndex, columnIndex);
        if (openLSelector.isVisible())
            return openLSelector;
        else
            return standardSelectorTemplate.format(rowIndex, columnIndex);
    }

    public String getCellText(int rowIndex, int columnIndex) {
        return getCell(rowIndex, columnIndex).getText().trim();
    }

    public void doubleClickCell(int rowIndex, int columnIndex) {
        PlaywrightWebElement cell = getCell(rowIndex, columnIndex);
        cell.doubleClick();
    }

    public void editCell(int rowIndex, int columnIndex, String text, boolean pressEnter) {
        doubleClickCell(rowIndex, columnIndex);

        inputLocator.press("Control+A");
        inputLocator.press("Delete");
        inputLocator.fill(text);
        if (pressEnter) {
            inputLocator.press("Enter");
        }
        WaitUtil.sleep(250);
    }

    public void editCell(int rowIndex, int columnIndex, String text) {
        doubleClickCell(rowIndex, columnIndex);
        editCell(rowIndex, columnIndex, text, true);
    }

    public int getRowsCount() {
        return rootLocator.getLocator().locator("xpath=.//tr[not(@class='hidden')]").count();
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