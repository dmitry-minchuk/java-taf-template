package domain.ui.webstudio.components.common;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import helpers.utils.WaitUtil;

import java.util.List;

public class TableComponent extends BaseComponent {

    private WebElement inputLocator;
    private List<PlaywrightTableRowComponent> rows;

    public TableComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public TableComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        inputLocator = new WebElement(page, "xpath=//*[@id='_t_te_editorWrapper']", "inputLocator");
        rows = createScopedComponentList(PlaywrightTableRowComponent.class, "xpath=.//tbody/tr", "rowSelectorTemplate");
    }

    public void clickCell(int rowIndex, int columnIndex) {
        getCell(rowIndex, columnIndex).click();
    }

    public WebElement getCell(int rowIndex, int columnIndex) {
        WaitUtil.waitForListNotEmpty(() -> rows, 3000, 100);
        PlaywrightTableRowComponent row = rows.get(rowIndex - 1);
        List<WebElement> cells = row.getCells();
        return cells.get(columnIndex - 1);
    }

    public String getCellText(int rowIndex, int columnIndex) {
        return getCell(rowIndex, columnIndex).getText().trim();
    }

    public void doubleClickCell(int rowIndex, int columnIndex) {
        WebElement cell = getCell(rowIndex, columnIndex);
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
        WaitUtil.sleep(100); // Wait for table to get filled up with values
        WaitUtil.waitForListNotEmpty(() -> rows, 3000, 250);
        return rows.size();
    }

    public PlaywrightTableRowComponent getRow(int rowIndex) {
        WaitUtil.waitForListNotEmpty(() -> rows, 3000, 250);
        return rows.get(rowIndex - 1);
    }

    // Inner class for table row operations
    public static class PlaywrightTableRowComponent extends BaseComponent {
        List<WebElement> cells;

        public PlaywrightTableRowComponent() {
            super(LocalDriverPool.getPage());
            initializeElements();
        }

        public PlaywrightTableRowComponent(WebElement rootLocator) {
            super(rootLocator);
            initializeElements();
        }

        private void initializeElements() {
            cells = createScopedElementList("xpath=./td", "cells");
        }

        public  List<WebElement> getCells() {
            WaitUtil.waitForListNotEmpty(() -> cells, 250, 50);
            return cells;
        }

        public List<String> getValue() {
            return cells.stream().map(e -> e.getText().trim()).toList();
        }
    }
}