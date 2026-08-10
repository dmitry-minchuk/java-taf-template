package domain.ui.webstudio.components.projectdetail;

import com.microsoft.playwright.Page;
import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.BaseComponent;

/**
 * The action bar of the React project-detail header (Open Revision / Close / Sync / Copy / Delete /
 * Compare / Export / Deploy). Buttons are scoped under {@code [data-testid=project-actions]} (verified
 * against the live 6.4.0 DOM); the overflow menu is an antd dropdown rendered into a body-level portal,
 * so its items are located at page level on purpose.
 */
public class ProjectHeaderActionsComponent extends BaseComponent {

    // Header buttons render with the screen, so a short probe decides bar-vs-overflow.
    private static final int PROBE_MS = DEFAULT_TIMEOUT_MS / 5;

    private final WebElement actionByLabel;
    private final WebElement moreBtn;
    // antd dropdown portal — rendered at body level, outside the header subtree
    private final WebElement overflowItem;

    public ProjectHeaderActionsComponent(Page page) {
        this(new WebElement(page, "[data-testid=project-actions]", "projectHeaderActions"));
    }

    public ProjectHeaderActionsComponent(WebElement rootLocator) {
        super(rootLocator);
        // The header bar also renders a hidden copy of every button to measure widths, so match the visible
        // one by its testid (<actionId>-<projectId>) rather than by label text.
        actionByLabel = createScopedElement("xpath=.//button[starts-with(@data-testid,'%s-')]", "headerAction");
        moreBtn = createScopedElement("[data-testid=project-actions-more]", "headerMoreBtn");
        overflowItem = new WebElement(page, "xpath=//div[contains(@class,'ant-dropdown')][not(contains(@class,'ant-dropdown-hidden'))]//button[normalize-space()='%s']", "headerOverflowItem");
    }

    /**
     * Clicks an action by its label, wherever the header keeps it. The header bar collapses trailing
     * actions into an overflow menu as the window narrows, so look there when the button is not on the bar.
     */
    public void clickAction(String actionLabel) {
        WebElement action = actionByLabel.format(actionIdOf(actionLabel));
        if (action.isVisible(PROBE_MS)) {
            LOGGER.info("Header action '{}' clicked on the bar", actionLabel);
            action.click();
            return;
        }
        LOGGER.info("Header action '{}' is not on the bar; opening the Actions menu", actionLabel);
        moreBtn.click();
        overflowItem.format(actionLabel).click();
    }

    // The header buttons are keyed by the action's own id; the overflow menu still lists them by label.
    private static String actionIdOf(String actionLabel) {
        return switch (actionLabel) {
            case "Open Revision" -> "openRevision";
            case "Delete Branch" -> "deleteBranch";
            default -> actionLabel.substring(0, 1).toLowerCase() + actionLabel.substring(1);
        };
    }
}
