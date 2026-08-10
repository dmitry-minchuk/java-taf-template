package domain.ui.webstudio.components.repositorytabcomponents;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * The React projects table of the projects list (Studio 6.4.0): rows with inline actions and a row
 * overflow menu. Row elements are scoped under {@code [data-testid=projects-table]} (verified against
 * the live DOM); the overflow menu is an antd dropdown rendered into a body-level portal, so its items
 * are located at page level on purpose.
 */
public class ProjectsTableComponent extends BaseComponent {

    // aria-label of the row's overflow trigger — a menu opener, not an action of its own.
    private static final String OVERFLOW_TRIGGER_LABEL = "Actions";
    // Inline row buttons render with the row itself, so a short probe decides inline-vs-overflow.
    private static final int PROBE_MS = DEFAULT_TIMEOUT_MS / 5;
    // Short enough that a row lost to a re-render is retried instead of waited out.
    private static final int ACTION_CLICK_TIMEOUT_MS = DEFAULT_TIMEOUT_MS / 2;

    private final WebElement rowByName;
    private final WebElement nameInRow;
    private final WebElement branchInRow;
    private final WebElement actionByName;
    private final WebElement rowMoreBtn;
    private final WebElement actionByNameAndState;
    private final WebElement rowMoreBtnByState;
    private final WebElement rowByNameAndState;
    private final WebElement rowActionButtons;
    // antd dropdown portal — rendered at body level, outside the table subtree
    private final WebElement overflowMenuItem;
    private final WebElement overflowMenuItems;

    public ProjectsTableComponent(Page page) {
        this(new WebElement(page, "[data-testid=projects-table]", "projectsTable"));
    }

    public ProjectsTableComponent(WebElement rootLocator) {
        super(rootLocator);
        rowByName = createScopedElement("xpath=.//tr[starts-with(@data-testid,'project-row')][.//span[normalize-space()='%s']]", "projectRow");
        // Click the name, not the row: a row also holds a branch switcher, and a row-wide click can hit it.
        nameInRow = createScopedElement("xpath=.//tr[starts-with(@data-testid,'project-row')]//span[normalize-space()='%s']", "projectNameInRow");
        branchInRow = createScopedElement("xpath=.//tr[starts-with(@data-testid,'project-row')][.//span[normalize-space()='%s']]//span[starts-with(@data-testid,'row-branch-')][not(contains(@data-testid,'-default'))][not(contains(@data-testid,'-trigger'))]", "projectBranchInRow");
        actionByName = createScopedElement("xpath=.//tr[starts-with(@data-testid,'project-row')][.//span[normalize-space()='%s']]//button[@aria-label='%s']", "projectRowAction");
        rowMoreBtn = createScopedElement("xpath=.//tr[starts-with(@data-testid,'project-row')][.//span[normalize-space()='%s']]//button[starts-with(@data-testid,'project-actions-')]", "projectRowMoreBtn");
        actionByNameAndState = createScopedElement("xpath=.//tr[starts-with(@data-testid,'project-row')][.//span[normalize-space()='%s']][.//button[@aria-label='%s']]//button[@aria-label='%s']", "projectRowActionByState");
        rowMoreBtnByState = createScopedElement("xpath=.//tr[starts-with(@data-testid,'project-row')][.//span[normalize-space()='%s']][.//button[@aria-label='%s']]//button[starts-with(@data-testid,'project-actions-')]", "projectRowMoreBtnByState");
        rowByNameAndState = createScopedElement("xpath=.//tr[starts-with(@data-testid,'project-row')][.//span[normalize-space()='%s']][.//button[@aria-label='%s']]", "projectRowByState");
        rowActionButtons = createScopedElement("xpath=.//tr[starts-with(@data-testid,'project-row')][.//span[normalize-space()='%s']]//button[@aria-label]", "projectRowActionButtons");
        overflowMenuItem = new WebElement(page, "xpath=//div[contains(@class,'ant-dropdown')][not(contains(@class,'ant-dropdown-hidden'))]//li[contains(@class,'ant-dropdown-menu-item')][normalize-space()='%s']", "overflowMenuItem");
        overflowMenuItems = new WebElement(page, "xpath=//div[contains(@class,'ant-dropdown')][not(contains(@class,'ant-dropdown-hidden'))]//li[contains(@class,'ant-dropdown-menu-item')]", "overflowMenuItems");
    }

    public boolean isProjectPresent(String projectName) {
        return rowByName.format(projectName).isVisible(DEFAULT_TIMEOUT_MS);
    }

    /** The project row element by name — for callers that need custom row interactions. */
    public WebElement getRow(String projectName) {
        return rowByName.format(projectName);
    }

