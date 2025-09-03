package configuration.core.ui;

import configuration.driver.PlaywrightDriverPool;
import helpers.utils.WaitUtil;

import java.util.List;

public class PlaywrightTableComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement openLSelectorTemplate;
    private PlaywrightWebElement standardSelectorTemplate;
    private PlaywrightWebElement inputLocator;
    private List<PlaywrightTableRowComponent> rows;

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

        rows = createScopedComponentList(PlaywrightTableRowComponent.class, "xpath=.//tbody/tr", "rowSelectorTemplate");
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
        editCell(rowIndex, columnIndex, text, true);
    }

    public int getRowsCount() {
        WaitUtil.waitForListNotEmpty(() -> rows, 3000, 250);
        return rows.size();
    }

    public PlaywrightTableRowComponent getRow(int rowIndex) {
        WaitUtil.waitForListNotEmpty(() -> rows, 3000, 250);
        return rows.get(rowIndex - 1);
    }

    // Inner class for table row operations
    public static class PlaywrightTableRowComponent extends PlaywrightBasePageComponent {
        List<PlaywrightWebElement> cells;

        public PlaywrightTableRowComponent() {
            super(PlaywrightDriverPool.getPage());
            initializeElements();
        }

        public PlaywrightTableRowComponent(PlaywrightWebElement rootLocator) {
            super(rootLocator);
            initializeElements();
        }

        private void initializeElements() {
            cells = createScopedElementList("xpath=./td", "cells");
        }

        public  List<PlaywrightWebElement> getCells() {
            WaitUtil.waitForListNotEmpty(() -> cells, 250, 50);
            return cells;
        }

        public List<String> getValue() {
            return cells.stream().map(e -> e.getText().trim()).toList();
        }
    }
}