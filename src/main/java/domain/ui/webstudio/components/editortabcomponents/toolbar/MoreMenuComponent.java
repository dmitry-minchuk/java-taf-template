package domain.ui.webstudio.components.editortabcomponents.toolbar;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.BaseComponent;
import domain.ui.webstudio.components.editortabcomponents.ChangesDialogComponent;
import domain.ui.webstudio.components.editortabcomponents.CompareExcelFilesDialogComponent;
import helpers.utils.WaitUtil;

import java.util.ArrayList;
import java.util.List;

public class MoreMenuComponent extends BaseComponent implements IMoreMenu {

    private static final int MENU_ITEM_VISIBLE_TIMEOUT_MS = 500;
    private static final int MENU_ITEM_CLICK_TIMEOUT_MS = 2000;
    private static final long MENU_RETRY_TIMEOUT_MS = DEFAULT_TIMEOUT_MS * 2L;

    private final WebElement toggle;
    private final WebElement changesBtn;
    private final WebElement revisionsBtn;
    private final WebElement compareExcelFilesBtn;
    private final WebElement tableDependenciesBtn;
    private final WebElement allMenuLinks;

    public MoreMenuComponent(Page page) {
        this(new WebElement(page, "xpath=//span[@id='topMorePanel']", "topMorePanel"));
    }

    public MoreMenuComponent(WebElement rootLocator) {
        super(rootLocator);
        toggle = createScopedElement("xpath=.//a[contains(@class,'dropdown-toggle')]", "moreBtn");
        changesBtn = createScopedElement("xpath=.//a[@id='topRevertLink']", "changesBtn");
        revisionsBtn = createScopedElement("xpath=.//a[@title='Show project revisions']", "revisionsBtn");
        compareExcelFilesBtn = createScopedElement("xpath=.//a[contains(text(),'Compare Excel files')]", "compareExcelFilesBtn");
        tableDependenciesBtn = createScopedElement("xpath=.//a[contains(text(),'Table Dependencies')]", "tableDependenciesBtn");
        allMenuLinks = createScopedElement("xpath=.//ul[contains(@class,'dropdown-menu')]//li//a", "allMoreMenuLinks");
    }

    public MoreMenuComponent open() {
        WaitUtil.sleep(1000, "Waiting before clicking More dropdown");
        toggle.click();
        WaitUtil.sleep(500, "Waiting for More dropdown to open");
        return this;
    }

    @Override
    public ChangesDialogComponent clickChanges() {
        waitUntilSpinnerLoaded();
        clickMenuItem(changesBtn, "Local Changes");
        return new ChangesDialogComponent().waitForLoaded();
    }

    @Override
    public void clickRevisions() {
        clickMenuItem(revisionsBtn, "Revisions");
        WaitUtil.sleep(500, "Waiting for Revisions dialog to open");
    }

    @Override
    public void clickTableDependencies() {
        clickMenuItem(tableDependenciesBtn, "Table Dependencies");
        WaitUtil.sleep(1000, "Waiting for Table Dependencies view to load");
    }

    private void clickMenuItem(WebElement item, String itemName) {
        boolean clicked = WaitUtil.retryAction(() -> {
            if (!item.isVisible(MENU_ITEM_VISIBLE_TIMEOUT_MS)) {
                toggle.click();
            }
            item.click(MENU_ITEM_CLICK_TIMEOUT_MS);
        }, MENU_RETRY_TIMEOUT_MS, 500, "Clicking '" + itemName + "' in the More menu, re-opening the menu if a toolbar refresh closed it");
        if (!clicked) {
            throw new IllegalStateException("'" + itemName + "' in the More menu could not be clicked within " + MENU_RETRY_TIMEOUT_MS + " ms");
        }
    }

    @Override
    public CompareExcelFilesDialogComponent clickCompareExcelFiles() {
        Page popup = page.waitForPopup(compareExcelFilesBtn::click);
        popup.waitForLoadState();
        return new CompareExcelFilesDialogComponent(popup);
    }

    public List<String> getMenuItems() {
        List<String> items = new ArrayList<>();
        Locator menuItems = allMenuLinks.getLocator();
        for (int i = 0; i < menuItems.count(); i++) {
            Locator item = menuItems.nth(i);
            if (item.isVisible()) {
                String text = item.textContent().trim();
                if (!text.isEmpty()) {
                    items.add(text);
                }
            }
        }
        return items;
    }
}