    /** Clicks the project name to open the React project-detail view. */
    public void clickProjectName(String projectName) {
        nameInRow.format(projectName).click();
    }

    /** Clicks a same-name row picked out by its state (an opened row offers "Close", a closed one "Open"). */
    public void clickRowByState(String projectName, boolean opened) {
        String action = opened ? "Close" : "Open";
        rowByNameAndState.format(projectName, action).getLocator().first().click();
    }

    /**
     * Clicks a row action by its label, wherever the row keeps it. Studio 6.4.0 shows only
     * Copy / Delete Branch / Open / Close as buttons and folds Save, Open Revision, Sync, Deploy,
     * Compare, Export and Delete into the row's overflow menu.
     */
    public void clickRowAction(String projectName, String actionLabel) {
        // The row is re-rendered when the projects list refreshes, which can drop the button between the moment it
        // is found and the moment it is pressed - and the overflow menu closes with it. So the whole
        // open-the-menu-and-pick sequence is retried rather than waited for once.
        WaitUtil.retryOnException(() -> {
            WebElement inlineAction = actionByName.format(projectName, actionLabel);
            if (inlineAction.isVisible(PROBE_MS)) {
                inlineAction.click(ACTION_CLICK_TIMEOUT_MS);
            } else {
                rowMoreBtn.format(projectName).click();
                overflowMenuItem.format(actionLabel).click(ACTION_CLICK_TIMEOUT_MS);
            }
            return null;
        }, DEFAULT_TIMEOUT_MS * 2, 500, "Pressing the row action '" + actionLabel + "' of project " + projectName);
    }

    /**
     * Clicks a row action on a same-name row picked out by its state (an opened row offers "Close", a
     * closed one "Open"), looking inline first and then in that row's overflow menu.
     */
    public void clickRowActionByState(String projectName, boolean opened, String actionLabel) {
        String stateAction = opened ? "Close" : "Open";
        WebElement inlineAction = actionByNameAndState.format(projectName, stateAction, actionLabel);
        if (inlineAction.isVisible(PROBE_MS)) {
            inlineAction.click();
            return;
        }
        rowMoreBtnByState.format(projectName, stateAction).click();
        overflowMenuItem.format(actionLabel).click();
    }

    /**
     * Every action a project's row offers (Open/Close/Copy/Export/Delete/Deploy/...), taking both the
     * inline buttons and the overflow menu into account — 6.4.0 keeps most actions behind the menu, so
     * the inline buttons alone are not the project's permission set.
     */
    public List<String> getProjectActionLabels(String projectName) {
        List<String> labels = new ArrayList<>();
        Locator btns = rowActionButtons.format(projectName).getLocator();
        WaitUtil.waitForCondition(() -> btns.count() > 0, DEFAULT_TIMEOUT_MS, 250, "Waiting for project row actions");
        int count = btns.count();
        for (int i = 0; i < count; i++) {
            String label = btns.nth(i).getAttribute("aria-label");
            if (label != null && !label.isEmpty() && !OVERFLOW_TRIGGER_LABEL.equals(label)) {
                labels.add(label);
            }
        }
        if (rowMoreBtn.format(projectName).isVisible(PROBE_MS)) {
            rowMoreBtn.format(projectName).click();
            Locator items = overflowMenuItems.getLocator();
            WaitUtil.waitForCondition(() -> items.count() > 0, DEFAULT_TIMEOUT_MS, 250, "Waiting for the row overflow menu");
            labels.addAll(items.allInnerTexts().stream().map(String::trim).filter(text -> !text.isEmpty()).toList());
            page.keyboard().press("Escape");
        }
        return labels;
    }

    /** The branch a project sits on, as shown in its row (the list has a branch column again in 6.4.0). */
    public String getProjectBranch(String projectName) {
        return branchInRow.format(projectName).waitForVisible(DEFAULT_TIMEOUT_MS).getText().trim();
    }

    // Rows are <tr data-testid=project-row-...> with the project name in the row's first <span>.
    public List<String> getAllVisibleProjectNames() {
        List<String> projectNames = new ArrayList<>();
        Locator rows = getRootLocator().getLocator().locator("xpath=.//tr[starts-with(@data-testid,'project-row')]");
        WaitUtil.waitForCondition(() -> rows.count() > 0, 5000, 250, "Waiting for projects to load");
        int count = rows.count();
        for (int i = 0; i < count; i++) {
            // Only count rows that are actually visible — the React name filter hides non-matching rows.
            if (!rows.nth(i).isVisible()) {
                continue;
            }
            String name = rows.nth(i).locator("span").first().textContent();
            if (name != null && !name.trim().isEmpty()) {
                projectNames.add(name.trim());
            }
        }
        return projectNames;
    }
}
