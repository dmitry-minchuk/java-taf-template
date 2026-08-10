package domain.ui.webstudio.components.editortabcomponents.toolbar;

import com.microsoft.playwright.Page;
import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.BaseComponent;
import domain.ui.webstudio.components.editortabcomponents.ChangesDialogComponent;
import domain.ui.webstudio.components.editortabcomponents.CompareExcelFilesDialogComponent;
import helpers.utils.WaitUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * The More dropdown of the editor's top toolbar (Revisions / Local Changes / Table Dependencies /
 * Compare Excel files). Scoped under {@code span#topMorePanel} — its menu renders inside the span
 * (verified against the live 6.4.0 DOM), so the items no longer collide with other open dropdowns.
 */
public class MoreMenuComponent extends BaseComponent implements IMoreMenu {

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
        waitUntilSpinnerLoaded(); // loadingPanel can still intercept the click right after a save under CI load
        changesBtn.click();
        WaitUtil.sleep(1500, "Waiting for Changes dialog to open");
        return new ChangesDialogComponent();
    }

    @Override
    public void clickRevisions() {
        revisionsBtn.click();
        WaitUtil.sleep(500, "Waiting for Revisions dialog to open");
    }

    @Override
    public void clickTableDependencies() {
        tableDependenciesBtn.click();
        WaitUtil.sleep(1000, "Waiting for Table Dependencies view to load");
    }

    @Override
    public CompareExcelFilesDialogComponent clickCompareExcelFiles() {
        Page popup = page.waitForPopup(compareExcelFilesBtn::click);
        popup.waitForLoadState();
        return new CompareExcelFilesDialogComponent(popup);
    }

    /** Visible item texts of the opened More menu. */
    public List<String> getMenuItems() {
        List<String> items = new ArrayList<>();
        com.microsoft.playwright.Locator menuItems = allMenuLinks.getLocator();
        for (int i = 0; i < menuItems.count(); i++) {
            com.microsoft.playwright.Locator item = menuItems.nth(i);
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
